package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Subselect;
import org.springframework.data.annotation.Immutable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Subselect("""
    SELECT O.OFFENDER_ID_DISPLAY                      AS OFFENDER_NO,
           OPP.OFFENDER_BOOK_ID                       AS BOOKING_ID,
           O.FIRST_NAME,
           O.LAST_NAME,
           OCA.EVENT_ID,
           OCA.EVENT_OUTCOME,
           OCA.PERFORMANCE_CODE                       AS PERFORMANCE,
           OCA.COMMENT_TEXT                           AS OUTCOME_COMMENT,
           OCA.PAY_FLAG                               AS PAID,
           AIL.DESCRIPTION                            AS CELL_LOCATION,
           CA.COURSE_ACTIVITY_TYPE                    AS EVENT,
           RD2.DESCRIPTION                            AS EVENT_DESCRIPTION,
           CS.START_TIME,
           CS.END_TIME,
           CA.DESCRIPTION                             AS DESCRIPTION,
           CA.INTERNAL_LOCATION_ID                    AS LOCATION_ID,
           COALESCE(AIL2.USER_DESC, AIL2.DESCRIPTION) AS EVENT_LOCATION,
           OPP.SUSPENDED_FLAG                         AS SUSPENDED
      FROM OFFENDER_PROGRAM_PROFILES OPP
           INNER JOIN OFFENDER_BOOKINGS OB           ON OB.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID
                                                        AND OB.ACTIVE_FLAG = 'Y'
                                                        AND OB.BOOKING_SEQ = 1
           INNER JOIN OFFENDERS O                    ON OB.OFFENDER_ID = O.OFFENDER_ID
           INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL  ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
           INNER JOIN COURSE_ACTIVITIES CA           ON CA.CRS_ACTY_ID = OPP.CRS_ACTY_ID
           INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL2 ON CA.INTERNAL_LOCATION_ID = AIL2.INTERNAL_LOCATION_ID
           INNER JOIN COURSE_SCHEDULES CS            ON CA.CRS_ACTY_ID = CS.CRS_ACTY_ID
                                                        AND CS.SCHEDULE_DATE >= TRUNC(OPP.OFFENDER_START_DATE)
                                                        AND TRUNC(CS.SCHEDULE_DATE) <= COALESCE(OPP.OFFENDER_END_DATE, CA.SCHEDULE_END_DATE, CS.SCHEDULE_DATE)
           LEFT JOIN REFERENCE_CODES RD2             ON RD2.CODE = CA.COURSE_ACTIVITY_TYPE
                                                        AND RD2.DOMAIN = 'INT_SCH_RSN'
           LEFT JOIN OFFENDER_COURSE_ATTENDANCES OCA ON OCA.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID
                                                        AND TRUNC(OCA.EVENT_DATE) = TRUNC(CS.SCHEDULE_DATE)
                                                        AND OCA.CRS_SCH_ID = CS.CRS_SCH_ID
     WHERE CA.COURSE_ACTIVITY_TYPE IS NOT NULL
       AND CS.CATCH_UP_CRS_SCH_ID IS NULL""")
@Immutable
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduledActivity {
    @Id
    private Long eventId;
    private Long bookingId;
    private String offenderNo;
    private String firstName;
    private String lastName;
    private String eventOutcome;
    private String performance;
    private String outcomeComment;
    private String paid;
    private String cellLocation;
    private String event;
    private String eventDescription;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private Long locationId;
    private String eventLocation;
    private String suspended;
}
