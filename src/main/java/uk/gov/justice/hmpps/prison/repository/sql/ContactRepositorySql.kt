package uk.gov.justice.hmpps.prison.repository.sql

enum class ContactRepositorySql(val sql: String) {
  RELATIONSHIP_TO_OFFENDER(
    """
        SELECT
        O.OFFENDER_CONTACT_PERSON_ID RELATIONSHIP_ID,
        P.PERSON_ID,
        P.LAST_NAME,
        P.FIRST_NAME,
        P.MIDDLE_NAME,
        O.CONTACT_TYPE,
        RC.DESCRIPTION AS CONTACT_DESCRIPTION,
        O.RELATIONSHIP_TYPE,
        RR.DESCRIPTION AS RELATIONSHIP_DESCRIPTION,
        O.EMERGENCY_CONTACT_FLAG,
        O.NEXT_OF_KIN_FLAG,
        O.ACTIVE_FLAG,
        O.EXPIRY_DATE,
        O.CONTACT_ROOT_OFFENDER_ID,
        O.APPROVED_VISITOR_FLAG,
        O.COMMENT_TEXT,
        O.CAN_BE_CONTACTED_FLAG,
        O.AWARE_OF_CHARGES_FLAG,
        O.CREATE_DATETIME,
        O.OFFENDER_BOOK_ID AS BOOKING_ID
        FROM OFFENDER_CONTACT_PERSONS O
        INNER JOIN PERSONS P ON P.PERSON_ID = O.PERSON_ID
        JOIN REFERENCE_CODES RC ON O.CONTACT_TYPE = RC.CODE and RC.DOMAIN = 'CONTACTS'
        JOIN REFERENCE_CODES RR ON O.RELATIONSHIP_TYPE = RR.CODE and RR.DOMAIN = 'RELATIONSHIP'
        WHERE  O.OFFENDER_BOOK_ID = :bookingId
        AND O.RELATIONSHIP_TYPE = COALESCE(:relationshipType, O.RELATIONSHIP_TYPE)
    """,
  ),
}
