package uk.gov.justice.hmpps.prison.repository.sql

enum class InmateRepositorySql(val sql: String) {
  FIND_INMATE_DETAIL(
    """
        SELECT B.OFFENDER_BOOK_ID BOOKING_ID,
        B.BOOKING_NO,
        O.OFFENDER_ID,
        O.ROOT_OFFENDER_ID,
        O.OFFENDER_ID_DISPLAY OFFENDER_NO,
        O.FIRST_NAME,
        CONCAT(O.middle_name, CASE WHEN middle_name_2 IS NOT NULL THEN concat(' ', O.middle_name_2) ELSE '' END) MIDDLE_NAME,
        O.LAST_NAME,
        B.AGY_LOC_ID AGENCY_ID,
        B.LIVING_UNIT_ID ASSIGNED_LIVING_UNIT_ID,
        CASE WHEN B.ACTIVE_FLAG = 'Y' THEN 1 ELSE 0 END ACTIVE_FLAG,
        B.IN_OUT_STATUS,
        B.STATUS_REASON,
        (SELECT MAX(OI.OFFENDER_IMAGE_ID)
                FROM OFFENDER_IMAGES OI
                WHERE OI.ACTIVE_FLAG = 'Y'
        AND IMAGE_OBJECT_TYPE = 'OFF_BKG'
        AND OI.OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID
                AND OI.IMAGE_VIEW_TYPE = 'FACE'
        AND OI.ORIENTATION_TYPE = 'FRONT') AS FACIAL_IMAGE_ID,
        O.BIRTH_DATE DATE_OF_BIRTH,
        O.BIRTH_PLACE,
        O.BIRTH_COUNTRY_CODE,
        B.ASSIGNED_STAFF_ID AS ASSIGNED_OFFICER_ID,
        B.BOOKING_BEGIN_DATE AS RECEPTION_DATE
        FROM OFFENDER_BOOKINGS B
        INNER JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
        WHERE B.OFFENDER_BOOK_ID = :bookingId
    """,
  ),

  FIND_OFFENDER(
    """
        SELECT B.OFFENDER_BOOK_ID BOOKING_ID,
        B.BOOKING_NO,
        O.OFFENDER_ID,
        O.ROOT_OFFENDER_ID,
        O.OFFENDER_ID_DISPLAY OFFENDER_NO,
        O.FIRST_NAME,
        CONCAT(O.middle_name, CASE WHEN middle_name_2 IS NOT NULL THEN concat(' ', O.middle_name_2) ELSE '' END) MIDDLE_NAME,
        O.LAST_NAME,
        B.AGY_LOC_ID AGENCY_ID,
        B.LIVING_UNIT_ID ASSIGNED_LIVING_UNIT_ID,
        CASE WHEN B.ACTIVE_FLAG = 'Y' THEN 1 ELSE 0 END ACTIVE_FLAG,
        B.IN_OUT_STATUS,
        B.STATUS_REASON,
        (SELECT MAX(OI.OFFENDER_IMAGE_ID)
                FROM OFFENDER_IMAGES OI
                WHERE OI.ACTIVE_FLAG = 'Y'
        AND IMAGE_OBJECT_TYPE = 'OFF_BKG'
        AND OI.OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID
                AND OI.IMAGE_VIEW_TYPE = 'FACE'
        AND OI.ORIENTATION_TYPE = 'FRONT') AS FACIAL_IMAGE_ID,
        O.BIRTH_DATE DATE_OF_BIRTH,
        O.BIRTH_PLACE,
        O.BIRTH_COUNTRY_CODE,
        B.ASSIGNED_STAFF_ID AS ASSIGNED_OFFICER_ID,
        B.BOOKING_BEGIN_DATE AS RECEPTION_DATE
        FROM OFFENDERS O
        LEFT JOIN OFFENDER_BOOKINGS B ON B.OFFENDER_ID = O.OFFENDER_ID
                WHERE O.OFFENDER_ID_DISPLAY = :offenderNo
        ORDER BY B.BOOKING_SEQ
    """,
  ),

  FIND_BASIC_INMATE_DETAIL(
    """
        SELECT B.OFFENDER_BOOK_ID,
        B.BOOKING_NO,
        O.OFFENDER_ID,
        O.ROOT_OFFENDER_ID,
        O.OFFENDER_ID_DISPLAY,
        O.FIRST_NAME,
        CONCAT(O.middle_name, CASE WHEN middle_name_2 IS NOT NULL THEN concat(' ', O.middle_name_2) ELSE '' END) MIDDLE_NAME,
        O.LAST_NAME,
        O.BIRTH_DATE,
        B.AGY_LOC_ID,
        B.LIVING_UNIT_ID,
        B.ACTIVE_FLAG
        FROM OFFENDER_BOOKINGS B
        INNER JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
                WHERE B.OFFENDER_BOOK_ID = :bookingId
    """,
  ),

  FIND_BASIC_INMATE_DETAIL_BY_OFFENDER_NO(
    """
        SELECT OB.OFFENDER_BOOK_ID  as BOOKING_ID,
        OB.BOOKING_NO,
        OB.AGY_LOC_ID        as AGENCY_ID,
        OB.LIVING_UNIT_ID    as ASSIGNED_LIVING_UNIT_ID,
        I.DESCRIPTION        as ASSIGNED_LIVING_UNIT_DESC,
        O.OFFENDER_ID_DISPLAY as OFFENDER_NO,
        O.FIRST_NAME,
        CONCAT(O.middle_name, CASE WHEN middle_name_2 IS NOT NULL THEN concat(' ', O.middle_name_2) ELSE '' END) MIDDLE_NAME,
        O.LAST_NAME,
        O.BIRTH_DATE as DATE_OF_BIRTH
        FROM OFFENDER_BOOKINGS OB
        INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID
        LEFT JOIN AGENCY_INTERNAL_LOCATIONS I ON OB.LIVING_UNIT_ID = I.INTERNAL_LOCATION_ID
                WHERE O.OFFENDER_ID_DISPLAY IN (:offenders)
        AND OB.BOOKING_SEQ = :bookingSeq
    """,
  ),

  FIND_ASSIGNED_LIVING_UNIT(
    """
        SELECT B.AGY_LOC_ID,
        B.LIVING_UNIT_ID,
        I.DESCRIPTION LIVING_UNIT_DESCRIPTION,
        AL.DESCRIPTION as AGENCY_NAME
        FROM OFFENDER_BOOKINGS B
        LEFT JOIN AGENCY_INTERNAL_LOCATIONS I ON B.LIVING_UNIT_ID = I.INTERNAL_LOCATION_ID
                LEFT JOIN AGENCY_LOCATIONS AL ON AL.AGY_LOC_ID = B.AGY_LOC_ID
                WHERE B.OFFENDER_BOOK_ID = :bookingId
    """,
  ),

