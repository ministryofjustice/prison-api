package uk.gov.justice.hmpps.prison.repository.sql

enum class OffenderCurfewRepositorySql(val sql: String) {
  OFFENDER_CURFEWS(
    """
        SELECT oc.offender_book_id,
        oc.offender_curfew_id,
        oc.assessment_date,
        oc.approval_status,
        oc.ard_crd_date
        FROM OFFENDER_CURFEWS oc
        INNER JOIN offender_bookings ob ON ob.offender_book_id = oc.offender_book_id
        WHERE     OB.active_flag = 'Y'
        AND OB.booking_seq = 1
        AND OB.agy_loc_id IN (:agencyLocationIds)
    """
  ),

  UPDATE_CURFEW_CHECKS_PASSED(
    """
        UPDATE OFFENDER_CURFEWS
                SET ASSESSMENT_DATE = :date,
        PASSED_FLAG = :checksPassed
                WHERE OFFENDER_CURFEW_ID = :curfewId
    """
  ),

  UPDATE_CURFEW_CHECKS_PASSED_DATE(
    """
        UPDATE OFFENDER_CURFEWS
                SET ASSESSMENT_DATE = :date
        WHERE OFFENDER_CURFEW_ID = :curfewId
    """
  ),

  UPDATE_APPROVAL_STATUS(
    """
        UPDATE OFFENDER_CURFEWS
                SET DECISION_DATE = :date,
        APPROVAL_STATUS = :approvalStatus
                WHERE OFFENDER_CURFEW_ID = :curfewId
    """
  ),

  UPDATE_APPROVAL_STATUS_DATE(
    """
        UPDATE OFFENDER_CURFEWS
                SET DECISION_DATE = :date
        WHERE OFFENDER_CURFEW_ID = :curfewId
    """
  ),

  CREATE_HDC_STATUS_TRACKING(
    """
        INSERT INTO HDC_STATUS_TRACKINGS (
                HDC_STATUS_TRACKING_ID,
                OFFENDER_CURFEW_ID,
                STATUS_CODE,
                UPDATE_DATE
        )
        VALUES (
                HDC_STATUS_TRACKING_ID.NEXTVAL,
                :offenderCurfewId,
                :statusCode,
                sysdate
        )
    """
  ),

  CREATE_HDC_STATUS_REASON(
    """
        INSERT INTO HDC_STATUS_REASONS (
                HDC_STATUS_REASON_ID,
                HDC_STATUS_TRACKING_ID,
                STATUS_REASON_CODE
        )
        VALUES (
                HDC_STATUS_REASON_ID.NEXTVAL,
                :hdcStatusTrackingId,
                :statusReasonCode
        )
    """
  ),

  LATEST_HOME_DETENTION_CURFEW(
    """
        SELECT *
                FROM (
                        SELECT OC.OFFENDER_CURFEW_ID  AS ID,
                        OC.APPROVAL_STATUS,
                        HSR.STATUS_REASON_CODE AS REFUSED_REASON,
                        OC.DECISION_DATE       AS APPROVAL_STATUS_DATE,
                        OC.PASSED_FLAG         AS PASSED,
                        OC.ASSESSMENT_DATE     AS CHECKS_PASSED_DATE
                                FROM OFFENDER_CURFEWS OC
                                LEFT JOIN HDC_STATUS_TRACKINGS HST ON HST.OFFENDER_CURFEW_ID = OC.OFFENDER_CURFEW_ID AND
                                HST.STATUS_CODE IN ( :statusTrackingCodes )
                                LEFT JOIN HDC_STATUS_REASONS HSR   ON HST.HDC_STATUS_TRACKING_ID = HSR.HDC_STATUS_TRACKING_ID
                                WHERE OFFENDER_BOOK_ID = :bookingId
                                ORDER BY OC.CREATE_DATETIME DESC,
                        HST.HDC_STATUS_TRACKING_ID DESC,
                        HSR.HDC_STATUS_REASON_ID DESC
                ) WHERE ROWNUM = 1
    """
  ),

  UPDATE_HDC_STATUS_REASON(
    """
        UPDATE HDC_STATUS_REASONS
                SET STATUS_REASON_CODE = :hdcStatusReason
        WHERE HDC_STATUS_TRACKING_ID = (
                SELECT HDC_STATUS_TRACKING_ID
                        FROM HDC_STATUS_TRACKINGS
                        WHERE OFFENDER_CURFEW_ID = :offenderCurfewId AND
                STATUS_CODE = :hdcStatusTrackingCode
        )
    """
  ),

  FIND_HDC_STATUS_TRACKING(
    """
        SELECT HDC_STATUS_TRACKING_ID
                FROM HDC_STATUS_TRACKINGS
                WHERE OFFENDER_CURFEW_ID = :curfewId AND
        STATUS_CODE = :statusCode
    """
  ),

  DELETE_HDC_STATUS_TRACKINGS(
    """
        DELETE
        FROM HDC_STATUS_TRACKINGS
                WHERE OFFENDER_CURFEW_ID = :curfewId AND
        STATUS_CODE IN (:codes)
    """
  ),

  DELETE_HDC_STATUS_REASONS(
    """
        DELETE
        FROM HDC_STATUS_REASONS
                WHERE HDC_STATUS_TRACKING_ID IN (
                select HDC_STATUS_TRACKING_ID
                        from HDC_STATUS_TRACKINGS
                        where OFFENDER_CURFEW_ID = :curfewId AND
                STATUS_CODE IN (:codes)
        )
    """
  ),

  RESET_OFFENDER_CURFEW(
    """
        UPDATE OFFENDER_CURFEWS
                SET PASSED_FLAG = NULL,
        APPROVAL_STATUS = NULL,
        DECISION_DATE = NULL,
        ASSESSMENT_DATE = NULL
        WHERE OFFENDER_CURFEW_ID = :curfewId
    """
  )
}
