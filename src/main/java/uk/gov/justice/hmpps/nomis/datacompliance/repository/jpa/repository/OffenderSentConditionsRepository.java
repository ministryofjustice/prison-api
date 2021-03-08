package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderSentConditions;

import java.util.List;
import java.util.Set;

@Repository
public interface OffenderSentConditionsRepository extends CrudRepository<OffenderSentConditions, Long> {

    @Query(value =
        "SELECT OFFENDER_SENT_CONDITION_ID, OFFENDER_BOOK_ID, NO_WORK_WITH_UNDER_AGE, GROOMING_FLAG " +
            "FROM OFFENDER_SENT_CONDITIONS osc " +
            "WHERE osc.OFFENDER_BOOK_ID IN (:bookIds) " +
            "AND (osc.NO_WORK_WITH_UNDER_AGE = 'Y' OR osc.GROOMING_FLAG = 'Y')", nativeQuery = true)
    List<OffenderSentConditions> findChildRelatedConditionsByBookings(Set<Long> bookIds);
}
