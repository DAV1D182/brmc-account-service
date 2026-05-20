package com.brmc.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class PodlGeneratorServiceTest {

    private final PodlGeneratorService service = new PodlGeneratorService(
            Clock.fixed(Instant.parse("2026-05-19T12:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void generatesPodlFromExcelTemplateRows() throws IOException {
        try (var inputStream = getClass().getResourceAsStream("/podl/PODL_prueba_payment_channel_info.xlsx")) {
            assertThat(inputStream).isNotNull();

            var podl = service.generateFromWorkbook(inputStream);

            assertThat(podl)
                    .contains("STORABLE CLASS /payment_channel_info {")
                    .contains("SQL_TABLE = \"payment_channel_info_t\";")
                    .contains("STRING PIN_FLD_NAME")
                    .contains("LENGTH = 255;")
                    .contains("STORABLE CLASS /payment_channel_info IMPLEMENTATION ORACLE7")
                    .contains("SQL_COLUMN = \"channel_key\";");
        }
    }

    @Test
    void rejectsEmptyPodlInput() {
        assertThatThrownBy(() -> service.generateFromRows(java.util.List.of()))
                .isInstanceOf(PodlGenerationException.class)
                .hasMessageContaining("no tiene filas de datos");
    }
}
