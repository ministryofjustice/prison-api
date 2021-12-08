package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Attendance;

import java.time.LocalDate;

public interface AttendanceRepository extends CrudRepository<Attendance, Long> {
    @Query(value = """
        SELECT attendance
        FROM Attendance attendance
            INNER JOIN attendance.offenderBooking booking
            INNER JOIN booking.offender offender
        WHERE offender.nomsId = :nomsId
            AND attendance.eventDate between :earliestDate and :latestDate
            AND (:outcome is null OR attendance.eventOutcome = :outcome)""")
    Page<Attendance> findByEventDateBetweenAndOutcome(final String nomsId, final LocalDate earliestDate, final LocalDate latestDate, final String outcome, final Pageable pageable);
}