  FIND_ALL_INMATES(
    """
        SELECT
        OB.OFFENDER_BOOK_ID,
        OB.BOOKING_NO,
        O.OFFENDER_ID_DISPLAY,
        OB.AGY_LOC_ID,
        O.FIRST_NAME,
        O.MIDDLE_NAME,
        O.LAST_NAME,
        O.BIRTH_DATE,
        NULL AS ALERT_TYPES,
        NULL AS ALIASES,
        OB.LIVING_UNIT_ID,
        AIL.DESCRIPTION as LIVING_UNIT_DESC,
        (
                SELECT MAX(OI.OFFENDER_IMAGE_ID)
                        FROM OFFENDER_IMAGES OI
                        WHERE OI.ACTIVE_FLAG = 'Y'
        AND IMAGE_OBJECT_TYPE = 'OFF_BKG'
        AND OI.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID
                AND OI.IMAGE_VIEW_TYPE = 'FACE'
        AND OI.ORIENTATION_TYPE = 'FRONT'
        ) AS FACE_IMAGE_ID,
        NULL AS ASSIGNED_OFFICER_ID,
        IST.BAND_CODE as BAND_CODE,
        OIS.IMPRISONMENT_STATUS as IMPRISONMENT_STATUS
        FROM OFFENDER_BOOKINGS OB
            INNER JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID
            LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
            LEFT JOIN (
                SELECT OFFENDER_BOOK_ID, IMPRISONMENT_STATUS FROM OFFENDER_IMPRISON_STATUSES OIST WHERE OIST.IMPRISON_STATUS_SEQ = 
                (SELECT MAX(IMPRISON_STATUS_SEQ) FROM OFFENDER_IMPRISON_STATUSES WHERE LATEST_STATUS = 'Y' AND OFFENDER_BOOK_ID = OIST.OFFENDER_BOOK_ID)
            ) OIS ON OIS.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID
            LEFT JOIN IMPRISONMENT_STATUSES IST ON IST.IMPRISONMENT_STATUS = OIS.IMPRISONMENT_STATUS
        WHERE OB.ACTIVE_FLAG = 'Y' AND OB.BOOKING_SEQ = 1
    """,
  ),

  ALERT_FILTER(
    """
        EXISTS(SELECT 1 FROM OFFENDER_ALERTS A
                WHERE OB.OFFENDER_BOOK_ID = A.OFFENDER_BOOK_ID
                AND A.ALERT_STATUS = 'ACTIVE'
                AND (A.EXPIRY_DATE IS NULL OR A.EXPIRY_DATE > SYSDATE)
                AND A.ALERT_CODE IN (:alerts))
    """,
  ),

  CASELOAD_FILTER(
    """
        EXISTS (select 1 from CASELOAD_AGENCY_LOCATIONS C WHERE OB.AGY_LOC_ID = C.AGY_LOC_ID AND C.CASELOAD_ID IN (:caseLoadId))
    """,
  ),

  ACTIVE_BOOKING_FILTER(
    """
        OB.ACTIVE_FLAG = 'Y'
    """,
  ),

  ASSESSMENT_CASELOAD_FILTER(
    """
        EXISTS (SELECT 1 FROM CASELOAD_AGENCY_LOCATIONS C, OFFENDER_BOOKINGS OB
                WHERE OB.OFFENDER_BOOK_ID = OFF_ASS.OFFENDER_BOOK_ID
                AND OB.AGY_LOC_ID = C.AGY_LOC_ID AND C.CASELOAD_ID IN (:caseLoadId))
    """,
  ),

  GET_ALERT_CODES_FOR_BOOKINGS(
    """
        SELECT OFFENDER_BOOK_ID AS BOOKING_ID,
        ALERT_CODE
        FROM OFFENDER_ALERTS
                WHERE OFFENDER_BOOK_ID IN (:bookingIds)
        AND ALERT_STATUS = 'ACTIVE'
        AND (EXPIRY_DATE IS NULL OR EXPIRY_DATE > :cutoffDate)
    """,
  ),

  FIND_INMATES_BY_LOCATION(
    """
        SELECT B.OFFENDER_BOOK_ID,
        B.BOOKING_NO,
        O.OFFENDER_ID_DISPLAY,
        B.AGY_LOC_ID,
        O.FIRST_NAME,
        O.MIDDLE_NAME,
        O.LAST_NAME,
        O.BIRTH_DATE,
        NULL AS ALIASES,
        B.LIVING_UNIT_ID,
        (
                SELECT MAX(OI.OFFENDER_IMAGE_ID)
                        FROM OFFENDER_IMAGES OI
                        WHERE OI.ACTIVE_FLAG = 'Y'
        AND IMAGE_OBJECT_TYPE = 'OFF_BKG'
        AND OI.OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID
                AND OI.IMAGE_VIEW_TYPE = 'FACE'
        AND OI.ORIENTATION_TYPE = 'FRONT'
        ) AS FACE_IMAGE_ID
                FROM OFFENDER_BOOKINGS B
        INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON C.CASELOAD_ID = :caseLoadId AND B.AGY_LOC_ID = C.AGY_LOC_ID
        LEFT JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
                WHERE B.ACTIVE_FLAG = 'Y' AND B.BOOKING_SEQ = 1
        AND B.LIVING_UNIT_ID IN (
            WITH LOCATION_SUBQUERY (INTERNAL_LOCATION_ID, PARENT_INTERNAL_LOCATION_ID) AS (
                SELECT INTERNAL_LOCATION_ID, NULL FROM AGENCY_INTERNAL_LOCATIONS WHERE INTERNAL_LOCATION_ID = :locationId
                UNION ALL
                SELECT a.INTERNAL_LOCATION_ID, a.PARENT_INTERNAL_LOCATION_ID FROM AGENCY_INTERNAL_LOCATIONS a, LOCATION_SUBQUERY l WHERE l.INTERNAL_LOCATION_ID = a.PARENT_INTERNAL_LOCATION_ID
            ) SELECT INTERNAL_LOCATION_ID FROM LOCATION_SUBQUERY
        )
    """,
  ),

  FIND_INMATES_OF_LOCATION_LIST(
    """
        SELECT B.OFFENDER_BOOK_ID AS BOOKING_ID,
        O.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
        O.FIRST_NAME,
        O.LAST_NAME,
        B.LIVING_UNIT_ID AS LOCATION_ID,
        AIL.DESCRIPTION AS LOCATION_DESCRIPTION
        FROM OFFENDER_BOOKINGS B
        INNER JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
                INNER JOIN AGENCY_INTERNAL_LOCATIONS AIL ON B.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
                WHERE B.ACTIVE_FLAG = 'Y'
        AND EXISTS (select 1 from CASELOAD_AGENCY_LOCATIONS C WHERE B.AGY_LOC_ID = C.AGY_LOC_ID AND C.CASELOAD_ID IN (:caseLoadIds))
        AND AIL.INTERNAL_LOCATION_ID in (:locations)
        AND B.AGY_LOC_ID = :agencyId
    """,
  ),

