package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Test
    public void findBedAssignmentHistory_forLocationAndDatePeriod() {
        final var cellHistory =
            repository.findByLivingUnitIdAndDateTimeRange(
                -16,
                LocalDateTime.of(2000, 10, 16, 0, 0, 0),
                LocalDateTime.of(2020, 10, 10, 0, 0, 0)
            );

        assertThat(cellHistory).containsExactlyInAnyOrder(
            BedAssignmentHistory.builder()
                .livingUnitId(-16L)
                .assignmentDate(LocalDate.of(2019, 10, 17))
                .assignmentDateTime(LocalDateTime.of(LocalDate.of(2019, 10, 17), LocalTime.of(11, 0)))
                .assignmentEndDate(LocalDate.of(2020, 1, 1))
                .assignmentEndDateTime(LocalDateTime.of(LocalDate.of(2020, 1, 1), LocalTime.of(11, 0)))
                .assignmentReason("ADM")
                .build(),
            BedAssignmentHistory.builder()
                .livingUnitId(-16L)
                .assignmentDate(LocalDate.of(2020, 4, 3))
                .assignmentDateTime(LocalDateTime.of(LocalDate.of(2020, 4, 3), LocalTime.of(11, 0)))
                .assignmentReason("ADM")
                .build(),
            BedAssignmentHistory.builder()
                .livingUnitId(-16L)
                .assignmentDate(LocalDate.of(1985, 4, 3))
                .assignmentEndDate(LocalDate.of(2018, 4, 3))
                .assignmentDateTime(LocalDateTime.of(LocalDate.of(1985, 4, 3), LocalTime.of(11, 0)))
                .assignmentEndDateTime(LocalDateTime.of(LocalDate.of(2018, 4, 3), LocalTime.of(11, 0)))
                .assignmentReason("ADM")
                .build());
    }

    @Test
    public void findBedAssignmentHistory_forDatePeriod() {
        final var cellHistory =
            repository.findByDateTimeRange(
                LocalDateTime.of(2040, 10, 16, 0, 0, 0),
                LocalDateTime.of(2042, 10, 10, 0, 0, 0)
            );

        assertThat(cellHistory).containsExactlyInAnyOrder(
            BedAssignmentHistory.builder()
                .livingUnitId(-16L)
                .assignmentDate(LocalDate.of(2040, 10, 17))
                .assignmentDateTime(LocalDateTime.of(LocalDate.of(2040, 10, 17), LocalTime.of(11, 0)))
                .assignmentEndDate(LocalDate.of(2041, 1, 1))
                .assignmentEndDateTime(LocalDateTime.of(LocalDate.of(2041, 1, 1), LocalTime.of(11, 0)))
                .assignmentReason("ADM")
                .build(),
            BedAssignmentHistory.builder()
                .livingUnitId(-17L)
                .assignmentDate(LocalDate.of(2040, 4, 3))
                .assignmentDateTime(LocalDateTime.of(LocalDate.of(2040, 4, 3), LocalTime.of(11, 0)))
                .assignmentReason("ADM")
                .build(),
            BedAssignmentHistory.builder()
                .livingUnitId(-18L)
                .assignmentDate(LocalDate.of(2040, 4, 3))
                .assignmentDateTime(LocalDateTime.of(LocalDate.of(2040, 4, 3), LocalTime.of(11, 0)))
                .assignmentEndDate(LocalDate.of(2041, 4, 3))
                .assignmentEndDateTime(LocalDateTime.of(LocalDate.of(2041, 4, 3), LocalTime.of(11, 0)))
                .assignmentReason("ADM")
                .build(),
            BedAssignmentHistory.builder()
                .livingUnitId(-16L)
                .assignmentDate(LocalDate.of(2020, 4, 3))
                .assignmentDateTime(LocalDateTime.of(LocalDate.of(2020, 4, 3), LocalTime.of(11, 0)))
                .assignmentReason("ADM")
                .build(),
            BedAssignmentHistory.builder()
                .livingUnitId(-15L)
                .assignmentDate(LocalDate.of(2020, 4, 3))
                .assignmentDateTime(LocalDateTime.of(LocalDate.of(2020, 4, 3), LocalTime.of(11, 0)))
                .assignmentReason("ADM")
                .build(),
            BedAssignmentHistory.builder()
                .livingUnitId(4005L)
                .assignmentDate(LocalDate.of(2019, 10, 17))
                .assignmentDateTime(LocalDateTime.of(LocalDate.of(2019, 10, 17), LocalTime.of(11, 0)))
                .assignmentReason("ADM")
                .build()
        );
    }

    @Test
    public void findBedAssignmentHistory_checksTime() {
        final var cellHistory =
            repository.findByLivingUnitIdAndDateTimeRange(
                -16,
                LocalDateTime.of(2020, 1, 1, 12, 0, 0),
                LocalDateTime.of(2020, 10, 10, 12, 12, 12)
            );

        assertThat(cellHistory).containsExactlyInAnyOrder(
            BedAssignmentHistory.builder()
                .livingUnitId(-16L)
                .assignmentDate(LocalDate.of(2020, 4, 3))
                .assignmentDateTime(LocalDateTime.of(LocalDate.of(2020, 4, 3), LocalTime.of(11, 0)))
                .assignmentReason("ADM")
                .build());
    }

    private void createBedAssignmentHistories(Long bookingId, Integer numberRecords) {
        IntStream.rangeClosed(1, numberRecords).forEach(seq -> {
            final var bookingAndSequence = new BedAssignmentHistory.BedAssignmentHistoryPK(bookingId, seq);
            final var bedAssignmentHistory =
                BedAssignmentHistory.builder()
                    .bedAssignmentHistoryPK(bookingAndSequence)
                    .livingUnitId(2L)
                    .assignmentDate(LocalDate.now())
                    .assignmentDateTime(LocalDateTime.now())
                    .build();
            repository.save(bedAssignmentHistory);
        });
    }
}
