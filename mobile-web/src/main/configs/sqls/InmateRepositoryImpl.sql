
FIND_INMATE_DETAIL {
     SELECT B.OFFENDER_BOOK_ID,
            B.BOOKING_NO,
            O.OFFENDER_ID_DISPLAY,
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
            B.LIVING_UNIT_ID,
            (
                SELECT * FROM (
                    SELECT IMAGE_ID
                      FROM IMAGES
                     WHERE ACTIVE_FLAG = 'Y'
                           AND IMAGE_OBJECT_TYPE = 'OFF_BKG'
                           AND IMAGE_OBJECT_ID = B.OFFENDER_BOOK_ID
                           AND IMAGE_VIEW_TYPE = 'FACE'
                           AND ORIENTATION_TYPE = 'FRONT'
                     ORDER BY CREATE_DATETIME DESC
                )
                WHERE ROWNUM <= 1
            ) AS FACE_IMAGE_ID,
            O.BIRTH_DATE,
            (SYSDATE - O.BIRTH_DATE) / 365 AS AGE
       FROM OFFENDER_BOOKINGS B
            LEFT JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
      WHERE B.ACTIVE_FLAG = 'Y' AND B.OFFENDER_BOOK_ID = :bookingId
}


FIND_ALL_INMATES {
     SELECT B.OFFENDER_BOOK_ID,
            B.BOOKING_NO,
            O.OFFENDER_ID_DISPLAY,
            B.AGY_LOC_ID,
            O.FIRST_NAME,
            O.MIDDLE_NAME,
            O.LAST_NAME,
            (
                SELECT LISTAGG(ALERT_TYPE, ',') WITHIN GROUP (ORDER BY ALERT_TYPE)
                  FROM (
                            SELECT DISTINCT( ALERT_TYPE)
                              FROM OFFENDER_ALERTS OA
                             WHERE B.OFFENDER_BOOK_ID = OA.OFFENDER_BOOK_ID AND OA.ALERT_STATUS = 'ACTIVE'
                       )
            ) AS ALERT_TYPES,
            B.LIVING_UNIT_ID,
            (
                SELECT * FROM (
                    SELECT IMAGE_ID
                      FROM IMAGES
                     WHERE ACTIVE_FLAG = 'Y'
                           AND IMAGE_OBJECT_TYPE = 'OFF_BKG'
                           AND IMAGE_OBJECT_ID = B.OFFENDER_BOOK_ID
                           AND IMAGE_VIEW_TYPE = 'FACE'
                           AND ORIENTATION_TYPE = 'FRONT'
                     ORDER BY CREATE_DATETIME DESC
                )
                WHERE ROWNUM <= 1
            ) AS FACE_IMAGE_ID
       FROM OFFENDER_BOOKINGS B
            LEFT JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
      WHERE B.ACTIVE_FLAG = 'Y'
}



FIND_INMATES_BY_LOCATION {
     SELECT B.OFFENDER_BOOK_ID,
            B.BOOKING_NO,
            O.OFFENDER_ID_DISPLAY,
            B.AGY_LOC_ID,
            O.FIRST_NAME,
            O.MIDDLE_NAME,
            O.LAST_NAME,
            (
                SELECT LISTAGG(ALERT_TYPE, ',') WITHIN GROUP (ORDER BY ALERT_TYPE)
                  FROM (
                            SELECT DISTINCT( ALERT_TYPE)
                              FROM OFFENDER_ALERTS OA
                             WHERE B.OFFENDER_BOOK_ID = OA.OFFENDER_BOOK_ID AND OA.ALERT_STATUS = 'ACTIVE'
                       )
            ) AS ALERT_TYPES,
            B.LIVING_UNIT_ID,
            (
                SELECT * FROM (
                    SELECT IMAGE_ID
                      FROM IMAGES
                     WHERE ACTIVE_FLAG = 'Y'
                           AND IMAGE_OBJECT_TYPE = 'OFF_BKG'
                           AND IMAGE_OBJECT_ID = B.OFFENDER_BOOK_ID
                           AND IMAGE_VIEW_TYPE = 'FACE'
                           AND ORIENTATION_TYPE = 'FRONT'
                     ORDER BY CREATE_DATETIME DESC
                )
                WHERE ROWNUM <= 1
            ) AS FACE_IMAGE_ID
       FROM OFFENDER_BOOKINGS B
            LEFT JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
      WHERE B.ACTIVE_FLAG = 'Y'
            AND B.LIVING_UNIT_ID IN (
                SELECT INTERNAL_LOCATION_ID
                  FROM AGENCY_INTERNAL_LOCATIONS START WITH INTERNAL_LOCATION_ID = :locationId
               CONNECT BY PRIOR INTERNAL_LOCATION_ID = PARENT_INTERNAL_LOCATION_ID
            )

}


