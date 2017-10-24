FIND_INMATE_DETAIL {
  SELECT B.OFFENDER_BOOK_ID,
         B.BOOKING_NO,
         O.OFFENDER_ID_DISPLAY,
         O.FIRST_NAME,
         O.MIDDLE_NAME,
         O.LAST_NAME,
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
         O.BIRTH_DATE,
         TRUNC(MONTHS_BETWEEN(sysdate, O.BIRTH_DATE)/12) AS AGE,
         (SELECT OKW.OFFICER_ID
          FROM OFFENDER_KEY_WORKERS OKW
          WHERE B.OFFENDER_BOOK_ID = OKW.OFFENDER_BOOK_ID
          AND OKW.ACTIVE_FLAG = 'Y' AND (OKW.EXPIRY_DATE is null OR OKW.EXPIRY_DATE >= sysdate)) AS ASSIGNED_OFFICER_ID
  FROM OFFENDER_BOOKINGS B
    INNER JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
  WHERE B.ACTIVE_FLAG = 'Y' AND B.OFFENDER_BOOK_ID = :bookingId
  AND EXISTS (select 1 from CASELOAD_AGENCY_LOCATIONS C WHERE B.AGY_LOC_ID = C.AGY_LOC_ID AND C.CASELOAD_ID IN (:caseLoadId))
}

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
        TRUNC(MONTHS_BETWEEN(sysdate, O.BIRTH_DATE)/12) AS AGE,
        NULL AS ALERT_TYPES,
        NULL AS ALIASES,
        B.LIVING_UNIT_ID,
        AIL.DESCRIPTION as LIVING_UNIT_DESC,
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

