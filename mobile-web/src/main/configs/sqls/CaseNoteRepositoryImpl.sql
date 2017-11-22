FIND_CASENOTES {
    SELECT CN.OFFENDER_BOOK_ID,
           CN.CASE_NOTE_TYPE,
           RC1.DESCRIPTION as CASE_NOTE_TYPE_DESC,
           CN.CASE_NOTE_SUB_TYPE,
           RC2.DESCRIPTION as CASE_NOTE_SUB_TYPE_DESC,
           CN.CASE_NOTE_TEXT,
           CN.CASE_NOTE_ID,
           CN.NOTE_SOURCE_CODE,
           CN.CREATE_DATETIME,
           concat(SM.LAST_NAME, concat(', ', SM.FIRST_NAME)) AS STAFF_NAME,
           CN.CONTACT_TIME
      FROM OFFENDER_CASE_NOTES CN
        JOIN reference_codes RC1 on RC1.code = CN.CASE_NOTE_TYPE AND RC1.domain = 'TASK_TYPE'
        JOIN reference_codes RC2 on RC2.code = CN.CASE_NOTE_SUB_TYPE AND RC2.domain = 'TASK_SUBTYPE'
        JOIN STAFF_MEMBERS SM ON SM.STAFF_ID = CN.STAFF_ID
      WHERE CN.OFFENDER_BOOK_ID = :bookingId

}

INSERT_CASE_NOTE {
    INSERT INTO OFFENDER_CASE_NOTES (
        CASE_NOTE_ID,
        OFFENDER_BOOK_ID,
        CONTACT_DATE,
        CONTACT_TIME,
        CASE_NOTE_TYPE,
        CASE_NOTE_SUB_TYPE,
        STAFF_ID,
	      CASE_NOTE_TEXT,
	      DATE_CREATION,
	      TIME_CREATION,
	      CREATE_USER_ID,
	      NOTE_SOURCE_CODE
	  ) VALUES (
	      CASE_NOTE_ID.NEXTVAL,
	      :bookingId,
	      :contactDate,
	      :contactTime,
	      :type,
	      :subType,
	      (SELECT  STAFF_ID FROM STAFF_MEMBERS WHERE USER_ID = :userId),
	      :text,
	      :createDate,
	      :createTime,
	      :createdBy,
	      :sourceCode
	  )
}

UPDATE_CASE_NOTE {
    UPDATE OFFENDER_CASE_NOTES SET
           CASE_NOTE_TEXT = :text,
           MODIFY_USER_ID = :modifyBy,
           AMENDMENT_FLAG = 'Y'
     WHERE CASE_NOTE_ID = :caseNoteId
}

FIND_CASENOTE{
    SELECT CN.OFFENDER_BOOK_ID,
           CN.CASE_NOTE_TYPE,
           RC1.DESCRIPTION as CASE_NOTE_TYPE_DESC,
           CN.CASE_NOTE_SUB_TYPE,
           RC2.DESCRIPTION as CASE_NOTE_SUB_TYPE_DESC,
           CN.CASE_NOTE_TEXT,
           CN.CASE_NOTE_ID,
           CN.NOTE_SOURCE_CODE,
           CN.CREATE_DATETIME,
           concat(SM.LAST_NAME, concat(', ', SM.FIRST_NAME)) AS STAFF_NAME,
           CN.CONTACT_TIME
      FROM OFFENDER_CASE_NOTES CN
        JOIN reference_codes RC1 on RC1.code = CN.CASE_NOTE_TYPE AND RC1.domain = 'TASK_TYPE'
        JOIN reference_codes RC2 on RC2.code = CN.CASE_NOTE_SUB_TYPE AND RC2.domain = 'TASK_SUBTYPE'
        JOIN STAFF_MEMBERS SM ON SM.STAFF_ID = CN.STAFF_ID
     WHERE CN.OFFENDER_BOOK_ID = :bookingId AND CN.CASE_NOTE_ID = :caseNoteId
}

GET_CASE_NOTE_COUNT {
  SELECT COUNT(*)
  FROM OFFENDER_CASE_NOTES
  WHERE OFFENDER_BOOK_ID = :bookingId
    AND CASE_NOTE_TYPE = :type
    AND CASE_NOTE_SUB_TYPE = :subType
    AND CONTACT_DATE >= TRUNC(COALESCE(:fromDate, CONTACT_DATE))
    AND TRUNC(CONTACT_DATE) <= COALESCE(:toDate, CONTACT_DATE)
}
