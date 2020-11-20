package uk.gov.justice.hmpps.prison.repository.v1.sql

enum class AlertV1RepositorySql(val sql: String) {
  ALERTS_BY_OFFENDER_WTIH_INACTIVE(
    """
        select ob.root_offender_id,
        ob.offender_book_id,
        ob.active_flag,
        ob.agy_loc_id,
        oa.alert_seq,
        oa.alert_type,
        rc1.description alert_type_desc,
        oa.alert_code,
        rc2.description alert_code_desc,
        oa.alert_date,
        oa.expiry_date,
        oa.alert_status,
        oa.comment_text,
        oa.authorize_person_text,
        oa.caseload_id,
        oa.verified_flag
        from offenders o
        join offender_bookings ob
        on ob.offender_id = o.offender_id
        and ob.booking_seq = 1
        left join offender_alerts oa
        on oa.offender_book_id = ob.offender_book_id
        and (:p_modified_since is null
        or oa.audit_timestamp >= :p_modified_since)
        left join reference_codes rc1
        on rc1.code = oa.alert_type
        and rc1.domain = 'ALERT'
        left join reference_codes rc2
        on rc2.code = oa.alert_code
        and rc2.domain = 'ALERT_CODE'
        and rc2.parent_domain = rc1.domain
        and rc2.parent_code = rc1.code
        where o.offender_id_display = :p_noms_id
        and ob.active_flag = 'Y'
        order by oa.alert_status, oa.alert_date
    """
  ),

  ALERTS_BY_OFFENDER(
    """
        select ob.root_offender_id,
        ob.offender_book_id,
        ob.active_flag,
        ob.agy_loc_id,
        oa.alert_seq,
        oa.alert_type,
        rc1.description alert_type_desc,
        oa.alert_code,
        rc2.description alert_code_desc,
        oa.alert_date,
        oa.expiry_date,
        oa.alert_status,
        oa.comment_text,
        oa.authorize_person_text,
        oa.caseload_id,
        oa.verified_flag
        from offenders o
        join offender_bookings ob
        on ob.offender_id = o.offender_id
                and ob.booking_seq = 1
        left join offender_alerts oa
                on oa.offender_book_id = ob.offender_book_id
                and oa.alert_status = 'ACTIVE'
        and (:p_modified_since is null
        or oa.audit_timestamp >= :p_modified_since)
        left join reference_codes rc1
                on rc1.code = oa.alert_type
                and rc1.domain = 'ALERT'
        left join reference_codes rc2
                on rc2.code = oa.alert_code
                and rc2.domain = 'ALERT_CODE'
        and rc2.parent_domain = rc1.domain
                and rc2.parent_code = rc1.code
                where o.offender_id_display = :p_noms_id
        and ob.active_flag = 'Y'
        order by oa.alert_date
    """
  )
}
