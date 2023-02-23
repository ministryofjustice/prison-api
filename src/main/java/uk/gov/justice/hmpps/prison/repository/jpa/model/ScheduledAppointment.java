package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Subselect;
import org.springframework.data.annotation.Immutable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Subselect(
    "       SELECT OIS.EVENT_ID AS EVENT_ID," +
    "       O.OFFENDER_ID_DISPLAY AS OFFENDER_NO," +
    "       O.FIRST_NAME AS FIRST_NAME," +
    "       O.LAST_NAME AS LAST_NAME," +
    "       OIS.EVENT_SUB_TYPE AS APPOINTMENT_TYPE_CODE," +
    "       RC2.DESCRIPTION AS APPOINTMENT_TYPE_DESCRIPTION," +
    "       OIS.EVENT_DATE AS EVENT_DATE," +
    "       OIS.START_TIME AS START_TIME," +
    "       OIS.END_TIME AS END_TIME," +
    "       OIS.TO_INTERNAL_LOCATION_ID AS LOCATION_ID," +
    "       COALESCE(AIL.USER_DESC, AIL.DESCRIPTION) AS LOCATION_DESCRIPTION," +
    "       OIS.AGY_LOC_ID AS AGENCY_ID," +
    "       OIS.CREATE_USER_ID AS CREATE_USER_ID" +
    "       FROM OFFENDER_IND_SCHEDULES OIS" +
    "         INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OIS.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y'" +
    "         INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID" +
    "         LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = OIS.EVENT_SUB_TYPE AND RC2.DOMAIN = 'INT_SCH_RSN'" +
    "         LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OIS.TO_INTERNAL_LOCATION_ID = AIL.INTERNAL_LOCATION_ID" +
    "       WHERE OIS.EVENT_TYPE = 'APP'"
)
@Immutable
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"eventId"})
@ToString(exclude = {"eventId"})
public class ScheduledAppointment {
    @Id
    private Long eventId;
    private String offenderNo;
    private String firstName;
    private String lastName;
    private LocalDate eventDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String appointmentTypeDescription;
    private String appointmentTypeCode;
    private String locationDescription;
    private Long locationId;
    private String createUserId;
    private String agencyId;
}

