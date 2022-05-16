package uk.gov.justice.hmpps.prison.repository.sql

enum class ScheduleRepositorySql(val sql: String) {
  GET_ACTIVITIES_AT_ONE_LOCATION(
    """
        SELECT O.OFFENDER_ID_DISPLAY   AS OFFENDER_NO,
        OPP.OFFENDER_BOOK_ID    AS BOOKING_ID,
        OCA.EVENT_ID,
        OCA.EVENT_OUTCOME,
        OCA.PERFORMANCE_CODE    AS PERFORMANCE,
        OCA.COMMENT_TEXT        AS OUTCOME_COMMENT,
        OCA.PAY_FLAG            AS PAID,
        O.FIRST_NAME,
        O.LAST_NAME,
        AIL.DESCRIPTION         AS cell_Location,
        CA.COURSE_ACTIVITY_TYPE AS EVENT,
        RD2.DESCRIPTION         AS EVENT_DESCRIPTION,
        CS.START_TIME,
        CS.END_TIME,
        CA.DESCRIPTION          AS "COMMENT",
        CA.INTERNAL_LOCATION_ID AS LOCATION_ID,
        OPP.SUSPENDED_FLAG      AS SUSPENDED
        FROM OFFENDER_PROGRAM_PROFILES OPP
        INNER JOIN OFFENDER_BOOKINGS OB
        ON OB.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y' AND OB.BOOKING_SEQ = 1
        INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID
        INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
        INNER JOIN COURSE_ACTIVITIES CA ON CA.CRS_ACTY_ID = OPP.CRS_ACTY_ID
        INNER JOIN COURSE_SCHEDULES CS ON CA.CRS_ACTY_ID = CS.CRS_ACTY_ID
        AND CS.SCHEDULE_DATE >= TRUNC(OPP.OFFENDER_START_DATE)
        AND TRUNC(CS.SCHEDULE_DATE) <= COALESCE(OPP.OFFENDER_END_DATE, CA.SCHEDULE_END_DATE, CS.SCHEDULE_DATE)
        AND CS.SCHEDULE_DATE >= TRUNC(COALESCE(:fromDate, CS.SCHEDULE_DATE))
        AND TRUNC(CS.SCHEDULE_DATE) <= COALESCE(:toDate, CS.SCHEDULE_DATE)
        LEFT JOIN REFERENCE_CODES RD2 ON RD2.CODE = CA.COURSE_ACTIVITY_TYPE AND RD2.DOMAIN = 'INT_SCH_RSN'
        LEFT JOIN OFFENDER_COURSE_ATTENDANCES OCA ON OCA.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID
        AND TRUNC(OCA.EVENT_DATE) = TRUNC(CS.SCHEDULE_DATE)
        AND OCA.CRS_SCH_ID = CS.CRS_SCH_ID
        WHERE CA.INTERNAL_LOCATION_ID = :locationId
        AND (OPP.OFFENDER_PROGRAM_STATUS = 'ALLOC' OR
        (OPP.OFFENDER_PROGRAM_STATUS = 'END' AND OPP.OFFENDER_END_DATE >= CS.SCHEDULE_DATE))
        AND (COALESCE(OPP.SUSPENDED_FLAG, 'N')) IN (:includeSuspended)
        AND CA.ACTIVE_FLAG = 'Y'
        AND CA.COURSE_ACTIVITY_TYPE IS NOT NULL
        AND CS.CATCH_UP_CRS_SCH_ID IS NULL
        AND (UPPER(TO_CHAR(CS.SCHEDULE_DATE, 'DY')), CS.SLOT_CATEGORY_CODE) NOT IN
        (SELECT OE.EXCLUDE_DAY, COALESCE(OE.SLOT_CATEGORY_CODE, CS.SLOT_CATEGORY_CODE)
        FROM OFFENDER_EXCLUDE_ACTS_SCHDS OE
        WHERE OE.OFF_PRGREF_ID = OPP.OFF_PRGREF_ID)
    """
  ),

