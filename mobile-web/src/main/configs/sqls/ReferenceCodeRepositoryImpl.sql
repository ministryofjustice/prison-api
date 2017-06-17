FIND_REF_CODES {
	select DOMAIN, CODE, DESCRIPTION, PARENT_DOMAIN, PARENT_CODE, ACTIVE_FLAG   
	from reference_codes 
	where domain = :domain
}

FIND_REF_CODE_DESC {
	select DOMAIN, CODE, DESCRIPTION, PARENT_DOMAIN, PARENT_CODE, ACTIVE_FLAG   
	from reference_codes 
	where CODE = :code and domain = :domain
}

FIND_ALERT_REF_CODES {
	select DOMAIN, CODE, DESCRIPTION, PARENT_DOMAIN, PARENT_CODE, ACTIVE_FLAG   
	from reference_codes 
	where domain = :domain and parent_code=:parentCode 
}

FIND_ALERT_REF_CODE_DESC {
    SELECT DOMAIN, CODE, DESCRIPTION, PARENT_DOMAIN, PARENT_CODE, ACTIVE_FLAG
      FROM reference_codes
	   WHERE domain = :domain AND parent_code = :parentCode AND CODE = :code
}

FIND_CASE_NOTE_TYPES {
    SELECT DISTINCT rc.description,
           work_type code , rc.domain, rc.PARENT_DOMAIN, rc.PARENT_CODE, rc.ACTIVE_FLAG
      FROM works w
           JOIN reference_codes rc on rc.code = w.work_type
     WHERE workflow_type = 'CNOTE'
       AND rc.domain = 'TASK_TYPE'
       AND w.manual_select_flag ='Y'
       AND w.active_flag  = 'Y'
       AND rc.code  <> 'WR'
}

FIND_CASE_NOTE_TYPE_BY_CODE {
    SELECT DISTINCT rc.description,
           work_type code , rc.domain, rc.PARENT_DOMAIN, rc.PARENT_CODE, rc.ACTIVE_FLAG
      FROM works w
           JOIN reference_codes rc on rc.code = w.work_type
     WHERE workflow_type = 'CNOTE'
       AND w.work_type :typeCode
       AND rc.domain = 'TASK_TYPE'
       AND w.manual_select_flag ='Y'
       AND w.active_flag  = 'Y'
       AND rc.code  <> 'WR'
}

FIND_CNOTE_SUB_TYPES_BY_TYPECODE_AND_SUBTYPECODE {
	SELECT rc.description,
       w.work_sub_type code, rc.domain, rc.PARENT_DOMAIN, rc.PARENT_CODE, rc.ACTIVE_FLAG
	FROM works w join
		reference_codes rc on rc.code = w.work_sub_type
 	WHERE workflow_type = 'CNOTE'
	   AND rc.domain = 'TASK_SUBTYPE'
	   AND w.work_type = :typeCode
	   AND w.work_sub_type = :subTypeCode
	   AND w.manual_select_flag ='Y'
	   AND w.active_flag  = 'Y'
  	order by  description ,code
}


FIND_CNOTE_TYPES_BY_CASELOAD {
    SELECT DISTINCT rc.description,
           work_type code , rc.domain, rc.PARENT_DOMAIN, rc.PARENT_CODE, rc.ACTIVE_FLAG
      FROM works w
           JOIN reference_codes rc on rc.code = w.work_type
     WHERE workflow_type = 'CNOTE'
       AND rc.domain = 'TASK_TYPE'
       AND w.caseload_type IN ( (SELECT caseload_type FROM caseloads WHERE caseload_id = :caseLoad), 'BOTH')
       AND w.manual_select_flag ='Y'
       AND w.active_flag  = 'Y'
       AND rc.code  <> 'WR'
}

FIND_CNOTE_SUB_TYPES_BY_CASE_NOTE_TYPE {
	SELECT rc.description,
       w.work_sub_type code, rc.domain, rc.PARENT_DOMAIN, rc.PARENT_CODE, rc.ACTIVE_FLAG
	FROM works w join
		reference_codes rc on rc.code = w.work_sub_type
 	WHERE workflow_type = 'CNOTE'
	   AND rc.domain = 'TASK_SUBTYPE'
	   AND w.work_type = :typeCode
	   AND w.manual_select_flag ='Y'
	   AND w.active_flag  = 'Y'
  	order by  description ,code
}
