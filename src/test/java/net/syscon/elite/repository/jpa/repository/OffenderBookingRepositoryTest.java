package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.*;
import net.syscon.elite.repository.jpa.model.OffenderMilitaryRecord.BookingAndSequence;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.web.config.AuditorAwareImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;

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

    private static final Long BOOKING_WITH_COURT_CASE_BUT_NO_EVENTS = -8L;

    @Autowired
    private OffenderBookingRepository repository;

    @Autowired
    private AgencyLocationRepository agencyLocationRepository;

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
                        .description("left")
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
                        .militaryBranch(new MilitaryBranch("ARM", "Army"))
                        .dischargeLocation("Somewhere")
                        .disciplinaryAction(new DisciplinaryAction("CM", "Court Martial"))
                        .build());
        repository.save(booking);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        final var persistedBooking = repository.findById(-2L).orElseThrow();

        assertThat(persistedBooking.getMilitaryRecords()).containsExactly(
                OffenderMilitaryRecord.builder()
                        .bookingAndSequence(new BookingAndSequence(booking, 1))
                        .startDate(LocalDate.parse("2000-01-01"))
                        .militaryBranch(new MilitaryBranch("ARM", "Army"))
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
                        .militaryBranch(new MilitaryBranch("ARM", "Army"))
                        .description("First record")
                        .build());
        booking.add(
                OffenderMilitaryRecord.builder()
                        .startDate(LocalDate.parse("2000-01-01"))
                        .militaryBranch(new MilitaryBranch("ARM", "Army"))
                        .description("Second record")
                        .build());

        repository.save(booking);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

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
                .containsOnly(
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
        var booking = repository.findById(-2L).orElseThrow();

        assertThat(booking.getCourtCases()).extracting(OffenderCourtCase::getId).containsOnly(-2L);

        booking.add(offenderCourtCase(-98L));
        booking.add(offenderCourtCase(-99L));

        repository.save(booking);

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        var persistedBooking = repository.findById(-2L).orElseThrow();

        assertThat(persistedBooking.getCourtCases()).extracting(OffenderCourtCase::getId).containsExactly(-2L, -98L, -99L);
    }

    @Test
    void saveCourtCaseEvent() {
        var offenderBooking = repository.findById(BOOKING_WITH_COURT_CASE_BUT_NO_EVENTS).orElseThrow();

        assertThat(offenderBooking.getCourtCases()).hasSize(1);

        var offenderCourtCase = offenderBooking.getCourtCases().stream().findFirst().orElseThrow();

        assertThat(offenderCourtCase.getCourtEvents()).isEmpty();

        CourtEvent courtEvent = courtEventFor(offenderCourtCase);

        offenderCourtCase.add(courtEvent);

        repository.save(offenderBooking);

        entityManager.flush();

        var persistedBooking = repository.findById(BOOKING_WITH_COURT_CASE_BUT_NO_EVENTS).orElseThrow();

        assertThat(persistedBooking.getCourtCases().get(0).getCourtEvents()).hasSize(1);

        CourtEvent persistedCourtEvent = persistedBooking.getCourtCases().stream()
                .map(OffenderCourtCase::getCourtEvents).findFirst().orElseThrow().get(0);

        assertThat(persistedCourtEvent)
                .extracting(
                        CourtEvent::getId,
                        CourtEvent::getCommentText,
                        CourtEvent::getCourtEventType,
                        CourtEvent::getCourtLocation,
                        CourtEvent::getDirectionCode,
                        CourtEvent::getEventDate,
                        CourtEvent::getEventStatus,
                        CourtEvent::getNextEventRequestFlag,
                        CourtEvent::getOffenderBooking,
                        CourtEvent::getOffenderCourtCase,
                        CourtEvent::getOrderRequestedFlag,
                        CourtEvent::getStartTime)
                .containsOnly(
                        courtEvent.getId(),
                        courtEvent.getCommentText(),
                        courtEvent.getCourtEventType(),
                        courtEvent.getCourtLocation(),
                        courtEvent.getDirectionCode(),
                        courtEvent.getEventDate(),
                        courtEvent.getEventStatus(),
                        courtEvent.getNextEventRequestFlag(),
                        courtEvent.getOffenderBooking(),
                        courtEvent.getOffenderCourtCase(),
                        courtEvent.getOrderRequestedFlag(),
                        courtEvent.getStartTime());
    }

    private CourtEvent courtEventFor(final OffenderCourtCase courtCase) {
        var eventDate = LocalDate.now();

        return CourtEvent.builder()
                .id(999L)
                .commentText("Comment text for court event")
                .courtEventType(new EventType("CRT", "Court Action"))
                .courtLocation(agencyLocationRepository.findById("COURT1").orElseThrow())
                .directionCode("OUT")
                .eventDate(eventDate)
                .eventStatus(new EventStatus("SCH", "Scheduled (Approved)"))
                .nextEventRequestFlag("X")
                .offenderBooking(courtCase.getOffenderBooking())
                .offenderCourtCase(courtCase)
                .orderRequestedFlag("Y")
                .startTime(eventDate.atTime(12, 0))
                .build();

    }

    private OffenderCourtCase offenderCourtCase(final Long caseIdentifier) {
        return OffenderCourtCase.builder()
                .id(caseIdentifier)
                .beginDate(LocalDate.EPOCH)
                .caseSeq(caseIdentifier)
                .agencyLocation(agencyLocationRepository.findById("COURT1").orElseThrow())
                .build();
    }
}


