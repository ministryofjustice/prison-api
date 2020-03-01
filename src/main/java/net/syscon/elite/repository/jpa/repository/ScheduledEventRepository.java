package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.ScheduledAppointment;
import net.syscon.elite.repository.jpa.model.ScheduledEvent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ScheduledEventRepository extends Repository<ScheduledEvent, Long> {
    @Query(nativeQuery = true, value =
            "SELECT O.OFFENDER_ID_DISPLAY AS offenderNo," +
            "       O.FIRST_NAME as fistName," +
            "       O.LAST_NAME as lastName," +
            "       OIS.EVENT_SUB_TYPE AS appoitnemntTypeCode," +
            "       RC2.DESCRIPTION AS appointmentTypeDescription," +
            "       OIS.EVENT_DATE AS eventDate," +
            "       OIS.START_TIME AS startTime," +
            "       OIS.END_TIME AS endTime," +
            "       OIS.TO_INTERNAL_LOCATION_ID AS locationId," +
            "       COALESCE(AIL.USER_DESC, AIL.DESCRIPTION) AS locationDescription," +
            "       OIS.AUDIT_USER_ID AS auditUserId" +
            "       FROM OFFENDER_IND_SCHEDULES OIS" +
            "         INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OIS.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y'" +
            "         INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID" +
            "         LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = OIS.EVENT_SUB_TYPE AND RC2.DOMAIN = 'INT_SCH_RSN'" +
            "         LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OIS.TO_INTERNAL_LOCATION_ID = AIL.INTERNAL_LOCATION_ID" +
            "       WHERE OIS.EVENT_TYPE = 'APP'" +
            " AND OIS.AGY_LOC_ID = :agencyId" +
            " AND ((:locationId is null) OR OIS.TO_INTERNAL_LOCATION_ID = :locationId)" +
            " AND OIS.EVENT_DATE = TRUNC(COALESCE(:date, OIS.EVENT_DATE))"
    )
    List<ScheduledAppointment> findAllAppointments(@Param("agencyId") final String agencyId, @Param("date") LocalDate date, @Param("locationId") final Long locationId);
}
