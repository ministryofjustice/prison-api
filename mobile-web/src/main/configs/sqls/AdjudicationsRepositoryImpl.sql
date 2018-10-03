FIND_AWARDS {
  SELECT s.oic_sanction_code as sanction_code,
         rc.description as sanction_code_description,
         s.sanction_months as months,
         s.sanction_days as days,
         s.compensation_amount as limit,
         s.comment_text as "comment",
         s.effective_date as effective_date,
         s.oic_hearing_id as hearing_id,
         s.result_seq as hearing_sequence
  FROM offender_oic_sanctions s
    INNER JOIN oic_hearing_results h ON s.oic_hearing_id = h.oic_hearing_id AND s.result_seq = h.result_seq
    LEFT JOIN reference_codes rc ON s.oic_sanction_code = rc.code AND rc.domain = 'OIC_SANCT'
  WHERE s.offender_book_id = :bookingId
    AND h.finding_code = 'PROVED'
    AND s.status in ('IMMEDIATE', 'SUSPENDED')
  ORDER BY s.oic_hearing_id, s.result_seq
}
