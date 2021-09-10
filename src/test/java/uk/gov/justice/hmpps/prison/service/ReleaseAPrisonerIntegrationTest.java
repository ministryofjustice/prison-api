package uk.gov.justice.hmpps.prison.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderNoPayPeriodRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderPayStatusRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProgramProfileRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceAdjustmentRepository;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@WithMockUser("ITAG_USER_ADM")
@ContextConfiguration(classes = TestClock.class)
@ActiveProfiles("test")
@Transactional
public class ReleaseAPrisonerIntegrationTest {

    @Autowired
    private PrisonerReleaseAndTransferService prisonerReleaseAndTransferService;

    @Autowired
    private OffenderBookingRepository offenderBookingRepository;

    @Autowired
    private AgencyInternalLocationRepository agencyInternalLocationRepository;

    @Autowired
    private BedAssignmentHistoriesRepository bedAssignmentHistoriesRepository;

    @Autowired
    private OffenderSentenceAdjustmentRepository offenderSentenceAdjustmentRepository;

    @Autowired
    private OffenderProgramProfileRepository offenderProgramProfileRepository;

    @Autowired
    private OffenderPayStatusRepository offenderPayStatusRepository;

    @Autowired
    private OffenderNoPayPeriodRepository offenderNoPayPeriodRepository;

    @Autowired
    private OffenderCaseNoteRepository offenderCaseNoteRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void deactivateAllActiveExternalMovements_IgnoringTheReleaseMovement() {
        releasePrisoner("A1234AA");

        final var offenderBooking = getOffenderBooking(false);

        final var activeExternalMovements =
            offenderBooking.getExternalMovements().stream().filter(externalMovement -> externalMovement.getActiveFlag().isActive()).count();

        assertThat(activeExternalMovements).isOne();
    }

    @Test
    public void replenishSpaceInPreviousLivingUnit() {
        final var offenderBooking = getOffenderBooking(true);
        final var currentlyAssignedLivingUnit = offenderBooking.getAssignedLivingUnit();
        final var occupancyBeforeRelease = currentlyAssignedLivingUnit.getCurrentOccupancy();
        final var parentOccupancyBeforeRelease =
            Optional.ofNullable(currentlyAssignedLivingUnit.getParentLocation().getCurrentOccupancy()).orElse(0);

        releasePrisoner("A1234AA");

        final var assignedLivingUnit = agencyInternalLocationRepository
            .findById(currentlyAssignedLivingUnit.getLocationId())
            .orElseThrow();

        assertThat(assignedLivingUnit.getCurrentOccupancy()).isGreaterThan(occupancyBeforeRelease);
        assertThat(assignedLivingUnit.getParentLocation().getCurrentOccupancy()).isGreaterThan(parentOccupancyBeforeRelease);

    }

    @Test
    public void disableAllBedAssignments() {
        releasePrisoner("A1234AA");

        final var allActiveBedAssignments = bedAssignmentHistoriesRepository
            .findAllByBedAssignmentHistoryPKOffenderBookingId(-1L)
            .stream().filter(bedAssignmentHistory -> bedAssignmentHistory.getAssignmentEndDate() == null || bedAssignmentHistory.getAssignmentEndDateTime() == null)
            .count();

        assertThat(allActiveBedAssignments).isZero();
    }

    @Test
    public void deactivateSentenceAdjustments() {
        releasePrisoner("A1234AA");

        final var allActiveSentenceAdjustments = offenderSentenceAdjustmentRepository.findAllByOffenderBooking_BookingId(-1L)
            .stream().filter(sentenceAdjustment -> sentenceAdjustment.isActive())
            .count();

        assertThat(allActiveSentenceAdjustments).isZero();
    }

    @Test
    public void deactivateAllProgramProfiles() {
        releasePrisoner("A1234AA");

        final var activeProgramProfiles =
            offenderProgramProfileRepository.findByOffenderBooking_BookingIdAndProgramStatus(-1L, "ALLOC")
                .stream().filter(offenderProgramProfile -> offenderProgramProfile.getEndDate() == null)
                .count();

        assertThat(activeProgramProfiles).isZero();
    }

    @Test
    public void deactivatePayStatus() {
        releasePrisoner("A1234AA");

        final var activePayStatues = offenderPayStatusRepository.findAllByBookingId(-1L)
            .stream().filter(offenderPayStatus -> offenderPayStatus.getEndDate() == null)
            .count();

        assertThat(activePayStatues).isZero();
    }

    @Test
    public void deactivateNoPayStatus() {
        releasePrisoner("A1234AA");

        final var activeNoPayStatues = offenderNoPayPeriodRepository.findAllByBookingId(-1L)
            .stream().filter(offenderNoPayPeriod -> offenderNoPayPeriod.getEndDate() == null)
            .count();

        assertThat(activeNoPayStatues).isZero();
    }

    @Test
    public void createsASingleReleaseExternalMovement() {
        releasePrisoner("A1234AA");

        final var releaseExternalMovement =
            getOffenderBooking(false).getExternalMovements().stream().filter(externalMovement -> externalMovement.getActiveFlag().isActive())
                .findFirst()
                .orElseThrow();

        assertThat(releaseExternalMovement.getMovementReason())
            .extracting("domain", "code", "description")
            .contains("MOVE_RSN", "CR", "Conditional Release (CJA91) -SH Term>1YR");

        assertThat(releaseExternalMovement.getMovementType())
            .extracting("domain", "code", "description")
            .contains("MOVE_TYPE", "REL", "Release");
    }

    @Test
    public void createsAReleaseCaseNote() {
        releasePrisoner("A1234AA");

        final var casesNotes = (List<OffenderCaseNote>) offenderCaseNoteRepository.findAll();

        final var releaseCaseNote = casesNotes
            .stream()
            .filter(casesNote -> casesNote.getOffenderBooking().getBookingId().equals(-1L))
            .sorted(Comparator.comparing(OffenderCaseNote::getCreateDatetime).reversed())
            .findFirst()
            .orElseThrow();

        assertThat(releaseCaseNote.getCaseNoteText()).isEqualTo("Released from LEEDS for reason: Conditional Release (CJA91) -SH Term>1YR.");
        assertThat(releaseCaseNote.getType()).extracting("code", "domain").contains("PRISON", "TASK_TYPE");
        assertThat(releaseCaseNote.getSubType()).extracting("code", "domain").contains("RELEASE", "TASK_SUBTYPE");
    }

    private OffenderBooking getOffenderBooking(final Boolean activeFlag) {
        return offenderBookingRepository.findByOffenderNomsIdAndActiveFlag("A1234AA", activeFlag ? "Y" : "N").orElseThrow();
    }

    private void releasePrisoner(final String offenderNo) {
        prisonerReleaseAndTransferService.releasePrisoner(offenderNo, RequestToReleasePrisoner.builder()
                .releaseTime(LocalDateTime.now().minusDays(100))
                .toLocationCode("OUT")
                .movementReasonCode("CR")
                .build(),
            null);
        entityManager.flush();
    }
}
