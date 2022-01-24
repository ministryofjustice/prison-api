package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitInformation;

import java.util.List;

@Repository
public interface VisitInformationRepository extends PagingAndSortingRepository<VisitInformation, String>, JpaSpecificationExecutor<VisitInformation> {
    @Query(value = """
        SELECT VISIT.AGY_LOC_ID prisonId,
               AGY.DESCRIPTION prisonDescription
          FROM OFFENDER_VISITS VISIT
         INNER JOIN AGENCY_LOCATIONS AGY ON VISIT.AGY_LOC_ID = AGY.AGY_LOC_ID
         WHERE VISIT.OFFENDER_BOOK_ID = :bookingId
         GROUP BY VISIT.AGY_LOC_ID, AGY.DESCRIPTION
         ORDER BY max(VISIT.START_TIME) DESC
        """, nativeQuery = true)
    List<Prison> findByBookingIdGroupByPrisonId(@Param("bookingId") final long bookingId);

    interface Prison {
        String getPrisonId();
        String getPrisonDescription();
    }
}