  FIND_PHYSICAL_CHARACTERISTICS_BY_BOOKING(
    """
        SELECT PT.PROFILE_TYPE AS TYPE,
        PT.DESCRIPTION AS CHARACTERISTIC,
        COALESCE(PC.DESCRIPTION, P.PROFILE_CODE) AS DETAIL,
        NULL AS IMAGE_ID
        FROM OFFENDER_PROFILE_DETAILS P
        INNER JOIN OFFENDER_BOOKINGS B ON B.OFFENDER_BOOK_ID = P.OFFENDER_BOOK_ID
                INNER JOIN PROFILE_TYPES PT ON PT.PROFILE_TYPE = P.PROFILE_TYPE
                AND PT.PROFILE_CATEGORY = 'PA' AND PT.ACTIVE_FLAG = 'Y'
        LEFT JOIN PROFILE_CODES PC ON PC.PROFILE_TYPE = PT.PROFILE_TYPE AND PC.PROFILE_CODE = P.PROFILE_CODE
                WHERE P.OFFENDER_BOOK_ID = :bookingId AND P.PROFILE_CODE IS NOT NULL
        ORDER BY P.LIST_SEQ
    """,
  ),

  FIND_PROFILE_INFORMATION_BY_BOOKING(
    """
        SELECT PT.PROFILE_TYPE AS TYPE,
        PT.DESCRIPTION AS question,
        COALESCE(PC.DESCRIPTION, P.PROFILE_CODE) AS result_value
        FROM OFFENDER_PROFILE_DETAILS P
        INNER JOIN OFFENDER_BOOKINGS B ON B.OFFENDER_BOOK_ID = P.OFFENDER_BOOK_ID
                INNER JOIN PROFILE_TYPES PT ON PT.PROFILE_TYPE = P.PROFILE_TYPE
                AND PT.PROFILE_CATEGORY = 'PI' AND (PT.ACTIVE_FLAG = 'Y' OR PT.PROFILE_TYPE = 'RELF')
        LEFT JOIN PROFILE_CODES PC ON PC.PROFILE_TYPE = PT.PROFILE_TYPE
                AND PC.PROFILE_CODE = P.PROFILE_CODE
                WHERE P.OFFENDER_BOOK_ID = :bookingId AND P.PROFILE_CODE IS NOT NULL
        ORDER BY P.LIST_SEQ
    """,
  ),

  GET_OFFENDER_IDENTIFIERS_BY_BOOKING(
    """
        SELECT
        IDENTIFIER_TYPE "TYPE",
        OI.IDENTIFIER "VALUE",
        OB.OFFENDER_BOOK_ID BOOKING_ID,
        O.OFFENDER_ID_DISPLAY OFFENDER_NO,
        ISSUED_AUTHORITY_TEXT,
        ISSUED_DATE,
        OI.CASELOAD_TYPE,
        OI.OFFENDER_ID_SEQ,
        OI.CREATE_DATETIME AS WHEN_CREATED
        FROM OFFENDER_IDENTIFIERS OI
        JOIN OFFENDERS O ON O.OFFENDER_ID = OI.OFFENDER_ID
        JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_ID = O.OFFENDER_ID
        WHERE OB.OFFENDER_BOOK_ID = :bookingId
        AND OI.OFFENDER_ID_SEQ = (SELECT MAX(OFFENDER_ID_SEQ)
                FROM OFFENDER_IDENTIFIERS OI2
                WHERE OI2.OFFENDER_ID = OI.OFFENDER_ID
                AND OI2.IDENTIFIER_TYPE = OI.IDENTIFIER_TYPE )
    """,
  ),

  GET_OFFENDER_IDENTIFIERS_BY_OFFENDER_ID(
    """
        SELECT
        IDENTIFIER_TYPE "TYPE",
        OI.IDENTIFIER "VALUE",
        O.OFFENDER_ID_DISPLAY OFFENDER_NO,
        ISSUED_AUTHORITY_TEXT,
        ISSUED_DATE,
        OI.CASELOAD_TYPE,
        OI.OFFENDER_ID_SEQ,
        OI.CREATE_DATETIME AS WHEN_CREATED
        FROM OFFENDER_IDENTIFIERS OI
        JOIN OFFENDERS O ON O.OFFENDER_ID = OI.OFFENDER_ID
        WHERE O.OFFENDER_ID = :offenderId
        AND OI.OFFENDER_ID_SEQ = (SELECT MAX(OFFENDER_ID_SEQ)
                FROM OFFENDER_IDENTIFIERS OI2
                WHERE OI2.OFFENDER_ID = OI.OFFENDER_ID
                AND OI2.IDENTIFIER_TYPE = OI.IDENTIFIER_TYPE )
    """,
  ),

  FIND_PHYSICAL_MARKS_BY_BOOKING(
    """
        SELECT (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = M.MARK_TYPE AND DOMAIN='MARK_TYPE') AS TYPE,
        (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = M.SIDE_CODE AND DOMAIN='SIDE') AS SIDE,
        (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = M.BODY_PART_CODE AND DOMAIN='BODY_PART') AS BODY_PART,
        (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = M.PART_ORIENTATION_CODE AND DOMAIN='PART_ORIENT') AS ORENTIATION,
        M.COMMENT_TEXT,
        (SELECT MAX(I.OFFENDER_IMAGE_ID)
                FROM OFFENDER_IMAGES I
                WHERE B.OFFENDER_BOOK_ID = I.OFFENDER_BOOK_ID
                AND I.ACTIVE_FLAG = 'Y'
        AND M.MARK_TYPE = I.IMAGE_VIEW_TYPE
                AND M.BODY_PART_CODE = I.ORIENTATION_TYPE
                AND M.ID_MARK_SEQ = I.IMAGE_OBJECT_ID
        ) AS IMAGE_ID
                FROM OFFENDER_IDENTIFYING_MARKS M
        JOIN OFFENDER_BOOKINGS B ON B.OFFENDER_BOOK_ID = M.OFFENDER_BOOK_ID
        WHERE B.OFFENDER_BOOK_ID = :bookingId
        AND M.BODY_PART_CODE != 'CONV'
    """,
  ),

  FIND_PERSONAL_CARE_NEEDS_BY_BOOKING(
    """
        SELECT OHP.OFFENDER_HEALTH_PROBLEM_ID AS PERSONAL_CARE_NEED_ID,
        OHP.PROBLEM_TYPE,
        OHP.PROBLEM_CODE,
        OHP.PROBLEM_STATUS,
        OHP.DESCRIPTION as COMMENT_TEXT,
        ref.DESCRIPTION as PROBLEM_DESCRIPTION,
        OHP.START_DATE,
        OHP.END_DATE
        FROM OFFENDER_HEALTH_PROBLEMS OHP
        INNER JOIN REFERENCE_CODES ref
                ON ref.CODE = OHP.PROBLEM_CODE
                AND ref.DOMAIN = 'HEALTH_PBLM'
        WHERE OHP.START_DATE <= sysdate
                AND (OHP.END_DATE >= sysdate or OHP.END_DATE is null)
        AND OHP.PROBLEM_TYPE in (:problemCodes)
        AND OHP.OFFENDER_BOOK_ID = :bookingId
    """,
  ),

