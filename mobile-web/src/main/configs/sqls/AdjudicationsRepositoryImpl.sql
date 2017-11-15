FIND_AWARDS {
  SELECT s.oic_sanction_code as sanction_code,
         rc.description as sanction_code_description,
         s.sanction_months as months,
         s.sanction_days as days,
         s.compensation_amount as limit,
         s.comment_text as "comment",
         s.effective_date as effective_date
  FROM offender_oic_sanctions s
    LEFT JOIN reference_codes rc ON s.oic_sanction_code = rc.code AND rc.domain = 'OIC_SANCT'
  WHERE s.offender_book_id = :bookingId
}
