package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.DisciplinaryAction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryBranch;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryDischarge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryRank;
import uk.gov.justice.hmpps.prison.repository.jpa.model.NonAssociationReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord.BookingAndSequence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPropertyContainer;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PropertyContainer;
import uk.gov.justice.hmpps.prison.repository.jpa.model.WarZone;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

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
                                .type(AgencyLocationType.COURT_TYPE)
                                .active(true)
                                .build());
    }

    @Test
    void saveOffenderCourtCases() {
        final var booking = repository.findById(-2L).orElseThrow();

        assertThat(booking.getCourtCases()).extracting(OffenderCourtCase::getCaseSeq).containsOnly(1L);

        booking.add(offenderCourtCase(2L));
        booking.add(offenderCourtCase(3L));

        repository.save(booking);

        entityManager.flush();

        final var persistedBooking = repository.findById(-2L).orElseThrow();

        assertThat(persistedBooking.getCourtCases()).extracting(OffenderCourtCase::getCaseSeq).contains(1L, 2L, 3L);
    }

    private OffenderCourtCase offenderCourtCase(final Long sequence) {
        return OffenderCourtCase.builder()
                .beginDate(LocalDate.EPOCH)
                .caseSeq(sequence)
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
        final var parentParentLocation = AgencyInternalLocation.builder().locationId(-1L).locationType("WING").agencyId("LEI")
            .currentOccupancy(null).operationalCapacity(13).description("LEI-A").userDescription("Block A").capacity(14)
            .certifiedFlag(true).locationCode("A").active(true).build();

        AgencyInternalLocation.builder().locationId(-2L).locationType("LAND").agencyId("LEI").capacity(14)
            .currentOccupancy(null).operationalCapacity(13).description("LEI-A-1").parentLocation(parentParentLocation).userDescription("Landing A/1")
            .certifiedFlag(true).locationCode("1").active(true).build();

        assertThat(repository.findById(-1L).orElseThrow().getPropertyContainers()).flatExtracting(
                OffenderPropertyContainer::getContainerId,
                OffenderPropertyContainer::getSealMark,
                OffenderPropertyContainer::getInternalLocation,
                OffenderPropertyContainer::isActive,
                OffenderPropertyContainer::getContainerType)
                .containsAnyOf(
                        -1L,
                        "TEST10",
                        AgencyInternalLocation.builder()
                                .locationId(-10L)
                                .active(true)
                                .certifiedFlag(true)
                                .locationType("CELL")
                                .agencyId("LEI")
                                .description("LEI-A-1-8")
                                .userDescription(null)
                                .locationCode("8")
                                .build(),
                        "Y",
                        new PropertyContainer("BULK", "Bulk"));
    }

    @Test
    void getNonAssociations() {
        final var nonAssociations = repository.findById(-1L).orElseThrow().getNonAssociationDetails();
        assertThat(nonAssociations).extracting(OffenderNonAssociationDetail::getNonAssociationReason).containsExactly(new NonAssociationReason("VIC", "Victim"), new NonAssociationReason("RIV", "Rival Gang"));
    }

    @Test
    void findByOffenderNomsIdAndActive() {
        final var optionalOffenderBooking = repository.findByOffenderNomsIdAndActive("A1234AA", true);
        //noinspection unchecked
        assertThat(optionalOffenderBooking).get().extracting(OffenderBooking::getBookingId, o -> o.getRootOffender().getId()).containsExactly(-1L, -1001L);
    }

    @Test
    void findByOffenderNomsIdAndActiveIsN() {
        final var optionalOffenderBooking = repository.findByOffenderNomsIdAndActive("A1234AA", false);
        assertThat(optionalOffenderBooking).isEmpty();
    }
}


