package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProgramProfile;

import java.time.LocalDate;
import java.util.List;

public interface OffenderProgramProfileRepository extends CrudRepository<OffenderProgramProfile, Long> {
    List<OffenderProgramProfile> findByOffenderBooking_BookingIdAndProgramStatus(Long bookingId, String programStatus);

    @Query(
        value = """
            SELECT OPP
            FROM OffenderProgramProfile OPP
            WHERE OPP.offenderBooking.offender.nomsId = :nomsId 
               AND OPP.programStatus IN :programStatuses
               AND (OPP.endDate >= :earliestEndDate OR OPP.endDate IS NULL)
        """
    )
    Page<OffenderProgramProfile> findByNomisIdAndProgramStatusAndEndDateAfter(String nomsId, List<String> programStatuses, LocalDate earliestEndDate, Pageable pageable);
}
