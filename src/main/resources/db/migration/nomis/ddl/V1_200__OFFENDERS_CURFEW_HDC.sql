CREATE TRIGGER OFFENDER_CURFEWS_HDC
    AFTER UPDATE ON OFFENDER_CURFEWS
    REFERENCING OLD AS OLD NEW AS NEW
    FOR EACH ROW
BEGIN ATOMIC
    IF NEW.passed_flag = 'Y' AND
       nvl(NEW.passed_flag ,'***') <> nvl(OLD.passed_flag , '***')
    THEN
        INSERT INTO HDC_STATUS_TRACKINGS(HDC_STATUS_TRACKING_ID ,OFFENDER_CURFEW_ID ,STATUS_CODE, UPDATE_DATE)
        VALUES(hdc_status_tracking_id.nextval, NEW.offender_curfew_id, 'MAN_CK_PASS', SYSDATE);

        INSERT INTO HDC_STATUS_TRACKINGS(HDC_STATUS_TRACKING_ID ,OFFENDER_CURFEW_ID, STATUS_CODE, UPDATE_DATE)
        VALUES(hdc_status_tracking_id.nextval, NEW.offender_curfew_id, 'ELIGIBLE', SYSDATE);

    ELSEIF NEW.passed_flag = 'N' AND
          nvl(NEW.passed_flag ,'***') <> nvl(OLD.passed_flag , '***')
    THEN
        INSERT INTO HDC_STATUS_TRACKINGS(HDC_STATUS_TRACKING_ID ,OFFENDER_CURFEW_ID ,STATUS_CODE, UPDATE_DATE)
        VALUES(hdc_status_tracking_id.nextval, NEW.offender_curfew_id, 'MAN_CK_FAIL', SYSDATE);

        INSERT INTO HDC_STATUS_TRACKINGS(HDC_STATUS_TRACKING_ID ,OFFENDER_CURFEW_ID, STATUS_CODE, UPDATE_DATE)
        VALUES(hdc_status_tracking_id.nextval, NEW.offender_curfew_id, 'INELIGIBLE', SYSDATE);
    END IF;

    IF NEW.approval_status = 'APPROVED'  AND
       nvl(NEW.approval_status, '***') <> nvl(OLD.approval_status , '***')
    THEN
        INSERT INTO HDC_STATUS_TRACKINGS(HDC_STATUS_TRACKING_ID ,OFFENDER_CURFEW_ID ,STATUS_CODE, UPDATE_DATE)
        VALUES(hdc_status_tracking_id.nextval, NEW.offender_curfew_id, 'GRANTED', SYSDATE);
    END IF;
    IF NEW.review_reason = 'OPT_IN'  AND
       nvl(NEW.review_reason, '***') <> nvl(OLD.review_reason , '***')
    THEN
        INSERT INTO HDC_STATUS_TRACKINGS(HDC_STATUS_TRACKING_ID, OFFENDER_CURFEW_ID, STATUS_CODE, UPDATE_DATE)
        VALUES(hdc_status_tracking_id.nextval, NEW.offender_curfew_id, 'OPT_IN', SYSDATE);
    ELSEIF NEW.review_reason IS NOT NULL AND
          NEW.review_reason <> 'OPT_IN' AND
          nvl(NEW.review_reason, '***') <> nvl(OLD.review_reason , '***')
    THEN

        INSERT INTO HDC_STATUS_TRACKINGS(HDC_STATUS_TRACKING_ID, OFFENDER_CURFEW_ID, STATUS_CODE, UPDATE_DATE)
        VALUES(hdc_status_tracking_id.nextval, NEW.offender_curfew_id, 'CHANGE', SYSDATE);
    END IF;

END;
