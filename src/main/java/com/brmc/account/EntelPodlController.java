package com.brmc.account;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * API ENTEL para convertir la plantilla Excel PODL_INPUT en archivo .podl.
 */
@RestController
@RequestMapping("/api/entel")
class EntelPodlController {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final PodlGeneratorService podlGeneratorService;

    EntelPodlController(PodlGeneratorService podlGeneratorService) {
        this.podlGeneratorService = podlGeneratorService;
    }

    /**
     * Genera un archivo PODL desde una plantilla Excel diligenciada.
     *
     * @param file archivo .xlsx con hoja PODL_INPUT.
     * @return archivo .podl descargable.
     */
    @PostMapping(value = "/podl", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<String> generatePodl(@RequestParam("file") MultipartFile file) {
        var podl = podlGeneratorService.generate(file);
        var fileName = "entel_podl_" + LocalDateTime.now().format(FILE_TIMESTAMP) + ".podl";
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .headers(headers -> headers.setContentDisposition(ContentDisposition.attachment()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()))
                .body(podl);
    }

    @ExceptionHandler(PodlGenerationException.class)
    ResponseEntity<String> handlePodlGenerationException(PodlGenerationException exception) {
        return ResponseEntity.badRequest()
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .body(exception.getMessage());
    }
}
