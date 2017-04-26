FIND_CASENOTES {
    select OFFENDER_BOOK_ID, 
    CASE_NOTE_TYPE, 
    CASE_NOTE_SUB_TYPE, 
    CASE_NOTE_TEXT, 
    CASE_NOTE_ID, 
    NOTE_SOURCE_CODE, 
    CREATE_DATETIME, 
    CREATE_USER_ID,
    CONTACT_DATE
    from offender_case_notes 
    where offender_Book_Id = :bookingId
}

INSERT_CASE_NOTE {
	INSERT INTO OFFENDER_CASE_NOTES (CASE_NOTE_ID, OFFENDER_BOOK_ID, 
	CONTACT_DATE, CONTACT_TIME, CASE_NOTE_TYPE, CASE_NOTE_SUB_TYPE, STAFF_ID, 
	CASE_NOTE_TEXT, DATE_CREATION, TIME_CREATION, CREATE_USER_ID, NOTE_SOURCE_CODE, MODIFY_DATETIME) 
	VALUES 
    (case_note_id.nextval, :bookingID, :contactDate, :contactTime, :type, :subType, (SELECT  STAFF_ID FROM staff_members where user_Id = :user_Id), :text, :createDate, :createTime, :createdBy, :sourceCode, null)

}

UPDATE_CASE_NOTE {
	UPDATE OFFENDER_CASE_NOTES SET CASE_NOTE_TEXT = :text,
                              MODIFY_USER_ID = :modifyBy
                              WHERE CASE_NOTE_ID = :caseNoteId
}

FIND_CaseNote{
	select OFFENDER_BOOK_ID, 
    CASE_NOTE_TYPE, 
    CASE_NOTE_SUB_TYPE, 
    CASE_NOTE_TEXT, 
    CASE_NOTE_ID, 
    NOTE_SOURCE_CODE, 
    CREATE_DATETIME, 
    CREATE_USER_ID,
    CONTACT_DATE
    from offender_case_notes 
    where offender_Book_Id = :bookingId and CASE_NOTE_ID = :caseNoteId
}