  FIND_PERSONAL_CARE_NEEDS_BY_OFFENDER(
    """
        SELECT OHP.OFFENDER_HEALTH_PROBLEM_ID AS PERSONAL_CARE_NEED_ID,
        OHP.PROBLEM_TYPE,
        OHP.PROBLEM_CODE,
        OHP.PROBLEM_STATUS,
        OHP.DESCRIPTION       as COMMENT_TEXT,
        REF.DESCRIPTION       as PROBLEM_DESCRIPTION,
        OHP.START_DATE,
        OHP.END_DATE,
        O.OFFENDER_ID_DISPLAY AS OFFENDER_NO
        FROM OFFENDER_HEALTH_PROBLEMS OHP
        JOIN REFERENCE_CODES REF ON REF.CODE = OHP.PROBLEM_CODE AND REF.DOMAIN = 'HEALTH_PBLM'
        JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OHP.OFFENDER_BOOK_ID
        JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID
        WHERE OHP.START_DATE <= sysdate
                AND (OHP.END_DATE >= sysdate or OHP.END_DATE is null)
        AND OHP.PROBLEM_TYPE in (:problemCodes)
        AND O.OFFENDER_ID_DISPLAY IN (:offenderNos)
        ORDER BY OFFENDER_NO, OHP.START_DATE """,
  ),

  FIND_REASONABLE_ADJUSTMENTS_BY_BOOKING(
    """
        SELECT OHP.OFFENDER_HEALTH_PROBLEM_ID AS PERSONAL_CARE_NEED_ID,
        OHP.OFFENDER_BOOK_ID,
        OMT.TREATMENT_CODE,
        RC.DESCRIPTION AS TREATMENT_DESCRIPTION,
        OMT.COMMENT_TEXT,
        OMT.START_DATE,
        OMT.END_DATE,
        OMT.AGY_LOC_ID AS AGENCY_ID,
        AL.DESCRIPTION AS AGENCY_DESCRIPTION
        FROM OFFENDER_MEDICAL_TREATMENTS OMT
        JOIN OFFENDER_HEALTH_PROBLEMS OHP
          ON OHP.OFFENDER_HEALTH_PROBLEM_ID = OMT.OFFENDER_HEALTH_PROBLEM_ID
                  AND OHP.START_DATE <= sysdate
                  AND (OHP.END_DATE >= sysdate or OHP.END_DATE is null)
        JOIN OFFENDER_BOOKINGS OBT
          ON OBT.OFFENDER_BOOK_ID = OHP.OFFENDER_BOOK_ID
                  JOIN REFERENCE_CODES RC ON OMT.TREATMENT_CODE = RC.CODE AND RC.DOMAIN = 'HEALTH_TREAT'
        LEFT OUTER JOIN AGENCY_LOCATIONS AL ON AL.AGY_LOC_ID = OMT.AGY_LOC_ID
        WHERE OMT.TREATMENT_CODE in (:treatmentCodes)
        AND OHP.OFFENDER_BOOK_ID = :bookingId
    """,
  ),

  FIND_PHYSICAL_ATTRIBUTES_BY_BOOKING(
    """
        SELECT O.SEX_CODE,
        O.RACE_CODE,
        RCS.DESCRIPTION AS GENDER,
        RCE.DESCRIPTION AS ETHNICITY,
        P.HEIGHT_FT,
        P.HEIGHT_IN,
        P.HEIGHT_CM,
        P.WEIGHT_LBS,
        P.WEIGHT_KG
        FROM OFFENDER_BOOKINGS B
        LEFT JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
                LEFT JOIN OFFENDER_PHYSICAL_ATTRIBUTES P ON B.OFFENDER_BOOK_ID = P.OFFENDER_BOOK_ID AND P.ATTRIBUTE_SEQ = 1
        LEFT JOIN REFERENCE_CODES RCE ON O.RACE_CODE = RCE.CODE AND RCE.DOMAIN = 'ETHNICITY'
        LEFT JOIN REFERENCE_CODES RCS ON O.SEX_CODE = RCS.CODE AND RCS.DOMAIN = 'SEX'
        WHERE B.OFFENDER_BOOK_ID = :bookingId
    """,
  ),

  FIND_ACTIVE_APPROVED_ASSESSMENT(
    """
        SELECT
        OFF_ASS.OFFENDER_BOOK_ID AS BOOKING_ID,
        ASS.ASSESSMENT_CODE,
        ASS.DESCRIPTION          AS ASSESSMENT_DESCRIPTION,
        OFF_ASS.REVIEW_SUP_LEVEL_TYPE,
        REF_REVIEW.DESCRIPTION   AS REVIEW_SUP_LEVEL_TYPE_DESC,
        OFF_ASS.OVERRIDED_SUP_LEVEL_TYPE,
        REF_OVERRIDE.DESCRIPTION AS OVERRIDED_SUP_LEVEL_TYPE_DESC,
        OFF_ASS.CALC_SUP_LEVEL_TYPE,
        REF_CAL_SUP.DESCRIPTION  AS CALC_SUP_LEVEL_TYPE_DESC,
        ASS.CASELOAD_TYPE,
        CASE WHEN ASS.CELL_SHARING_ALERT_FLAG = 'Y' THEN 1 ELSE 0 END AS CELL_SHARING_ALERT_FLAG,
        OFF_ASS.ASSESS_STATUS,
        OFF_ASS.ASSESSMENT_DATE,
        OFF_ASS.ASSESSMENT_SEQ,
        OFF_ASS.NEXT_REVIEW_DATE,
        OFF_ASS.ASSESS_COMMENT_TEXT
        FROM OFFENDER_ASSESSMENTS OFF_ASS
        JOIN ASSESSMENTS ASS ON OFF_ASS.ASSESSMENT_TYPE_ID = ASS.ASSESSMENT_ID
        LEFT JOIN REFERENCE_CODES REF_REVIEW
                ON OFF_ASS.REVIEW_SUP_LEVEL_TYPE = REF_REVIEW.CODE AND REF_REVIEW.DOMAIN = 'SUP_LVL_TYPE'
        LEFT JOIN REFERENCE_CODES REF_OVERRIDE
                ON OFF_ASS.OVERRIDED_SUP_LEVEL_TYPE = REF_OVERRIDE.CODE AND REF_OVERRIDE.DOMAIN = 'SUP_LVL_TYPE'
        LEFT JOIN REFERENCE_CODES REF_CAL_SUP
                ON OFF_ASS.CALC_SUP_LEVEL_TYPE = REF_CAL_SUP.CODE AND REF_CAL_SUP.DOMAIN = 'SUP_LVL_TYPE'
        WHERE OFF_ASS.ASSESS_STATUS = 'A'
        AND OFF_ASS.OFFENDER_BOOK_ID IN (:bookingIds)
        AND (:assessmentCode IS NULL OR ASS.ASSESSMENT_CODE = :assessmentCode)
    """,
  ),

