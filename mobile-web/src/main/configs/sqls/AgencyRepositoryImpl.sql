GET_AGENCIES {
  SELECT DISTINCT AGY_LOC_ID AGENCY_ID,
         DESCRIPTION,
         AGENCY_LOCATION_TYPE AGENCY_TYPE
  FROM AGENCY_LOCATIONS
  WHERE ACTIVE_FLAG = 'Y'
    AND AGY_LOC_ID NOT IN ('OUT','TRN')
}

FIND_AGENCIES_BY_USERNAME {
  SELECT DISTINCT A.AGY_LOC_ID AGENCY_ID,
         A.DESCRIPTION,
         A.AGENCY_LOCATION_TYPE AGENCY_TYPE
  FROM AGENCY_LOCATIONS A
    INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON A.AGY_LOC_ID = C.AGY_LOC_ID
  WHERE A.ACTIVE_FLAG = 'Y'
    AND A.AGY_LOC_ID NOT IN ('OUT','TRN')
    AND C.CASELOAD_ID IN
      (SELECT SAC.CASELOAD_ID
       FROM STAFF_ACCESSIBLE_CASELOADS SAC
         INNER JOIN STAFF_MEMBERS SM ON SM.STAFF_ID = SAC.STAFF_ID
           AND SM.PERSONNEL_TYPE = 'STAFF' AND SM.USER_ID = :username)
}

FIND_AGENCIES_BY_CURRENT_CASELOAD {
  SELECT DISTINCT A.AGY_LOC_ID AGENCY_ID,
    A.DESCRIPTION,
                  A.AGENCY_LOCATION_TYPE AGENCY_TYPE
  FROM AGENCY_LOCATIONS A
    INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON A.AGY_LOC_ID = C.AGY_LOC_ID
  WHERE A.ACTIVE_FLAG = 'Y'
        AND A.AGY_LOC_ID NOT IN ('OUT','TRN')
        AND C.CASELOAD_ID IN
            (SELECT SM.ASSIGNED_CASELOAD_ID
             FROM STAFF_MEMBERS SM
             WHERE SM.PERSONNEL_TYPE = 'STAFF' AND SM.USER_ID = :username)
}

FIND_AGENCIES_BY_CASELOAD {
  SELECT A.AGY_LOC_ID AGENCY_ID,
         A.DESCRIPTION,
         A.AGENCY_LOCATION_TYPE AGENCY_TYPE
  FROM AGENCY_LOCATIONS A
    INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON A.AGY_LOC_ID = C.AGY_LOC_ID
  WHERE A.ACTIVE_FLAG = 'Y'
        AND A.AGY_LOC_ID NOT IN ('OUT','TRN')
        AND C.CASELOAD_ID = :caseloadId
}

GET_AGENCY {
    SELECT A.AGY_LOC_ID AGENCY_ID,
           A.DESCRIPTION,
           A.AGENCY_LOCATION_TYPE AGENCY_TYPE
    FROM AGENCY_LOCATIONS A
    WHERE A.ACTIVE_FLAG = 'Y'
      AND A.AGY_LOC_ID = :agencyId
}

GET_AGENCY_LOCATIONS {
  SELECT A.INTERNAL_LOCATION_ID LOCATION_ID,
         A.AGY_LOC_ID AGENCY_ID,
         A.INTERNAL_LOCATION_TYPE LOCATION_TYPE,
         A.DESCRIPTION,
         A.PARENT_INTERNAL_LOCATION_ID PARENT_LOCATION_ID,
         A.NO_OF_OCCUPANT CURRENT_OCCUPANCY,
         A.OPERATION_CAPACITY OPERATIONAL_CAPACITY,
         A.USER_DESC USER_DESCRIPTION
  FROM AGENCY_INTERNAL_LOCATIONS A
  WHERE A.ACTIVE_FLAG = 'Y'
  AND A.AGY_LOC_ID = :agencyId
}

GET_AGENCY_LOCATIONS_FOR_EVENT_TYPE {
  SELECT AIL.INTERNAL_LOCATION_ID LOCATION_ID,
         AIL.AGY_LOC_ID AGENCY_ID,
         AIL.INTERNAL_LOCATION_TYPE LOCATION_TYPE,
         ILU.INTERNAL_LOCATION_USAGE LOCATION_USAGE,
         AIL.DESCRIPTION,
         AIL.PARENT_INTERNAL_LOCATION_ID PARENT_LOCATION_ID,
         AIL.NO_OF_OCCUPANT CURRENT_OCCUPANCY,
         AIL.OPERATION_CAPACITY OPERATIONAL_CAPACITY,
         AIL.USER_DESC USER_DESCRIPTION
  FROM AGENCY_INTERNAL_LOCATIONS AIL
    INNER JOIN INT_LOC_USAGE_LOCATIONS ILUL ON AIL.INTERNAL_LOCATION_ID = ILUL.INTERNAL_LOCATION_ID
    INNER JOIN INTERNAL_LOCATION_USAGES ILU ON ILU.AGY_LOC_ID = AIL.AGY_LOC_ID
      AND ILU.INTERNAL_LOCATION_USAGE_ID = ILUL.INTERNAL_LOCATION_USAGE_ID
  WHERE ILU.INTERNAL_LOCATION_USAGE in ( :eventTypes )
  AND AIL.AGY_LOC_ID = :agencyId
  AND AIL.ACTIVE_FLAG = 'Y'
  AND AIL.DEACTIVATE_DATE IS NULL
  AND AIL.INTERNAL_LOCATION_CODE <> 'RTU'
  AND NOT EXISTS (SELECT 1
                  FROM INT_LOC_USAGE_LOCATIONS
                  WHERE PARENT_USAGE_LOCATION_ID = ILUL.USAGE_LOCATION_ID)
}

