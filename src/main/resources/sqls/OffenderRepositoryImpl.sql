SEARCH_OFFENDERS {
SELECT
  O.OFFENDER_ID_DISPLAY OFFENDER_NO,
  O.TITLE TITLE,
  O.SUFFIX SUFFIX,
  O.FIRST_NAME FIRST_NAME,
  CONCAT (O.MIDDLE_NAME, CASE WHEN MIDDLE_NAME_2 IS NOT NULL THEN CONCAT (' ', O.MIDDLE_NAME_2) ELSE '' END) MIDDLE_NAMES,
  O.LAST_NAME LAST_NAME,
  O.BIRTH_DATE DATE_OF_BIRTH,
  RCE.DESCRIPTION ETHNICITY,
  RCS.DESCRIPTION GENDER,
  RCC.DESCRIPTION BIRTH_COUNTRY,
  OB.OFFENDER_BOOK_ID LATEST_BOOKING_ID,
  OB.BOOKING_BEGIN_DATE RECEPTION_DATE,
  OB.ACTIVE_FLAG CURRENTLY_IN_PRISON,
  OB.AGY_LOC_ID LATEST_LOCATION_ID,
  AL.DESCRIPTION LATEST_LOCATION,
  CASE WHEN CAST(IST.BAND_CODE AS int) <= 8
           THEN 'Convicted'
       WHEN CAST(IST.BAND_CODE AS int) > 8 AND CAST(IST.BAND_CODE AS int) < 11
           THEN 'Remand'
       WHEN CAST(IST.BAND_CODE AS int) = 11
           THEN 'Convicted'
       WHEN CAST(IST.BAND_CODE AS int) > 11
           THEN 'Remand'
       ELSE NULL END                     CONVICTED_STATUS,
  CASE WHEN OPD2.PROFILE_CODE IS NOT NULL THEN OPD2.PROFILE_CODE ELSE PC1.DESCRIPTION END NATIONALITIES,
  PC3.DESCRIPTION RELIGION,
  PC2.DESCRIPTION MARITAL_STATUS,
  OIS.IMPRISONMENT_STATUS,
  OI1.IDENTIFIER PNC_NUMBER,
  OI2.IDENTIFIER CRO_NUMBER
FROM OFFENDERS O
  INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_ID = O.OFFENDER_ID AND OB.BOOKING_SEQ = 1
  INNER JOIN AGENCY_LOCATIONS AL ON AL.AGY_LOC_ID = OB.AGY_LOC_ID
  LEFT JOIN OFFENDER_IDENTIFIERS OI1 ON OI1.OFFENDER_ID = OB.OFFENDER_ID AND OI1.IDENTIFIER_TYPE = 'PNC'
  LEFT JOIN OFFENDER_IDENTIFIERS OI2 ON OI2.OFFENDER_ID = OB.OFFENDER_ID AND OI2.IDENTIFIER_TYPE = 'CRO'
  LEFT JOIN OFFENDER_IMPRISON_STATUSES OIS ON OIS.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OIS.LATEST_STATUS = 'Y'
  LEFT JOIN IMPRISONMENT_STATUSES IST ON IST.IMPRISONMENT_STATUS = OIS.IMPRISONMENT_STATUS
  LEFT JOIN REFERENCE_CODES RCE ON O.RACE_CODE = RCE.CODE AND RCE.DOMAIN = 'ETHNICITY'
  LEFT JOIN REFERENCE_CODES RCS ON O.SEX_CODE = RCS.CODE AND RCS.DOMAIN = 'SEX'
  LEFT JOIN REFERENCE_CODES RCC ON O.BIRTH_COUNTRY_CODE = RCC.CODE AND RCC.DOMAIN = 'COUNTRY'
  LEFT JOIN OFFENDER_PROFILE_DETAILS OPD1 ON OPD1.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OPD1.PROFILE_TYPE = 'NAT'
  LEFT JOIN OFFENDER_PROFILE_DETAILS OPD2 ON OPD2.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OPD2.PROFILE_TYPE = 'NATIO'
  LEFT JOIN OFFENDER_PROFILE_DETAILS OPD3 ON OPD3.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OPD3.PROFILE_TYPE = 'RELF'
  LEFT JOIN OFFENDER_PROFILE_DETAILS OPD4 ON OPD4.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OPD4.PROFILE_TYPE = 'MARITAL'
  LEFT JOIN PROFILE_CODES PC1 ON PC1.PROFILE_TYPE = OPD1.PROFILE_TYPE AND PC1.PROFILE_CODE = OPD1.PROFILE_CODE
  LEFT JOIN PROFILE_CODES PC2 ON PC2.PROFILE_TYPE = OPD4.PROFILE_TYPE AND PC2.PROFILE_CODE = OPD4.PROFILE_CODE
  LEFT JOIN PROFILE_CODES PC3 ON PC3.PROFILE_TYPE = OPD3.PROFILE_TYPE AND PC3.PROFILE_CODE = OPD3.PROFILE_CODE
}

