INSERT_CASE_NOTE {
    INSERT INTO OFFENDER_CASE_NOTES (
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
	      :bookingId,
	      :contactDate,
	      :contactTime,
	      :type,
	      :subType,
	      (SELECT STAFF_ID FROM STAFF_USER_ACCOUNTS WHERE USERNAME = :userId),
	      :text,
	      :createDate,
	      :createTime,
	      :createdBy,
	      :sourceCode
	  )
		RETURNING CASE_NOTE_ID
}

GET_CASE_NOTE_COUNT {
  SELECT COUNT(*)
  FROM OFFENDER_CASE_NOTES
  WHERE OFFENDER_BOOK_ID = :bookingId
    AND CASE_NOTE_TYPE = :type
    AND CASE_NOTE_SUB_TYPE = :subType
    AND CONTACT_DATE >= date_trunc('day', COALESCE(:fromDate, CONTACT_DATE))
    AND date_trunc('day', CONTACT_DATE) <= COALESCE(:toDate, CONTACT_DATE)
}
