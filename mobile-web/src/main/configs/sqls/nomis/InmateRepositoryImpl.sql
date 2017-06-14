FIND_INMATE_DETAIL {
  SELECT B.OFFENDER_BOOK_ID,
    B.BOOKING_NO,
    O.OFFENDER_ID_DISPLAY,
    O.FIRST_NAME,
    O.MIDDLE_NAME,
    O.LAST_NAME,
    -- CURRENT LOCATION ID (tbd)
    (
      SELECT OFFENDER_IMAGE_ID
      FROM OFFENDER_IMAGES
      WHERE ACTIVE_FLAG = 'Y'
            AND OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID
            AND IMAGE_VIEW_TYPE = 'FACE'
            AND ORIENTATION_TYPE = 'FRONT'
    ) AS FACE_IMAGE_ID,
    O.BIRTH_DATE,
    (SYSDATE - O.BIRTH_DATE) / 365 AS AGE,
    OKW.OFFICER_ID AS ASSIGNED_OFFICER_ID
  FROM OFFENDER_BOOKINGS B
    INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON C.CASELOAD_ID = :caseLoadId AND B.AGY_LOC_ID = C.AGY_LOC_ID
    LEFT JOIN AGENCY_INTERNAL_LOCATIONS I ON B.LIVING_UNIT_ID = I.INTERNAL_LOCATION_ID
    LEFT JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
    LEFT JOIN OFFENDER_KEY_WORKERS OKW ON OKW.OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID AND OKW.ACTIVE_FLAG = 'Y' AND (OKW.EXPIRY_DATE is null OR OKW.EXPIRY_DATE >= sysdate)
  WHERE B.ACTIVE_FLAG = 'Y' AND B.OFFENDER_BOOK_ID = :bookingId
}

FIND_ALL_INMATES {
  WITH TOTAL_COUNT AS (
    SELECT COUNT(*) AS RECORD_COUNT
    FROM OFFENDER_BOOKINGS B
      INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON C.CASELOAD_ID = :caseLoadId AND B.AGY_LOC_ID = C.AGY_LOC_ID
      LEFT JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
    WHERE B.ACTIVE_FLAG = 'Y' :andQuery)
  SELECT * FROM TOTAL_COUNT, (
    SELECT QRY_SQL.*, ROWNUM rnum FROM (
      SELECT
        B.OFFENDER_BOOK_ID,
        B.BOOKING_NO,
        O.OFFENDER_ID_DISPLAY,
        B.AGY_LOC_ID,
        O.FIRST_NAME,
        O.MIDDLE_NAME,
        O.LAST_NAME,
        NULL AS ALERT_TYPES,
        B.LIVING_UNIT_ID,
        (
          SELECT OFFENDER_IMAGE_ID
          FROM OFFENDER_IMAGES
          WHERE ACTIVE_FLAG = 'Y'
            AND OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID
            AND IMAGE_VIEW_TYPE = 'FACE'
            AND ORIENTATION_TYPE = 'FRONT'
    ) AS FACE_IMAGE_ID,
    NULL AS ASSIGNED_OFFICER_ID
  FROM OFFENDER_BOOKINGS B
    INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON C.CASELOAD_ID = :caseLoadId AND B.AGY_LOC_ID = C.AGY_LOC_ID
    LEFT JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
  WHERE B.ACTIVE_FLAG = 'Y' :andQuery :orderBy) QRY_SQL
    WHERE ROWNUM <= :offset + :limit)
  WHERE rnum >= :offset + 1
}

FIND_INMATES_BY_LOCATION {
  SELECT B.OFFENDER_BOOK_ID,
    B.BOOKING_NO,
    O.OFFENDER_ID_DISPLAY,
    B.AGY_LOC_ID,
    O.FIRST_NAME,
    O.MIDDLE_NAME,
    O.LAST_NAME,
    B.LIVING_UNIT_ID,
    (
      SELECT OFFENDER_IMAGE_ID
      FROM OFFENDER_IMAGES
      WHERE ACTIVE_FLAG = 'Y'
            AND OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID
            AND IMAGE_VIEW_TYPE = 'FACE'
            AND ORIENTATION_TYPE = 'FRONT'
    ) AS FACE_IMAGE_ID
  FROM OFFENDER_BOOKINGS B
    INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON C.CASELOAD_ID = :caseLoadId AND B.AGY_LOC_ID = C.AGY_LOC_ID
    LEFT JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
  WHERE B.ACTIVE_FLAG = 'Y'
        AND B.LIVING_UNIT_ID IN (
    SELECT INTERNAL_LOCATION_ID
    FROM AGENCY_INTERNAL_LOCATIONS START WITH INTERNAL_LOCATION_ID = :locationId
    CONNECT BY PRIOR INTERNAL_LOCATION_ID = PARENT_INTERNAL_LOCATION_ID
  )

}