  GET_ALL_ACTIVITIES_AT_AGENCY(
    """
        SELECT O.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
        OPP.OFFENDER_BOOK_ID AS BOOKING_ID,
        OCA.EVENT_ID,
        OCA.EVENT_OUTCOME,
        OCA.PERFORMANCE_CODE AS PERFORMANCE,
        OCA.COMMENT_TEXT AS OUTCOME_COMMENT,
        OCA.PAY_FLAG AS PAID,
        O.FIRST_NAME,
        O.LAST_NAME,
        AIL.DESCRIPTION AS cell_Location,
        CA.COURSE_ACTIVITY_TYPE AS EVENT,
        RD2.DESCRIPTION AS EVENT_DESCRIPTION,
        CS.START_TIME,
        CS.END_TIME,
        CA.DESCRIPTION AS "COMMENT",
        CA.INTERNAL_LOCATION_ID AS LOCATION_ID,
        OPP.SUSPENDED_FLAG AS SUSPENDED
        FROM OFFENDER_PROGRAM_PROFILES OPP
        INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y' AND OB.BOOKING_SEQ = 1
        INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID
                INNER JOIN COURSE_ACTIVITIES CA ON CA.CRS_ACTY_ID = OPP.CRS_ACTY_ID
                INNER JOIN COURSE_SCHEDULES CS ON CA.CRS_ACTY_ID = CS.CRS_ACTY_ID
                INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
                AND CS.SCHEDULE_DATE >= TRUNC(OPP.OFFENDER_START_DATE)
        AND TRUNC(CS.SCHEDULE_DATE) <= COALESCE(OPP.OFFENDER_END_DATE, CA.SCHEDULE_END_DATE, CS.SCHEDULE_DATE)
        AND CS.SCHEDULE_DATE >= TRUNC(COALESCE(:fromDate, CS.SCHEDULE_DATE))
        AND TRUNC(CS.SCHEDULE_DATE) <= COALESCE(:toDate, CS.SCHEDULE_DATE)
        LEFT JOIN REFERENCE_CODES RD2 ON RD2.CODE = CA.COURSE_ACTIVITY_TYPE AND RD2.DOMAIN = 'INT_SCH_RSN'
        LEFT JOIN OFFENDER_COURSE_ATTENDANCES OCA ON OCA.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID
                AND TRUNC(OCA.EVENT_DATE) = TRUNC(CS.SCHEDULE_DATE)
        AND OCA.CRS_SCH_ID = CS.CRS_SCH_ID
                WHERE CA.AGY_LOC_ID = :agencyId
        AND (OPP.OFFENDER_PROGRAM_STATUS = 'ALLOC'
                OR (OPP.OFFENDER_PROGRAM_STATUS = 'END' AND OPP.OFFENDER_END_DATE >= CS.SCHEDULE_DATE))
        AND (COALESCE(OPP.SUSPENDED_FLAG, 'N')) IN (:includeSuspended)
        AND CA.ACTIVE_FLAG = 'Y'
        AND CA.COURSE_ACTIVITY_TYPE IS NOT NULL
        AND CS.CATCH_UP_CRS_SCH_ID IS NULL
                AND (UPPER(TO_CHAR(CS.SCHEDULE_DATE, 'DY')), CS.SLOT_CATEGORY_CODE) NOT IN
        (SELECT OE.EXCLUDE_DAY, COALESCE(OE.SLOT_CATEGORY_CODE, CS.SLOT_CATEGORY_CODE)
        FROM OFFENDER_EXCLUDE_ACTS_SCHDS OE
        WHERE OE.OFF_PRGREF_ID = OPP.OFF_PRGREF_ID)
    """
  ),

