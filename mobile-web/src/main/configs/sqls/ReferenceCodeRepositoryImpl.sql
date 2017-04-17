FIND_REF_CODES {
	select DESCRIPTION, CODE   
	from reference_codes 
	where domain = :domain and ACTIVE_FLAG = 'Y'
  	order by  description ,code
}

FIND_REF_CODE_DESC {
	select DESCRIPTION, CODE   
	from reference_codes 
	where CODE = :code and domain = :domain
}

FIND_CNOTE_TYPES_BY_CASE_LOAD {
	SELECT DISTINCT rc.description, 
       work_type code 
  	FROM works w,
       reference_codes rc
 	WHERE workflow_type = 'CNOTE'
	   AND rc.domain = 'TASK_TYPE'
	   AND rc.code = w.work_type
	   AND (w.caseload_type IN (:caseLoad, 'BOTH' )
	   AND   w.manual_select_flag ='Y' 
	   AND w.active_flag  = 'Y')
	   AND rc.code  <> 'WR'
  	order by  description ,code
}

FIND_CNOTE_SUB_TYPES_BY_CASE_NOTE_TYPE {
	SELECT DISTINCT rc.description, 
       w.work_sub_type code
  	FROM works w,
       reference_codes rc
 	WHERE workflow_type = 'CNOTE'
	   AND rc.domain = 'TASK_SUBTYPE'
	   AND rc.code = w.work_sub_type
	   AND ((w.work_type = :caseNoteType  
	   AND  w.manual_select_flag ='Y' 
	   AND  w.active_flag  = 'Y') )
  	order by  description ,code
}