FIND_INMATES_BY_LOCATION {
  SELECT B.OFFENDER_BOOK_ID,
    B.BOOKING_NO,
    O.OFFENDER_ID_DISPLAY,
    B.AGY_LOC_ID,
    O.FIRST_NAME,
    O.MIDDLE_NAME,
    O.LAST_NAME,
    O.BIRTH_DATE,
    TRUNC(MONTHS_BETWEEN(sysdate, O.BIRTH_DATE)/12) AS AGE,
    NULL AS ALIASES,
    B.LIVING_UNIT_ID,
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
    (SELECT COALESCE(RCIEP.DESCRIPTION, OIL.IEP_LEVEL) FROM OFFENDER_IEP_LEVELS OIL LEFT JOIN REFERENCE_CODES RCIEP ON RCIEP.CODE = OIL.IEP_LEVEL AND RCIEP.DOMAIN = 'IEP_LEVEL'
    WHERE OIL.OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID and OIL.IEP_LEVEL_SEQ = (SELECT MAX(OIL2.IEP_LEVEL_SEQ) FROM OFFENDER_IEP_LEVELS OIL2 WHERE OIL2.OFFENDER_BOOK_ID = OIL.OFFENDER_BOOK_ID)) AS IEP_LEVEL
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
  SELECT PT.DESCRIPTION AS CHARACTERISTIC,
         COALESCE(PC.DESCRIPTION, P.PROFILE_CODE) AS DETAIL,
         NULL AS IMAGE_ID
  FROM OFFENDER_PROFILE_DETAILS P
    INNER JOIN OFFENDER_BOOKINGS B ON B.OFFENDER_BOOK_ID = P.OFFENDER_BOOK_ID
    INNER JOIN PROFILE_TYPES PT ON PT.PROFILE_TYPE = P.PROFILE_TYPE
      AND PT.PROFILE_CATEGORY = 'PA' AND PT.ACTIVE_FLAG = 'Y'
    LEFT JOIN PROFILE_CODES PC ON PC.PROFILE_TYPE = PT.PROFILE_TYPE AND PC.PROFILE_CODE = P.PROFILE_CODE
  WHERE P.OFFENDER_BOOK_ID = :bookingId AND P.PROFILE_CODE IS NOT NULL
  ORDER BY P.LIST_SEQ
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
         O.BIRTH_DATE,
         TRUNC(MONTHS_BETWEEN(sysdate, O.BIRTH_DATE)/12) AS AGE,
         NULL AS ALERT_TYPES,
         NULL AS ALIASES,
         B.LIVING_UNIT_ID,
         AIL.DESCRIPTION as LIVING_UNIT_DESC,
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

FIND_PRISONERS {
  SELECT
    O.OFFENDER_ID_DISPLAY,
    O.TITLE,
    O.SUFFIX,
    O.FIRST_NAME,
    CONCAT(O.middle_name, CASE WHEN middle_name_2 IS NOT NULL
      THEN concat(' ', O.middle_name_2)
                          ELSE '' END) MIDDLE_NAMES,
    O.LAST_NAME,
    O.BIRTH_DATE,
    RCE.DESCRIPTION       AS       ETHNICITY,
    RCS.DESCRIPTION       AS       SEX,
    RCC.DESCRIPTION       AS       BIRTH_COUNTRY,
    ob.booking_begin_date,
    ob.active_flag,
    ob.agy_loc_id,
    al.description                 AGY_LOC_DESC,
    COALESCE(ord.release_date, ord.auto_release_date) RELEASE_DATE,
    CASE WHEN CAST(ist.band_code AS BIGINT) <= 8
      THEN 'Convicted'
    WHEN CAST(ist.band_code AS BIGINT) > 8
      THEN 'Remand'
    ELSE NULL END                  CONVICTED_STATUS,
    CASE WHEN opd2.profile_code IS NOT NULL
      THEN opd2.profile_code
    ELSE pc.description END        NATIONALITIES,
    pc3.description                RELIGION,
    pc2.description                MARITAL_STATUS,
    ois.imprisonment_status,
    (SELECT oi1.identifier
     FROM offender_identifiers oi1
     WHERE oi1.offender_id = ob.offender_id
           AND oi1.identifier_type = 'PNC'
           AND oi1.OFFENDER_ID_SEQ = (SELECT MAX(OFFENDER_ID_SEQ)  FROM offender_identifiers oi11 where oi11.OFFENDER_ID = oi1.offender_id AND oi11.identifier_type = oi1.identifier_type )) PNC_NUMBER,
    (SELECT oi2.identifier
     FROM offender_identifiers oi2
     WHERE oi2.offender_id = ob.offender_id
           AND oi2.identifier_type = 'CRO'
           AND oi2.OFFENDER_ID_SEQ = (SELECT MAX(OFFENDER_ID_SEQ)  FROM offender_identifiers oi21 where oi21.OFFENDER_ID = oi2.offender_id AND oi21.identifier_type = oi2.identifier_type )) CRO_NUMBER
  FROM OFFENDERS O
    JOIN OFFENDER_BOOKINGS OB
      ON OB.offender_id = o.offender_id
         AND OB.booking_seq = 1
    join agency_locations al
      on al.agy_loc_id = ob.agy_loc_id
    left join offender_release_details ord
      on ord.offender_book_id = ob.offender_book_id
    LEFT JOIN offender_imprison_statuses ois
      ON ois.offender_book_id = OB.offender_book_id
         AND ois.latest_status = 'Y'
    LEFT JOIN imprisonment_statuses ist
      ON ist.imprisonment_status = ois.imprisonment_status
    LEFT JOIN REFERENCE_CODES RCE ON O.RACE_CODE = RCE.CODE
                                     AND RCE.DOMAIN = 'ETHNICITY'
    LEFT JOIN REFERENCE_CODES RCS ON O.SEX_CODE = RCS.CODE
                                     AND RCS.DOMAIN = 'SEX'
    LEFT JOIN REFERENCE_CODES RCC ON O.BIRTH_COUNTRY_CODE = RCC.CODE
                                     AND RCC.DOMAIN = 'COUNTRY'
    LEFT JOIN offender_profile_details opd1
      ON opd1.offender_book_id = ob.offender_book_id
         AND opd1.profile_type = 'NAT'
    LEFT JOIN offender_profile_details opd2
      ON opd2.offender_book_id = ob.offender_book_id
         AND opd2.profile_type = 'NATIO'
    LEFT JOIN offender_profile_details opd3
      ON opd3.offender_book_id = ob.offender_book_id
         AND opd3.profile_type = 'RELF'
    LEFT JOIN offender_profile_details opd4
      ON opd4.offender_book_id = ob.offender_book_id
         AND opd4.profile_type = 'MARITAL'
    LEFT JOIN profile_codes pc
      ON pc.profile_type = opd1.profile_type
         AND pc.profile_code = opd1.profile_code
    LEFT JOIN profile_codes pc2
      ON pc2.profile_type = opd4.profile_type
         AND pc2.profile_code = opd4.profile_code
    LEFT JOIN profile_codes pc3
      ON pc3.profile_type = opd3.profile_type
         AND pc3.profile_code = opd3.profile_code
}