PRISONERS_AT_LOCATION {
    SELECT OB.AGY_LOC_ID                                                                             ESTABLISHMENT_CODE,
           OB.OFFENDER_BOOK_ID                                                                       BOOKING_ID,
           O.OFFENDER_ID_DISPLAY                                                                     NOMS_ID,
           O.FIRST_NAME                                                                              GIVEN_NAME1,
           CONCAT(O.MIDDLE_NAME,
                  CASE WHEN MIDDLE_NAME_2 IS NOT NULL THEN CONCAT(' ', O.MIDDLE_NAME_2) ELSE '' END) GIVEN_NAME2,
           O.LAST_NAME                                                                               LAST_NAME,
           OB.REQUEST_NAME AS                                                                        REQUESTED_NAME,
           O.BIRTH_DATE                                                                              DATE_OF_BIRTH,
           RCS.DESCRIPTION                                                                           GENDER,
           CASE
               WHEN english_speaking.english_speaking_flag = 'Y' THEN 1
               ELSE 0
               END                                                                                   ENGLISH_SPEAKING,
           AIL.DESCRIPTION                                                                           CELL_LOCATION,
           OB.BOOKING_BEGIN_DATE                                                                     BOOKING_BEGIN_DATE,
           dt_admission.admission_date                                                               ADMISSION_DATE,
           'ACTIVE ' || OB.in_out_status                                                             COMMUNITY_STATUS,
           CAST(IST.BAND_CODE AS int)                                                                BAND_CODE
    FROM OFFENDER_BOOKINGS OB
             INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID AND OB.ACTIVE_FLAG = 'Y' AND OB.BOOKING_SEQ = 1
             INNER JOIN AGENCY_LOCATIONS AL ON AL.AGY_LOC_ID = OB.AGY_LOC_ID
             INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
             LEFT JOIN OFFENDER_IMPRISON_STATUSES OIS ON OIS.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OIS.LATEST_STATUS = 'Y'
             LEFT JOIN IMPRISONMENT_STATUSES IST ON IST.IMPRISONMENT_STATUS = OIS.IMPRISONMENT_STATUS
             LEFT JOIN REFERENCE_CODES RCS ON O.SEX_CODE = RCS.CODE AND RCS.DOMAIN = 'SEX'
             LEFT JOIN (
        SELECT DISTINCT offender_book_id,
               'Y' AS english_speaking_flag
        FROM offender_languages
        WHERE language_code = 'ENG'
          AND speak_skill IN ('Y',
                              'A',
                              'G',
                              'D')) english_speaking on english_speaking.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID

             LEFT JOIN (
        SELECT m.offender_book_id,
                        Max(m.movement_time) AS admission_date
        FROM offender_external_movements m,
             offender_bookings h
        WHERE h.booking_status = 'O'
          AND h.active_flag = 'Y'
          AND m.direction_code = 'IN'
          AND m.movement_type = 'ADM'
          AND m.to_agy_loc_id = h.agy_loc_id
          AND h.offender_book_id = m.offender_book_id
        GROUP BY m.offender_book_id) dt_admission on dt_admission.OFFENDER_BOOK_ID = ob.OFFENDER_BOOK_ID
    WHERE OB.AGY_LOC_ID = :agencyId
}