  FIND_APPROVED_ASSESSMENT_BY_OFFENDER_NO(
    """
        SELECT
        O.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
        OFF_ASS.OFFENDER_BOOK_ID AS BOOKING_ID,
        ASS.ASSESSMENT_CODE,
        ASS.DESCRIPTION          AS ASSESSMENT_DESCRIPTION,
        OFF_ASS.REVIEW_SUP_LEVEL_TYPE,
        REF_REVIEW.DESCRIPTION   AS REVIEW_SUP_LEVEL_TYPE_DESC,
        OFF_ASS.OVERRIDED_SUP_LEVEL_TYPE,
        REF_OVERRIDE.DESCRIPTION AS OVERRIDED_SUP_LEVEL_TYPE_DESC,
        OFF_ASS.CALC_SUP_LEVEL_TYPE,
        REF_CAL_SUP.DESCRIPTION  AS CALC_SUP_LEVEL_TYPE_DESC,
        ASS.CASELOAD_TYPE,
        CASE WHEN ASS.CELL_SHARING_ALERT_FLAG = 'Y' THEN 1 ELSE 0 END AS CELL_SHARING_ALERT_FLAG,
        OFF_ASS.ASSESS_STATUS,
        OFF_ASS.ASSESSMENT_DATE,
        OFF_ASS.ASSESSMENT_SEQ,
        OFF_ASS.NEXT_REVIEW_DATE,
        OFF_ASS.EVALUATION_DATE AS APPROVAL_DATE,
        OFF_ASS.ASSESS_COMMENT_TEXT,
        OFF_ASS.ASSESS_STAFF_ID,
        OFF_ASS.CREATION_USER,
        OFF_ASS.ASSESSMENT_CREATE_LOCATION
        FROM OFFENDER_ASSESSMENTS OFF_ASS
        JOIN ASSESSMENTS ASS ON OFF_ASS.ASSESSMENT_TYPE_ID = ASS.ASSESSMENT_ID
        JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OFF_ASS.OFFENDER_BOOK_ID
        JOIN OFFENDERS O ON OB.OFFENDER_ID = O.OFFENDER_ID
        LEFT JOIN REFERENCE_CODES REF_REVIEW
                ON OFF_ASS.REVIEW_SUP_LEVEL_TYPE = REF_REVIEW.CODE AND REF_REVIEW.DOMAIN = 'SUP_LVL_TYPE'
        LEFT JOIN REFERENCE_CODES REF_OVERRIDE
                ON OFF_ASS.OVERRIDED_SUP_LEVEL_TYPE = REF_OVERRIDE.CODE AND REF_OVERRIDE.DOMAIN = 'SUP_LVL_TYPE'
        LEFT JOIN REFERENCE_CODES REF_CAL_SUP
                ON OFF_ASS.CALC_SUP_LEVEL_TYPE = REF_CAL_SUP.CODE AND REF_CAL_SUP.DOMAIN = 'SUP_LVL_TYPE'
        WHERE (:assessmentCode IS NULL OR ASS.ASSESSMENT_CODE = :assessmentCode)
        AND O.OFFENDER_ID_DISPLAY IN (:offenderNos)
    """,
  ),

  GET_UNCATEGORISED(
    """
        SELECT
        at_offender.offender_id_display       AS OFFENDER_NO,
        at_offender_booking.offender_book_id  AS BOOKING_ID,
        at_offender.last_name,
        at_offender.first_name,
        off_ass.assessment_seq,
        off_ass.assessment_date,
        off_ass.next_review_date,
        off_ass.assess_status,
        sm.first_name                         AS CATEGORISER_FIRST_NAME,
        sm.last_name                          AS CATEGORISER_LAST_NAME,
        COALESCE(off_ass.review_sup_level_type,
                off_ass.overrided_sup_level_type,
                off_ass.calc_sup_level_type) AS CATEGORY
        FROM
        offenders at_offender
                INNER JOIN offender_bookings at_offender_booking ON at_offender.offender_id = at_offender_booking.offender_id
                AND at_offender_booking.active_flag = 'Y'
        AND at_offender_booking.in_out_status IN ('IN', 'OUT')
        LEFT JOIN offender_assessments off_ass ON off_ass.offender_book_id = at_offender_booking.offender_book_id
                AND off_ass.assessment_type_id = :assessmentId
        AND off_ass.assess_status IN ('A','P')
        LEFT JOIN staff_members sm ON off_ass.assess_staff_id = sm.staff_id
                WHERE at_offender_booking.agy_loc_id = :agencyId
    """,
  ),

  GET_APPROVED_CATEGORISED(
    """
        select
        o.offender_id_display as offender_no,
        ob.offender_book_id as booking_id,
        o.last_name,
        o.first_name,
        off_ass.assessment_seq,
        off_ass.assessment_date,
        off_ass.evaluation_date as approval_date,
        COALESCE(off_ass.review_sup_level_type, off_ass.overrided_sup_level_type, off_ass.calc_sup_level_type) as category,
        sm.first_name as categoriser_first_name,
        sm.last_name as categoriser_last_name,
        sm_a.first_name as approver_first_name,
        sm_a.last_name as approver_last_name

        from
        offender_assessments off_ass
                join offender_bookings ob on ob.offender_book_id = off_ass.offender_book_id
        join offenders o on ob.offender_id = o.offender_id
        join staff_members sm on off_ass.assess_staff_id = sm.staff_id
        join staff_members sm_a ON sm_a.staff_id  = (select su.staff_id from staff_user_accounts su where off_ass.modify_user_id = su.username)

        where off_ass.assessment_create_location = :agencyId
        and off_ass.evaluation_date >= :cutOffDate
        and off_ass.assess_status = :assessStatus
        and off_ass.assessment_type_id = :assessmentId
    """,
  ),

  GET_RECATEGORISE(
    """
        select
        o.offender_id_display    as offender_no,
        ob.offender_book_id      as booking_id,
        o.last_name,
        o.first_name,
        off_ass.assessment_seq,
        off_ass.assessment_date,
        off_ass.evaluation_date as approval_date,
        COALESCE(off_ass.review_sup_level_type, off_ass.overrided_sup_level_type, off_ass.calc_sup_level_type) as category,
        off_ass.assess_status,
        off_ass.next_review_date
        from
        offender_assessments off_ass
                join offender_bookings ob on ob.offender_book_id = off_ass.offender_book_id
        join offenders         o  on ob.offender_id      = o.offender_id
        where ob.agy_loc_id = :agencyId
        and off_ass.assessment_type_id = :assessmentId
        order by off_ass.next_review_date asc
    """,
  ),

