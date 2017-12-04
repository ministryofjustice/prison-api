CREATE OR REPLACE TRIGGER OMTOFFSRC
BEFORE INSERT OR DELETE OR UPDATE
  ON offenders
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
  BEGIN
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;

    IF INSERTING
       OR (UPDATING
           AND :NEW.last_name != :OLD.last_name)
    THEN
      :NEW.last_name_soundex := SOUNDEX(:NEW.last_name);
      :NEW.last_name_key := oms_miscellaneous.return_key(:NEW.last_name);
      :NEW.last_name_alpha_key := SUBSTR(:NEW.last_name, 1, 1);
    END IF;

    IF INSERTING
       OR (UPDATING
           AND :NEW.first_name != :OLD.first_name)
    THEN
      :NEW.first_name_key := oms_miscellaneous.return_key(:NEW.first_name);
    END IF;

    IF :NEW.middle_name IS NOT NULL
       AND (INSERTING
            OR (UPDATING
                AND :NEW.middle_name != :OLD.middle_name)
       )
    THEN
      :NEW.middle_name_key :=
      oms_miscellaneous.return_key(:NEW.middle_name);
    END IF;

    IF ((INSERTING
         AND :NEW.root_offender_id IS NULL)
        OR (UPDATING
            AND :OLD.root_offender_id IS NULL)
    )
    THEN
      :NEW.root_offender_id :=
      NVL(:NEW.alias_offender_id, :NEW.offender_id);
    END IF;
  END;
/

CREATE OR REPLACE TRIGGER OFFENDERS_TA
BEFORE INSERT OR UPDATE OR DELETE ON OFFENDERS
REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW
  DECLARE
    l_scn NUMBER;
    l_tid VARCHAR2(32);
  BEGIN
    IF INSERTING
    THEN
      :NEW.create_datetime := NVL(:NEW.create_datetime, systimestamp);
      :NEW.create_user_id := NVL(:NEW.create_user_id, user);
    ELSIF UPDATING
      THEN
        :NEW.modify_datetime := systimestamp;
        :NEW.modify_user_id := user;
    END IF;
    IF NOT DELETING
    THEN
      :NEW.Audit_timestamp := systimestamp;
      :NEW.Audit_User_ID := SYS_CONTEXT('NOMIS', 'AUDIT_USER_ID', 30);
      :NEW.Audit_Module_Name := SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 65);
      :NEW.Audit_Client_User_ID := SYS_CONTEXT('NOMIS', 'AUDIT_CLIENT_USER_ID', 64);
      :NEW.Audit_Client_IP_Address := SYS_CONTEXT('NOMIS', 'AUDIT_CLIENT_IP_ADDRESS', 39);
      :NEW.Audit_Client_Workstation_Name := SYS_CONTEXT('NOMIS', 'AUDIT_CLIENT_WORKSTATION_NAME', 64);
      :NEW.Audit_Additional_Info := SYS_CONTEXT('NOMIS', 'AUDIT_ADDITIONAL_INFO', 256);
    ELSE
      l_tid := DBMS_TRANSACTION.local_transaction_id(create_transaction=>FALSE);
      SELECT current_scn
      INTO l_scn
      FROM v$database;
      INSERT INTO OMS_DELETED_ROWS
      (
        table_name,
        xid,
        scn,
        audit_timestamp,
        audit_user_id,
        audit_module_name,
        audit_client_user_id,
        audit_client_ip_address,
        audit_client_workstation_name,
        audit_additional_info
      )
      VALUES
        (
          'OFFENDERS',
          converttoxid(l_tid),
          l_scn,
          systimestamp,
          SYS_CONTEXT('NOMIS', 'AUDIT_USER_ID', 30),
          SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 65),
          SYS_CONTEXT('NOMIS', 'AUDIT_CLIENT_USER_ID', 64),
          SYS_CONTEXT('NOMIS', 'AUDIT_CLIENT_IP_ADDRESS', 39),
          SYS_CONTEXT('NOMIS', 'AUDIT_CLIENT_WORKSTATION_NAME', 64),
          SYS_CONTEXT('NOMIS', 'AUDIT_ADDITIONAL_INFO', 256)
        );
    END IF;
  END;
/

CREATE OR REPLACE TRIGGER OFFENDERS_T1
AFTER
DELETE
  ON OFFENDERS
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
  DECLARE V_numrows INTEGER;
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;

    SELECT count(*)
    INTO v_numrows
    FROM addresses
    WHERE owner_Class = 'OFF'
          AND Owner_id = :old.offENDer_id;
    IF (v_numrows > 0)
    THEN
      raise_application_error(
          -20001,
          'Cannot DELETE the offENDer record because offENDer address records exists.'
      );
    END IF;

    SELECT count(*)
    INTO v_numrows
    FROM phones
    WHERE owner_Class = 'OFF'
          AND Owner_id = :old.offENDer_id;
    IF (v_numrows > 0)
    THEN
      raise_application_error(
          -20001,
          'Cannot DELETE the offENDer record because offENDer phone records exists.'
      );
    END IF;

    SELECT count(*)
    INTO v_numrows
    FROM internet_addresses
    WHERE owner_Class = 'OFF'
          AND Owner_id = :old.offENDer_id;
    IF (v_numrows > 0)
    THEN
      raise_application_error(
          -20001,
          'Cannot DELETE the offENDer record because offENDer internet addresses records exists.'
      );
    END IF;

    EXCEPTION
    WHEN OTHERS THEN
    tag_error.handle();
  END;
/

