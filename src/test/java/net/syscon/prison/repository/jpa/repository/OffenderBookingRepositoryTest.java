package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.ActiveFlag;
import net.syscon.prison.repository.jpa.model.AgencyInternalLocation;
import net.syscon.prison.repository.jpa.model.AgencyLocation;
import net.syscon.prison.repository.jpa.model.CaseStatus;
import net.syscon.prison.repository.jpa.model.DisciplinaryAction;
import net.syscon.prison.repository.jpa.model.LegalCaseType;
import net.syscon.prison.repository.jpa.model.MilitaryBranch;
import net.syscon.prison.repository.jpa.model.MilitaryDischarge;
import net.syscon.prison.repository.jpa.model.MilitaryRank;
import net.syscon.prison.repository.jpa.model.OffenderCourtCase;
import net.syscon.prison.repository.jpa.model.OffenderMilitaryRecord;
import net.syscon.prison.repository.jpa.model.OffenderMilitaryRecord.BookingAndSequence;
import net.syscon.prison.repository.jpa.model.OffenderPropertyContainer;
import net.syscon.prison.repository.jpa.model.PropertyContainer;
import net.syscon.prison.repository.jpa.model.WarZone;
import net.syscon.prison.security.AuthenticationFacade;
import net.syscon.prison.web.config.AuditorAwareImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class OffenderBookingRepositoryTest {

    @Autowired
    private OffenderBookingRepository repository;

    @Autowired
    private AgencyLocationRepository agencyLocationRepository;

    @Autowired
    private ReferenceCodeRepository<MilitaryBranch> militaryBranchRepository;

    @Autowired
    private ReferenceCodeRepository<DisciplinaryAction> disciplinaryActionRepository;

    @Autowired
    private AgencyInternalLocationRepository agencyInternalLocationRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void getMilitaryRecords() {
        final var booking = repository.findById(-1L).orElseThrow();

        assertThat(booking.getMilitaryRecords()).containsExactly(
                OffenderMilitaryRecord.builder()
                        .bookingAndSequence(new BookingAndSequence(booking, 1))
                        .startDate(LocalDate.parse("2000-01-01"))
                        .endDate(LocalDate.parse("2020-10-17"))
                        .militaryDischarge(new MilitaryDischarge("DIS", "Dishonourable"))
                        .warZone(new WarZone("AFG", "Afghanistan"))
                        .militaryBranch(new MilitaryBranch("ARM", "Army"))
                        .description("Some Description Text")
                        .unitNumber("auno")
                        .enlistmentLocation("Somewhere")
                        .militaryRank(new MilitaryRank("LCPL_RMA", "Lance Corporal  (Royal Marines)"))
                        .serviceNumber("asno")
                        .disciplinaryAction(new DisciplinaryAction("CM", "Court Martial"))
                        .dischargeLocation("Sheffield")
                        .build(),
                OffenderMilitaryRecord.builder()
                        .bookingAndSequence(new BookingAndSequence(booking, 2))
                        .startDate(LocalDate.parse("2001-01-01"))
                        .militaryBranch(new MilitaryBranch("NAV", "Navy"))
                        .description("second record")
                        .build());
    }

    @Test
    void saveMilitaryRecords() {
        final var booking = repository.findById(-2L).orElseThrow();
        final var militaryRecords = booking.getMilitaryRecords();

        assertThat(militaryRecords).isEmpty();

        booking.add(
                OffenderMilitaryRecord.builder()
                        .startDate(LocalDate.parse("2000-01-01"))
                        .militaryBranch(militaryBranchRepository.findById(MilitaryBranch.ARMY).orElseThrow())
                        .dischargeLocation("Somewhere")
                        .disciplinaryAction(disciplinaryActionRepository.findById(DisciplinaryAction.COURT_MARTIAL).orElseThrow())
                        .build());
        repository.save(booking);

        entityManager.flush();

        final var persistedBooking = repository.findById(-2L).orElseThrow();

        assertThat(persistedBooking.getMilitaryRecords()).containsExactly(
                OffenderMilitaryRecord.builder()
                        .bookingAndSequence(new BookingAndSequence(booking, 1))
                        .startDate(LocalDate.parse("2000-01-01"))
                        .militaryBranch(militaryBranchRepository.findById(MilitaryBranch.ARMY).orElseThrow())
                        .dischargeLocation("Somewhere")
                        .disciplinaryAction(new DisciplinaryAction("CM", "Court Martial"))
                        .build());

        // also check auditing
        assertThat(persistedBooking.getMilitaryRecords().get(0)).extracting("createUserId").isEqualTo("user");
    }

    @Test
    void saveMultipleMilitaryRecords() {
        final var booking = repository.findById(-3L).orElseThrow();

        final var militaryRecords = booking.getMilitaryRecords();

        assertThat(militaryRecords).isEmpty();

        booking.add(
                OffenderMilitaryRecord.builder()
                        .startDate(LocalDate.parse("2000-01-01"))
                        .militaryBranch(militaryBranchRepository.findById(MilitaryBranch.ARMY).orElseThrow())
                        .description("First record")
                        .build());
        booking.add(
                OffenderMilitaryRecord.builder()
                        .startDate(LocalDate.parse("2000-01-01"))
                        .militaryBranch(militaryBranchRepository.findById(MilitaryBranch.ARMY).orElseThrow())
                        .description("Second record")
                        .build());

        repository.save(booking);

        entityManager.flush();

        final var persistedBooking = repository.findById(-3L).orElseThrow();

        assertThat(persistedBooking.getMilitaryRecords()).extracting(OffenderMilitaryRecord::getDescription).containsExactly("First record", "Second record");
    }

    @Test
    void getOffendersCourtCase() {
        assertThat(repository.findById(-1L).orElseThrow().getCourtCases()).flatExtracting(
                OffenderCourtCase::getId,
                OffenderCourtCase::getCaseStatus,
                OffenderCourtCase::getLegalCaseType,
                OffenderCourtCase::getAgencyLocation)
                .containsExactly(
                        -1L,
                        Optional.of(new CaseStatus("A", "Active")),
                        Optional.of(new LegalCaseType("A", "Adult")),
                        AgencyLocation.builder()
                                .id("COURT1")
                                .description("Court 1")
                                .type("CRT")
                                .activeFlag(ActiveFlag.Y)
                                .build());
    }

    @Test
    void saveOffenderCourtCases() {
        final var booking = repository.findById(-2L).orElseThrow();

        assertThat(booking.getCourtCases()).extracting(OffenderCourtCase::getId).containsOnly(-2L);

        booking.add(offenderCourtCase(-98L));
        booking.add(offenderCourtCase(-99L));

        repository.save(booking);

        entityManager.flush();

        final var persistedBooking = repository.findById(-2L).orElseThrow();

        assertThat(persistedBooking.getCourtCases()).extracting(OffenderCourtCase::getId).containsExactly(-2L, -98L, -99L);
    }

    private OffenderCourtCase offenderCourtCase(final Long caseIdentifier) {
        return OffenderCourtCase.builder()
                .id(caseIdentifier)
                .beginDate(LocalDate.EPOCH)
                .caseSeq(caseIdentifier)
                .agencyLocation(agencyLocationRepository.findById("COURT1").orElseThrow())
                .build();
    }

    @Test
    void updateLivingUnit() {
        final var offenderBooking = repository.findById(-1L).orElseThrow();
        final var livingUnit = agencyInternalLocationRepository.findById(-4L).orElseThrow();
        offenderBooking.setAssignedLivingUnit(livingUnit);
        repository.save(offenderBooking);

        entityManager.flush();
        final var result = repository.findById(-1L).orElseThrow();
        assertThat(result.getAssignedLivingUnit().getLocationId()).isEqualTo(-4L);
    }

    @Test
    void getOffenderPropertyContainers() {
        assertThat(repository.findById(-1L).orElseThrow().getPropertyContainers()).flatExtracting(
                OffenderPropertyContainer::getContainerId,
                OffenderPropertyContainer::getSealMark,
                OffenderPropertyContainer::getInternalLocation,
                OffenderPropertyContainer::getActiveFlag,
                OffenderPropertyContainer::getContainerType)
                .containsExactly(
                        -1L,
                        "TEST10",
                        AgencyInternalLocation.builder()
                                .locationId(-10L)
                                .activeFlag(ActiveFlag.Y)
                                .locationType("CELL")
                                .agencyId("LEI")
                                .description("LEI-A-1-8")
                                .parentLocationId(-2L)
                                .currentOccupancy(0)
                                .operationalCapacity(1)
                                .userDescription(null)
                                .locationCode("8")
                                .build(),
                        "Y",
                        new PropertyContainer("BULK", "Bulk"));
    }
}


