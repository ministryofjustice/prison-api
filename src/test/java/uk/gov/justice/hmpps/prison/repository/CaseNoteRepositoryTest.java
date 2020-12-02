package uk.gov.justice.hmpps.prison.repository;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteEvent;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageByBookingId;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.web.config.CacheConfig;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@SuppressWarnings("SqlResolve")
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = {PersistenceConfigs.class, CacheConfig.class})
@Slf4j
public class CaseNoteRepositoryTest {

    @Autowired
    private CaseNoteRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void testGetCaseNoteTypesByCaseLoadType() {
        final var types = repository.getCaseNoteTypesByCaseLoadType("COMM");

        //noinspection unchecked
        assertThat(types).extracting(ReferenceCode::getSubCodes).containsOnly(List.of());
    }

    @Test
    public void testGetCaseNoteTypeWithSubTypesByCaseLoadType() {
        final var types = repository.getCaseNoteTypesWithSubTypesByCaseLoadTypeAndActiveFlag("COMM", true);

        // Spot check
        final var type = types.stream().filter(x -> x.getCode().equals("DRR")).findFirst();
        assertThat(type).isPresent();

        final var subTypes = type.orElseThrow().getSubCodes();

        assertThat(subTypes).extracting(ReferenceCode::getCode).contains("DTEST");
    }

    @Test
    public void testCreateCaseNote() {

        final var startTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        final long bookingId = -16;
        final var newCaseNote = newCaseNote();
        final var sourceCode = "source code";
        final var username = "username";
        final long staffId = -4;
        final long caseNoteId = repository.createCaseNote(bookingId, newCaseNote, sourceCode, username, staffId);

        final var map = jdbcTemplate.queryForMap("select TIME_CREATION, CREATE_DATETIME from offender_case_notes where CASE_NOTE_ID = ?", caseNoteId);

        final var timeCreation = ((Timestamp) map.get("TIME_CREATION")).toLocalDateTime();
        final var createDateTime = ((Timestamp) map.get("CREATE_DATETIME")).toLocalDateTime();

        assertThat(timeCreation).isBetween(startTime, startTime.plusSeconds(5));

        assertThat(timeCreation).isBetween(createDateTime.minusSeconds(2), createDateTime.plusSeconds(2));


        jdbcTemplate.update("delete from offender_case_notes where case_note_id = ?", caseNoteId);
    }

    @Test
    public void testCaseNoteTimes() {
        final long bookingId = -16;
        final var newCaseNote = newCaseNote();
        final var sourceCode = "source code";
        final var username = "username";
        final long staffId = -4;
        final long caseNoteId = repository.createCaseNote(bookingId, newCaseNote, sourceCode, username, staffId);

        final var caseNote = repository.getCaseNote(bookingId, caseNoteId).orElseThrow();

        final var contactDateTime = caseNote.getOccurrenceDateTime();
        final var createDateTime = caseNote.getCreationDateTime();

        assertThat(contactDateTime).isBetween(createDateTime.minusSeconds(2), createDateTime.plusSeconds(1));

        jdbcTemplate.update("delete from offender_case_notes where case_note_id = ?", caseNoteId);
    }

    @Test
    public void getCaseNoteUsageByBookingIdSingleCaseNote() {
        final var notes = repository.getCaseNoteUsageByBookingId("COMMS", "COM_OUT", List.of(-2), LocalDate.of(2017, 1, 1), LocalDate.of(2018, 1, 1));

        assertThat(notes).containsOnly(new CaseNoteUsageByBookingId(-2, "COMMS", "COM_OUT", 1, LocalDateTime.parse("2017-05-06T17:11:00")));
    }

    @Test
    public void getCaseNoteUsageByBookingIdMultipleCaseNote() {
        final var notes = repository.getCaseNoteUsageByBookingId("OBSERVE", "OBS_GEN", List.of(-3), LocalDate.of(2017, 1, 1), LocalDate.of(2017, 8, 1));

        assertThat(notes).containsOnly(new CaseNoteUsageByBookingId(-3, "OBSERVE", "OBS_GEN", 6, LocalDateTime.parse("2017-07-31T12:00")));
    }

    @Test
    public void getCaseNoteUsageByBookingIdMultipleBookingIds() {
        final var notes = repository.getCaseNoteUsageByBookingId("OBSERVE", "OBS_GEN", List.of(-16, -3), LocalDate.of(2017, 1, 1), LocalDate.of(2017, 8, 1));

        assertThat(notes).containsOnly(
            new CaseNoteUsageByBookingId(-3, "OBSERVE", "OBS_GEN", 6, LocalDateTime.parse("2017-07-31T12:00")),
            new CaseNoteUsageByBookingId(-16, "OBSERVE", "OBS_GEN", 1, LocalDateTime.parse("2017-05-13T12:00")));
    }

    @Test
    public void getCaseNoteEvents() {
        // NOTE: offender_case_notes.audit_timestamp is populated by hsqldb with a value TRUNCATED to the millisecond level, so it is
        // possible that it could end up earlier than LocalDateTime.now(), causing a test failure!
        final var start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        final var caseNote = newCaseNote();
        caseNote.setText("Testing of getCaseNoteEvents");
        final var id = repository.createCaseNote(-16, caseNote, "source", "user", -4L);

        final var caseNoteEvents = repository.getCaseNoteEvents(start, Set.of("GEN", "BOB"), 1000);
        log.info("Used start time of {}", start);
        assertThat(caseNoteEvents).extracting(
            CaseNoteEvent::getNomsId,
            CaseNoteEvent::getId,
            CaseNoteEvent::getContent,
            CaseNoteEvent::getEstablishmentCode,
            CaseNoteEvent::getNoteType,
            CaseNoteEvent::getStaffName
        ).contains(Tuple.tuple(
            "A1234AP",
            id,
            "Testing of getCaseNoteEvents",
            "MUL",
            "GEN HIS",
            "User, Test"
        ));
        final var event = caseNoteEvents.stream().filter((e) -> e.getContent().equals("Testing of getCaseNoteEvents")).findFirst().orElseThrow();
        assertThat(event.getContactTimestamp()).isBetween(start.minusSeconds(1), LocalDateTime.now().plusSeconds(1));
        assertThat(event.getNotificationTimestamp()).isBetween(start.minusSeconds(1), LocalDateTime.now().plusSeconds(1));
    }

    @Test
    public void getCaseNoteEvents_Limit() {
        final var start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        final var caseNote = newCaseNote();
        caseNote.setText("Testing of getCaseNoteEvents_Limit");
        repository.createCaseNote(-16, caseNote, "source", "user", -4L);
        repository.createCaseNote(-16, caseNote, "source", "user", -4L);

        final var caseNoteEvents = repository.getCaseNoteEvents(start, Set.of("GEN", "BOB"), 1);
        assertThat(caseNoteEvents).hasSize(1);
    }

    @Test
    public void getCaseNoteEvents_Types() {
        final var start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        final var caseNote = newCaseNote();
        caseNote.setText("Testing of getCaseNoteEvents_Types");
        repository.createCaseNote(-16, caseNote, "source", "user", -4L);
        repository.createCaseNote(-16, caseNote, "source", "user", -4L);

        final var caseNoteEvents = repository.getCaseNoteEvents(start, Set.of("BOB"), 1);
        assertThat(caseNoteEvents).hasSize(0);
    }

    private NewCaseNote newCaseNote() {
        final var newCaseNote = new NewCaseNote();
        newCaseNote.setText("text");
        newCaseNote.setType("GEN");
        newCaseNote.setSubType("HIS");
        return newCaseNote;
    }
}
