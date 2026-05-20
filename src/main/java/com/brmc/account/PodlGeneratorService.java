package com.brmc.account;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Genera archivos PODL desde la hoja PODL_INPUT de un workbook .xlsx.
 */
@Service
class PodlGeneratorService {

    private static final String SHEET_NAME = "PODL_INPUT";
    private static final DateTimeFormatter GENERATED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String> REQUIRED_COLUMNS = List.of(
            "storable_class",
            "sql_table",
            "field_type",
            "field_name",
            "field_description",
            "create_rule",
            "modify_rule",
            "sql_column"
    );
    private static final Set<String> FIELD_TYPES = Set.of("POID", "STRING", "ENUM", "INT", "DECIMAL", "TIMESTAMP", "ARRAY");
    private static final Set<String> CREATE_RULES = Set.of("System", "Required", "Optional");
    private static final Set<String> MODIFY_RULES = Set.of("Writeable", "ReadOnly");
    private final Clock clock;

    PodlGeneratorService() {
        this(Clock.systemDefaultZone());
    }

    PodlGeneratorService(Clock clock) {
        this.clock = clock;
    }

    /**
     * Lee el archivo Excel cargado desde la UI y retorna el contenido PODL.
     *
     * @param file archivo .xlsx con hoja PODL_INPUT.
     * @return contenido PODL listo para descargar.
     */
    String generate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new PodlGenerationException("Debe cargar un archivo Excel .xlsx con la hoja PODL_INPUT.");
        }
        var originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        if (!originalName.endsWith(".xlsx")) {
            throw new PodlGenerationException("Solo se soportan archivos .xlsx generados desde la plantilla PODL.");
        }
        try (var inputStream = file.getInputStream()) {
            return generateFromWorkbook(inputStream);
        } catch (IOException exception) {
            throw new PodlGenerationException("No fue posible leer el archivo Excel: " + exception.getMessage(), exception);
        }
    }

    /**
     * Genera PODL desde un InputStream de workbook para pruebas y para el endpoint web.
     *
     * @param inputStream workbook .xlsx.
     * @return contenido PODL.
     */
    String generateFromWorkbook(InputStream inputStream) {
        var rows = readPodlRows(inputStream);
        return generateFromRows(rows);
    }

    String generateFromRows(List<Map<String, String>> rows) {
        if (rows.isEmpty()) {
            throw new PodlGenerationException("La hoja PODL_INPUT no tiene filas de datos para generar PODL.");
        }

        var errors = new ArrayList<String>();
        var warnings = new ArrayList<String>();
        var classes = new LinkedHashMap<String, PodlClass>();

        for (var row : rows) {
            var rowNumber = row.getOrDefault("__rowNumber", "?");
            for (var required : REQUIRED_COLUMNS) {
                if (isBlank(row.get(required))) {
                    errors.add("PODL_INPUT fila " + rowNumber + " campo " + required + ": es obligatorio.");
                }
            }

            var fieldType = upper(row.get("field_type"));
            if (!isBlank(fieldType) && !FIELD_TYPES.contains(fieldType)) {
                errors.add("PODL_INPUT fila " + rowNumber + " campo field_type: valor no soportado '" + row.get("field_type") + "'.");
            }
            if ("STRING".equals(fieldType) && isBlank(row.get("field_length"))) {
                errors.add("PODL_INPUT fila " + rowNumber + " campo field_length: obligatorio para STRING.");
            }
            var createRule = clean(row.get("create_rule"));
            if (!isBlank(createRule) && !CREATE_RULES.contains(createRule)) {
                errors.add("PODL_INPUT fila " + rowNumber + " campo create_rule: use System, Required u Optional.");
            }
            var modifyRule = clean(row.get("modify_rule"));
            if (!isBlank(modifyRule) && !MODIFY_RULES.contains(modifyRule)) {
                errors.add("PODL_INPUT fila " + rowNumber + " campo modify_rule: use Writeable o ReadOnly.");
            }
            var fieldName = clean(row.get("field_name"));
            if (!isBlank(fieldName) && !fieldName.startsWith("PIN_FLD_") && !fieldName.startsWith("EXT_FLD_")) {
                warnings.add("PODL_INPUT fila " + rowNumber + " campo field_name: '" + fieldName
                        + "' no inicia con PIN_FLD_ ni EXT_FLD_. Se conserva tal como viene.");
            }

            if (hasBlockingBasics(row)) {
                var storableClass = clean(row.get("storable_class"));
                var podlClass = classes.computeIfAbsent(storableClass, key -> new PodlClass(
                        key,
                        clean(row.get("class_description")),
                        clean(row.get("sql_table")),
                        defaultValue(row.get("read_access"), "Self"),
                        defaultValue(row.get("write_access"), "Self"),
                        defaultValue(row.get("seq_start"), "1"),
                        defaultValue(row.get("is_partitioned"), "0"),
                        defaultValue(row.get("event_type"), "NONE")
                ));
                podlClass.fields().add(new PodlField(
                        parseOrder(row.get("field_order"), rowNumber, podlClass.fields().size() + 1, warnings),
                        upper(row.get("field_type")),
                        fieldName,
                        clean(row.get("field_description")),
                        normalizeNumber(row.get("field_length")),
                        createRule,
                        modifyRule,
                        binary(row.get("auditable")),
                        binary(row.get("encryptable")),
                        binary(row.get("serializable")),
                        clean(row.get("sql_column")),
                        rowNumber
                ));
            }
        }

        if (!errors.isEmpty()) {
            throw new PodlGenerationException(validationReport(errors, warnings));
        }
        if (classes.isEmpty()) {
            throw new PodlGenerationException("No se detectaron clases validas en PODL_INPUT.");
        }

        classes.values().forEach(podlClass -> ensurePoid(podlClass, warnings));
        return renderPodl(classes, warnings);
    }

    private List<Map<String, String>> readPodlRows(InputStream inputStream) {
        try {
            var entries = unzip(inputStream);
            var sharedStrings = sharedStrings(entries);
            var sheetPath = podlSheetPath(entries);
            var sheetBytes = entries.get(sheetPath);
            if (sheetBytes == null) {
                throw new PodlGenerationException("No se encontro la hoja " + SHEET_NAME + " dentro del archivo Excel.");
            }
            return rowsFromSheet(sheetBytes, sharedStrings);
        } catch (IOException | ParserConfigurationException | SAXException exception) {
            throw new PodlGenerationException("No fue posible procesar el Excel PODL: " + exception.getMessage(), exception);
        }
    }

    private Map<String, byte[]> unzip(InputStream inputStream) throws IOException {
        var entries = new HashMap<String, byte[]>();
        try (var zip = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    entries.put(entry.getName(), zip.readAllBytes());
                }
            }
        }
        if (!entries.containsKey("xl/workbook.xml")) {
            throw new PodlGenerationException("El archivo no parece ser un .xlsx valido.");
        }
        return entries;
    }

    private List<String> sharedStrings(Map<String, byte[]> entries)
            throws ParserConfigurationException, IOException, SAXException {
        var bytes = entries.get("xl/sharedStrings.xml");
        if (bytes == null) {
            return List.of();
        }
        var document = parse(bytes);
        var strings = new ArrayList<String>();
        var items = document.getElementsByTagNameNS("*", "si");
        for (int index = 0; index < items.getLength(); index++) {
            strings.add(textFromChildren((Element) items.item(index), "t"));
        }
        return strings;
    }

    private String podlSheetPath(Map<String, byte[]> entries)
            throws ParserConfigurationException, IOException, SAXException {
        var workbook = parse(entries.get("xl/workbook.xml"));
        var rels = parse(entries.get("xl/_rels/workbook.xml.rels"));
        var relTargets = new HashMap<String, String>();
        var relationships = rels.getElementsByTagNameNS("*", "Relationship");
        for (int index = 0; index < relationships.getLength(); index++) {
            var relationship = (Element) relationships.item(index);
            relTargets.put(relationship.getAttribute("Id"), relationship.getAttribute("Target"));
        }

        var sheets = workbook.getElementsByTagNameNS("*", "sheet");
        for (int index = 0; index < sheets.getLength(); index++) {
            var sheet = (Element) sheets.item(index);
            if (SHEET_NAME.equalsIgnoreCase(sheet.getAttribute("name"))) {
                var relationshipId = sheet.getAttributeNS(
                        "http://schemas.openxmlformats.org/officeDocument/2006/relationships",
                        "id"
                );
                if (isBlank(relationshipId)) {
                    relationshipId = sheet.getAttribute("r:id");
                }
                var target = relTargets.get(relationshipId);
                if (isBlank(target)) {
                    throw new PodlGenerationException("La hoja " + SHEET_NAME + " no tiene una relacion valida en workbook.xml.");
                }
                return normalizeWorkbookTarget(target);
            }
        }
        throw new PodlGenerationException("El Excel debe tener una hoja llamada " + SHEET_NAME + ".");
    }

    private String normalizeWorkbookTarget(String target) {
        var cleanTarget = target.replace("\\", "/");
        if (cleanTarget.startsWith("/")) {
            return cleanTarget.substring(1);
        }
        if (cleanTarget.startsWith("xl/")) {
            return cleanTarget;
        }
        return "xl/" + cleanTarget;
    }

    private List<Map<String, String>> rowsFromSheet(byte[] sheetBytes, List<String> sharedStrings)
            throws ParserConfigurationException, IOException, SAXException {
        var document = parse(sheetBytes);
        var rows = document.getElementsByTagNameNS("*", "row");
        var headers = new TreeMap<Integer, String>();
        var output = new ArrayList<Map<String, String>>();
        var headerFound = false;

        for (int rowIndex = 0; rowIndex < rows.getLength(); rowIndex++) {
            var row = (Element) rows.item(rowIndex);
            var values = readRow(row, sharedStrings);
            if (values.isEmpty()) {
                continue;
            }
            if (!headerFound) {
                values.forEach((index, value) -> headers.put(index, normalizeHeader(value)));
                for (var required : REQUIRED_COLUMNS) {
                    if (!headers.containsValue(required)) {
                        throw new PodlGenerationException("La hoja " + SHEET_NAME + " no tiene la columna obligatoria " + required + ".");
                    }
                }
                headerFound = true;
                continue;
            }

            var rowMap = new LinkedHashMap<String, String>();
            headers.forEach((index, header) -> rowMap.put(header, values.getOrDefault(index, "")));
            if (rowMap.values().stream().allMatch(PodlGeneratorService::isBlank)) {
                continue;
            }
            rowMap.put("__rowNumber", row.getAttribute("r"));
            output.add(rowMap);
        }
        if (!headerFound) {
            throw new PodlGenerationException("La hoja " + SHEET_NAME + " no tiene encabezados.");
        }
        return output;
    }

    private Map<Integer, String> readRow(Element row, List<String> sharedStrings) {
        var values = new TreeMap<Integer, String>();
        var cells = row.getElementsByTagNameNS("*", "c");
        for (int index = 0; index < cells.getLength(); index++) {
            var cell = (Element) cells.item(index);
            var reference = cell.getAttribute("r");
            var columnIndex = columnIndex(reference);
            if (columnIndex < 0) {
                columnIndex = index;
            }
            values.put(columnIndex, cellValue(cell, sharedStrings));
        }
        return values;
    }

    private String cellValue(Element cell, List<String> sharedStrings) {
        var type = cell.getAttribute("t");
        if ("inlineStr".equals(type)) {
            return textFromChildren(cell, "t");
        }
        var value = firstChildText(cell, "v");
        if ("s".equals(type) && !isBlank(value)) {
            var index = Integer.parseInt(value);
            return index >= 0 && index < sharedStrings.size() ? sharedStrings.get(index) : "";
        }
        return value == null ? "" : value;
    }

    private Document parse(byte[] bytes) throws ParserConfigurationException, IOException, SAXException {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException ignored) {
            // Some XML parsers do not expose this hardening flag.
        }
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
    }

    private static int columnIndex(String reference) {
        if (isBlank(reference)) {
            return -1;
        }
        var result = 0;
        var found = false;
        for (var character : reference.toCharArray()) {
            if (Character.isLetter(character)) {
                found = true;
                result = result * 26 + (Character.toUpperCase(character) - 'A' + 1);
            }
        }
        return found ? result - 1 : -1;
    }

    private String renderPodl(Map<String, PodlClass> classes, List<String> warnings) {
        var builder = new StringBuilder();
        builder.append("# Generated by BRMC Billing Care ENTEL PODL generator\r\n");
        builder.append("# Generated at ").append(LocalDateTime.now(clock).format(GENERATED_AT_FORMAT)).append("\r\n");
        builder.append("# Classes: ").append(classes.keySet()).append("\r\n");
        if (!warnings.isEmpty()) {
            builder.append("# Warnings:\r\n");
            warnings.forEach(warning -> builder.append("# - ").append(warning).append("\r\n"));
        }
        builder.append("\r\n");

        classes.values().forEach(podlClass -> {
            var fields = podlClass.fields().stream()
                    .sorted(Comparator.comparing(PodlField::order).thenComparing(PodlField::fieldName))
                    .toList();
            builder.append("#=======================================\r\n");
            builder.append("#  Storable Class ").append(podlClass.storableClass()).append("\r\n");
            builder.append("#=======================================\r\n");
            builder.append("STORABLE CLASS ").append(podlClass.storableClass()).append(" {\r\n");
            builder.append("    SEQ_START = ").append(podlClass.seqStart()).append(";\r\n");
            builder.append("    READ_ACCESS = ").append(podlClass.readAccess()).append(";\r\n");
            builder.append("    WRITE_ACCESS = ").append(podlClass.writeAccess()).append(";\r\n");
            builder.append("    DESCR = \"").append(escape(podlClass.description())).append("\";\r\n");
            builder.append("    IS_PARTITIONED = ").append(podlClass.isPartitioned()).append(";\r\n");
            builder.append("    EVENT_TYPE = ").append(podlClass.eventType()).append(";\r\n\r\n");
            fields.forEach(field -> renderField(builder, field));
            builder.append("}\r\n\r\n");
            builder.append("#=======================================\r\n");
            builder.append("#  Implementation ORACLE7 ").append(podlClass.storableClass()).append("\r\n");
            builder.append("#=======================================\r\n");
            builder.append("STORABLE CLASS ").append(podlClass.storableClass()).append(" IMPLEMENTATION ORACLE7 {\r\n");
            builder.append("    SQL_TABLE = \"").append(escape(podlClass.sqlTable())).append("\";\r\n\r\n");
            fields.forEach(field -> {
                builder.append("    ").append(field.fieldType()).append(" ").append(field.fieldName()).append(" {\r\n");
                builder.append("        SQL_COLUMN = \"").append(escape(field.sqlColumn())).append("\";\r\n");
                builder.append("    }\r\n\r\n");
            });
            builder.append("}\r\n\r\n");
        });
        return builder.toString();
    }

    private void renderField(StringBuilder builder, PodlField field) {
        builder.append("    ").append(field.fieldType()).append(" ").append(field.fieldName()).append(" {\r\n");
        builder.append("        DESCR = \"").append(escape(field.description())).append("\";\r\n");
        builder.append("        ORDER = ").append(field.order()).append(";\r\n");
        if (!isBlank(field.length())) {
            builder.append("        LENGTH = ").append(field.length()).append(";\r\n");
        }
        builder.append("        CREATE = ").append(field.createRule()).append(";\r\n");
        builder.append("        MODIFY = ").append(field.modifyRule()).append(";\r\n");
        builder.append("        AUDITABLE = ").append(field.auditable()).append(";\r\n");
        builder.append("        ENCRYPTABLE = ").append(field.encryptable()).append(";\r\n");
        builder.append("        SERIALIZABLE = ").append(field.serializable()).append(";\r\n");
        builder.append("    }\r\n\r\n");
    }

    private void ensurePoid(PodlClass podlClass, List<String> warnings) {
        var hasPoid = podlClass.fields().stream().anyMatch(field -> "PIN_FLD_POID".equals(field.fieldName()));
        if (hasPoid) {
            return;
        }
        warnings.add("La clase " + podlClass.storableClass()
                + " no tenia PIN_FLD_POID. Se agrego POID PIN_FLD_POID con SQL_COLUMN poid.");
        podlClass.fields().add(new PodlField(
                0,
                "POID",
                "PIN_FLD_POID",
                "Object poid",
                "",
                "System",
                "Writeable",
                "0",
                "0",
                "0",
                "poid",
                "auto"
        ));
    }

    private boolean hasBlockingBasics(Map<String, String> row) {
        return REQUIRED_COLUMNS.stream().allMatch(column -> !isBlank(row.get(column)))
                && FIELD_TYPES.contains(upper(row.get("field_type")));
    }

    private int parseOrder(String value, String rowNumber, int fallback, List<String> warnings) {
        if (isBlank(value)) {
            warnings.add("PODL_INPUT fila " + rowNumber + " campo field_order vacio. Se uso el orden de la fila.");
            return fallback;
        }
        try {
            return new BigDecimal(value.trim()).setScale(0, RoundingMode.DOWN).intValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            warnings.add("PODL_INPUT fila " + rowNumber + " campo field_order invalido. Se uso el orden de la fila.");
            return fallback;
        }
    }

    private static String validationReport(List<String> errors, List<String> warnings) {
        var builder = new StringBuilder("No se genero PODL por errores bloqueantes.\n\nErrores:\n");
        errors.forEach(error -> builder.append("- ").append(error).append('\n'));
        if (!warnings.isEmpty()) {
            builder.append("\nAdvertencias:\n");
            warnings.forEach(warning -> builder.append("- ").append(warning).append('\n'));
        }
        return builder.toString();
    }

    private static String normalizeHeader(String value) {
        return clean(value).toLowerCase(Locale.ROOT);
    }

    private static String normalizeNumber(String value) {
        if (isBlank(value)) {
            return "";
        }
        try {
            return new BigDecimal(value.trim()).stripTrailingZeros().toPlainString();
        } catch (NumberFormatException exception) {
            return clean(value);
        }
    }

    private static String binary(String value) {
        var clean = clean(value);
        return "1".equals(clean) ? "1" : "0";
    }

    private static String upper(String value) {
        return clean(value).toUpperCase(Locale.ROOT);
    }

    private static String defaultValue(String value, String fallback) {
        return isBlank(value) ? fallback : clean(value);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String firstChildText(Element element, String localName) {
        var children = element.getElementsByTagNameNS("*", localName);
        return children.getLength() == 0 ? "" : children.item(0).getTextContent();
    }

    private static String textFromChildren(Element element, String localName) {
        var children = element.getElementsByTagNameNS("*", localName);
        var builder = new StringBuilder();
        for (int index = 0; index < children.getLength(); index++) {
            var child = children.item(index);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                builder.append(child.getTextContent());
            }
        }
        return builder.toString();
    }

    private static String escape(String value) {
        return Objects.toString(value, "")
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private record PodlClass(
            String storableClass,
            String description,
            String sqlTable,
            String readAccess,
            String writeAccess,
            String seqStart,
            String isPartitioned,
            String eventType,
            List<PodlField> fields
    ) {
        PodlClass(
                String storableClass,
                String description,
                String sqlTable,
                String readAccess,
                String writeAccess,
                String seqStart,
                String isPartitioned,
                String eventType
        ) {
            this(
                    storableClass,
                    isBlank(description) ? storableClass : description,
                    sqlTable,
                    readAccess,
                    writeAccess,
                    seqStart,
                    isPartitioned,
                    eventType,
                    new ArrayList<>()
            );
        }
    }

    private record PodlField(
            int order,
            String fieldType,
            String fieldName,
            String description,
            String length,
            String createRule,
            String modifyRule,
            String auditable,
            String encryptable,
            String serializable,
            String sqlColumn,
            String rowNumber
    ) {
    }
}