  GET_OFFENDER_CATEGORISATIONS(
    """
        select
        o.offender_id_display as offender_no,
        ob.offender_book_id as booking_id,
        o.last_name,
        o.first_name,
        off_ass.assessment_seq,
        off_ass.next_review_date,
        off_ass.assessment_date,
        off_ass.evaluation_date as approval_date,
        COALESCE(off_ass.review_sup_level_type, off_ass.overrided_sup_level_type, off_ass.calc_sup_level_type) as category,
        off_ass.assess_status,
        sm.first_name as categoriser_first_name,
        sm.last_name as categoriser_last_name,
        sm_a.first_name as approver_first_name,
        sm_a.last_name as approver_last_name

        from
        offender_assessments off_ass
                join offender_bookings ob on ob.offender_book_id = off_ass.offender_book_id
        join offenders o on ob.offender_id = o.offender_id
        join staff_members sm on off_ass.assess_staff_id = sm.staff_id
        left join staff_user_accounts sua on sua.username = off_ass.modify_user_id
                left join staff_members sm_a ON sm_a.staff_id = sua.staff_id

                where off_ass.offender_book_id in (:bookingIds)
        and (:agencyId is null or off_ass.assessment_create_location = :agencyId)
        -- included to ensure only authorised bookings are returned (unless client has system role)
        and off_ass.assessment_type_id = :assessmentId
    """,
  ),

  GET_CATEGORY_ASSESSMENT_ID(
    """
        select assessment_id from assessments a where a.assessment_class='TYPE' and a.assessment_code='CATEGORY'
    """,
  ),

  INSERT_CATEGORY(
    """
        Insert into OFFENDER_ASSESSMENTS
        (OFFENDER_BOOK_ID,
        ASSESSMENT_SEQ,
        ASSESSMENT_DATE,
        ASSESSMENT_TYPE_ID,
        SCORE,
        ASSESS_STATUS,
        CALC_SUP_LEVEL_TYPE,
        ASSESS_STAFF_ID,
        ASSESSOR_STAFF_ID,
        ASSESS_COMMENT_TEXT,
        NEXT_REVIEW_DATE,
        ASSESS_COMMITTE_CODE,
        CREATION_DATE,
        CREATION_USER,
        ASSESSMENT_CREATE_LOCATION,
        PLACE_AGY_LOC_ID
        )
        VALUES
        (:bookingId,
        :seq,
        :assessmentDate,
        :assessmentTypeId,
        (select s.MAX_SCORE from assessment_supervisions s where s.assessment_id = :assessmentTypeId and s.supervision_level_type = :category),
        :assessStatus,  -- P  (AWAITING_APPROVAL)
        :category,
        :assessStaffId,
        :assessStaffId,
        :assessComment,
        :reviewDate,
        :assessCommitteeCode,
        :dateTime,
        :userId,
        :agencyId,
        :placementAgencyId
        )
    """,
  ),

  UPDATE_CATEGORY(
    """
        update OFFENDER_ASSESSMENTS set
        ASSESSMENT_DATE = :assessmentDate,
        CALC_SUP_LEVEL_TYPE = COALESCE(:category, CALC_SUP_LEVEL_TYPE),
        ASSESS_COMMENT_TEXT = COALESCE(:assessComment, ASSESS_COMMENT_TEXT),
        NEXT_REVIEW_DATE = COALESCE(:reviewDate, NEXT_REVIEW_DATE),
        ASSESS_COMMITTE_CODE = COALESCE(:assessCommitteeCode, ASSESS_COMMITTE_CODE)
        where OFFENDER_BOOK_ID=:bookingId
        and ASSESSMENT_SEQ=:seq
        and ASSESSMENT_TYPE_ID=:assessmentTypeId
        and ASSESS_STATUS='P'
    """,
  ),

  APPROVE_CATEGORY(
    """
        update OFFENDER_ASSESSMENTS set
        ASSESS_STATUS=:assessStatus,
        EVALUATION_DATE=:evaluationDate,
        EVALUATION_RESULT_CODE=:evaluationResultCode,
        REVIEW_SUP_LEVEL_TYPE=:category,
        REVIEW_SUP_LEVEL_TEXT=:approvedCategoryComment,
        REVIEW_COMMITTE_CODE=:reviewCommitteeCode,
        COMMITTE_COMMENT_TEXT=:committeeCommentText,
        NEXT_REVIEW_DATE=COALESCE(:nextReviewDate, NEXT_REVIEW_DATE),
        REVIEW_PLACE_AGY_LOC_ID=:approvedPlacementAgencyId,
        REVIEW_PLACEMENT_TEXT=:approvedPlacementText
                where OFFENDER_BOOK_ID=:bookingId
        and ASSESSMENT_SEQ=:seq
        and ASSESSMENT_TYPE_ID=:assessmentTypeId
        and ASSESS_STATUS='P'
    """,
  ),

  REJECT_CATEGORY(
    """
        update OFFENDER_ASSESSMENTS set
        EVALUATION_DATE=:evaluationDate,
        EVALUATION_RESULT_CODE=:evaluationResultCode,
        REVIEW_COMMITTE_CODE=:reviewCommitteeCode,
        COMMITTE_COMMENT_TEXT=:committeeCommentText
                where OFFENDER_BOOK_ID=:bookingId
        and ASSESSMENT_SEQ=:seq
        and ASSESSMENT_TYPE_ID=:assessmentTypeId
        and ASSESS_STATUS='P'
    """,
  ),

  CATEGORY_SET_STATUS(
    """
        update OFFENDER_ASSESSMENTS set
        ASSESS_STATUS=:assessStatus
                where OFFENDER_BOOK_ID=:bookingId
        and ASSESSMENT_SEQ in (:seq)
    """,
  ),

  UPDATE_CATEORY_NEXT_REVIEW_DATE(
    """
        update OFFENDER_ASSESSMENTS
                set
        NEXT_REVIEW_DATE=:nextReviewDate
                where OFFENDER_BOOK_ID=:bookingId
        and ASSESSMENT_SEQ = (SELECT MAX (OA.ASSESSMENT_SEQ) FROM OFFENDER_ASSESSMENTS OA
                WHERE OA.OFFENDER_BOOK_ID = :bookingId and OA.ASSESS_STATUS = 'A' and OA.ASSESSMENT_TYPE_ID=:assessmentTypeId)

    """,
  ),

  OFFENDER_ASSESSMENTS_SEQ_MAX(
    """
        SELECT MAX (ASSESSMENT_SEQ) FROM OFFENDER_ASSESSMENTS OA WHERE OA.OFFENDER_BOOK_ID = :bookingId
    """,
  ),

  GET_OFFENDER_CATEGORY_SEQUENCES(
    """
        SELECT ASSESSMENT_SEQ FROM OFFENDER_ASSESSMENTS OA
        WHERE OA.OFFENDER_BOOK_ID = :bookingId
        AND ASSESSMENT_TYPE_ID=:assessmentTypeId
        AND ASSESS_STATUS in (:statuses)
        ORDER BY ASSESSMENT_SEQ DESC
    """,
  ),

