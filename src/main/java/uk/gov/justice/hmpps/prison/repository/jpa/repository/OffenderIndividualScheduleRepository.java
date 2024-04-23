package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule;

import java.util.List;
import java.util.Optional;

public interface OffenderIndividualScheduleRepository extends JpaRepository<OffenderIndividualSchedule, Long> {

    Optional<OffenderIndividualSchedule> findOneByParentEventId(Long parentEventId);

    List<OffenderIndividualSchedule> findByOffenderBooking_BookingIdOrderByIdAsc(final Long bookingId);

    Optional<OffenderIndividualSchedule> findOneByOffenderBookingBookingIdAndParentEventId(Long bookingId, Long parentEventId);

}
