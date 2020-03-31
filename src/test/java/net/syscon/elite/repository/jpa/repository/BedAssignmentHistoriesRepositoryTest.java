package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.BedAssignmentHistory;
import net.syscon.elite.repository.jpa.model.OffenderBooking;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.web.config.AuditorAwareImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class BedAssignmentHistoriesRepositoryTest {

    @Autowired
    private BedAssignmentHistoriesRepository repository;

    @Test
    public void getMaxSeqForBookingId_noRecords() {
        assertThat(repository.getMaxSeqForBookingId(-2L)).isEqualTo(0);
    }

    @Test
    public void getMaxSeqForBookingId_singleRecord() {
        createBedAssignmentHistories(-3L, 1);

        assertThat(repository.getMaxSeqForBookingId(-3L)).isEqualTo(1);
    }

    @Test
    public void getMaxSeqForBookingId_severalRecords() {
        createBedAssignmentHistories(-4L, 4);

        assertThat(repository.getMaxSeqForBookingId(-4L)).isEqualTo(4);
    }

    private void createBedAssignmentHistories(Long bookingId, Integer numberRecords) {
        IntStream.rangeClosed(1, numberRecords).forEach(seq -> {
            final var bookingAndSequence = new BedAssignmentHistory.BookingAndSequence(bookingId, seq);
            final var bedAssignmentHistory =
                    BedAssignmentHistory.builder()
                            .bookingAndSequence(bookingAndSequence)
                            .livingUnitId(2L)
                            .assignmentDate(LocalDate.now())
                            .assignmentDateTime(LocalDateTime.now())
                            .build();
            repository.save(bedAssignmentHistory);
        });

    }
}
