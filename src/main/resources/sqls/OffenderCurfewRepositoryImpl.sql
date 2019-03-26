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
       APPROVAL_STATUS = :approvalStatus,
       REFUSED_REASON = :refusedReason
 WHERE OFFENDER_BOOK_ID = :bookingId AND
       CREATE_DATETIME = (
         SELECT MAX(CREATE_DATETIME)
           FROM OFFENDER_CURFEWS
          WHERE OFFENDER_BOOK_ID = :bookingId
       )
}

LATEST_HOME_DETENTION_CURFEW {
SELECT APPROVAL_STATUS,
       REFUSED_REASON,
       DECISION_DATE APPROVAL_STATUS_DATE,
       PASSED_FLAG as PASSED,
       ASSESSMENT_DATE as CHECKS_PASSED_DATE
  FROM OFFENDER_CURFEWS
 WHERE OFFENDER_BOOK_ID = :bookingId AND
     CREATE_DATETIME = (
     SELECT MAX(CREATE_DATETIME)
       FROM OFFENDER_CURFEWS
      WHERE OFFENDER_BOOK_ID = :bookingId
   )
}