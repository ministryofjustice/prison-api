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
@Subselect("SELECT O.OFFENDER_ID_DISPLAY                      AS OFFENDER_NO,\n" +
        "       OPP.OFFENDER_BOOK_ID                          AS BOOKING_ID,\n" +
        "       O.FIRST_NAME,\n" +
        "       O.LAST_NAME,\n" +
        "       OCA.EVENT_ID,\n" +
        "       OCA.EVENT_OUTCOME,\n" +
        "       OCA.PERFORMANCE_CODE                       AS PERFORMANCE,\n" +
        "       OCA.COMMENT_TEXT                           AS OUTCOME_COMMENT,\n" +
        "       OCA.PAY_FLAG                               AS PAID,\n" +
        "       AIL.DESCRIPTION                            AS CELL_LOCATION,\n" +
        "       CA.COURSE_ACTIVITY_TYPE                    AS EVENT,\n" +
        "       RD2.DESCRIPTION                            AS EVENT_DESCRIPTION,\n" +
        "       CS.START_TIME,\n" +
        "       CS.END_TIME,\n" +
        "       CA.DESCRIPTION                             AS DESCRIPTION,\n" +
        "       CA.INTERNAL_LOCATION_ID                    AS LOCATION_ID,\n" +
        "       COALESCE(AIL2.USER_DESC, AIL2.DESCRIPTION) AS EVENT_LOCATION\n" +
        "  FROM OFFENDER_PROGRAM_PROFILES OPP\n" +
        "       INNER JOIN OFFENDER_BOOKINGS OB           ON OB.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID\n" +
        "                                                    AND OB.ACTIVE_FLAG = 'Y'\n" +
        "                                                    AND OB.BOOKING_SEQ = 1\n" +
        "       INNER JOIN OFFENDERS O                    ON OB.OFFENDER_ID = O.OFFENDER_ID\n" +
        "       INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL  ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID\n" +
        "       INNER JOIN COURSE_ACTIVITIES CA           ON CA.CRS_ACTY_ID = OPP.CRS_ACTY_ID\n" +
        "       INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL2 ON CA.INTERNAL_LOCATION_ID = AIL2.INTERNAL_LOCATION_ID\n" +
        "       INNER JOIN COURSE_SCHEDULES CS            ON CA.CRS_ACTY_ID = CS.CRS_ACTY_ID\n" +
        "                                                    AND CS.SCHEDULE_DATE >= TRUNC(OPP.OFFENDER_START_DATE)\n" +
        "                                                    AND TRUNC(CS.SCHEDULE_DATE) <= COALESCE(OPP.OFFENDER_END_DATE, CA.SCHEDULE_END_DATE, CS.SCHEDULE_DATE)\n" +
        "       LEFT JOIN REFERENCE_CODES RD2             ON RD2.CODE = CA.COURSE_ACTIVITY_TYPE\n" +
        "                                                    AND RD2.DOMAIN = 'INT_SCH_RSN'\n" +
        "       LEFT JOIN OFFENDER_COURSE_ATTENDANCES OCA ON OCA.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID\n" +
        "                                                    AND TRUNC(OCA.EVENT_DATE) = TRUNC(CS.SCHEDULE_DATE)\n" +
        "                                                    AND OCA.CRS_SCH_ID = CS.CRS_SCH_ID\n" +
        " WHERE " +
        "       CA.COURSE_ACTIVITY_TYPE IS NOT NULL\n" +
        "       AND CS.CATCH_UP_CRS_SCH_ID IS NULL")
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
}
