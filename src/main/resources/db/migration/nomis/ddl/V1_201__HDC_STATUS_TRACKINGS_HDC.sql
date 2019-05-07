CREATE TRIGGER hdc_status_trackings_hdc
    AFTER INSERT
    ON hdc_status_trackings
    REFERENCING NEW AS NEW
    FOR EACH ROW
BEGIN ATOMIC

    DECLARE v_enh VARCHAR (1);
    DECLARE v_rev_rea VARCHAR (40);

    IF NEW.status_code = 'AUTO_CK_PASS'
    THEN
        INSERT INTO hdc_status_reasons
        (hdc_status_reason_id,
         hdc_status_tracking_id,
         status_reason_code,
         reason_source)
        VALUES (hdc_status_reason_id.NEXTVAL,
                NEW.hdc_status_tracking_id,
                'SENT_CALC',
                NULL);
    ELSEIF NEW.status_code = 'SUIT_ASSESS'
    THEN
        INSERT INTO hdc_status_reasons
        (hdc_status_reason_id,
         hdc_status_tracking_id,
         status_reason_code,
         reason_source)
        VALUES (hdc_status_reason_id.NEXTVAL,
                NEW.hdc_status_tracking_id,
                'FIRST_REF',
                NULL);
    ELSEIF NEW.status_code = 'MAN_CK_PASS'
    THEN
        INSERT INTO hdc_status_reasons
        (hdc_status_reason_id,
         hdc_status_tracking_id,
         status_reason_code,
         reason_source)
        VALUES (hdc_status_reason_id.NEXTVAL,
                NEW.hdc_status_tracking_id,
                'MAN_CK',
                NULL);
    ELSEIF NEW.status_code = 'ELIGIBLE'
    THEN
        INSERT INTO hdc_status_reasons
        (hdc_status_reason_id,
         hdc_status_tracking_id,
         status_reason_code,
         reason_source)
        VALUES (hdc_status_reason_id.NEXTVAL,
                NEW.hdc_status_tracking_id,
                'PASS_ALL_CK',
                NULL);
    ELSEIF NEW.status_code = 'INELIGIBLE'
    THEN
        INSERT INTO hdc_status_reasons
        (hdc_status_reason_id,
         hdc_status_tracking_id,
         status_reason_code,
         reason_source)
        VALUES (hdc_status_reason_id.NEXTVAL,
                NEW.hdc_status_tracking_id,
                'MAN_CK_FAIL',
                NULL);
    ELSEIF NEW.status_code = 'ENH_ASSESS'
    THEN
        INSERT INTO hdc_status_reasons
        (hdc_status_reason_id,
         hdc_status_tracking_id,
         status_reason_code,
         reason_source)
        VALUES (hdc_status_reason_id.NEXTVAL,
                NEW.hdc_status_tracking_id,
                'BOARD_REF',
                NULL);
    ELSEIF NEW.status_code = 'GRANTED'
    THEN
--         set v_enh = ocdtrhdc.get_param('ENH_ASSESS');
        set v_enh = 'Y';

        IF v_enh IS NOT NULL
        THEN
            INSERT INTO hdc_status_reasons
            (hdc_status_reason_id,
             hdc_status_tracking_id,
             status_reason_code,
             reason_source)
            VALUES (hdc_status_reason_id.NEXTVAL,
                    NEW.hdc_status_tracking_id,
                    'AFTER_ENH',
                    NULL);
        ELSE
            INSERT INTO hdc_status_reasons
            (hdc_status_reason_id,
             hdc_status_tracking_id,
             status_reason_code,
             reason_source)
            VALUES (hdc_status_reason_id.NEXTVAL,
                    NEW.hdc_status_tracking_id,
                    'AFTER_SUIT',
                    NULL);
        END IF;
    ELSEIF NEW.status_code = 'OPT_IN'
    THEN
        INSERT INTO hdc_status_reasons
        (hdc_status_reason_id,
         hdc_status_tracking_id,
         status_reason_code,
         reason_source)
        VALUES (hdc_status_reason_id.NEXTVAL,
                NEW.hdc_status_tracking_id,
                'INM_REQUEST',
                NULL);
    ELSEIF NEW.status_code = 'CHANGE'
    THEN
--         SET v_rev_rea = ocdtrhdc.get_param('REV_REA');
        SET v_rev_rea = 'REV_DUMMY';

        INSERT INTO hdc_status_reasons
        (hdc_status_reason_id,
         hdc_status_tracking_id,
         status_reason_code,
         reason_source)
        VALUES (hdc_status_reason_id.NEXTVAL,
                NEW.hdc_status_tracking_id,
                v_rev_rea,
                NULL);
    END IF;

END;