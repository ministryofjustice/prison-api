FIND_REFERENCE_CODES_BY_DOMAIN_PARENT {
    SELECT code, domain, description, parent_domain, parent_code, active_flag
	    FROM reference_codes
	   WHERE domain = :domain AND parent_code = :parentCode
}

FIND_REFERENCE_CODES_BY_DOMAIN {
    SELECT code, domain, description, parent_domain, parent_code, active_flag
	    FROM reference_codes
	   WHERE domain = :domain
}

FIND_REFERENCE_CODE_BY_DOMAIN_CODE {
    SELECT code, domain, description, parent_domain, parent_code, active_flag
	    FROM reference_codes
	   WHERE domain = :domain AND code = :code
}

FIND_REFERENCE_CODE_BY_DOMAIN_PARENT_CODE {
    SELECT code, domain, description, parent_domain, parent_code, active_flag
	    FROM reference_codes
	   WHERE domain = :domain AND  parent_code = :parentCode AND code = :code
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
					work_type code, 
					rc.domain, 
					rc.PARENT_DOMAIN, 
					rc.PARENT_CODE, 
					rc.ACTIVE_FLAG
	FROM works w JOIN reference_codes rc on rc.code = w.work_type
	WHERE workflow_type = 'CNOTE'
	AND rc.domain = 'TASK_TYPE'
	AND w.caseload_type IN ( (SELECT caseload_type FROM caseloads WHERE caseload_id = :caseLoad), 'BOTH')
	AND w.manual_select_flag ='Y'
	AND w.active_flag  = 'Y'
	AND rc.code  <> 'WR'
}

FIND_CNOTE_SUB_TYPES_BY_CASE_NOTE_TYPE {
	SELECT	rc.description,
			w.work_sub_type code, 
			rc.domain, rc.PARENT_DOMAIN, 
			rc.PARENT_CODE, 
			rc.ACTIVE_FLAG
	FROM works w join reference_codes rc on rc.code = w.work_sub_type
 	WHERE workflow_type = 'CNOTE'
	AND rc.domain = 'TASK_SUBTYPE'
	AND w.work_type = :caseNoteType
	AND w.manual_select_flag ='Y'
	AND w.active_flag  = 'Y'
  	order by  description ,code
}
