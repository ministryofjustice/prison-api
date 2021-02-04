package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderRestrictions;

import java.util.List;
import java.util.Set;

@Repository
public interface OffenderRestrictionsRepository extends CrudRepository<OffenderRestrictions, String> {

    @Query(value =
        "SELECT * " +
            "FROM offender_restrictions " +
            "WHERE offender_book_id IN (:bookIds) " +
            "AND (RESTRICTION_TYPE IN (:offenderRestrictionCodes)" +
            "OR REGEXP_LIKE(comment_text, :regex, 'i'))", nativeQuery = true)
    List<OffenderRestrictions> findOffenderRestrictions(Set<Long> bookIds, Set<String> offenderRestrictionCodes,  String regex);
}