  GET_ACTIVITIES(
    """
        SELECT O.OFFENDER_ID_DISPLAY                      AS OFFENDER_NO,
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
        CA.DESCRIPTION                             AS "COMMENT",
        CA.INTERNAL_LOCATION_ID                    AS LOCATION_ID,
        COALESCE(AIL2.USER_DESC, AIL2.DESCRIPTION) AS EVENT_LOCATION,
        CS.SLOT_CATEGORY_CODE AS TIME_SLOT,
        (
                CASE
                        WHEN EXISTS (
                        SELECT *
                                FROM OFFENDER_EXCLUDE_ACTS_SCHDS OE
                                WHERE OE.OFF_PRGREF_ID = OPP.OFF_PRGREF_ID
                                AND OE.EXCLUDE_DAY = UPPER(TO_CHAR(CS.SCHEDULE_DATE, 'DY'))
                                AND (OE.SLOT_CATEGORY_CODE IS NULL OR CS.SLOT_CATEGORY_CODE = OE.SLOT_CATEGORY_CODE)
                )
                        THEN 'Y'
                        ELSE NULL
                        END
                )                                         AS EXCLUDED
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
        AND CS.SCHEDULE_DATE = TRUNC(COALESCE(:date, CS.SCHEDULE_DATE))
        LEFT JOIN REFERENCE_CODES RD2             ON RD2.CODE = CA.COURSE_ACTIVITY_TYPE
                AND RD2.DOMAIN = 'INT_SCH_RSN'
        LEFT JOIN OFFENDER_COURSE_ATTENDANCES OCA ON OCA.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID
                AND TRUNC(OCA.EVENT_DATE) = TRUNC(CS.SCHEDULE_DATE)
        AND OCA.CRS_SCH_ID = CS.CRS_SCH_ID
                WHERE (OPP.OFFENDER_PROGRAM_STATUS = 'ALLOC'
                        OR (OPP.OFFENDER_PROGRAM_STATUS = 'END' AND OPP.OFFENDER_END_DATE >= :date))
        AND COALESCE(OPP.SUSPENDED_FLAG, 'N') = 'N'
        AND CA.ACTIVE_FLAG = 'Y'
        AND CA.COURSE_ACTIVITY_TYPE IS NOT NULL
        AND CS.CATCH_UP_CRS_SCH_ID IS NULL
    """
  ),

  GET_COURT_EVENTS(
    """
        SELECT
        O.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
        O.FIRST_NAME,
        O.LAST_NAME,
        CEV.EVENT_ID,
        'COURT'               AS EVENT_TYPE,
        CEV.EVENT_STATUS,
        CEV.EVENT_OUTCOME,
        CEV.COURT_EVENT_TYPE  AS EVENT,
        RC.DESCRIPTION        AS EVENT_DESCRIPTION,
        CEV.START_TIME,
        CEV.COMMENT_TEXT      AS "COMMENT"
        FROM COURT_EVENTS CEV
        INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = CEV.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y'
        INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID
                LEFT JOIN REFERENCE_CODES RC ON RC.CODE = CEV.COURT_EVENT_TYPE AND RC.DOMAIN = 'MOVE_RSN'
        WHERE CEV.EVENT_DATE = :date
        AND O.OFFENDER_ID_DISPLAY in (:offenderNos)
    """
  ),

  GET_APPOINTMENTS_AT_LOCATION(
    """
        SELECT O.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
        OIS.OFFENDER_BOOK_ID AS BOOKING_ID,
        O.FIRST_NAME,
        O.LAST_NAME,
        AIL.DESCRIPTION AS cell_Location,
        OIS.EVENT_SUB_TYPE AS EVENT,
        RC2.DESCRIPTION AS EVENT_DESCRIPTION,
        OIS.START_TIME,
        OIS.END_TIME,
        OIS.COMMENT_TEXT AS "COMMENT",
        OIS.TO_INTERNAL_LOCATION_ID AS LOCATION_ID
        FROM OFFENDER_IND_SCHEDULES OIS
        INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OIS.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y'
        INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID
                INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
                LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = OIS.EVENT_SUB_TYPE AND RC2.DOMAIN = 'INT_SCH_RSN'
        WHERE OIS.TO_INTERNAL_LOCATION_ID = :locationId
        AND OIS.EVENT_TYPE = 'APP'
        AND OIS.EVENT_DATE >= TRUNC(COALESCE(:fromDate, OIS.EVENT_DATE))
        AND TRUNC(OIS.EVENT_DATE) <= COALESCE(:toDate, OIS.EVENT_DATE)
    """
  ),

  GET_VISITS_AT_LOCATION(
    """
        SELECT O.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
        VIS.OFFENDER_BOOK_ID AS BOOKING_ID,
        O.FIRST_NAME,
        O.LAST_NAME,
        AIL.DESCRIPTION AS CELL_LOCATION,
        'VISIT' AS EVENT,
        RC2.DESCRIPTION AS EVENT_DESCRIPTION,
        VIS.START_TIME,
        VIS.END_TIME,
        VIS.VISIT_STATUS EVENT_STATUS,
        RC3.DESCRIPTION AS "COMMENT",
        VIS.VISIT_INTERNAL_LOCATION_ID AS LOCATION_ID
        FROM OFFENDER_VISITS VIS
        INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = VIS.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y'
        INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID
                INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
                LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = 'VISIT' AND RC2.DOMAIN = 'INT_SCH_RSN'
        LEFT JOIN REFERENCE_CODES RC3 ON RC3.CODE = VIS.VISIT_TYPE AND RC3.DOMAIN = 'VISIT_TYPE'
        WHERE VIS.VISIT_INTERNAL_LOCATION_ID = :locationId
        AND VIS.VISIT_DATE >= TRUNC(COALESCE(:fromDate, VIS.VISIT_DATE))
        AND TRUNC(VIS.VISIT_DATE) <= COALESCE(:toDate, VIS.VISIT_DATE)
    """
  ),

