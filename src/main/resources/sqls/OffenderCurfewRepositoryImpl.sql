OFFENDER_CURFEWS {
  SELECT oc.offender_book_id,
         oc.offender_curfew_id,
         oc.assessment_date,
         oc.approval_status,
         oc.ard_crd_date
  FROM OFFENDER_CURFEWS oc
         INNER JOIN offender_bookings ob ON ob.offender_book_id = oc.offender_book_id
  WHERE     OB.active_flag = 'Y'
    AND OB.booking_seq = 1
    AND OB.agy_loc_id IN (:agencyLocationIds)
}

UPDATE_CURFEW_CHECKS_PASSED {
  UPDATE OFFENDER_CURFEWS
     SET ASSESSMENT_DATE = :date,
         PASSED_FLAG = :checksPassed
   WHERE OFFENDER_BOOK_ID = :bookingId AND
         CREATE_DATETIME = (
           SELECT MAX(CREATE_DATETIME)
             FROM OFFENDER_CURFEWS
            WHERE OFFENDER_BOOK_ID = :bookingId
         )
}

UPDATE_APPROVAL_STATUS {
UPDATE OFFENDER_CURFEWS
   SET DECISION_DATE = :date,
       APPROVAL_STATUS = :approvalStatus
 WHERE OFFENDER_CURFEW_ID = :curfewId
}

CREATE_HDC_STATUS_TRACKING {
    INSERT INTO HDC_STATUS_TRACKINGS (
        HDC_STATUS_TRACKING_ID,
        OFFENDER_CURFEW_ID,
        STATUS_CODE,
        UPDATE_DATE
    )
    VALUES (
        HDC_STATUS_TRACKING_ID.NEXTVAL,
        :offenderCurfewId,
        :statusCode,
        sysdate
    )
}

CREATE_HDC_STATUS_REASON {
    INSERT INTO HDC_STATUS_REASONS (
       HDC_STATUS_REASON_ID,
       HDC_STATUS_TRACKING_ID,
       STATUS_REASON_CODE
    )
    VALUES (
       HDC_STATUS_REASON_ID.NEXTVAL,
       :hdcStatusTrackingId,
       :statusReasonCode
    )
}

LATEST_HOME_DETENTION_CURFEW {
SELECT *
  FROM (
       SELECT OC.APPROVAL_STATUS,
              HSR.STATUS_REASON_CODE AS REFUSED_REASON,
              OC.DECISION_DATE       AS APPROVAL_STATUS_DATE,
              OC.PASSED_FLAG         AS PASSED,
              OC.ASSESSMENT_DATE     AS CHECKS_PASSED_DATE
         FROM OFFENDER_CURFEWS OC
              LEFT JOIN HDC_STATUS_TRACKINGS HST ON HST.OFFENDER_CURFEW_ID = OC.OFFENDER_CURFEW_ID AND
                                                    HST.STATUS_CODE = :statusTrackingCode
              LEFT JOIN HDC_STATUS_REASONS HSR   ON HST.HDC_STATUS_TRACKING_ID = HSR.HDC_STATUS_TRACKING_ID
        WHERE OFFENDER_BOOK_ID = :bookingId
     ORDER BY OC.CREATE_DATETIME DESC,
              HST.HDC_STATUS_TRACKING_ID DESC,
              HSR.HDC_STATUS_REASON_ID DESC
      ) WHERE ROWNUM = 1
}

LATEST_HOME_DETENTION_CURFEW_ID {
SELECT *
FROM (
         SELECT OFFENDER_CURFEW_ID
         FROM OFFENDER_CURFEWS
         WHERE OFFENDER_BOOK_ID = :bookingId
         ORDER BY CREATE_DATETIME DESC
     ) WHERE ROWNUM = 1
}

UPDATE_HDC_STATUS_REASON {
    UPDATE HDC_STATUS_REASONS
       SET STATUS_REASON_CODE = :hdcStatusReason
     WHERE HDC_STATUS_TRACKING_ID = (
         SELECT HDC_STATUS_TRACKING_ID
           FROM HDC_STATUS_TRACKINGS
          WHERE OFFENDER_CURFEW_ID = :offenderCurfewId AND
                STATUS_CODE = :hdcStatusTrackingCode
         )
}