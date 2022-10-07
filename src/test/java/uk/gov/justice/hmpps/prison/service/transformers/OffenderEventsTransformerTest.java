package uk.gov.justice.hmpps.prison.service.transformers;

import org.junit.jupiter.api.Test;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OffenderEventsTransformerTest {

    private static final String NOT_A_CASE_NOTE = "NOT_A_CASE_NOTE";
    private static final String CASE_NOTE_JSON = """
            {"case_note":{"id":61342651,"contact_datetime":"1819-02-20 14:09:00"
            ,"source":{"code":"INST"
            ,"desc":"Prison"
            },"type":{"code":"GEN"
            ,"desc":"General"
            },"sub_type":{"code":"OSE"
            ,"desc":""
            },"staff_member":{"id":483079,"name":"White, Barry"
            ,"userid":"QWU90D"
            },"text":"[redacted]stice.gov.uk \\n\\u260F 01811 8055 (Direct \\u2013 22222) \\u#### N/A [redacted]"
            ,"amended":false}}""";

    @Test
    public void localDateOfBehavesAppropriately() {
        assertThat(OffenderEventsTransformer.localDateOf("2019-02-14 10:11:12")).isEqualTo(LocalDate.of(2019, 2, 14));
        assertThat(OffenderEventsTransformer.localDateOf("14-FEB-2019")).isEqualTo(LocalDate.of(2019, 2, 14));
        assertThat(OffenderEventsTransformer.localDateOf("14-FEB-19")).isEqualTo(LocalDate.of(2019, 2, 14));
        assertThat(OffenderEventsTransformer.localDateOf(null)).isNull();
        assertThat(OffenderEventsTransformer.localDateOf("Some rubbish")).isNull();
    }

    @Test
    public void localTimeOfBehavesAppropriately() {
        assertThat(OffenderEventsTransformer.localTimeOf("2019-02-14 10:11:12")).isEqualTo(LocalTime.of(10, 11, 12));
        assertThat(OffenderEventsTransformer.localTimeOf("09:10:11")).isEqualTo(LocalTime.of(9, 10, 11));
        assertThat(OffenderEventsTransformer.localTimeOf(null)).isNull();
        assertThat(OffenderEventsTransformer.localTimeOf("Some rubbish")).isNull();
    }

    @Test
    public void localDateTimeOfDateAndTimeBehavesAppropriately() {
        assertThat(OffenderEventsTransformer.localDateTimeOf("2019-02-14 00:00:00", "1970-01-01 10:11:12")).isEqualTo(LocalDateTime.of(2019, 2, 14, 10, 11, 12));
        assertThat(OffenderEventsTransformer.localDateTimeOf(null, "1970-01-01 10:11:12")).isNull();
        assertThat(OffenderEventsTransformer.localDateTimeOf(null, "Some rubbish")).isNull();
        assertThat(OffenderEventsTransformer.localDateTimeOf(null, null)).isNull();
        assertThat(OffenderEventsTransformer.localDateTimeOf("2019-02-14 00:00:00", "Some rubbish")).isEqualTo(LocalDateTime.of(2019, 2, 14, 0, 0, 0));
        assertThat(OffenderEventsTransformer.localDateTimeOf("2019-02-14 00:00:00", null)).isEqualTo(LocalDateTime.of(2019, 2, 14, 0, 0, 0));
    }

    @Test
    public void canCorrectlyDecodeCaseNoteEventTypes() {
        final var transformer = new OffenderEventsTransformer(mock(TypesTransformer.class));

        assertThat(transformer.caseNoteEventTypeOf(OffenderEvent.builder()
                .eventType("CASE_NOTE")
                .eventData1(CASE_NOTE_JSON)
                .build())).isEqualTo("GEN-OSE");
    }

    @Test
    public void nonCaseNoteEventTypesAreNotDecoded() {
        final var transformer = new OffenderEventsTransformer(mock(TypesTransformer.class));

        assertThat(transformer.caseNoteEventTypeOf(OffenderEvent.builder()
                .eventType(NOT_A_CASE_NOTE)
                .eventData1(CASE_NOTE_JSON)
                .build())).isEqualTo(NOT_A_CASE_NOTE);
    }

    @Test
    public void canCorrectlyDecodeCaseNoteId() {
        final var transformer = new OffenderEventsTransformer(mock(TypesTransformer.class));

        assertThat(transformer.caseNoteIdOf(OffenderEvent.builder()
                .eventType("CASE_NOTE")
                .eventData1(CASE_NOTE_JSON)
                .build())).isEqualTo(61342651);
    }

    @Test
    public void nonCaseNoteIdsAreNotDecoded() {
        final var transformer = new OffenderEventsTransformer(mock(TypesTransformer.class));

        assertThat(transformer.caseNoteIdOf(OffenderEvent.builder()
                .eventType(NOT_A_CASE_NOTE)
                .eventData1(CASE_NOTE_JSON)
                .build())).isNull();
    }
}
