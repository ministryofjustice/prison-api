package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEducation;

public interface OffenderEducationRepository extends PagingAndSortingRepository<OffenderEducation, OffenderEducation.PK> {

    @Query(
        value = """
            SELECT OE
            FROM OffenderBooking B
                     INNER JOIN OffenderEducation OE ON OE.id.bookingId = B.bookingId
            WHERE B.offender.nomsId = :nomsId
            ORDER BY OE.startDate
                    """
    )
    Page<OffenderEducation> findAllByNomisId(@Param("nomsId") String nomsId, Pageable pageable);


    @Query(
        value = """
            SELECT OE
            FROM OffenderBooking B
                     INNER JOIN OffenderEducation OE ON OE.id.bookingId = B.bookingId
            WHERE B.offender.nomsId IN :nomsIds
            ORDER BY OE.startDate
        """
    )
    List<OffenderEducation> findAllByNomisIdIn(@Param("nomsIds") Set<String> nomsIds);
}
