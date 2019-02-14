QUESTIONNAIRE {
  select q.code,
         q.questionnaire_id,
         qq.questionnaire_que_id,
         qq.que_seq question_seq,
         qq.description question_desc,
         ans_seq answer_seq,
         qa.description answer_desc,
         qq.list_seq question_list_seq,
         qq.active_flag question_active_flag,
         qq.expiry_date question_expiry_date,
         qq.multiple_answer_flag,
         qa.questionnaire_ans_id,
         next_questionnaire_que_id,
         qa.list_seq answer_list_seq,
         qa.active_flag answer_active_flag,
         qa.expiry_date answer_expiry_date,
         qa.date_required_flag,
         qa.comment_required_flag
  from questionnaires q
         join questionnaire_questions qq on q.questionnaire_id = qq.questionnaire_id
         join questionnaire_answers qa on qa.QUESTIONNAIRE_QUE_ID = qq.QUESTIONNAIRE_QUE_ID
  where q.questionnaire_category = :category and q.code = :code
  order by qq.que_seq, qa.ans_seq
}

GET_INCIDENT_CASE {
select ic.INCIDENT_CASE_ID,
       ic.REPORTED_STAFF_ID,
       ic.REPORT_DATE,
       ic.REPORT_TIME,
       ic.INCIDENT_DATE,
       ic.INCIDENT_TIME,
       ic.INCIDENT_STATUS,
       ic.AGY_LOC_ID AGENCY_ID,
       INCIDENT_TITLE,
       INCIDENT_TYPE,
       INCIDENT_DETAILS,
       RESPONSE_LOCKED_FLAG
 from INCIDENT_CASES ic
 where ic.INCIDENT_CASE_ID = :incidentCaseId
}

GET_INCIDENT_CASES_BY_OFFENDER_NO {
select icp.OFFENDER_BOOK_ID,
       ic.INCIDENT_CASE_ID,
       ic.REPORTED_STAFF_ID,
       ic.REPORT_DATE,
       ic.REPORT_TIME,
       ic.INCIDENT_DATE,
       ic.INCIDENT_TIME,
       ic.INCIDENT_STATUS,
       ic.AGY_LOC_ID,
       INCIDENT_TITLE,
       INCIDENT_TYPE,
       INCIDENT_DETAILS,
       RESPONSE_LOCKED_FLAG,
       PARTY_SEQ,
       icp.PARTICIPATION_ROLE,
       icp.COMMENT_TEXT,
       OUTCOME_CODE,
       icq.question_seq,
       icq.questionnaire_que_id,
       que_seq,
       qq.description Question,
       qa.description Answer,
       qa.ans_seq,
       icr.RESPONSE_DATE,
       icr.RESPONSE_COMMENT_TEXT,
       icr.RECORD_STAFF_ID
from INCIDENT_CASES ic
       join INCIDENT_CASE_PARTIES icp on ic.INCIDENT_CASE_ID = icp.INCIDENT_CASE_ID
       join OFFENDER_BOOKINGS ob on ob.offender_book_id = icp.offender_book_id
       join OFFENDERS o on o.offender_id = ob.offender_id
       join incident_case_questions icq on ic.INCIDENT_CASE_ID = icq.INCIDENT_CASE_ID
       join questionnaire_questions qq on qq.questionnaire_que_id = icq.questionnaire_que_id
       join incident_case_responses icr on icr.incident_case_id = icq.incident_case_id
       join questionnaire_answers qa
            on qa.QUESTIONNAIRE_ANS_ID = icr.QUESTIONNAIRE_ANS_ID and qa.QUESTIONNAIRE_QUE_ID = qq.QUESTIONNAIRE_QUE_ID
where ic.incident_type = :incidentType %s
  and o.offender_id_display = :offenderNo
order by icp.offender_book_id, ic.incident_case_id, icq.question_seq
}

GET_INCIDENT_CASES_BY_BOOKING_ID {
select icp.OFFENDER_BOOK_ID,
       ic.INCIDENT_CASE_ID,
       ic.REPORTED_STAFF_ID,
       ic.REPORT_DATE,
       ic.REPORT_TIME,
       ic.INCIDENT_DATE,
       ic.INCIDENT_TIME,
       ic.INCIDENT_STATUS,
       ic.AGY_LOC_ID,
       INCIDENT_TITLE,
       INCIDENT_TYPE,
       INCIDENT_DETAILS,
       RESPONSE_LOCKED_FLAG,
       PARTY_SEQ,
       icp.PARTICIPATION_ROLE,
       icp.COMMENT_TEXT,
       OUTCOME_CODE,
       icq.question_seq,
       icq.questionnaire_que_id,
       que_seq,
       qq.description Question,
       qa.description Answer,
       qa.ans_seq,
       icr.RESPONSE_DATE,
       icr.RESPONSE_COMMENT_TEXT,
       icr.RECORD_STAFF_ID
from INCIDENT_CASES ic
       join INCIDENT_CASE_PARTIES icp on ic.INCIDENT_CASE_ID = icp.INCIDENT_CASE_ID
       join incident_case_questions icq on ic.INCIDENT_CASE_ID = icq.INCIDENT_CASE_ID
       join questionnaire_questions qq on qq.questionnaire_que_id = icq.questionnaire_que_id
       join incident_case_responses icr on icr.incident_case_id = icq.incident_case_id
       join questionnaire_answers qa
            on qa.QUESTIONNAIRE_ANS_ID = icr.QUESTIONNAIRE_ANS_ID and qa.QUESTIONNAIRE_QUE_ID = qq.QUESTIONNAIRE_QUE_ID
where ic.incident_type = :incidentType %s
  and icp.OFFENDER_BOOK_ID = :bookingId
order by icp.offender_book_id, ic.incident_case_id, icq.question_seq
}

PARTICIPATION_ROLES {
    AND icp.participation_role IN (:participationRoles)
}
