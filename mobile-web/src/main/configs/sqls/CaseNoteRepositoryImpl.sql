FIND_CASENOTES {
    SELECT CN.OFFENDER_BOOK_ID,
           CN.CASE_NOTE_TYPE,
           CN.CASE_NOTE_SUB_TYPE,
           CN.CASE_NOTE_TEXT,
           CN.CASE_NOTE_ID,
           CN.NOTE_SOURCE_CODE,
           CN.CREATE_DATETIME,
           CN.CREATE_USER_ID,
           CN.CONTACT_DATE
      FROM OFFENDER_CASE_NOTES CN
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
	      :bookingID,
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
           MODIFY_USER_ID = :modifyBy
     WHERE CASE_NOTE_ID = :caseNoteId
}

FIND_CASENOTE{
    SELECT CN.OFFENDER_BOOK_ID,
           CN.CASE_NOTE_TYPE,
           CN.CASE_NOTE_SUB_TYPE,
           CN.CASE_NOTE_TEXT,
           CN.CASE_NOTE_ID,
           CN.NOTE_SOURCE_CODE,
           CN.CREATE_DATETIME,
           CN.CREATE_USER_ID,
           CN.CONTACT_DATE
      FROM OFFENDER_CASE_NOTES CN
     WHERE CN.OFFENDER_BOOK_ID = :bookingId AND CN.CASE_NOTE_ID = :caseNoteId

}

