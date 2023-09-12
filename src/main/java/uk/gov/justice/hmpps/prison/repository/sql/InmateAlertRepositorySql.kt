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
        MODIFY_DATETIME,
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

  FIND_INMATE_ALERT(
    """
        select ALERT_SEQ,
        ALERT_DATE,
        ALERT_TYPE,
        ALERT_STATUS,
        EXPIRY_DATE,
        COALESCE(alType.DESCRIPTION, ALERT_TYPE) as ALERT_TYPE_DESC,
        ALERT_CODE,
        COALESCE(alCode.DESCRIPTION, ALERT_CODE) as ALERT_CODE_DESC,
        EXPIRY_DATE,
        MODIFY_DATETIME,
        COMMENT_TEXT,
        SMC.FIRST_NAME                              ADD_FIRST_NAME,
        SMC.LAST_NAME                               ADD_LAST_NAME,
        SMU.FIRST_NAME                              UPDATE_FIRST_NAME,
        SMU.LAST_NAME                               UPDATE_LAST_NAME
                from offender_alerts
                LEFT JOIN REFERENCE_CODES altype ON altype.DOMAIN = 'ALERT' and altype.CODE = ALERT_TYPE
        LEFT JOIN REFERENCE_CODES alCode ON alCode.DOMAIN = 'ALERT_CODE' and alCode.CODE = ALERT_CODE

        LEFT JOIN STAFF_USER_ACCOUNTS SUAC ON SUAC.USERNAME = offender_alerts.CREATE_USER_ID
                LEFT JOIN STAFF_MEMBERS SMC on SUAC.STAFF_ID = SMC.STAFF_ID

                LEFT JOIN STAFF_USER_ACCOUNTS SUAU on SUAU.USERNAME = offender_alerts.MODIFY_USER_ID
                left join STAFF_MEMBERS SMU on SUAU.STAFF_ID = SMU.STAFF_ID
                where OFFENDER_BOOK_ID = :bookingId
        and ALERT_SEQ = :alertSeqId
    """,
  ),

  FIND_INMATE_OFFENDERS_ALERTS(
    """
        SELECT OA.ALERT_SEQ,
        OA.OFFENDER_BOOK_ID,
        O.OFFENDER_ID_DISPLAY,
        OA.ALERT_DATE,
        OA.ALERT_TYPE,
        COALESCE(ALTYPE.DESCRIPTION, OA.ALERT_TYPE) AS ALERT_TYPE_DESC,
        OA.ALERT_CODE,
        COALESCE(ALCODE.DESCRIPTION, OA.ALERT_CODE) AS ALERT_CODE_DESC,
        OA.EXPIRY_DATE,
        OA.MODIFY_DATETIME,
        OA.ALERT_STATUS,
        OA.COMMENT_TEXT
        FROM OFFENDER_ALERTS OA
        INNER JOIN OFFENDER_BOOKINGS B ON B.OFFENDER_BOOK_ID = OA.OFFENDER_BOOK_ID
                INNER JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
                LEFT JOIN REFERENCE_CODES ALTYPE ON ALTYPE.DOMAIN = 'ALERT' AND ALTYPE.CODE = OA.ALERT_TYPE
        LEFT JOIN REFERENCE_CODES ALCODE ON ALCODE.DOMAIN = 'ALERT_CODE' AND ALCODE.CODE = OA.ALERT_CODE
        WHERE O.OFFENDER_ID_DISPLAY IN (:offenderNos) AND (:agencyId IS NULL OR B.AGY_LOC_ID = :agencyId)
    """,
  ),

  GET_ALERT_CANDIDATES(
    """
        select distinct o.offender_id_display as offender_no
        from OFFENDER_ALERTS icp
        join OFFENDER_BOOKINGS ob on ob.offender_book_id = icp.offender_book_id
        join OFFENDERS o on o.offender_id = ob.offender_id
        where icp.modify_datetime > :cutoffTimestamp
    """,
  ),

  CREATE_ALERT(
    """
        INSERT INTO OFFENDER_ALERTS (
                OFFENDER_BOOK_ID,
                ROOT_OFFENDER_ID,
                ALERT_TYPE,
                ALERT_CODE,
                ALERT_SEQ,
                ALERT_DATE,
                ALERT_STATUS,
                COMMENT_TEXT,
                CREATE_USER_ID,
                CASELOAD_TYPE
        )
        VALUES
        (:bookingId,
        (SELECT O.ROOT_OFFENDER_ID FROM OFFENDER_BOOKINGS O WHERE O.OFFENDER_BOOK_ID = :bookingId),
        :alertType,
        :alertSubType,
        COALESCE ((SELECT MAX(ALERT_SEQ) + 1 FROM OFFENDER_ALERTS WHERE OFFENDER_BOOK_ID = :bookingId),1),
        :alertDate,
        :status,
        :commentText,
        USER,
        :caseLoadType
        )
    """,
  ),

  LOCK_ALERT(
    """
        SELECT 1
        FROM OFFENDER_ALERTS
        WHERE OFFENDER_BOOK_ID = :bookingId
        AND ALERT_SEQ = :alertSeq
        FOR UPDATE
    """,
  ),

  EXPIRE_ALERT(
    """
        UPDATE OFFENDER_ALERTS SET
        ALERT_STATUS = :alertStatus,
        EXPIRY_DATE = :expiryDate,
        COMMENT_TEXT =
                CASE WHEN :comment is NULL
                        THEN (SELECT COMMENT_TEXT FROM OFFENDER_ALERTS OA WHERE OA.OFFENDER_BOOK_ID = :bookingId AND OA.ALERT_SEQ = :alertSeq)
        ELSE :comment
        END,
        MODIFY_USER_ID = USER
        WHERE ALERT_SEQ = :alertSeq
        AND OFFENDER_BOOK_ID = :bookingId
    """,
  ),

  UPDATE_ALERT_COMMENT(
    """
        UPDATE OFFENDER_ALERTS SET
        COMMENT_TEXT = :comment,
        MODIFY_USER_ID = USER
        WHERE ALERT_SEQ = :alertSeq
        AND OFFENDER_BOOK_ID = :bookingId
    """,
  ),

  INSERT_WORK_FLOW(
    """
        INSERT INTO WORK_FLOWS (
                WORK_FLOW_ID,
                OBJECT_CODE,
                OBJECT_ID,
                OBJECT_SEQ
        )
        VALUES (
                work_flow_id.NEXTVAL,
                :objectCode,
                :bookingId,
                :alertSeq
        )
    """,
  ),

  INSERT_WORK_FLOW_LOG(
    """
        INSERT INTO WORK_FLOW_LOGS (
                WORK_FLOW_ID,
                WORK_FLOW_SEQ,
                WORK_ACTION_CODE,
                WORK_ACTION_DATE,
                WORK_FLOW_STATUS,
                CREATE_DATE,
                CREATE_USER_ID
        )
        VALUES (
                :workFlowId,
                :workFlowSeq,
                :actionCode,
                SYSDATE,
                :workFlowStatus,
                SYSDATE,
                USER
        )
    """,
  ),

  INSERT_NEXT_WORK_FLOW_LOG(
    """
        INSERT INTO WORK_FLOW_LOGS
        (WORK_FLOW_ID,
        WORK_FLOW_SEQ,
        WORK_ACTION_CODE,
        WORK_ACTION_DATE,
        WORK_FLOW_STATUS,
        CREATE_DATE,
        CREATE_USER_ID)
        SELECT
        WF.WORK_FLOW_ID,
        (SELECT NVL (MAX (WORK_FLOW_SEQ), 0) + 1 FROM WORK_FLOW_LOGS WHERE WORK_FLOW_ID = WF.WORK_FLOW_ID),
        :actionCode,
        SYSDATE,
        :workFlowStatus,
        SYSDATE,
        USER
        FROM WORK_FLOWS WF
        WHERE WF.OBJECT_ID = :bookingId
        AND WF.OBJECT_SEQ = :alertSeq
        AND WF.OBJECT_CODE = :alertCode
    """,
  ),
}
