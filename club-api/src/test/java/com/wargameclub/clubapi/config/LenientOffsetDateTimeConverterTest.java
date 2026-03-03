package com.wargameclub.clubapi.config;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LenientOffsetDateTimeConverterTest {

    private final LenientOffsetDateTimeConverter converter = new LenientOffsetDateTimeConverter();

    @Test
    void convertParsesIsoWithOffset() {
        OffsetDateTime value = converter.convert("2026-03-03T21:39:55.870198803+03:00");

        assertThat(value).isEqualTo(OffsetDateTime.parse("2026-03-03T21:39:55.870198803+03:00"));
    }

    @Test
    void convertParsesIsoWithSpaceInsteadOfPlus() {
        OffsetDateTime value = converter.convert("2026-03-03T21:39:55.870198803 03:00");

        assertThat(value).isEqualTo(OffsetDateTime.parse("2026-03-03T21:39:55.870198803+03:00"));
    }

    @Test
    void convertThrowsForInvalidValue() {
        assertThatThrownBy(() -> converter.convert("not-a-date"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
