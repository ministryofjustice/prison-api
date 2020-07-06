package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.DuplicateOffender;

import java.util.List;
import java.util.Set;

@Repository
public interface DuplicateOffenderRepository extends org.springframework.data.repository.Repository<DuplicateOffender, String> {

    /**
     * The following query finds offenders that share the same
     * PNC number as the reference offender. This check takes
     * into account that either 2 or 4 digits may be used in
     * the first part (year) of the number.
     */
    @Query(value =
                    "SELECT DISTINCT(O1.OFFENDER_ID_DISPLAY) FROM OFFENDERS O1 " +
                    "INNER JOIN OFFENDER_IDENTIFIERS OI1 " +
                    "ON O1.OFFENDER_ID = OI1.OFFENDER_ID " +
                    "WHERE OI1.IDENTIFIER_TYPE = 'PNC' " +
                    "AND O1.OFFENDER_ID_DISPLAY != :offenderNo " +
                    "AND REGEXP_SUBSTR(TRIM(UPPER(OI1.IDENTIFIER)), '[0-9]{2}/') " +
                    "|| REGEXP_SUBSTR(TRIM(UPPER(OI1.IDENTIFIER)), '[1-9][0-9]*[A-Z]$') " +
                    "IN (:formattedIds)",
            nativeQuery = true)
    List<DuplicateOffender> getOffendersWithMatchingPncNumbers(String offenderNo, Set<String> formattedIds);

    /**
     * The following query finds offenders that share the same
     * CRO number as the reference offender.
     */
    @Query(value =
            "SELECT DISTINCT(O1.OFFENDER_ID_DISPLAY) FROM OFFENDERS O1 " +
                    "INNER JOIN OFFENDER_IDENTIFIERS OI1 " +
                    "ON O1.OFFENDER_ID = OI1.OFFENDER_ID " +
                    "WHERE OI1.IDENTIFIER_TYPE = 'CRO' " +
                    "AND O1.OFFENDER_ID_DISPLAY != :offenderNo " +
                    "AND (" +
                    "    REGEXP_REPLACE(REGEXP_SUBSTR(TRIM(UPPER(OI1.IDENTIFIER)), '/[0-9]{2}'), '/', '')" +
                    "    || '/'" +
                    "    || REGEXP_REPLACE(REGEXP_SUBSTR(TRIM(UPPER(OI1.IDENTIFIER)), '[1-9][0-9]*/'), '/', '')" +
                    "    || REGEXP_SUBSTR(TRIM(UPPER(OI1.IDENTIFIER)), '[A-Z]$') " +
                    "    IN (:formattedIds)" +
                    "    OR REGEXP_SUBSTR(TRIM(UPPER(OI1.IDENTIFIER)), '[0-9]{2}/') " +
                    "    || REGEXP_SUBSTR(TRIM(UPPER(OI1.IDENTIFIER)), '[1-9][0-9]*[A-Z]$') " +
                    "    IN (:formattedIds)" +
                    ")",
            nativeQuery = true)
    List<DuplicateOffender> getOffendersWithMatchingCroNumbers(String offenderNo, Set<String> formattedIds);

    /**
     * The following query finds offenders that share the same
     * LIDS booking number.
     */
    @Query(value =
            "SELECT DISTINCT(O1.OFFENDER_ID_DISPLAY) FROM OFFENDERS O1 " +
            "INNER JOIN OFFENDER_BOOKINGS OB1 " +
            "ON OB1.OFFENDER_ID = O1.OFFENDER_ID " +
            "WHERE O1.OFFENDER_ID_DISPLAY != :offenderNo " +
            "AND OB1.BOOKING_NO IN ( " +
            "    SELECT DISTINCT(OB2.BOOKING_NO) FROM OFFENDERS O2" +
            "    INNER JOIN OFFENDER_BOOKINGS OB2" +
            "    ON OB2.OFFENDER_ID = O2.OFFENDER_ID" +
            "    WHERE O2.OFFENDER_ID_DISPLAY = :offenderNo" +
            ")",
            nativeQuery = true)
    List<DuplicateOffender> getOffendersWithMatchingLidsNumbers(String offenderNo);

