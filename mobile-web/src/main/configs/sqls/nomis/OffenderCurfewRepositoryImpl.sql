OFFENDERS_WITHOUT_CURFEW_APPROVAL_STATUS {

SELECT oc_most_recent.offender_book_id,
       ob.agy_loc_id agency_location_id
  FROM offender_curfews oc_most_recent
       INNER JOIN (
                 SELECT oc.offender_book_id,
                        max(oc.offender_curfew_id) offender_curfew_id
                   FROM offender_curfews oc
                        INNER JOIN (
                                      SELECT offender_book_id,
                                             max(nvl(assessment_date, Date '9999-12-31')) assessment_date
                                        FROM offender_curfews
                                    GROUP BY offender_book_id
                        ) oc_max_ad ON
                            oc.offender_book_id = oc_max_ad.offender_book_id AND
                            nvl(oc.assessment_date, Date '9999-12-31') = oc_max_ad.assessment_date
               GROUP BY oc.offender_book_id
             ) oc_max_id ON oc_max_id.offender_curfew_id = oc_most_recent.offender_curfew_id
       INNER JOIN offender_bookings ob ON ob.OFFENDER_BOOK_ID = oc_most_recent.offender_book_id
 WHERE oc_most_recent.approval_status IS NULL
       AND OB.active_flag = 'Y'
       AND OB.booking_seq = 1
}
