FIND_CASENOTES {
    select OFFENDER_BOOK_ID, 
    CASE_NOTE_TYPE, 
    CASE_NOTE_SUB_TYPE, 
    CASE_NOTE_TEXT, 
    STAFF_ID, 
    CASE_NOTE_ID, 
    NOTE_SOURCE_CODE, 
    CREATE_DATETIME, 
    CREATE_USER_ID,
    MODIFY_DATETIME
    from offender_case_notes 
    where offender_Book_Id = :bookingId
}