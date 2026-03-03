package com.wargameclub.clubapi.config;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import org.springframework.core.convert.converter.Converter;

/**
 * Конвертер OffsetDateTime, устойчивый к случаю, когда '+' в query-параметре
 * декодируется как пробел.
 */
public class LenientOffsetDateTimeConverter implements Converter<String, OffsetDateTime> {

    @Override
    public OffsetDateTime convert(String source) {
        if (source == null) {
            return null;
        }
        String value = source.trim();
        OffsetDateTime parsed = tryParse(value);
        if (parsed != null) {
            return parsed;
        }
        if (value.contains(" ") && !value.contains("+")) {
            parsed = tryParse(value.replace(' ', '+'));
            if (parsed != null) {
                return parsed;
            }
        }
        throw new IllegalArgumentException("Cannot parse OffsetDateTime: " + source);
    }

    private OffsetDateTime tryParse(String value) {
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
