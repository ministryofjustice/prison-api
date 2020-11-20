package uk.gov.justice.hmpps.prison.repository.sql

enum class IncidentCaseRepositorySql(val sql: String) {
  QUESTIONNAIRE(
    """
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
    """
  ),

  GET_INCIDENT_CASE(
    """
        select ic.INCIDENT_CASE_ID,
        ic.REPORTED_STAFF_ID,
        ic.REPORT_DATE,
        ic.REPORT_TIME,
        ic.INCIDENT_DATE,
        ic.INCIDENT_TIME,
        ic.INCIDENT_STATUS,
        ic.AGY_LOC_ID AGENCY_ID,
        ic.INCIDENT_TITLE,
        ic.INCIDENT_TYPE,
        ic.INCIDENT_DETAILS,
        ic.RESPONSE_LOCKED_FLAG,
        icq.question_seq,
        icq.questionnaire_que_id,
        qa.questionnaire_ans_id,
        qq.description question,
        qa.description answer,
        icr.RESPONSE_DATE,
        icr.RESPONSE_COMMENT_TEXT,
        icr.RECORD_STAFF_ID
        from INCIDENT_CASES ic
        join incident_case_questions icq on ic.INCIDENT_CASE_ID = icq.INCIDENT_CASE_ID
        join questionnaire_questions qq on qq.questionnaire_que_id = icq.questionnaire_que_id
        join incident_case_responses icr on icr.incident_case_id = icq.incident_case_id
        join questionnaire_answers qa
        on qa.QUESTIONNAIRE_ANS_ID = icr.QUESTIONNAIRE_ANS_ID and qa.QUESTIONNAIRE_QUE_ID = qq.QUESTIONNAIRE_QUE_ID
                where ic.INCIDENT_CASE_ID IN (:incidentCaseIds)
    """
  ),

  GET_PARTIES_INVOLVED(
    """
        select icp.INCIDENT_CASE_ID,
        icp.PARTY_SEQ,
        icp.OFFENDER_BOOK_ID BOOKING_ID,
        icp.STAFF_ID,
        icp.PERSON_ID,
        icp.PARTICIPATION_ROLE,
        icp.OUTCOME_CODE,
        icp.COMMENT_TEXT
        from INCIDENT_CASE_PARTIES icp
        where icp.INCIDENT_CASE_ID IN (:incidentCaseIds)
    """
  ),

  GET_INCIDENT_CASES_BY_OFFENDER_NO(
    """
        select DISTINCT icp.INCIDENT_CASE_ID
        from INCIDENT_CASE_PARTIES icp
        join INCIDENT_CASES ic on ic.INCIDENT_CASE_ID = icp.INCIDENT_CASE_ID
        join OFFENDER_BOOKINGS ob on ob.offender_book_id = icp.offender_book_id
        join OFFENDERS o on o.offender_id = ob.offender_id
        where o.offender_id_display = :offenderNo
    """
  ),

  GET_INCIDENT_CASES_BY_BOOKING_ID(
    """
        select DISTINCT icp.INCIDENT_CASE_ID
        from INCIDENT_CASE_PARTIES icp
        join INCIDENT_CASES ic on ic.INCIDENT_CASE_ID = icp.INCIDENT_CASE_ID
        where icp.OFFENDER_BOOK_ID = :bookingId
    """
  ),

  FILTER_BY_PARTICIPATION(
    """
        icp.participation_role IN (:participationRoles)
    """
  ),

  FILTER_BY_TYPE(
    """
        ic.incident_type IN (:incidentTypes)
    """
  ),

  GET_INCIDENT_CANDIDATES(
    """
        select distinct o.offender_id_display as offender_no
        from (
                select icp.offender_book_id, ic.modify_datetime
                from incident_cases ic
                join incident_case_parties icp on ic.incident_case_id = icp.incident_case_id
                union
                select icp.offender_book_id, icr.modify_datetime as offender_no
                from incident_case_responses icr
                join incident_case_parties icp on icr.incident_case_id = icp.incident_case_id
                union
                select icp.offender_book_id, icp.modify_datetime as offender_no
                from incident_case_parties icp
        ) data
                join offender_bookings ob on ob.offender_book_id = data.offender_book_id
        join offenders o on o.offender_id = ob.offender_id
        where data.modify_datetime > :cutoffTimestamp
    """
  )
}