  FIND_INMATE_ALIASES(
    """
        SELECT O.LAST_NAME,
        O.FIRST_NAME,
        O.MIDDLE_NAME,
        O.BIRTH_DATE,
        RCE.DESCRIPTION AS ETHNICITY,
        RCS.DESCRIPTION AS SEX,
        RCNT.DESCRIPTION AS ALIAS_TYPE,
        O.CREATE_DATE
        FROM OFFENDERS O
        INNER JOIN OFFENDER_BOOKINGS OB ON O.ROOT_OFFENDER_ID = OB.ROOT_OFFENDER_ID
                AND O.OFFENDER_ID != OB.OFFENDER_ID
                LEFT JOIN REFERENCE_CODES RCE ON O.RACE_CODE = RCE.CODE
                AND RCE.DOMAIN = 'ETHNICITY'
        LEFT JOIN REFERENCE_CODES RCS ON O.SEX_CODE = RCS.CODE
                AND RCS.DOMAIN = 'SEX'
        LEFT JOIN REFERENCE_CODES RCNT ON O.ALIAS_NAME_TYPE = RCNT.CODE
                AND RCNT.DOMAIN = 'NAME_TYPE'
        WHERE OB.OFFENDER_BOOK_ID = :bookingId
    """,
  ),

  FIND_OFFENDERS(
    """
        SELECT
        O.OFFENDER_ID_DISPLAY             OFFENDER_NO,
        O.TITLE                           TITLE,
        O.SUFFIX                          SUFFIX,
        O.FIRST_NAME                      FIRST_NAME,
        CONCAT(O.MIDDLE_NAME,
                CASE WHEN O.MIDDLE_NAME_2 IS NOT NULL
                        THEN CONCAT(' ', O.MIDDLE_NAME_2)
        ELSE '' END)                    MIDDLE_NAMES,
        O.LAST_NAME                       LAST_NAME,
        O.BIRTH_DATE                      DATE_OF_BIRTH,
        RCE.DESCRIPTION                   ETHNICITY,
        RCE.CODE                          ETHNICITY_CODE,
        RCS.DESCRIPTION                   GENDER,
        O.SEX_CODE                        SEX_CODE,
        RCC.DESCRIPTION                   BIRTH_COUNTRY,
        OB.OFFENDER_BOOK_ID               LATEST_BOOKING_ID,
        OB.BOOKING_BEGIN_DATE             RECEPTION_DATE,
        OB.ACTIVE_FLAG                    CURRENTLY_IN_PRISON,
        OB.AGY_LOC_ID                     LATEST_LOCATION_ID,
        AL.DESCRIPTION                    LATEST_LOCATION,
        AIL.DESCRIPTION                   INTERNAL_LOCATION,
        IST.BAND_CODE,
        CASE WHEN OPD2.PROFILE_CODE IS NOT NULL
                THEN OPD2.PROFILE_CODE
                ELSE PC.DESCRIPTION END           NATIONALITIES,
        PC3.DESCRIPTION                   RELIGION,
        PC3.PROFILE_CODE                  RELIGION_CODE,
        PC2.DESCRIPTION                   MARITAL_STATUS,
        OIS.IMPRISONMENT_STATUS,
        IST.DESCRIPTION                   IMPRISONMENT_STATUS_DESC,
        (SELECT OI1.IDENTIFIER
                FROM OFFENDER_IDENTIFIERS OI1
                WHERE OI1.OFFENDER_ID = OB.OFFENDER_ID
                AND OI1.IDENTIFIER_TYPE = 'PNC'
                AND OI1.OFFENDER_ID_SEQ = (SELECT MAX(OFFENDER_ID_SEQ)
                FROM OFFENDER_IDENTIFIERS OI11
                WHERE OI11.OFFENDER_ID = OI1.OFFENDER_ID
                AND OI11.IDENTIFIER_TYPE = OI1.IDENTIFIER_TYPE )) PNC_NUMBER,
        (SELECT OI2.IDENTIFIER
                FROM OFFENDER_IDENTIFIERS OI2
                WHERE OI2.OFFENDER_ID = OB.OFFENDER_ID
                AND OI2.IDENTIFIER_TYPE = 'CRO'
                AND OI2.OFFENDER_ID_SEQ = (SELECT MAX(OFFENDER_ID_SEQ)
                FROM OFFENDER_IDENTIFIERS OI21
                WHERE OI21.OFFENDER_ID = OI2.OFFENDER_ID
                AND OI21.IDENTIFIER_TYPE = OI2.IDENTIFIER_TYPE )) CRO_NUMBER
                FROM OFFENDERS O
        INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_ID = O.OFFENDER_ID AND OB.BOOKING_SEQ = 1
        INNER JOIN AGENCY_LOCATIONS AL ON AL.AGY_LOC_ID = OB.AGY_LOC_ID
                LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
                LEFT JOIN OFFENDER_IMPRISON_STATUSES OIS ON OIS.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OIS.LATEST_STATUS = 'Y'
        LEFT JOIN IMPRISONMENT_STATUSES IST ON IST.IMPRISONMENT_STATUS = OIS.IMPRISONMENT_STATUS
                LEFT JOIN REFERENCE_CODES RCE ON O.RACE_CODE = RCE.CODE AND RCE.DOMAIN = 'ETHNICITY'
        LEFT JOIN REFERENCE_CODES RCS ON O.SEX_CODE = RCS.CODE AND RCS.DOMAIN = 'SEX'
        LEFT JOIN REFERENCE_CODES RCC ON O.BIRTH_COUNTRY_CODE = RCC.CODE AND RCC.DOMAIN = 'COUNTRY'
        LEFT JOIN OFFENDER_PROFILE_DETAILS OPD1 ON OPD1.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OPD1.PROFILE_TYPE = 'NAT'
        LEFT JOIN OFFENDER_PROFILE_DETAILS OPD2 ON OPD2.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OPD2.PROFILE_TYPE = 'NATIO'
        LEFT JOIN OFFENDER_PROFILE_DETAILS OPD3 ON OPD3.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OPD3.PROFILE_TYPE = 'RELF'
        LEFT JOIN OFFENDER_PROFILE_DETAILS OPD4 ON OPD4.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OPD4.PROFILE_TYPE = 'MARITAL'
        LEFT JOIN PROFILE_CODES PC ON PC.PROFILE_TYPE = OPD1.PROFILE_TYPE AND PC.PROFILE_CODE = OPD1.PROFILE_CODE
                LEFT JOIN PROFILE_CODES PC2 ON PC2.PROFILE_TYPE = OPD4.PROFILE_TYPE AND PC2.PROFILE_CODE = OPD4.PROFILE_CODE
                LEFT JOIN PROFILE_CODES PC3 ON PC3.PROFILE_TYPE = OPD3.PROFILE_TYPE AND PC3.PROFILE_CODE = OPD3.PROFILE_CODE
    """,
  ),

