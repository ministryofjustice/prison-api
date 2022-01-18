package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Subselect;
import org.springframework.data.annotation.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Subselect("""
    SELECT * FROM
        (SELECT VISIT.OFFENDER_VISIT_ID VISIT_ID,
                VISIT.VISIT_STATUS VISIT_STATUS,
                RC1.DESCRIPTION VISIT_STATUS_DESCRIPTION,
                VISIT.OFFENDER_BOOK_ID BOOKING_ID,
                VISIT_EXTRA.OUTCOME_REASON_CODE CANCELLATION_REASON,
                RC5.DESCRIPTION CANCEL_REASON_DESCRIPTION,
                VISIT_EXTRA.EVENT_STATUS,
                RC2.DESCRIPTION EVENT_STATUS_DESCRIPTION,
                LEAD_VISITOR.PERSON_ID VISITOR_PERSON_ID,
                NVL(VISIT_EXTRA.EVENT_OUTCOME,'ATT') EVENT_OUTCOME,
                RC4.DESCRIPTION EVENT_OUTCOME_DESCRIPTION,
                VISIT.START_TIME,
                VISIT.END_TIME,
                COALESCE(AIL.USER_DESC, AIL.DESCRIPTION, AGY.DESCRIPTION) LOCATION,
                VISIT.VISIT_TYPE,
                RC3.DESCRIPTION VISIT_TYPE_DESCRIPTION,
                P.FIRST_NAME || ' ' || P.LAST_NAME LEAD_VISITOR,
                VISIT.AGY_LOC_ID PRISON_ID,
                AGY.DESCRIPTION PRISON_DESCRIPTION
           FROM OFFENDER_VISITS VISIT
          INNER JOIN OFFENDER_BOOKINGS BOOKING ON BOOKING.OFFENDER_BOOK_ID = VISIT.OFFENDER_BOOK_ID AND BOOKING.ACTIVE_FLAG = 'Y'
          INNER JOIN OFFENDER_VISIT_VISITORS VISIT_EXTRA ON VISIT.OFFENDER_VISIT_ID = VISIT_EXTRA.OFFENDER_VISIT_ID AND VISIT_EXTRA.OFFENDER_BOOK_ID IS NOT NULL
           LEFT JOIN OFFENDER_VISIT_VISITORS LEAD_VISITOR ON VISIT.OFFENDER_VISIT_ID = LEAD_VISITOR.OFFENDER_VISIT_ID AND LEAD_VISITOR.GROUP_LEADER_FLAG = 'Y' AND LEAD_VISITOR.PERSON_ID IS NOT NULL
           LEFT JOIN REFERENCE_CODES RC1 ON RC1.DOMAIN = 'VIS_COMPLETE' AND RC1.CODE = VISIT.VISIT_STATUS
           LEFT JOIN REFERENCE_CODES RC2 ON RC2.DOMAIN = 'EVENT_STS' AND RC2.CODE = VISIT_EXTRA.EVENT_STATUS
           LEFT JOIN REFERENCE_CODES RC3 ON RC3.DOMAIN = 'VISIT_TYPE' AND RC3.CODE = VISIT.VISIT_TYPE
           LEFT JOIN REFERENCE_CODES RC4 ON RC4.DOMAIN = 'OUTCOMES' AND RC4.CODE = NVL(VISIT_EXTRA.EVENT_OUTCOME,'ATT')
           LEFT JOIN REFERENCE_CODES RC5 ON RC5.DOMAIN = 'MOVE_CANC_RS' AND RC5.CODE = VISIT_EXTRA.OUTCOME_REASON_CODE
           LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON VISIT.VISIT_INTERNAL_LOCATION_ID = AIL.INTERNAL_LOCATION_ID
           LEFT JOIN AGENCY_LOCATIONS AGY ON VISIT.AGY_LOC_ID = AGY.AGY_LOC_ID
           LEFT JOIN PERSONS P ON P.PERSON_ID = LEAD_VISITOR.PERSON_ID
          ORDER BY VISIT.START_TIME DESC
        )
    """)
@Immutable
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class VisitInformation {
    @Id
    private Long visitId;
    private String visitStatus;
    private String visitStatusDescription;
    private Long bookingId;
    private Long visitorPersonId;
    private String cancellationReason;
    private String cancelReasonDescription;
    private String eventStatus;
    private String eventStatusDescription;
    private String eventOutcome;
    private String eventOutcomeDescription;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String visitType;
    private String visitTypeDescription;
    private String leadVisitor;
    private String prisonId;
    private String prisonDescription;
}
