package net.syscon.elite.repository;

import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.web.config.CacheConfig;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = { PersistenceConfigs.class, CacheConfig.class })
public class CaseNoteRepositoryTest {

    @Autowired
    private CaseNoteRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void testGetCaseNoteTypesByCaseLoadType() {
        final var types = repository.getCaseNoteTypesByCaseLoadType("COMM");

        assertNotNull(types);
        assertFalse(types.isEmpty());

        // Ensure each type has null value for sub-types
        types.forEach(type -> assertNull(type.getSubCodes()));
    }

    @Test
    public void testGetCaseNoteTypeWithSubTypesByCaseLoadType() {
        final var types = repository.getCaseNoteTypesWithSubTypesByCaseLoadType("COMM");

        // Spot check
        final var type = types.stream().filter(x -> x.getCode().equals("DRR")).findFirst();

        assertTrue(type.isPresent());

        final var subTypes = type.get().getSubCodes();

        assertNotNull(subTypes);
        assertFalse(subTypes.isEmpty());

        assertTrue(subTypes.stream().anyMatch(x -> x.getCode().equals("DTEST")));
    }

    @Test
    public void testCreateCaseNote() {

        final var startTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        final long bookingId = -4;
        final var newCaseNote = newCaseNote();
        final var sourceCode = "source code";
        final var username = "username";
        final long staffId = -2;
        final long caseNoteId = repository.createCaseNote(bookingId, newCaseNote, sourceCode, username, staffId);

        final var map = jdbcTemplate.queryForMap("select TIME_CREATION, CREATE_DATETIME from offender_case_notes where CASE_NOTE_ID = ?", caseNoteId);

        final var timeCreation = ((Timestamp) map.get("TIME_CREATION")).toLocalDateTime();
        final var createDateTime = ((Timestamp) map.get("CREATE_DATETIME")).toLocalDateTime();

        assertThat(timeCreation).isBetween(startTime, startTime.plusSeconds(5));

        assertThat(timeCreation).isBetween(createDateTime.minusSeconds(1), createDateTime.plusSeconds(1));


        jdbcTemplate.update("delete from offender_case_notes where case_note_id = ?", caseNoteId);
    }

    @Test
    public void testCaseNoteTimes() {
        final long bookingId = -4;
        final var newCaseNote = newCaseNote();
        final var sourceCode = "source code";
        final var username = "username";
        final long staffId = -2;
        final long caseNoteId = repository.createCaseNote(bookingId, newCaseNote, sourceCode, username, staffId);

        final var caseNote = repository.getCaseNote(-4, caseNoteId).get();

        final var contactDateTime = caseNote.getOccurrenceDateTime();
        final var createDateTime = caseNote.getCreationDateTime();

        assertThat(contactDateTime).isBetween(createDateTime.minusSeconds(2), createDateTime.plusSeconds(1));

        jdbcTemplate.update("delete from offender_case_notes where case_note_id = ?", caseNoteId);
    }

    private NewCaseNote newCaseNote() {
        final var newCaseNote = new NewCaseNote();
        newCaseNote.setText("text");
        newCaseNote.setType("GEN");
        newCaseNote.setSubType("HIS");
        return newCaseNote;
    }
}
