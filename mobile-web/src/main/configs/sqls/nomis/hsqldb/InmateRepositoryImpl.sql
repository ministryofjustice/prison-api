FIND_ASSIGNED_LIVING_UNIT {
SELECT B.AGY_LOC_ID,
  B.LIVING_UNIT_ID,
  I.DESCRIPTION LIVING_UNIT_DESCRIPTION,
  AL.DESCRIPTION as AGENCY_NAME
FROM OFFENDER_BOOKINGS B
  LEFT JOIN AGENCY_INTERNAL_LOCATIONS I ON B.LIVING_UNIT_ID = I.INTERNAL_LOCATION_ID
  LEFT JOIN AGENCY_LOCATIONS AL ON AL.AGY_LOC_ID = B.AGY_LOC_ID
WHERE B.ACTIVE_FLAG = 'Y' AND B.OFFENDER_BOOK_ID = :bookingId
}

FIND_ALL_INMATES {
SELECT
  B.OFFENDER_BOOK_ID,
  B.BOOKING_NO,
  O.OFFENDER_ID_DISPLAY,
  B.AGY_LOC_ID,
  O.FIRST_NAME,
  O.MIDDLE_NAME,
  O.LAST_NAME,
  O.BIRTH_DATE,
  NULL AS ALERT_TYPES,
  NULL AS ALIASES,
  B.LIVING_UNIT_ID,
  AIL.DESCRIPTION AS LIVING_UNIT_DESC,
  (
    SELECT OI.OFFENDER_IMAGE_ID
    FROM OFFENDER_IMAGES OI
    WHERE OI.ACTIVE_FLAG = 'Y'
          AND IMAGE_OBJECT_TYPE = 'OFF_BKG'
          AND OI.OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID
          AND OI.IMAGE_VIEW_TYPE = 'FACE'
          AND OI.ORIENTATION_TYPE = 'FRONT'
          AND CREATE_DATETIME = (SELECT MAX(CREATE_DATETIME)
                                 FROM OFFENDER_IMAGES
                                 WHERE ACTIVE_FLAG = 'Y'
                                       AND IMAGE_OBJECT_TYPE = 'OFF_BKG'
                                       AND OFFENDER_BOOK_ID = OI.OFFENDER_BOOK_ID
                                       AND IMAGE_VIEW_TYPE = 'FACE'
                                       AND ORIENTATION_TYPE = 'FRONT')
  ) AS FACE_IMAGE_ID,
  NULL AS ASSIGNED_OFFICER_ID,
  (SELECT COALESCE(RCIEP.DESCRIPTION, OIL.IEP_LEVEL) FROM OFFENDER_IEP_LEVELS OIL LEFT JOIN REFERENCE_CODES RCIEP ON RCIEP.CODE = OIL.IEP_LEVEL AND RCIEP.DOMAIN = 'IEP_LEVEL'
  WHERE OIL.OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID and OIL.IEP_LEVEL_SEQ = (SELECT MAX(OIL2.IEP_LEVEL_SEQ) FROM OFFENDER_IEP_LEVELS OIL2 WHERE OIL2.OFFENDER_BOOK_ID = OIL.OFFENDER_BOOK_ID)) AS IEP_LEVEL
FROM OFFENDER_BOOKINGS B
  INNER JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
  LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON B.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
WHERE B.ACTIVE_FLAG = 'Y'
      AND EXISTS (select 1 from CASELOAD_AGENCY_LOCATIONS C WHERE B.AGY_LOC_ID = C.AGY_LOC_ID AND C.CASELOAD_ID IN (:caseLoadId))
}

FIND_MY_ASSIGNMENTS {
  SELECT B.OFFENDER_BOOK_ID,
    B.BOOKING_NO,
    O.OFFENDER_ID_DISPLAY,
    B.AGY_LOC_ID,
    O.FIRST_NAME,
    O.MIDDLE_NAME,
    O.LAST_NAME,
    O.BIRTH_DATE,
    TRUNC(MONTHS_BETWEEN(sysdate, O.BIRTH_DATE)/12) AS AGE,
    NULL AS ALERT_TYPES,
    NULL AS ALIASES,
    B.LIVING_UNIT_ID,
    AIL.DESCRIPTION AS LIVING_UNIT_DESC,
    (SELECT OI.OFFENDER_IMAGE_ID
     FROM OFFENDER_IMAGES OI
     WHERE OI.ACTIVE_FLAG = 'Y'
           AND IMAGE_OBJECT_TYPE = 'OFF_BKG'
           AND OI.OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID
           AND OI.IMAGE_VIEW_TYPE = 'FACE'
           AND OI.ORIENTATION_TYPE = 'FRONT'
           AND CREATE_DATETIME = (SELECT MAX(CREATE_DATETIME)
                                  FROM OFFENDER_IMAGES
                                  WHERE ACTIVE_FLAG = 'Y'
                                        AND IMAGE_OBJECT_TYPE = 'OFF_BKG'
                                        AND OFFENDER_BOOK_ID = OI.OFFENDER_BOOK_ID
                                        AND IMAGE_VIEW_TYPE = 'FACE'
                                        AND ORIENTATION_TYPE = 'FRONT')) AS FACE_IMAGE_ID,
    (SELECT COALESCE(RCIEP.DESCRIPTION, OIL.IEP_LEVEL) FROM OFFENDER_IEP_LEVELS OIL LEFT JOIN REFERENCE_CODES RCIEP ON RCIEP.CODE = OIL.IEP_LEVEL AND RCIEP.DOMAIN = 'IEP_LEVEL'
    WHERE OIL.OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID and OIL.IEP_LEVEL_SEQ = (SELECT MAX(OIL2.IEP_LEVEL_SEQ) FROM OFFENDER_IEP_LEVELS OIL2 WHERE OIL2.OFFENDER_BOOK_ID = OIL.OFFENDER_BOOK_ID)) AS IEP_LEVEL
  FROM OFFENDER_BOOKINGS B
    INNER JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
    INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON B.AGY_LOC_ID = C.AGY_LOC_ID AND C.CASELOAD_ID = :caseLoadId
    INNER JOIN OFFENDER_KEY_WORKERS OKW ON OKW.OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID
                                           AND OKW.ACTIVE_FLAG = 'Y'
                                           AND (OKW.EXPIRY_DATE is null OR OKW.EXPIRY_DATE >= sysdate)
    LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON B.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
  WHERE B.ACTIVE_FLAG = 'Y'
        AND OKW.OFFICER_ID = :staffId
}
