package uk.gov.justice.hmpps.prison.service.transformers;

import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class TypesTransformer {

    public LocalDate localDateOf(final Timestamp timestamp) {
        return Optional.ofNullable(timestamp)
                .map(t -> t.toLocalDateTime().toLocalDate())
                .orElse(null);
    }

    public LocalDateTime localDateTimeOf(final Timestamp timestamp) {
        return Optional.ofNullable(timestamp)
                .map(Timestamp::toLocalDateTime)
                .orElse(null);
    }

    public LocalDateTime localDateTimeOf(final Timestamp date, final Timestamp time) {
        return Optional.ofNullable(date)
                .map(Timestamp::toLocalDateTime)
                .map(dateTime ->
                        Optional.ofNullable(time)
                            .map(t -> t.toLocalDateTime().toLocalTime().atDate(dateTime.toLocalDate()))
                            .orElse(dateTime))
                .orElse(null);
    }

    public Boolean ynToBoolean(final String yn) {
        return Optional.ofNullable(yn)
                .map("Y"::equalsIgnoreCase)
                .orElse(false);
    }

    public Boolean isActiveOf(final String active) {
        return Optional.ofNullable(active)
                .map("A"::equalsIgnoreCase)
                .orElse(false);
    }
}