  FIND_OFFENDERS_WITH_ALIASES(
    """
        SELECT
        O.OFFENDER_ID                     OFFENDER_ID,
        O.OFFENDER_ID_DISPLAY             OFFENDER_NO,
        O.TITLE                           TITLE,
        O.SUFFIX                          SUFFIX,
        O.FIRST_NAME                      FIRST_NAME,
        CONCAT(O.MIDDLE_NAME,
                CASE WHEN O.MIDDLE_NAME_2 IS NOT NULL
                        THEN CONCAT(' ', O.MIDDLE_NAME_2)
        ELSE '' END)                    MIDDLE_NAMES,
        O.LAST_NAME                       LAST_NAME,
        O.BIRTH_DATE                      DATE_OF_BIRTH,
        RCE.DESCRIPTION                   ETHNICITY,
        RCE.CODE                          ETHNICITY_CODE,
        RCS.DESCRIPTION                   GENDER,
        O.SEX_CODE                        SEX_CODE,
        RCC.DESCRIPTION                   BIRTH_COUNTRY,
        WB.OFFENDER_BOOK_ID               LATEST_BOOKING_ID,
        WB.BOOKING_BEGIN_DATE             RECEPTION_DATE,
        WB.ACTIVE_FLAG                    CURRENTLY_IN_PRISON,
        WB.AGY_LOC_ID                     LATEST_LOCATION_ID,
        AL.DESCRIPTION                    LATEST_LOCATION,
        AIL.DESCRIPTION                   INTERNAL_LOCATION,
        IST.BAND_CODE                     BAND_CODE,
        CASE WHEN OPD2.PROFILE_CODE IS NOT NULL
                THEN OPD2.PROFILE_CODE
                ELSE PC.DESCRIPTION END           NATIONALITIES,
        PC3.DESCRIPTION                   RELIGION,
        PC3.PROFILE_CODE                  RELIGION_CODE,
        PC2.DESCRIPTION                   MARITAL_STATUS,
        OIS.IMPRISONMENT_STATUS,
        (SELECT OI1.IDENTIFIER
                FROM OFFENDER_IDENTIFIERS OI1
                WHERE OI1.OFFENDER_ID = WB.OFFENDER_ID
                AND OI1.IDENTIFIER_TYPE = 'PNC'
                AND OI1.OFFENDER_ID_SEQ = (SELECT MAX(OFFENDER_ID_SEQ)
                FROM OFFENDER_IDENTIFIERS OI11
                WHERE OI11.OFFENDER_ID = OI1.OFFENDER_ID
                AND OI11.IDENTIFIER_TYPE = OI1.IDENTIFIER_TYPE )) PNC_NUMBER,
        (SELECT OI2.IDENTIFIER
                FROM OFFENDER_IDENTIFIERS OI2
                WHERE OI2.OFFENDER_ID = WB.OFFENDER_ID
                AND OI2.IDENTIFIER_TYPE = 'CRO'
                AND OI2.OFFENDER_ID_SEQ = (SELECT MAX(OFFENDER_ID_SEQ)
                FROM OFFENDER_IDENTIFIERS OI21
                WHERE OI21.OFFENDER_ID = OI2.OFFENDER_ID
                AND OI21.IDENTIFIER_TYPE = OI2.IDENTIFIER_TYPE )) CRO_NUMBER,
        WO_FIRST_NAME CURRENT_WORKING_FIRST_NAME,
        WO_LAST_NAME  CURRENT_WORKING_LAST_NAME,
        WO_BIRTH_DATE CURRENT_WORKING_BIRTH_DATE
                FROM OFFENDERS O
        INNER JOIN OFFENDER_BOOKINGS WB ON (
                WB.ROOT_OFFENDER_ID = O.ROOT_OFFENDER_ID AND
                        WB.BOOKING_SEQ = 1
        )
        INNER JOIN (SELECT FIRST_NAME AS WO_FIRST_NAME, LAST_NAME AS WO_LAST_NAME, BIRTH_DATE AS WO_BIRTH_DATE, OFFENDER_ID AS WO_OFFENDER_ID FROM OFFENDERS) WO on WO_OFFENDER_ID = WB.OFFENDER_ID
        INNER JOIN AGENCY_LOCATIONS AL ON AL.AGY_LOC_ID = WB.AGY_LOC_ID
                LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON WB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
                LEFT JOIN OFFENDER_IMPRISON_STATUSES OIS ON OIS.OFFENDER_BOOK_ID = WB.OFFENDER_BOOK_ID AND OIS.LATEST_STATUS = 'Y'
        LEFT JOIN IMPRISONMENT_STATUSES IST ON IST.IMPRISONMENT_STATUS = OIS.IMPRISONMENT_STATUS
                LEFT JOIN REFERENCE_CODES RCE ON O.RACE_CODE = RCE.CODE AND RCE.DOMAIN = 'ETHNICITY'
        LEFT JOIN REFERENCE_CODES RCS ON O.SEX_CODE = RCS.CODE AND RCS.DOMAIN = 'SEX'
        LEFT JOIN REFERENCE_CODES RCC ON O.BIRTH_COUNTRY_CODE = RCC.CODE AND RCC.DOMAIN = 'COUNTRY'
        LEFT JOIN OFFENDER_PROFILE_DETAILS OPD1 ON OPD1.OFFENDER_BOOK_ID = WB.OFFENDER_BOOK_ID AND OPD1.PROFILE_TYPE = 'NAT'
        LEFT JOIN OFFENDER_PROFILE_DETAILS OPD2 ON OPD2.OFFENDER_BOOK_ID = WB.OFFENDER_BOOK_ID AND OPD2.PROFILE_TYPE = 'NATIO'
        LEFT JOIN OFFENDER_PROFILE_DETAILS OPD3 ON OPD3.OFFENDER_BOOK_ID = WB.OFFENDER_BOOK_ID AND OPD3.PROFILE_TYPE = 'RELF'
        LEFT JOIN OFFENDER_PROFILE_DETAILS OPD4 ON OPD4.OFFENDER_BOOK_ID = WB.OFFENDER_BOOK_ID AND OPD4.PROFILE_TYPE = 'MARITAL'
        LEFT JOIN PROFILE_CODES PC ON PC.PROFILE_TYPE = OPD1.PROFILE_TYPE AND PC.PROFILE_CODE = OPD1.PROFILE_CODE
                LEFT JOIN PROFILE_CODES PC2 ON PC2.PROFILE_TYPE = OPD4.PROFILE_TYPE AND PC2.PROFILE_CODE = OPD4.PROFILE_CODE
                LEFT JOIN PROFILE_CODES PC3 ON PC3.PROFILE_TYPE = OPD3.PROFILE_TYPE AND PC3.PROFILE_CODE = OPD3.PROFILE_CODE
    """,
  ),

  LOCATION_FILTER_SQL(
    """
        AIL.DESCRIPTION LIKE :locationPrefix
    """,
  ),

  GET_IMPRISONMENT_STATUS(
    """
        SELECT
        OB.OFFENDER_BOOK_ID BOOKING_ID,
        IST.BAND_CODE as BAND_CODE,
        IST.DESCRIPTION as DESCRIPTION,
        OIS.IMPRISONMENT_STATUS as IMPRISONMENT_STATUS,
        OIS.IMPRISON_STATUS_SEQ
        FROM OFFENDER_BOOKINGS OB
        JOIN OFFENDER_IMPRISON_STATUSES OIS ON OIS.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OIS.LATEST_STATUS = 'Y'
        JOIN IMPRISONMENT_STATUSES IST ON IST.IMPRISONMENT_STATUS = OIS.IMPRISONMENT_STATUS
        WHERE OB.OFFENDER_BOOK_ID = :bookingId
    """,
  ),
}
