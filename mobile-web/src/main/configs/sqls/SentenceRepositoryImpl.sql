GET_CASE {
  -- not currently used, this gets a case id for the query below
select ocs.case_id,
       ocs.case_info_number,
       ocs.begin_date,
       ocs.case_status,
       ocs.agy_loc_id court_code,
        al.description court_desc,
       ocs.case_type case_type_code,
       rc.description case_type_desc
  from offender_cases ocs
       join agency_locations al on al.agy_loc_id = ocs.agy_loc_id
  left join reference_codes rc  on rc.code = ocs.case_type and rc.domain = 'LEG_CASE_TYP'
 where ocs.offender_book_id = :offenderBookingId
 order by ocs.case_status asc, ocs.begin_date desc
}

GET_CHARGES {
  -- not currently used, this services the 'old API' /offenders/<noms_id>/charges call
select och.offender_charge_id,
       och.statute_code,
       och.offence_code,
       och.no_of_offences,
       och.most_serious_flag,
       och.charge_status,
       offs.severity_ranking,
       offs.description offence_desc,
       s.description statute_desc,
       orc.result_code,
       orc.description result_desc,
       orc.disposition_code,
       r1.description disposition_desc,
       orc.conviction_flag,
       ist.imprisonment_status,
       ist.description imprisonment_status_desc,
       ist.band_code,
       r2.description band_desc
  from offender_charges och
       join offences offs                on offs.offence_code = och.offence_code and offs.statute_code = och.statute_code
       join statutes s                   on s.statute_code = offs.statute_code
  left join offence_result_codes orc     on och.result_code_1 = orc.result_code
  left join reference_codes r1           on r1.code = orc.disposition_code and r1.domain = 'OFF_RESULT'
  left join imprison_status_mappings ism on ism.offence_result_code = orc.result_code
  left join imprisonment_statuses ist    on ist.imprisonment_status_id = ism.imprisonment_status_id
  left join reference_codes r2           on r2.code = ist.band_code and r2.domain = 'IMPSBAND'
 where och.case_id = :caseId
 order by och.charge_status asc, och.most_serious_flag desc, offs.severity_ranking
}

GET_MAIN_OFFENCE {
  SELECT offs.description FROM offender_charges och
  JOIN offences offs ON offs.offence_code = och.offence_code AND offs.statute_code = och.statute_code
  WHERE och.offender_book_id = :bookingId AND och.most_serious_flag = 'Y' AND och.charge_status = 'A'
}

GET_SENTENCE_LENGTH {
  SELECT osc.effective_sentence_length FROM offender_sent_calculations osc
  WHERE offender_sent_calculation_id = 
    (SELECT MAX(offender_sent_calculation_id) FROM offender_sent_calculations WHERE offender_book_id = :bookingId)
}

GET_RELEASE_DATE {  
  SELECT CASE WHEN release_date is null THEN auto_release_date ELSE release_date END release_date FROM offender_release_details
  WHERE offender_book_id = :bookingId  
}