GET_AGENCY_LOCATIONS_FOR_EVENTS_BOOKED {
  SELECT AIL.INTERNAL_LOCATION_ID LOCATION_ID,
         AIL.USER_DESC USER_DESCRIPTION
  FROM AGENCY_INTERNAL_LOCATIONS AIL
                      -- INTERNAL_LOCATION_USAGE_ID,INTERNAL_LOCATION_ID,CAPACITY,USAGE_LOCATION_TYPE,LIST_SEQ,USAGE_LOCATION_ID,PARENT_USAGE_LOCATION_ID
     INNER JOIN INT_LOC_USAGE_LOCATIONS ILUL ON AIL.INTERNAL_LOCATION_ID = ILUL.INTERNAL_LOCATION_ID  -- -2,-28,100,'CLAS',99,19948,null
     INNER JOIN INTERNAL_LOCATION_USAGES ILU ON ILU.AGY_LOC_ID = AIL.AGY_LOC_ID
                                            AND ILU.INTERNAL_LOCATION_USAGE_ID = ILUL.INTERNAL_LOCATION_USAGE_ID
  WHERE ILU.INTERNAL_LOCATION_USAGE = 'APP' -- -2,'LEI','APP',
  AND AIL.AGY_LOC_ID = :agencyId  --   -28, 'VIS', 'VISIT', 'LEI', 'LEI-VIS', null, 'Visiting Room'
  AND AIL.ACTIVE_FLAG = 'Y' -- default
  AND AIL.DEACTIVATE_DATE IS NULL
  AND AIL.INTERNAL_LOCATION_CODE <> 'RTU'
  AND NOT EXISTS (SELECT 1
                  FROM INT_LOC_USAGE_LOCATIONS
                  WHERE PARENT_USAGE_LOCATION_ID = ILUL.USAGE_LOCATION_ID)
  AND AIL.INTERNAL_LOCATION_ID in (
    (SELECT distinct CA.INTERNAL_LOCATION_ID
        FROM OFFENDER_PROGRAM_PROFILES OPP
           INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OPP.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y'
           INNER JOIN COURSE_ACTIVITIES CA ON CA.CRS_ACTY_ID = OPP.CRS_ACTY_ID
           INNER JOIN COURSE_SCHEDULES CS
                ON OPP.CRS_ACTY_ID = CS.CRS_ACTY_ID
               AND CS.SCHEDULE_DATE >= TRUNC(OPP.OFFENDER_START_DATE)
               AND TRUNC(CS.SCHEDULE_DATE) <= COALESCE(OPP.OFFENDER_END_DATE, CA.SCHEDULE_END_DATE, CS.SCHEDULE_DATE)
               AND CS.START_TIME BETWEEN :periodStart AND :periodEnd
        WHERE OPP.OFFENDER_PROGRAM_STATUS = 'ALLOC'
          AND COALESCE(OPP.SUSPENDED_FLAG, 'N') = 'N'
          AND CA.ACTIVE_FLAG = 'Y'
          AND CA.COURSE_ACTIVITY_TYPE IS NOT NULL
          AND CS.CATCH_UP_CRS_SCH_ID IS NULL
          AND CA.AGY_LOC_ID = :agencyId
    ) UNION (
        SELECT distinct OIS.TO_INTERNAL_LOCATION_ID
        FROM OFFENDER_IND_SCHEDULES OIS
            INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OIS.OFFENDER_BOOK_ID AND OB.ACTIVE_FLAG = 'Y'
        WHERE OIS.EVENT_TYPE = 'APP'
          AND OIS.EVENT_STATUS = 'SCH'
          AND OIS.START_TIME BETWEEN :periodStart AND :periodEnd
          AND OIS.AGY_LOC_ID = :agencyId --- ********** TO_AGY_LOC_ID ?
    ) UNION (
--INSERT INTO OFFENDER_VISITS (OFFENDER_VISIT_ID, OFFENDER_BOOK_ID, VISIT_DATE, START_TIME, END_TIME, VISIT_TYPE, VISIT_STATUS, VISIT_INTERNAL_LOCATION_ID, AGY_LOC_ID) VALUES (-10, -1, TO_DATE('2017-06-10', 'YYYY-MM-DD'),
            -- TO_DATE('2017-06-10 14:30:00', 'YYYY-MM-DD HH24:MI:SS'), TO_DATE('2017-06-10 15:30:00', 'YYYY-MM-DD HH24:MI:SS'), 'SCON', 'SCH', -28,  'LEI');
         SELECT distinct VIS.VISIT_INTERNAL_LOCATION_ID -- OFFENDER_BOOK_ID -1
         FROM OFFENDER_VISITS VIS
         WHERE VIS.START_TIME BETWEEN :periodStart AND :periodEnd
           AND VIS.VISIT_STATUS <> 'CANC'
           AND VIS.AGY_LOC_ID = :agencyId
    )
  )
  ORDER BY USER_DESCRIPTION
}

FIND_PRISON_ADDRESSES_PHONE_NUMBERS {
  SELECT
  '123' agency_id,
  'placeholder type' address_type,
  'placeholder premise' premise,
  'placeholder street' STREET,
  'placeholder locality' LOCALITY,
  'placeholder city' CITY,
  'placeholder country' COUNTRY,
  'placeholder postcode' POSTAL_CODE,
  'placeholder phone type' PHONE_TYPE,
  'placeholder phone number' PHONE_NO,
  'placeholder ext number' EXT_NO
FROM DUAL
}
