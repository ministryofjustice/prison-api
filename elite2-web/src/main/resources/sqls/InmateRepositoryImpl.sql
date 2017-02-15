
FIND_INMATE_DETAIL {
     SELECT B.OFFENDER_BOOK_ID, -- inmateId
            B.BOOKING_NO,  -- bookingId
            O.OFFENDER_ID_DISPLAY, -- offenderId
            O.FIRST_NAME,
            O.MIDDLE_NAME,
            O.LAST_NAME,
            (
                SELECT LISTAGG(ALERT_TYPE, ',') WITHIN GROUP (ORDER BY ALERT_TYPE)
                  FROM (
                            SELECT DISTINCT( ALERT_TYPE)
                              FROM OFFENDER_ALERTS A
                             WHERE B.OFFENDER_BOOK_ID = A.OFFENDER_BOOK_ID AND A.ALERT_STATUS = 'ACTIVE'
                       )
            ) AS ALERT_TYPES,
            B.AGY_LOC_ID,
            -- CURRENT LOCATION ID (tbd)
            B.LIVING_UNIT_ID
       FROM OFFENDER_BOOKINGS B LEFT JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
      WHERE B.ACTIVE_FLAG = 'Y'
            AND B.LIVING_UNIT_ID IN (
                SELECT INTERNAL_LOCATION_ID
                  FROM AGENCY_INTERNAL_LOCATIONS START WITH INTERNAL_LOCATION_ID = :locationId
               CONNECT BY PRIOR INTERNAL_LOCATION_ID = PARENT_INTERNAL_LOCATION_ID
            )
}


FIND_INMATES_BY_LOCATION {
     SELECT B.OFFENDER_BOOK_ID, -- inmateId
            B.BOOKING_NO,  -- bookingId
            O.OFFENDER_ID_DISPLAY, -- offenderId
            B.AGY_LOC_ID,
            O.FIRST_NAME,
            O.MIDDLE_NAME,
            O.LAST_NAME,
            B.LIVING_UNIT_ID,
            (
                SELECT LISTAGG(ALERT_TYPE, ',') WITHIN GROUP (ORDER BY ALERT_TYPE)
                  FROM (
                            SELECT DISTINCT( ALERT_TYPE)
                              FROM OFFENDER_ALERTS OA
                             WHERE B.OFFENDER_BOOK_ID = OA.OFFENDER_BOOK_ID AND OA.ALERT_STATUS = 'ACTIVE'
                       )
            ) AS ALERT_TYPES
       FROM OFFENDER_BOOKINGS B LEFT JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
      WHERE B.ACTIVE_FLAG = 'Y'
            AND B.LIVING_UNIT_ID IN (
                SELECT INTERNAL_LOCATION_ID
                  FROM AGENCY_INTERNAL_LOCATIONS START WITH INTERNAL_LOCATION_ID = :locationId
               CONNECT BY PRIOR INTERNAL_LOCATION_ID = PARENT_INTERNAL_LOCATION_ID
            )

})


