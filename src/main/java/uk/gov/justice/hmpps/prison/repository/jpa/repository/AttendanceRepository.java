package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Attendance;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends CrudRepository<Attendance, Long> {
    @Query(
        value = """
            SELECT ATT
            FROM Attendance ATT
            WHERE ATT.offenderBookingId IN (:offenderBookingIds) 
               AND ATT.eventDate >= :earliestActivityDate
               AND ATT.eventDate <= :latestActivityDate 
        """
    )
    List<Attendance> findByBookingIdsAndEventDate(List<Long> offenderBookingIds, LocalDate earliestActivityDate, LocalDate latestActivityDate);
}
