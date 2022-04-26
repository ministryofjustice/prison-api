package uk.gov.justice.hmpps.prison.service;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceAdjustment;
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
import java.time.Clock;
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

    @Autowired
    private Clock clock;

    private final String OFFENDER_NO = "A1234AA";

    @Test
    public void deactivateAllActiveExternalMovements_IgnoringTheReleaseMovement() {
        releasePrisoner(OFFENDER_NO);

        final var offenderBooking = getOffenderBooking(OFFENDER_NO, false);

        final var activeExternalMovements =
            offenderBooking.getExternalMovements().stream().filter(ExternalMovement::isActive).count();

        assertThat(activeExternalMovements).isOne();
    }

    @Test
    @Ignore("This test is disabled because decrementing the occupancy is done by a trigger that only runs in Oracle not H2")
    public void replenishSpaceInPreviousLivingUnit() {
        // Test disabled until we can test against Oracle - previously worked since code incorrectly decremented the occupancy
        // so the occupancy was decremented twice
        final var offenderBooking = getOffenderBooking(OFFENDER_NO, true);
        final var currentlyAssignedLivingUnit = offenderBooking.getAssignedLivingUnit();
        final var occupancyBeforeRelease = currentlyAssignedLivingUnit.getCurrentOccupancy();
        final var parentOccupancyBeforeRelease =
            Optional.ofNullable(currentlyAssignedLivingUnit.getParentLocation().getCurrentOccupancy()).orElse(0);

        releasePrisoner(OFFENDER_NO);

        final var assignedLivingUnit = agencyInternalLocationRepository
            .findById(currentlyAssignedLivingUnit.getLocationId())
            .orElseThrow();

        assertThat(assignedLivingUnit.getCurrentOccupancy()).isLessThan(occupancyBeforeRelease);
        assertThat(assignedLivingUnit.getParentLocation().getCurrentOccupancy()).isLessThan(parentOccupancyBeforeRelease);

    }

    @Test
    public void deactivateAllBedAssignments() {
        releasePrisoner(OFFENDER_NO);

        final var allActiveBedAssignments = bedAssignmentHistoriesRepository
            .findAllByBedAssignmentHistoryPKOffenderBookingId(-1L)
            .stream().filter(bedAssignmentHistory -> bedAssignmentHistory.getAssignmentEndDate() == null || bedAssignmentHistory.getAssignmentEndDateTime() == null)
            .count();

        assertThat(allActiveBedAssignments).isZero();
    }

    @Test
    public void deactivateSentenceAdjustments() {
        releasePrisoner(OFFENDER_NO);

        final var allActiveSentenceAdjustments = offenderSentenceAdjustmentRepository.findAllByOffenderBooking_BookingId(-1L)
            .stream().filter(SentenceAdjustment::isActive)
            .count();

        assertThat(allActiveSentenceAdjustments).isZero();
    }

    @Test
    public void deactivateAllProgramProfiles() {
        releasePrisoner(OFFENDER_NO);

        final var activeProgramProfiles =
            offenderProgramProfileRepository.findByOffenderBooking_BookingIdAndProgramStatus(-1L, "ALLOC")
                .stream().filter(offenderProgramProfile -> offenderProgramProfile.getEndDate() == null)
                .count();

        assertThat(activeProgramProfiles).isZero();
    }

    @Test
    public void deactivatePayStatus() {
        releasePrisoner(OFFENDER_NO);

        final var activePayStatues = offenderPayStatusRepository.findAllByBookingId(-1L)
            .stream().filter(offenderPayStatus -> offenderPayStatus.getEndDate() == null)
            .count();

        assertThat(activePayStatues).isZero();
    }

    @Test
    public void deactivateNoPayStatus() {
        releasePrisoner(OFFENDER_NO);

        final var activeNoPayStatues = offenderNoPayPeriodRepository.findAllByBookingId(-1L)
            .stream().filter(offenderNoPayPeriod -> offenderNoPayPeriod.getEndDate() == null)
            .count();

        assertThat(activeNoPayStatues).isZero();
    }

    @Test
    public void createsASingleReleaseExternalMovement() {
        releasePrisoner(OFFENDER_NO);

        final var releaseExternalMovement =
            getOffenderBooking(OFFENDER_NO, false).getExternalMovements().stream().filter(ExternalMovement::isActive)
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
        releasePrisoner(OFFENDER_NO);

        final var casesNotes = (List<OffenderCaseNote>) offenderCaseNoteRepository.findAll();

        final var releaseCaseNote = casesNotes
            .stream()
            .filter(casesNote -> casesNote.getOffenderBooking().getBookingId().equals(-1L)).max(Comparator.comparing(OffenderCaseNote::getCreateDatetime))
            .orElseThrow();

        assertThat(releaseCaseNote.getCaseNoteText()).isEqualTo("Released from LEEDS for reason: Conditional Release (CJA91) -SH Term>1YR.");
        assertThat(releaseCaseNote.getType()).extracting("code", "domain").contains("PRISON", "TASK_TYPE");
        assertThat(releaseCaseNote.getSubType()).extracting("code", "domain").contains("RELEASE", "TASK_SUBTYPE");
    }

    @Test
    public void putsOffenderBookingInTheCorrectState() {
        releasePrisoner(OFFENDER_NO);

        final var offenderBooking = getOffenderBooking(OFFENDER_NO, false);

        final var agencyOutside = AgencyLocation.builder().id("OUT").description("OUTSIDE").build();

        assertThat(offenderBooking).extracting(
                "inOutStatus",
                "active",
                "bookingStatus",
                "livingUnitMv",
                "assignedLivingUnit",
                "bookingEndDate",
                "location",
                "statusReason",
                "commStatus")
            .contains("OUT", false, "C", null, null, LocalDateTime.now(clock), agencyOutside, "REL-CR", null);
    }

    private OffenderBooking getOffenderBooking(final String offenderNo, final Boolean active) {
        return offenderBookingRepository.findByOffenderNomsIdAndActive(offenderNo, active).orElseThrow();
    }

    private void releasePrisoner(final String offenderNo) {
        prisonerReleaseAndTransferService.releasePrisoner(offenderNo, RequestToReleasePrisoner.builder()
                .releaseTime(LocalDateTime.now(clock))
                .toLocationCode("OUT")
                .movementReasonCode("CR")
                .build(),
            null);
        entityManager.flush();
    }
}
