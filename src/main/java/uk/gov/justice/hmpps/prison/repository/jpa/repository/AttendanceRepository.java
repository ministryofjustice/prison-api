package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Attendance;

import java.time.LocalDate;

public interface AttendanceRepository extends CrudRepository<Attendance, Long> {
    @Query(
        value = """
            SELECT ATT
            FROM Attendance ATT
                INNER JOIN ATT.offenderBooking booking
                INNER JOIN booking.offender offender
            WHERE offender.nomsId = :nomsId
                AND ATT.eventDate between :earliestDate and :latestDate"""
    )
    Page<Attendance> findByEventDateBetween(final String nomsId, final LocalDate earliestDate, final LocalDate latestDate, final Pageable pageable);
}
