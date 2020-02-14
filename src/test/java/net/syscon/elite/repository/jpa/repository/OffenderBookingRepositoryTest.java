package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.*;
import net.syscon.elite.repository.jpa.model.OffenderMilitaryRecord.BookingAndSequence;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.web.config.AuditorAwareImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.LocalDate;

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

    @Test
    public void getMilitaryRecords() {
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
    public void saveMilitaryRecords() {
        final var booking = repository.findById(-2L).orElseThrow();
        final var militaryRecords = booking.getMilitaryRecords();
        assertThat(militaryRecords).hasSize(0);
        booking.addOffenderMilitaryRecord(
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
    public void saveMultipleMilitaryRecords() {
        final var booking = repository.findById(-3L).orElseThrow();
        final var militaryRecords = booking.getMilitaryRecords();
        assertThat(militaryRecords).hasSize(0);
        booking.addOffenderMilitaryRecord(
                OffenderMilitaryRecord.builder()
                        .startDate(LocalDate.parse("2000-01-01"))
                        .militaryBranch(new MilitaryBranch("ARM", "Army"))
                        .description("First record")
                        .build());
        booking.addOffenderMilitaryRecord(
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
}