  GET_VISITS(
    """
        SELECT O.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
        O.FIRST_NAME,
        O.LAST_NAME,
        'VISIT' AS EVENT,
        RC2.DESCRIPTION AS EVENT_DESCRIPTION,
        VIS.START_TIME,
        VIS.END_TIME,
        VIS.VISIT_STATUS EVENT_STATUS,
        RC3.DESCRIPTION AS "COMMENT",
        VIS.VISIT_INTERNAL_LOCATION_ID AS LOCATION_ID,
        COALESCE(AIL.USER_DESC, AIL.DESCRIPTION) AS EVENT_LOCATION
        FROM OFFENDER_VISITS VIS
        INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = VIS.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y'
        INNER JOIN OFFENDERS O on O.OFFENDER_ID = OB.OFFENDER_ID
                LEFT JOIN REFERENCE_CODES RC1 ON RC1.CODE = 'VISIT' AND RC1.DOMAIN = 'INT_SCH_TYPE'
        LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = 'VISIT' AND RC2.DOMAIN = 'INT_SCH_RSN'
        LEFT JOIN REFERENCE_CODES RC3 ON RC3.CODE = VIS.VISIT_TYPE AND RC3.DOMAIN = 'VISIT_TYPE'
        LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON VIS.VISIT_INTERNAL_LOCATION_ID = AIL.INTERNAL_LOCATION_ID
                WHERE VIS.VISIT_DATE = TRUNC(COALESCE(:date, VIS.VISIT_DATE))
    """
  ),

  GET_APPOINTMENTS(
    """
        SELECT O.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
        O.FIRST_NAME,
        O.LAST_NAME,
        OIS.EVENT_SUB_TYPE AS EVENT,
        RC2.DESCRIPTION AS EVENT_DESCRIPTION,
        OIS.START_TIME,
        OIS.END_TIME,
        OIS.COMMENT_TEXT AS "COMMENT",
        OIS.TO_INTERNAL_LOCATION_ID AS LOCATION_ID,
        COALESCE(AIL.USER_DESC, AIL.DESCRIPTION) EVENT_LOCATION
                FROM OFFENDER_IND_SCHEDULES OIS
        INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OIS.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y'
        INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID
                LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = OIS.EVENT_SUB_TYPE AND RC2.DOMAIN = 'INT_SCH_RSN'
        LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OIS.TO_INTERNAL_LOCATION_ID = AIL.INTERNAL_LOCATION_ID

                WHERE OIS.EVENT_TYPE = 'APP'
        AND OIS.EVENT_DATE = TRUNC(COALESCE(:date, OIS.EVENT_DATE))
    """
  ),

  GET_EXTERNAL_TRANSFERS(
    """
        SELECT O.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
        O.FIRST_NAME,
        O.LAST_NAME,
        OIS.EVENT_SUB_TYPE AS EVENT,
        OIS.EVENT_STATUS EVENT_STATUS,
        RC2.DESCRIPTION AS EVENT_DESCRIPTION,
        OIS.START_TIME,
        OIS.END_TIME,
        OIS.COMMENT_TEXT AS "COMMENT",
        OIS.TO_INTERNAL_LOCATION_ID AS LOCATION_ID
        FROM OFFENDER_IND_SCHEDULES OIS
        INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OIS.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y'
        INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID
                LEFT JOIN REFERENCE_CODES RC2 ON RC2.CODE = OIS.EVENT_SUB_TYPE AND RC2.DOMAIN = 'MOVE_RSN'
        WHERE
        OIS.EVENT_TYPE = 'TRN' AND
                OIS.EVENT_CLASS = 'EXT_MOV' AND
                OIS.AGY_LOC_ID = :agencyId AND
        OIS.EVENT_DATE = TRUNC(COALESCE(:date, OIS.EVENT_DATE))
    """
  ),

  AND_OFFENDER_NUMBERS(" AND O.OFFENDER_ID_DISPLAY in (:offenderNos)")
}
