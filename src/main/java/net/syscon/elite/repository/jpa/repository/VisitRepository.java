package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.Visit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitRepository extends PagingAndSortingRepository<Visit, String> {
    @Query(value =
            "SELECT * FROM" +
                    "(SELECT VISIT.OFFENDER_VISIT_ID VISIT_ID," +
                    "VISITOR.OUTCOME_REASON_CODE CANCELLATION_REASON," +
                    "RC5.DESCRIPTION CANCEL_REASON_DESCRIPTION," +
                    "VISITOR.EVENT_STATUS," +
                    "RC2.DESCRIPTION EVENT_STATUS_DESCRIPTION," +
                    "NVL(VISITOR.EVENT_OUTCOME,'ATT') EVENT_OUTCOME," +
                    "RC4.DESCRIPTION EVENT_OUTCOME_DESCRIPTION," +
                    "VISIT.START_TIME," +
                    "VISIT.END_TIME," +
                    "COALESCE(AIL.USER_DESC, AIL.DESCRIPTION, AGY.DESCRIPTION) LOCATION," +
                    "VISIT.VISIT_TYPE," +
                    "RC3.DESCRIPTION VISIT_TYPE_DESCRIPTION," +
                    "P.FIRST_NAME || ' ' || P.LAST_NAME LEAD_VISITOR," +
                    "OCP.RELATIONSHIP_TYPE RELATIONSHIP," +
                    "RC1.DESCRIPTION RELATIONSHIP_DESCRIPTION " +
                    "FROM OFFENDER_VISITS VISIT " +
                    "INNER JOIN OFFENDER_BOOKINGS BOOKING ON BOOKING.OFFENDER_BOOK_ID = VISIT.OFFENDER_BOOK_ID AND BOOKING.ACTIVE_FLAG = 'Y' " +
                    "LEFT JOIN OFFENDER_VISIT_VISITORS VISITOR ON VISIT.OFFENDER_VISIT_ID = VISITOR.OFFENDER_VISIT_ID AND VISITOR.GROUP_LEADER_FLAG = 'Y' AND VISITOR.PERSON_ID IS NOT NULL " +
                    "LEFT JOIN OFFENDER_CONTACT_PERSONS OCP ON VISITOR.PERSON_ID = OCP.PERSON_ID AND VISIT.OFFENDER_BOOK_ID = OCP.OFFENDER_BOOK_ID " +
                    "LEFT JOIN REFERENCE_CODES RC1 ON RC1.DOMAIN = 'RELATIONSHIP' AND RC1.CODE = OCP.RELATIONSHIP_TYPE " +
                    "LEFT JOIN REFERENCE_CODES RC2 ON RC2.DOMAIN = 'EVENT_STS' AND RC2.CODE = VISITOR.EVENT_STATUS " +
                    "LEFT JOIN REFERENCE_CODES RC3 ON RC3.DOMAIN = 'VISIT_TYPE' AND RC3.CODE = VISIT.VISIT_TYPE " +
                    "LEFT JOIN REFERENCE_CODES RC4 ON RC4.DOMAIN = 'OUTCOMES' AND RC4.CODE = NVL(VISITOR.EVENT_OUTCOME,'ATT') " +
                    "LEFT JOIN REFERENCE_CODES RC5 ON RC5.DOMAIN = 'MOVE_CANC_RS' AND RC5.CODE = VISITOR.OUTCOME_REASON_CODE " +
                    "LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON VISIT.VISIT_INTERNAL_LOCATION_ID = AIL.INTERNAL_LOCATION_ID " +
                    "LEFT JOIN AGENCY_LOCATIONS AGY ON VISIT.AGY_LOC_ID = AGY.AGY_LOC_ID " +
                    "LEFT JOIN PERSONS P ON P.PERSON_ID = VISITOR.PERSON_ID " +
                    "WHERE VISIT.OFFENDER_BOOK_ID = :bookingId " +
                    "ORDER BY VISIT.START_TIME DESC, VISIT.OFFENDER_VISIT_ID DESC)",
            nativeQuery = true)
    List<Visit> getVisits(@Param("bookingId") final Long bookingId);;
}