    /**
     * The following query identifies potential duplicate offender records
     * based on matching personal details. Key components of this query are
     * as follows:
     *
     * - The Jaro Winkler algorithm is used to fuzzy match details to avoid
     *   misspellings or typos affecting the result
     *
     * - The match is points based, with certain fields having a higher
     *   weighting
     *
     * - All offender aliases are taken into account and for each field the
     *   values with maximum similarity are used towards the points calculation
     *
     * - The weightings have been chosen based on empirical evidence
     */
    @Query(value =
            "WITH OFFENDER_MATCH_DETAILS AS (" +
            "    SELECT *" +
            "    FROM MV_OFFENDER_MATCH_DETAILS" +
            "    WHERE OFFENDER_ID_DISPLAY = :offenderNo" +
            "), " +
            "SIMILARITY AS (" +
            "    SELECT" +
            "        O.OFFENDER_ID_DISPLAY AS OFFENDER_ID_DISPLAY," +
            "        (CASE WHEN OMD.AVG_HEIGHT = 0 THEN 0 ELSE ((OMD.AVG_HEIGHT - ABS(O.AVG_HEIGHT - OMD.AVG_HEIGHT)) * 100 / OMD.AVG_HEIGHT) END) AS HEIGHT_SIMILARITY," +
            "        UTL_MATCH.JARO_WINKLER_SIMILARITY (O.LAST_NAME_KEY, OMD.LAST_NAME_KEY) AS LAST_NAME_SIMILARITY," +
            "        UTL_MATCH.JARO_WINKLER_SIMILARITY (O.FIRST_NAME_KEY, OMD.FIRST_NAME_KEY) AS FIRST_NAME_SIMILARITY," +
            "        (CASE WHEN ABS(MONTHS_BETWEEN(O.BIRTH_DATE, OMD.BIRTH_DATE)) >= 120 THEN 0" +
            "         ELSE UTL_MATCH.JARO_WINKLER_SIMILARITY (TO_CHAR(O.BIRTH_DATE, 'YYYY-MM-DD'), TO_CHAR(OMD.BIRTH_DATE, 'YYYY-MM-DD')) END" +
            "        ) AS BIRTH_DATE_SIMILARITY," +
            "        UTL_MATCH.JARO_WINKLER_SIMILARITY (O.BIRTH_PLACE, OMD.BIRTH_PLACE) AS BIRTH_PLACE_SIMILARITY," +
            "        UTL_MATCH.JARO_WINKLER_SIMILARITY (O.STREET, OMD.STREET) AS STREET_SIMILARITY," +
            "        UTL_MATCH.JARO_WINKLER_SIMILARITY (O.LOCALITY, OMD.LOCALITY) AS LOCALITY_SIMILARITY," +
            "        (CASE WHEN O.CITY_CODE = OMD.CITY_CODE THEN 100 ELSE 0 END) AS CITY_CODE_SIMILARITY," +
            "        (CASE WHEN O.COUNTY_CODE = OMD.COUNTY_CODE THEN 100 ELSE 0 END) AS COUNTY_CODE_SIMILARITY," +
            "        (CASE WHEN O.POSTAL_CODE = OMD.POSTAL_CODE THEN 100 ELSE 0 END) AS POSTAL_CODE_SIMILARITY" +
            "    FROM" +
            "        MV_OFFENDER_MATCH_DETAILS O" +
            "        CROSS JOIN OFFENDER_MATCH_DETAILS OMD" +
            "    WHERE" +
            "        O.OFFENDER_ID_DISPLAY != OMD.OFFENDER_ID_DISPLAY" +
            "        AND (" +
            "            O.BIRTH_DATE = OMD.BIRTH_DATE" +
            "            OR O.BIRTH_PLACE = OMD.BIRTH_PLACE" +
            "            OR O.LAST_NAME_KEY = OMD.LAST_NAME_KEY" +
            "            OR O.FIRST_NAME_KEY = OMD.FIRST_NAME_KEY" +
            "            OR O.STREET = OMD.STREET" +
            "            OR O.LOCALITY = OMD.LOCALITY" +
            "            OR O.POSTAL_CODE = OMD.POSTAL_CODE" +
            "            )" +
            "), " +
            "MAX_LAST_NAME_SIMILARITY AS (" +
            "    SELECT DISTINCT S1.OFFENDER_ID_DISPLAY, S1.LAST_NAME_SIMILARITY" +
            "    FROM SIMILARITY S1" +
            "    LEFT JOIN SIMILARITY S2" +
            "    ON S1.OFFENDER_ID_DISPLAY = S2.OFFENDER_ID_DISPLAY AND S1.LAST_NAME_SIMILARITY < S2.LAST_NAME_SIMILARITY" +
            "    WHERE S2.OFFENDER_ID_DISPLAY IS NULL" +
            "), " +
            "MAX_FIRST_NAME_SIMILARITY AS (" +
            "    SELECT DISTINCT S1.OFFENDER_ID_DISPLAY, S1.FIRST_NAME_SIMILARITY" +
            "    FROM SIMILARITY S1" +
            "    LEFT JOIN SIMILARITY S2" +
            "    ON S1.OFFENDER_ID_DISPLAY = S2.OFFENDER_ID_DISPLAY AND S1.FIRST_NAME_SIMILARITY < S2.FIRST_NAME_SIMILARITY" +
            "    WHERE S2.OFFENDER_ID_DISPLAY IS NULL" +
            "), " +
            "MAX_BIRTH_DATE_SIMILARITY AS (" +
            "    SELECT DISTINCT S1.OFFENDER_ID_DISPLAY, S1.BIRTH_DATE_SIMILARITY" +
            "    FROM SIMILARITY S1" +
            "    LEFT JOIN SIMILARITY S2" +
            "    ON S1.OFFENDER_ID_DISPLAY = S2.OFFENDER_ID_DISPLAY AND S1.BIRTH_DATE_SIMILARITY < S2.BIRTH_DATE_SIMILARITY" +
            "    WHERE S2.OFFENDER_ID_DISPLAY IS NULL" +
            "), " +
            "MAX_BIRTH_PLACE_SIMILARITY AS (" +
            "    SELECT DISTINCT S1.OFFENDER_ID_DISPLAY, S1.BIRTH_PLACE_SIMILARITY" +
            "    FROM SIMILARITY S1" +
            "    LEFT JOIN SIMILARITY S2" +
            "    ON S1.OFFENDER_ID_DISPLAY = S2.OFFENDER_ID_DISPLAY AND S1.BIRTH_PLACE_SIMILARITY < S2.BIRTH_PLACE_SIMILARITY" +
            "    WHERE S2.OFFENDER_ID_DISPLAY IS NULL" +
            "), " +
            "MAX_ADDRESS_SIMILARITY AS (" +
            "    SELECT DISTINCT" +
            "        S1.OFFENDER_ID_DISPLAY," +
            "        S1.STREET_SIMILARITY," +
            "        S1.LOCALITY_SIMILARITY," +
            "        S1.CITY_CODE_SIMILARITY," +
            "        S1.COUNTY_CODE_SIMILARITY," +
            "        S1.POSTAL_CODE_SIMILARITY" +
            "    FROM SIMILARITY S1" +
            "    LEFT JOIN SIMILARITY S2" +
            "    ON S1.OFFENDER_ID_DISPLAY = S2.OFFENDER_ID_DISPLAY" +
            "    AND (" +
            "        S1.STREET_SIMILARITY +" +
            "        S1.LOCALITY_SIMILARITY +" +
            "        S1.CITY_CODE_SIMILARITY +" +
            "        S1.COUNTY_CODE_SIMILARITY +" +
            "        S1.POSTAL_CODE_SIMILARITY" +
            "        )" +
            "        <" +
            "        (" +
            "        S2.STREET_SIMILARITY +" +
            "        S2.LOCALITY_SIMILARITY +" +
            "        S2.CITY_CODE_SIMILARITY +" +
            "        S2.COUNTY_CODE_SIMILARITY +" +
            "        S2.POSTAL_CODE_SIMILARITY" +
            "        )" +
            "    WHERE S2.OFFENDER_ID_DISPLAY IS NULL" +
            "), " +
            "SCORING_TABLE AS (" +
            "    SELECT" +
            "        S.OFFENDER_ID_DISPLAY," +
            "        (CASE WHEN (S.HEIGHT_SIMILARITY >= 97 OR S.HEIGHT_SIMILARITY IS NULL) THEN 0 ELSE (CASE WHEN S.HEIGHT_SIMILARITY >= 95 THEN -1 ELSE -3 END) END) AS HEIGHT_SCORE," +
            "        (CASE WHEN MLNS.LAST_NAME_SIMILARITY = 100 THEN 2 ELSE (CASE WHEN MLNS.LAST_NAME_SIMILARITY >= 90 THEN 1 ELSE 0 END) END) AS LAST_NAME_SCORE," +
            "        (CASE WHEN MFNS.FIRST_NAME_SIMILARITY = 100 THEN 2 ELSE (CASE WHEN MFNS.FIRST_NAME_SIMILARITY >= 90 THEN 1 ELSE 0 END) END) AS FIRST_NAME_SCORE," +
            "        (CASE WHEN MBDS.BIRTH_DATE_SIMILARITY = 100 THEN 2 ELSE (CASE WHEN MBDS.BIRTH_DATE_SIMILARITY >= 85 THEN 1 ELSE 0 END) END) AS BIRTH_DATE_SCORE," +
            "        (CASE WHEN MBPS.BIRTH_PLACE_SIMILARITY = 100 THEN 2 ELSE (CASE WHEN MBPS.BIRTH_PLACE_SIMILARITY >= 95 THEN 1 ELSE 0 END) END) AS BIRTH_PLACE_SCORE," +
            "        (CASE WHEN MAS.STREET_SIMILARITY = 100 THEN 2 ELSE (CASE WHEN MAS.STREET_SIMILARITY >= 85 THEN 1 ELSE 0 END) END) AS STREET_SCORE," +
            "        (CASE WHEN MAS.LOCALITY_SIMILARITY = 100 THEN 2 ELSE (CASE WHEN MAS.LOCALITY_SIMILARITY >= 90 THEN 1 ELSE 0 END) END) AS LOCALITY_SCORE," +
            "        (CASE WHEN MAS.CITY_CODE_SIMILARITY = 100 THEN 2 ELSE 0 END) AS CITY_CODE_SCORE," +
            "        (CASE WHEN MAS.COUNTY_CODE_SIMILARITY = 100 THEN 2 ELSE 0 END) AS COUNTY_CODE_SCORE," +
            "        (CASE WHEN MAS.POSTAL_CODE_SIMILARITY = 100 THEN 2 ELSE 0 END) AS POSTAL_CODE_SCORE" +
            "    FROM SIMILARITY S" +
            "    LEFT JOIN MAX_LAST_NAME_SIMILARITY MLNS" +
            "    ON S.OFFENDER_ID_DISPLAY = MLNS.OFFENDER_ID_DISPLAY" +
            "    LEFT JOIN MAX_FIRST_NAME_SIMILARITY MFNS" +
            "    ON S.OFFENDER_ID_DISPLAY = MFNS.OFFENDER_ID_DISPLAY" +
            "    LEFT JOIN MAX_BIRTH_DATE_SIMILARITY MBDS" +
            "    ON S.OFFENDER_ID_DISPLAY = MBDS.OFFENDER_ID_DISPLAY" +
            "    LEFT JOIN MAX_BIRTH_PLACE_SIMILARITY MBPS" +
            "    ON S.OFFENDER_ID_DISPLAY = MBPS.OFFENDER_ID_DISPLAY" +
            "    LEFT JOIN MAX_ADDRESS_SIMILARITY MAS" +
            "    ON S.OFFENDER_ID_DISPLAY = MAS.OFFENDER_ID_DISPLAY" +
            ") " +
            "SELECT DISTINCT OFFENDER_ID_DISPLAY FROM SCORING_TABLE " +
            "WHERE" +
            "    HEIGHT_SCORE +" +
            "    2 * LAST_NAME_SCORE +" +
            "    FIRST_NAME_SCORE +" +
            "    2 * BIRTH_DATE_SCORE +" +
            "    BIRTH_PLACE_SCORE +" +
            "    ((STREET_SCORE + LOCALITY_SCORE + CITY_CODE_SCORE + COUNTY_CODE_SCORE) / 2) +" +
            "    2 * POSTAL_CODE_SCORE" +
            "    > 11",
            nativeQuery = true)
    List<DuplicateOffender> getOffendersWithMatchingDetails(String offenderNo);
}
