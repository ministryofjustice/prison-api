package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderPendingDeletion;

import java.time.LocalDate;

@Repository
public interface OffenderPendingDeletionRepository extends CrudRepository<OffenderPendingDeletion, String> {

    /**
     * The following query finds offenders that satisfy the following requirements.
     *
     * The offender's deletion due date (latest sentence expiry date plus seven years)
     * is within the requested time window AND:
     *
     *   * Inactive, Out
     *   * Not a lifer or IPP
     *   * Never unlawfully at large
     *   * No health problems
     *   * Not deceased in custody
     *   * Only has one booking across all aliases
     *   * No incidents linking offender to another offender
     *   * No XTACT alert
     */
    @Query(value =
                    "WITH SINGLE_BOOKINGS(OFFENDER_ID_DISPLAY, OFFENDER_BOOK_ID) AS (" +
                    "    SELECT DISTINCT O1.OFFENDER_ID_DISPLAY, OB.OFFENDER_BOOK_ID FROM OFFENDERS O1" +
                    "    INNER JOIN OFFENDER_BOOKINGS OB" +
                    "    ON O1.OFFENDER_ID = OB.OFFENDER_ID" +
                    "    INNER JOIN OFFENDER_SENT_CALCULATIONS OSC" +
                    "    ON OSC.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID" +
                    "    WHERE (SELECT COUNT(*) FROM OFFENDER_BOOKINGS OB WHERE OB.OFFENDER_ID = O1.OFFENDER_ID) = 1" +
                    "    AND NOT EXISTS (" +
                    "        SELECT 1 FROM OFFENDERS O2 WHERE O1.OFFENDER_ID_DISPLAY = O2.OFFENDER_ID_DISPLAY" +
                    "                                   AND O2.OFFENDER_ID != O1.OFFENDER_ID" +
                    "                                   AND (SELECT COUNT(*) FROM OFFENDER_BOOKINGS OB2 WHERE OB2.OFFENDER_ID = O2.OFFENDER_ID) > 0" +
                    "    )" +
                    "    AND OB.IN_OUT_STATUS = 'OUT'" +
                    "    AND OB.ACTIVE_FLAG = 'N'" +
                    "    AND OSC.OFFENDER_SENT_CALCULATION_ID = (SELECT MAX(OSC2.OFFENDER_SENT_CALCULATION_ID) FROM OFFENDER_SENT_CALCULATIONS OSC2 WHERE OSC2.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID)" +
                    "    AND COALESCE(OSC.SED_OVERRIDED_DATE, OSC.SED_CALCULATED_DATE) IS NOT NULL" +
                    "    AND COALESCE(OSC.SED_OVERRIDED_DATE, OSC.SED_CALCULATED_DATE) >= add_months(:fromDate, -84)" +
                    "    AND COALESCE(OSC.SED_OVERRIDED_DATE, OSC.SED_CALCULATED_DATE) < add_months(:toDate, -84)" +
                    ") " +
                    "SELECT SB.OFFENDER_ID_DISPLAY FROM SINGLE_BOOKINGS SB " +
                    "WHERE NOT EXISTS (" +
                    "    SELECT 1 FROM OFFENDER_SENTENCE_TERMS OST" +
                    "    WHERE OST.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    "    AND (OST.LIFE_SENTENCE_FLAG = 'Y' OR OST.YEARS = 99)" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM OFFENDER_EXTERNAL_MOVEMENTS OEM" +
                    "    WHERE OEM.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    "    AND (" +
                    "        OEM.MOVEMENT_REASON_CODE = 'DEC'" +
                    "        OR OEM.MOVEMENT_REASON_CODE IN (SELECT MOVEMENT_REASON_CODE FROM MOVEMENT_REASONS WHERE ESC_RECAP_FLAG = 'Y')" +
                    "    )" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM AGENCY_INCIDENT_PARTIES AIP1, AGENCY_INCIDENT_PARTIES AIP2" +
                    "    WHERE AIP1.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    "    AND AIP1.AGENCY_INCIDENT_ID = AIP2.AGENCY_INCIDENT_ID" +
                    "    AND AIP2.OFFENDER_BOOK_ID != SB.OFFENDER_BOOK_ID" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM INCIDENT_CASE_PARTIES ICP1, INCIDENT_CASE_PARTIES ICP2" +
                    "    WHERE ICP1.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    "    AND ICP1.INCIDENT_CASE_ID = ICP2.INCIDENT_CASE_ID" +
                    "    AND ICP2.OFFENDER_BOOK_ID != SB.OFFENDER_BOOK_ID" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM OFFENDER_HEALTH_PROBLEMS OHP" +
                    "    WHERE OHP.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM OFFENDER_ALERTS OA" +
                    "    WHERE OA.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    "    AND OA.ALERT_CODE = 'XTACT'" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM IWP_DOCUMENTS ID" +
                    "    WHERE ID.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM OFFENDER_NON_ASSOCIATIONS ONA" +
                    "    WHERE ONA.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    "    OR ONA.NS_OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    ") " +
                    "ORDER BY SB.OFFENDER_ID_DISPLAY ASC",
            countQuery =
                    "WITH SINGLE_BOOKINGS(OFFENDER_ID_DISPLAY, OFFENDER_BOOK_ID) AS (" +
                    "    SELECT DISTINCT O1.OFFENDER_ID_DISPLAY, OB.OFFENDER_BOOK_ID FROM OFFENDERS O1" +
                    "    INNER JOIN OFFENDER_BOOKINGS OB" +
                    "    ON O1.OFFENDER_ID = OB.OFFENDER_ID" +
                    "    INNER JOIN OFFENDER_SENT_CALCULATIONS OSC" +
                    "    ON OSC.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID" +
                    "    WHERE (SELECT COUNT(*) FROM OFFENDER_BOOKINGS OB WHERE OB.OFFENDER_ID = O1.OFFENDER_ID) = 1" +
                    "    AND NOT EXISTS (" +
                    "        SELECT 1 FROM OFFENDERS O2 WHERE O1.OFFENDER_ID_DISPLAY = O2.OFFENDER_ID_DISPLAY" +
                    "                                   AND O2.OFFENDER_ID != O1.OFFENDER_ID" +
                    "                                   AND (SELECT COUNT(*) FROM OFFENDER_BOOKINGS OB2 WHERE OB2.OFFENDER_ID = O2.OFFENDER_ID) > 0" +
                    "    )" +
                    "    AND OB.IN_OUT_STATUS = 'OUT'" +
                    "    AND OB.ACTIVE_FLAG = 'N'" +
                    "    AND OSC.OFFENDER_SENT_CALCULATION_ID = (SELECT MAX(OSC2.OFFENDER_SENT_CALCULATION_ID) FROM OFFENDER_SENT_CALCULATIONS OSC2 WHERE OSC2.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID)" +
                    "    AND COALESCE(OSC.SED_OVERRIDED_DATE, OSC.SED_CALCULATED_DATE) IS NOT NULL" +
                    "    AND COALESCE(OSC.SED_OVERRIDED_DATE, OSC.SED_CALCULATED_DATE) >= add_months(:fromDate, -84)" +
                    "    AND COALESCE(OSC.SED_OVERRIDED_DATE, OSC.SED_CALCULATED_DATE) < add_months(:toDate, -84)" +
                    ") " +
                    "SELECT count(*) FROM SINGLE_BOOKINGS SB " +
                    "WHERE NOT EXISTS (" +
                    "    SELECT 1 FROM OFFENDER_SENTENCE_TERMS OST" +
                    "    WHERE OST.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    "    AND (OST.LIFE_SENTENCE_FLAG = 'Y' OR OST.YEARS = 99)" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM OFFENDER_EXTERNAL_MOVEMENTS OEM" +
                    "    WHERE OEM.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    "    AND (" +
                    "        OEM.MOVEMENT_REASON_CODE = 'DEC'" +
                    "        OR OEM.MOVEMENT_REASON_CODE IN (SELECT MOVEMENT_REASON_CODE FROM MOVEMENT_REASONS WHERE ESC_RECAP_FLAG = 'Y')" +
                    "    )" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM AGENCY_INCIDENT_PARTIES AIP1, AGENCY_INCIDENT_PARTIES AIP2" +
                    "    WHERE AIP1.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    "    AND AIP1.AGENCY_INCIDENT_ID = AIP2.AGENCY_INCIDENT_ID" +
                    "    AND AIP2.OFFENDER_BOOK_ID != SB.OFFENDER_BOOK_ID" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM INCIDENT_CASE_PARTIES ICP1, INCIDENT_CASE_PARTIES ICP2" +
                    "    WHERE ICP1.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    "    AND ICP1.INCIDENT_CASE_ID = ICP2.INCIDENT_CASE_ID" +
                    "    AND ICP2.OFFENDER_BOOK_ID != SB.OFFENDER_BOOK_ID" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM OFFENDER_HEALTH_PROBLEMS OHP" +
                    "    WHERE OHP.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM OFFENDER_ALERTS OA" +
                    "    WHERE OA.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    "    AND OA.ALERT_CODE = 'XTACT'" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM IWP_DOCUMENTS ID" +
                    "    WHERE ID.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    ") " +
                    "AND NOT EXISTS (" +
                    "    SELECT 1 FROM OFFENDER_NON_ASSOCIATIONS ONA" +
                    "    WHERE ONA.OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    "    OR ONA.NS_OFFENDER_BOOK_ID = SB.OFFENDER_BOOK_ID" +
                    ")",
                    nativeQuery = true)
    Page<OffenderPendingDeletion> getOffendersDueForDeletionBetween(@Param("fromDate") final LocalDate fromDate,
                                                                    @Param("toDate") final LocalDate toDate,
                                                                    final Pageable pageable);
}
