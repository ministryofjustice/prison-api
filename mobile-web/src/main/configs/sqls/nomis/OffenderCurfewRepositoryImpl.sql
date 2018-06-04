
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
