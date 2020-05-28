package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.DuplicateOffender;

import java.util.List;

@Repository
public interface DuplicateOffenderRepository extends CrudRepository<DuplicateOffender, String> {

    /**
     * The following query finds offenders that share the same
     * PNC number as the reference offender. This check takes
     * into account that either 2 or 4 digits may be used in
     * the first part (year) of the number.
     */
    @Query(value =
                    "SELECT O1.OFFENDER_ID_DISPLAY FROM OFFENDERS O1 " +
                    "INNER JOIN OFFENDER_IDENTIFIERS OI1 " +
                    "ON O1.OFFENDER_ID = OI1.OFFENDER_ID " +
                    "AND OI1.IDENTIFIER_TYPE = 'PNC'" +
                    "AND REGEXP_SUBSTR(UPPER(OI1.IDENTIFIER), '[0-9]{2,2}/[0-9]+[A-Z]+') IN (" +
                    "    SELECT REGEXP_SUBSTR(UPPER(OI2.IDENTIFIER), '[0-9]{2,2}/[0-9]+[A-Z]+') FROM OFFENDERS O2" +
                    "    INNER JOIN OFFENDER_IDENTIFIERS OI2" +
                    "    ON O2.OFFENDER_ID = OI2.OFFENDER_ID" +
                    "    AND OI2.IDENTIFIER_TYPE = 'PNC'" +
                    "    WHERE O2.OFFENDER_ID_DISPLAY = :offenderNo" +
                    ") " +
                    "WHERE O1.OFFENDER_ID_DISPLAY != :offenderNo",
            nativeQuery = true)
    List<DuplicateOffender> getOffendersWithMatchingPncNumber(String offenderNo);
}
