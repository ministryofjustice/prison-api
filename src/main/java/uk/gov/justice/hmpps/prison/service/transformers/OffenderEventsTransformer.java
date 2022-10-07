package uk.gov.justice.hmpps.prison.service.transformers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.api.model.OffenderEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Component
public class OffenderEventsTransformer {

    private final TypesTransformer typesTransformer;

    @Autowired
    public OffenderEventsTransformer(final TypesTransformer typesTransformer) {
        this.typesTransformer = typesTransformer;
    }

    public OffenderEvent offenderEventOf(final uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEvent offenderEvent) {
        return Optional.ofNullable(offenderEvent)
                .map(event -> OffenderEvent.builder()
                        .caseNoteId(caseNoteIdOf(event))
                        .eventId(event.getEventId().toString())
                        .eventDatetime(typesTransformer.localDateTimeOf(event.getEventTimestamp()))
                        .eventType(caseNoteEventTypeOf(event))
                        .rootOffenderId(event.getRootOffenderId())
                        .offenderIdDisplay(event.getOffenderIdDisplay())
                        .agencyLocationId(event.getAgencyLocId())
                        .build()).orElse(null);
    }

    public String caseNoteEventTypeOf(final uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEvent event) {
        if (event.getEventType().equalsIgnoreCase("CASE_NOTE")) {
            final var eventData = event.getEventData();
            final var typePattern = Pattern.compile("(?<=\\btype.{0,4}\\bcode.{0,4})(\\w+)");
            final var typeMatcher = typePattern.matcher(eventData);

            final var subtypePattern = Pattern.compile("(?<=\\bsub_type.{0,4}\\bcode.{0,4})(\\w+)");
            final var subtypeMatcher = subtypePattern.matcher(eventData);

            if (typeMatcher.find() && subtypeMatcher.find()) {
                return String.format("%s-%s", typeMatcher.group(), subtypeMatcher.group());
            }
        }

        return event.getEventType();
    }

    public Long caseNoteIdOf(final uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEvent event) {
        if (event.getEventType().equalsIgnoreCase("CASE_NOTE")) {
            final var eventData = event.getEventData();
            final var typePattern = Pattern.compile("(?<=\\bcase_note.{0,4}\\bid.{0,4})(\\w+)");
            final var typeMatcher = typePattern.matcher(eventData);

            return typeMatcher.find() ? longOf(typeMatcher.group()) : null;
        }
        return null;
    }

    private Long longOf(final String num) {
        return Optional.ofNullable(num).map(Long::valueOf).orElse(null);
    }


    public static LocalDate localDateOf(final String date) {
        final var pattern = "[yyyy-MM-dd HH:mm:ss][yyyy-MM-dd][dd-MMM-yyyy][dd-MMM-yy]";
        try {
            return Optional.ofNullable(date)
                    .map(d -> LocalDate.parse(d, new DateTimeFormatterBuilder().parseCaseInsensitive().appendPattern(pattern).toFormatter()))
                    .orElse(null);
        } catch (final DateTimeParseException dtpe) {
            log.error("Unable to parse {} into a LocalDate using pattern {}", date, pattern);
        }
        return null;
    }

    public static LocalTime localTimeOf(final String dateTime) {
        final var pattern = "[yyyy-MM-dd ]HH:mm:ss";
        try {
            return Optional.ofNullable(dateTime)
                    .map(d -> LocalTime.parse(d, DateTimeFormatter.ofPattern(pattern)))
                    .orElse(null);
        } catch (final DateTimeParseException dtpe) {
            log.error("Unable to parse {} into a LocalTime using pattern {}", dateTime, pattern);
        }
        return null;
    }

    public static LocalDateTime localDateTimeOf(final String date, final String time) {

        final var maybeLocalDate = Optional.ofNullable(localDateOf(date));
        final var maybeLocalTime = Optional.ofNullable((localTimeOf(time)));

        return maybeLocalDate
                .map(ld -> maybeLocalTime.map(lt -> lt.atDate(ld)).orElse(ld.atStartOfDay()))
                .orElse(null);
    }
}
