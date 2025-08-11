package uk.gov.justice.hmpps.prison.repository.sql

enum class InmateAlertRepositorySql(val sql: String) {
  FIND_INMATE_ALERTS(
    """
        SELECT ALERT_SEQ,
        ALERT_DATE,
        ALERT_TYPE,
        COALESCE(alType.DESCRIPTION, ALERT_TYPE) as ALERT_TYPE_DESC,
        ALERT_CODE,
        COALESCE(alCode.DESCRIPTION, ALERT_CODE) as ALERT_CODE_DESC,
        EXPIRY_DATE,
        OA.MODIFY_DATETIME,
        ALERT_STATUS,
        COMMENT_TEXT,
        SMC.FIRST_NAME                              ADD_FIRST_NAME,
        SMC.LAST_NAME                               ADD_LAST_NAME,
        SMU.FIRST_NAME                              UPDATE_FIRST_NAME,
        SMU.LAST_NAME                               UPDATE_LAST_NAME

        FROM OFFENDER_ALERTS OA
        LEFT JOIN REFERENCE_CODES ALTYPE ON ALTYPE.DOMAIN = 'ALERT' and ALTYPE.CODE = ALERT_TYPE
        LEFT JOIN REFERENCE_CODES ALCODE ON ALCODE.DOMAIN = 'ALERT_CODE' and ALCODE.CODE = ALERT_CODE

        LEFT JOIN STAFF_USER_ACCOUNTS SUAC ON SUAC.USERNAME = OA.CREATE_USER_ID
        LEFT JOIN STAFF_MEMBERS SMC on SUAC.STAFF_ID = SMC.STAFF_ID

        LEFT JOIN STAFF_USER_ACCOUNTS SUAU on SUAU.USERNAME = OA.MODIFY_USER_ID
        left join STAFF_MEMBERS SMU on SUAU.STAFF_ID = SMU.STAFF_ID

        WHERE OA.OFFENDER_BOOK_ID = :bookingId and (:alertStatus IS NULL OR OA.ALERT_STATUS = :alertStatus)
    """,
  ),
}
