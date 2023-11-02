package uk.gov.justice.hmpps.prison.repository;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteSubType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class, PersistenceConfigs.class})
@WithMockUser
@Slf4j
public class CaseNoteRepositoryTest {

    @Autowired
    private CaseNoteRepository repository;

    @Autowired
    private OffenderCaseNoteRepository offenderCaseNoteRepository;

    @Autowired
    private OffenderBookingRepository offenderBookingRepository;

    @Autowired
    private ReferenceCodeRepository<CaseNoteType> caseNoteTypeReferenceCodeRepository;
    @Autowired
    private ReferenceCodeRepository<CaseNoteSubType> caseNoteSubTypeReferenceCodeRepository;

    @Autowired
    private StaffUserAccountRepository staffUserAccountRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

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
        final long caseNoteId = createCaseNote(bookingId, newCaseNote, sourceCode);

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
        final long caseNoteId = createCaseNote(bookingId, newCaseNote, sourceCode);

        final var caseNote = offenderCaseNoteRepository.findByIdAndOffenderBooking_BookingId(caseNoteId, bookingId).orElseThrow();

        final var contactDateTime = caseNote.getOccurrenceDateTime();
        final var createDateTime = caseNote.getCreateDatetime();

        assertThat(contactDateTime).isBetween(createDateTime.minusSeconds(2), createDateTime.plusSeconds(1));

        jdbcTemplate.update("delete from offender_case_notes where case_note_id = ?", caseNoteId);
    }

    private NewCaseNote newCaseNote() {
        final var newCaseNote = new NewCaseNote();
        newCaseNote.setText("text");
        newCaseNote.setType("GEN");
        newCaseNote.setSubType("HIS");
        newCaseNote.setOccurrenceDateTime(LocalDateTime.now());
        return newCaseNote;
    }

    private long createCaseNote(final long bookingId, final NewCaseNote newCaseNote, final String sourceCode) {
        final var caseNote = OffenderCaseNote.builder()
            .caseNoteText(newCaseNote.getText())
            .type(caseNoteTypeReferenceCodeRepository.findById(CaseNoteType.pk(newCaseNote.getType())).orElseThrow(EntityNotFoundException.withId(newCaseNote.getType())))
            .subType(caseNoteSubTypeReferenceCodeRepository.findById(CaseNoteSubType.pk(newCaseNote.getSubType())).orElseThrow(EntityNotFoundException.withId(newCaseNote.getSubType())))
            .noteSourceCode(sourceCode)
            .author(staffUserAccountRepository.findById("ITAG_USER").orElseThrow().getStaff())
            .occurrenceDateTime(newCaseNote.getOccurrenceDateTime())
            .occurrenceDate(newCaseNote.getOccurrenceDateTime().toLocalDate())
            .amendmentFlag(false)
            .offenderBooking(offenderBookingRepository.findById(bookingId).orElseThrow())
            .build();
        final var id = offenderCaseNoteRepository.save(caseNote).getId();
        entityManager.flush();
        return id;
    }
}