FIND_PHYSICAL_CHARACTERISTICS_BY_BOOKING {
  select PT.DESCRIPTION AS CHARACTERISTIC, PC.DESCRIPTION AS DETAIL
  from OFFENDER_PROFILE_DETAILS P
    JOIN OFFENDER_BOOKINGS B ON B.OFFENDER_BOOK_ID = P.OFFENDER_BOOK_ID
    JOIN CASELOAD_AGENCY_LOCATIONS C ON C.CASELOAD_ID = :caseLoadId AND B.AGY_LOC_ID = C.AGY_LOC_ID
    JOIN PROFILE_TYPES PT ON PT.PROFILE_TYPE = P.PROFILE_TYPE AND PROFILE_CATEGORY = 'PA'
    LEFT JOIN PROFILE_CODES PC ON PC.PROFILE_TYPE = PT.PROFILE_TYPE AND PC.PROFILE_CODE = P.PROFILE_CODE
  where P.OFFENDER_BOOK_ID = :bookingId
  ORDER BY PT.PROFILE_TYPE
}


FIND_PHYSICAL_MARKS_BY_BOOKING {
SELECT (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = M.MARK_TYPE AND DOMAIN='MARK_TYPE') AS TYPE,
       (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = M.SIDE_CODE AND DOMAIN='SIDE') AS SIDE,
       (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = M.BODY_PART_CODE AND DOMAIN='BODY_PART') AS BODY_PART,
       (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = M.PART_ORIENTATION_CODE AND DOMAIN='PART_ORIENT') AS ORENTIATION,
  M.COMMENT_TEXT,
       (SELECT I.OFFENDER_IMAGE_ID
        FROM OFFENDER_IMAGES I
        WHERE B.OFFENDER_BOOK_ID = I.OFFENDER_BOOK_ID
              AND I.ACTIVE_FLAG = 'Y'
              AND M.MARK_TYPE = I.IMAGE_VIEW_TYPE
             AND M.BODY_PART_CODE = I.ORIENTATION_TYPE
       ) AS IMAGE_ID
FROM OFFENDER_IDENTIFYING_MARKS M
  JOIN OFFENDER_BOOKINGS B ON B.OFFENDER_BOOK_ID = M.OFFENDER_BOOK_ID
  JOIN CASELOAD_AGENCY_LOCATIONS C ON C.CASELOAD_ID = :caseLoadId AND B.AGY_LOC_ID = C.AGY_LOC_ID
WHERE B.OFFENDER_BOOK_ID = :bookingId
      AND B.ACTIVE_FLAG = 'Y'
      AND M.BODY_PART_CODE != 'CONV'
}


FIND_MY_ASSIGNMENTS {
SELECT B.OFFENDER_BOOK_ID,
  B.BOOKING_NO,
  O.OFFENDER_ID_DISPLAY,
  B.AGY_LOC_ID,
  O.FIRST_NAME,
  O.MIDDLE_NAME,
  O.LAST_NAME,
  NULL AS ALERT_TYPES,
  B.LIVING_UNIT_ID,
  (
    SELECT OFFENDER_IMAGE_ID
    FROM OFFENDER_IMAGES
    WHERE ACTIVE_FLAG = 'Y'
          AND OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID
          AND IMAGE_VIEW_TYPE = 'FACE'
          AND ORIENTATION_TYPE = 'FRONT'
  ) AS FACE_IMAGE_ID
FROM OFFENDER_BOOKINGS B
  INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON C.CASELOAD_ID = :caseLoadId AND B.AGY_LOC_ID = C.AGY_LOC_ID
  JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
  JOIN OFFENDER_KEY_WORKERS OKW ON OKW.OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID AND OKW.ACTIVE_FLAG = 'Y' AND (OKW.EXPIRY_DATE is null OR OKW.EXPIRY_DATE >= sysdate)
WHERE B.ACTIVE_FLAG = 'Y'
      AND OKW.OFFICER_ID = :staffId
}