FIND_PHYSICAL_CHARACTERISTICS_BY_BOOKING {
    SELECT (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = P.PROFILE_TYPE AND DOMAIN='BODY_PART') AS CHARACTERISTIC,
           (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = P.PROFILE_CODE AND DOMAIN='PPTY_COLOR') AS DETAIL,
           (SELECT * FROM -- there is a chance that this will return two rows instead of one, in this case return the most recent image.
                   (SELECT I.IMAGE_ID
                      FROM IMAGES I
                     WHERE B.OFFENDER_BOOK_ID = :bookingId
                           AND B.OFFENDER_BOOK_ID = I.IMAGE_OBJECT_ID
                           AND I.ACTIVE_FLAG = 'Y'
                           AND I.IMAGE_OBJECT_TYPE = 'OFF_BKG'
                           AND I.IMAGE_VIEW_TYPE = P.PROFILE_TYPE
                     ORDER BY I.CREATE_DATETIME DESC
                   )
             WHERE ROWNUM <= 1
           ) AS IMAGE_ID
      FROM OFFENDER_BOOKINGS B
           LEFT JOIN OFFENDER_PROFILE_DETAILS P ON B.OFFENDER_BOOK_ID = P.OFFENDER_BOOK_ID
     WHERE B.OFFENDER_BOOK_ID = :bookingId
           AND B.ACTIVE_FLAG = 'Y'
           AND P.PROFILE_TYPE IN (SELECT CODE FROM REFERENCE_CODES WHERE DOMAIN='BODY_PART')
     ORDER BY B.OFFENDER_BOOK_ID
}


FIND_PHYSICAL_MARKS_BY_BOOKING {
    SELECT (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = M.MARK_TYPE AND DOMAIN='MARK_TYPE') AS TYPE,
           (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = M.SIDE_CODE AND DOMAIN='SIDE') AS SIDE,
           (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = M.BODY_PART_CODE AND DOMAIN='BODY_PART') AS BODY_PART,
           (SELECT DESCRIPTION FROM REFERENCE_CODES WHERE CODE = M.PART_ORIENTATION_CODE AND DOMAIN='PART_ORIENT') AS ORENTIATION,
           M.COMMENT_TEXT,
           (SELECT I.IMAGE_ID
              FROM IMAGES I
             WHERE B.OFFENDER_BOOK_ID = :bookingId
                   AND B.OFFENDER_BOOK_ID = I.IMAGE_OBJECT_ID
                   AND I.ACTIVE_FLAG = 'Y'
                   AND M.MARK_TYPE = I.IMAGE_VIEW_TYPE
                   AND M.BODY_PART_CODE = I.ORIENTATION_TYPE
                   AND M.ID_MARK_SEQ = I.IMAGE_OBJECT_SEQ
           ) AS IMAGE_ID
      FROM OFFENDER_BOOKINGS B
           INNER JOIN offender_identifying_marks M ON B.OFFENDER_BOOK_ID = M.OFFENDER_BOOK_ID
     WHERE B.OFFENDER_BOOK_ID = :bookingId
           AND B.ACTIVE_FLAG = 'Y'
           AND M.BODY_PART_CODE != 'CONV'
}

FIND_PHYSICAL_ATTRIBUTES_BY_BOOKING {
    SELECT O.SEX_CODE,
           O.RACE_CODE,
           P.HEIGHT_FT,
           P.HEIGHT_IN,
           P.HEIGHT_CM,
           P.WEIGHT_LBS,
           P.WEIGHT_KG
      FROM OFFENDER_BOOKINGS B
           LEFT JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
           LEFT JOIN OFFENDER_PHYSICAL_ATTRIBUTES P ON B.OFFENDER_BOOK_ID = P.OFFENDER_BOOK_ID
     WHERE B.OFFENDER_BOOK_ID = :bookingId
           AND B.ACTIVE_FLAG = 'Y'
}
FIND_ACTIVE_APPROVED_ASSESSMENT {
	SELECT
		offender_assessments.offender_book_id,
		assessments.assessment_code,
		assessments.description as assessment_description,
		ref_cd_SUP_LVL_TYPE.description as classification
		FROM offender_assessments, assessments, reference_codes ref_cd_SUP_LVL_TYPE
		WHERE offender_assessments.assessment_type_id = assessments.assessment_id
		AND assessments.assessment_class = 'TYPE'
		AND offender_assessments.offender_book_id = :bookingId
		AND offender_assessments.review_sup_level_type = ref_cd_SUP_LVL_TYPE.code
		AND ref_cd_SUP_LVL_TYPE.domain = 'SUP_LVL_TYPE'
		AND offender_assessments.assess_status = 'A'
		AND offender_assessments.evaluation_result_code = 'APP'
}
