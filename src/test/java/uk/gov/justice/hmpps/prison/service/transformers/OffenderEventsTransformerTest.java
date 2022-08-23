package uk.gov.justice.hmpps.prison.service.transformers;


import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEvent;
import uk.gov.justice.hmpps.prison.service.xtag.Xtag;
import uk.gov.justice.hmpps.prison.service.xtag.XtagContent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OffenderEventsTransformerTest {

    private static final String NOT_A_CASE_NOTE = "NOT_A_CASE_NOTE";
    private static final String CASE_NOTE_JSON = "{\"case_note\":{\"id\":61342651,\"contact_datetime\":\"1819-02-20 14:09:00\"\n" +
        ",\"source\":{\"code\":\"INST\"\n" +
        ",\"desc\":\"Prison\"\n" +
        "},\"type\":{\"code\":\"GEN\"\n" +
        ",\"desc\":\"General\"\n" +
        "},\"sub_type\":{\"code\":\"OSE\"\n" +
        ",\"desc\":\"\"\n" +
        "},\"staff_member\":{\"id\":483079,\"name\":\"White, Barry\"\n" +
        ",\"userid\":\"QWU90D\"\n" +
        "},\"text\":\"[redacted]stice.gov.uk \\n\\u260F 01811 8055 (Direct \\u2013 22222) \\u#### N/A [redacted]\"\n" +
        ",\"amended\":false}}";
    private OffenderEventsTransformer offenderEventsTransformer = new OffenderEventsTransformer(mock(TypesTransformer.class));

    @Test
    public void canDeserializeIntoXtagContent() {
        final var transformer = new OffenderEventsTransformer(mock(TypesTransformer.class));

        assertThat(transformer.xtagContentOf(ImmutableMap.of("x", "y"))).isNotNull();
    }

    @Test
    public void xtagEnqueueTimestampIsSeasonallyAdjustedIntoDaylightSavings() {
        final var lastSecondOfWinter = LocalDateTime.of(2019, 3, 31, 0, 59, 59);
        final var firstSecondOfSummer = lastSecondOfWinter.plusSeconds(1L);
        assertThat(OffenderEventsTransformer.xtagFudgedTimestampOf(lastSecondOfWinter)).isEqualTo(lastSecondOfWinter.minusHours(1L));
        assertThat(OffenderEventsTransformer.xtagFudgedTimestampOf(firstSecondOfSummer)).isEqualTo(firstSecondOfSummer);
    }

    @Test
    public void xtagEnqueueTimestampIsSeasonallyAdjustedIntoUTC() {
        final var lastSecondOfSummer = LocalDateTime.of(2019, 10, 27, 1, 59, 59);
        final var firstSecondOfWinter = lastSecondOfSummer.plusSeconds(1L);
        assertThat(OffenderEventsTransformer.xtagFudgedTimestampOf(lastSecondOfSummer)).isEqualTo(lastSecondOfSummer);
        assertThat(OffenderEventsTransformer.xtagFudgedTimestampOf(firstSecondOfWinter)).isEqualTo(firstSecondOfWinter.minusHours(1L));
    }

    @Test
    public void externalMovementDescriptorBehavesAppropriately() {
        assertThat(OffenderEventsTransformer.externalMovementEventOf(Xtag.builder().content(XtagContent.builder().p_record_deleted("Y").build()).build())).isEqualTo("EXTERNAL_MOVEMENT_RECORD-DELETED");
        assertThat(OffenderEventsTransformer.externalMovementEventOf(Xtag.builder().content(XtagContent.builder().p_record_deleted("N").build()).build())).isEqualTo("EXTERNAL_MOVEMENT_RECORD-INSERTED");
        assertThat(OffenderEventsTransformer.externalMovementEventOf(Xtag.builder().content(XtagContent.builder().p_record_deleted(UUID.randomUUID().toString()).build()).build())).isEqualTo("EXTERNAL_MOVEMENT_RECORD-UPDATED");
        assertThat(OffenderEventsTransformer.externalMovementEventOf(Xtag.builder().content(XtagContent.builder().build()).build())).isEqualTo("EXTERNAL_MOVEMENT_RECORD-UPDATED");
    }

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
    public void externalMovementRecordEventOfHandlesAgyLocIdsAsStrings() {

        final var transformer = new OffenderEventsTransformer(null);
        assertThat(transformer.externalMovementRecordEventOf(Xtag.builder().content(
            XtagContent.builder()
                .p_from_agy_loc_id("BARBECUE")
                .p_to_agy_loc_id("SAUCE")
                .build()
        ).build(), Optional.of(OffenderEventsTransformer.externalMovementEventOf(Xtag.builder().content(
            XtagContent.builder()
                .p_from_agy_loc_id("BARBECUE")
                .p_to_agy_loc_id("SAUCE")
                .build()
        ).build())))).extracting("fromAgencyLocationId", "toAgencyLocationId").containsOnly("BARBECUE", "SAUCE");
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

    @Test
    public void unknownEventTypesAreHandledAppropriately() {
        final var transformer = new OffenderEventsTransformer(mock(TypesTransformer.class));

        assertThat(transformer.offenderEventOf((Xtag) null)).isNull();
        assertThat(transformer.offenderEventOf(Xtag.builder().build())).isNull();
        assertThat(transformer.offenderEventOf(Xtag.builder().eventType("meh").build())).isNotNull();
    }


    @Test
    public void S2_RESULT_IsMappedTo_SENTENCE_DATES_CHANGED() {
        final var event = offenderEventsTransformer.offenderEventOf(Xtag
            .builder()
            .eventType("S2_RESULT")
            .content(XtagContent
                .builder()
                .p_offender_book_id("99")
                .p_offender_sent_calculation_id("88")
                .build())
            .build());

        assertThat(event.getEventType()).isEqualTo("SENTENCE_DATES-CHANGED");
    }

    @Test
    public void OFF_SENT_OASYS_IsMappedTo_SENTENCE_CALCULATION_DATES_CHANGED() {
        final var event = offenderEventsTransformer.offenderEventOf(Xtag
            .builder()
            .eventType("OFF_SENT_OASYS")
            .content(XtagContent
                .builder()
                .p_offender_book_id("99")
                .build())
            .build());

        assertThat(event.getEventType()).isEqualTo("SENTENCE_CALCULATION_DATES-CHANGED");
    }

    @Test
    public void bedAssignmentCorrectlyMapped() {
        final var now = LocalDateTime.now();

        final var event = offenderEventsTransformer.offenderEventOf(Xtag
            .builder()
            .eventType("BED_ASSIGNMENT_HISTORY-INSERTED")
            .nomisTimestamp(now)
            .content(XtagContent
                .builder()
                .p_offender_book_id("99")
                .p_bed_assign_seq("1")
                .p_living_unit_id("34123412")
                .build())
            .build());

        assertThat(event.getEventType()).isEqualTo("BED_ASSIGNMENT_HISTORY-INSERTED");
        assertThat(event.getBookingId()).isEqualTo(99L);
        assertThat(event.getBedAssignmentSeq()).isEqualTo(1);
        assertThat(event.getLivingUnitId()).isEqualTo(34123412L);
        assertThat(event.getNomisEventType()).isEqualTo("BED_ASSIGNMENT_HISTORY-INSERTED");
        assertThat(event.getEventDatetime()).isEqualTo(now);

    }

    @Test
    public void externalMovementChangedCorrectlyMapped() {
        final var now = LocalDateTime.now();

        final var event = offenderEventsTransformer.offenderEventOf(Xtag
            .builder()
            .eventType("EXTERNAL_MOVEMENT-CHANGED")
            .nomisTimestamp(now)
            .content(XtagContent
                .builder()
                .p_offender_book_id("232")
                .p_from_agy_loc_id("MDI")
                .p_to_agy_loc_id("HOSP")
                .p_direction_code("OUT")
                .p_movement_type("REL")
                .p_movement_reason_code("HP")
                .p_movement_seq("1")
                .p_movement_date("2019-02-14")
                .p_movement_time("2019-02-14 10:11:12")
                .build())
            .build());

        assertThat(event.getEventType()).isEqualTo("EXTERNAL_MOVEMENT-CHANGED");
        assertThat(event.getBookingId()).isEqualTo(232L);
        assertThat(event.getMovementSeq()).isEqualTo(1);
        assertThat(event.getNomisEventType()).isEqualTo("EXTERNAL_MOVEMENT-CHANGED");
        assertThat(event.getEventDatetime()).isEqualTo(now);
        assertThat(event.getFromAgencyLocationId()).isEqualTo("MDI");
        assertThat(event.getToAgencyLocationId()).isEqualTo("HOSP");
        assertThat(event.getDirectionCode()).isEqualTo("OUT");
        assertThat(event.getMovementReasonCode()).isEqualTo("HP");
        assertThat(event.getMovementType()).isEqualTo("REL");
    }

    @Test
    public void sentencingChangedCorrectlyMapped() {
        final var now = LocalDateTime.now();

        final var event = offenderEventsTransformer.offenderEventOf(Xtag
            .builder()
            .eventType("SENTENCING-CHANGED")
            .nomisTimestamp(now)
            .content(XtagContent
                .builder()
                .p_offender_book_id("2322322")
                .p_offender_id_display("A1234AA")
                .build())
            .build());

        assertThat(event.getEventType()).isEqualTo("SENTENCING-CHANGED");
        assertThat(event.getBookingId()).isEqualTo(2322322L);
        assertThat(event.getEventDatetime()).isEqualTo(now);
        assertThat(event.getOffenderIdDisplay()).isEqualTo("A1234AA");
    }

    @Test
    public void confirmedReleaseDateChangeMappedCorrectly() {
        final var now = LocalDateTime.now();

        final var event = offenderEventsTransformer.offenderEventOf(Xtag
            .builder()
            .eventType("CONFIRMED_RELEASE_DATE-CHANGED")
            .nomisTimestamp(now)
            .content(XtagContent
                .builder()
                .p_offender_book_id("99")
                .build())
            .build());

        assertThat(event.getEventType()).isEqualTo("CONFIRMED_RELEASE_DATE-CHANGED");
        assertThat(event.getBookingId()).isEqualTo(99L);
        assertThat(event.getEventDatetime()).isEqualTo(now);
    }

    @Test
    public void offenderIndividualScheduledEventMappedCorrectly() {
        final var now = LocalDateTime.now();

        final var event = offenderEventsTransformer.offenderEventOf(Xtag
            .builder()
            .eventType("SCHEDULE_INT_APP-CHANGED")
            .nomisTimestamp(now)
            .content(XtagContent
                .builder()
                .p_agy_loc_id("LEI")
                .p_event_id("2362162")
                .p_event_date("2022-07-19")
                .p_event_status("SCH")
                .p_offender_book_id("52303")
                .p_event_sub_type("CALA")
                .p_start_time("16:00:00")
                .p_end_time("16:30:00")
                .p_event_class("INT_MOV")
                .p_event_type("APP")
                .build())
            .build());

        assertThat(event.getEventType()).isEqualTo("APPOINTMENT_CHANGED");
        assertThat(event.getBookingId()).isEqualTo(52303L);
        assertThat(event.getEventDatetime()).isEqualTo(now);
        assertThat(event.getAgencyLocationId()).isEqualTo("LEI");
        assertThat(event.getScheduleEventId()).isEqualTo(2362162);
        assertThat(event.getScheduledStartTime()).isEqualTo(LocalDateTime.of(2022, 7, 19, 16,0,0));
        assertThat(event.getScheduledEndTime()).isEqualTo(LocalDateTime.of(2022, 7, 19, 16,30,0));
        assertThat(event.getScheduleEventStatus()).isEqualTo("SCH");
        assertThat(event.getScheduleEventType()).isEqualTo("APP");
        assertThat(event.getScheduleEventSubType()).isEqualTo("CALA");
        assertThat(event.getScheduleEventClass()).isEqualTo("INT_MOV");
    }

    @Test
    public void offenderIndividualScheduledEventMappedCorrectlyWhenEndDateMissing() {
        final var now = LocalDateTime.now();

        final var event = offenderEventsTransformer.offenderEventOf(Xtag
            .builder()
            .eventType("SCHEDULE_INT_APP-CHANGED")
            .nomisTimestamp(now)
            .content(XtagContent
                .builder()
                .p_agy_loc_id("LEI")
                .p_event_id("2362162")
                .p_event_date("2022-07-19")
                .p_event_status("SCH")
                .p_offender_book_id("52303")
                .p_event_sub_type("CALA")
                .p_start_time("16:00:00")
                .p_event_class("INT_MOV")
                .p_event_type("APP")
                .build())
            .build());

        assertThat(event.getEventType()).isEqualTo("APPOINTMENT_CHANGED");
        assertThat(event.getBookingId()).isEqualTo(52303L);
        assertThat(event.getScheduledStartTime()).isEqualTo(LocalDateTime.of(2022, 7, 19, 16,0,0));
        assertThat(event.getScheduledEndTime()).isNull();
    }

    @Test
    public void offenderIepUpdatedMappedCorrectly() {
        final var now = LocalDateTime.now();

        final var event = offenderEventsTransformer.offenderEventOf(Xtag
            .builder()
            .eventType("OFFENDER_IEP_LEVEL-UPDATED")
            .nomisTimestamp(now)
            .content(XtagContent
                .builder()
                .p_agy_loc_id("MDI")
                .p_iep_level("STD")
                .p_iep_level_seq("3")
                .p_event_id("2323")
                .p_event_date("2022-08-23")
                .p_offender_book_id("434")
                .p_offender_id_display("AF123")
                .p_audit_module_name("transfer")
                .build())
            .build());

        assertThat(event.getEventType()).isEqualTo("IEP_UPSERTED");
        assertThat(event.getBookingId()).isEqualTo(434L);
        assertThat(event.getEventDatetime()).isEqualTo(now);
        assertThat(event.getAgencyLocationId()).isEqualTo("MDI");
        assertThat(event.getIepSeq()).isEqualTo(3);
        assertThat(event.getIepLevel()).isEqualTo("STD");
        assertThat(event.getOffenderIdDisplay()).isEqualTo("AF123");
        assertThat(event.getAgencyLocationId()).isEqualTo("MDI");
        assertThat(event.getAuditModuleName()).isEqualTo("transfer");
    }
}
