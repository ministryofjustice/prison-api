package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import jakarta.persistence.LockModeType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule;

import java.util.List;
import java.util.Optional;

public interface OffenderIndividualScheduleRepository extends JpaRepository<OffenderIndividualSchedule, Long> {
    @NotNull
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OffenderIndividualSchedule> findWithLockById(@NotNull Long eventId);

    List<OffenderIndividualSchedule> findByOffenderBooking_BookingIdOrderByIdAsc(final Long bookingId);

    Optional<OffenderIndividualSchedule> findOneByOffenderBookingBookingIdAndParentEventId(Long bookingId, Long parentEventId);
}
