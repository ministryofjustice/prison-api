
CREATE OR REPLACE TRIGGER "OFFENDER_EXTERNAL_MOVEMENTS_T9"
AFTER INSERT
  ON offender_external_movements
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW
WHEN (NEW.movement_type IN ('REL', 'TRN'))
  BEGIN
    /*=============================================================================
    Version Number = 2.3 Date Modified = 09-Apr-2009
    ==============================================================================*/

    /******************************************************************************
    NAME:       OFFENDER_EXTERNAL_MOVEMENTS_T9
    PURPOSE:    To end the offender pay and no pay statuses on transfer or release

    REVISIONS:
    Ver        Date        Author           Description
    ---------  ----------  ---------------  ------------------------------------
    2.3        09/04/2009   Laurence         QC#14987 Performance.  Now does not fire if
                                             context in 'XTAG_MOVMENT_TRANSACTION' i.e.
                                             Migration / IEDT.
    2.2        02-Apr-2009  Ragini           15029: Added code to stop the execution for merge
    2.1        09/03/2009   PThakur          2. D#14477: Comment_text should not be updated.
    2.0        24/02/2009   PThakur          1. D#14161: Created this trigger.
    ******************************************************************************/

    -- Lines added by Ragini on 02-Apr-2009 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) IN ('MERGE', 'XTAG_MOVEMENT_TRANSACTIONS')
    THEN
      RETURN;
    END IF;
    UPDATE offender_pay_statuses ops
    SET ops.end_date = TRUNC(SYSDATE)
    --,ops.comment_text =  DECODE(:NEW.movement_type, 'REL','Released','TRN','Transferred',:NEW.movement_type)
    WHERE ops.offender_book_id = :NEW.offender_book_id
          AND (ops.end_date > TRUNC(SYSDATE)
               OR ops.end_date IS NULL);


    UPDATE offender_no_pay_periods onpp
    SET onpp.end_date = TRUNC(SYSDATE)
    --,onpp.comment_text =  DECODE(:NEW.movement_type, 'REL','Released','TRN','Transferred',:NEW.movement_type)
    WHERE onpp.offender_book_id = :NEW.offender_book_id
          AND (onpp.end_date > TRUNC(SYSDATE)
               OR onpp.end_date IS NULL);

    EXCEPTION
    WHEN OTHERS
    THEN
    tag_error.handle;
  END offender_external_movements_t9;

/



CREATE OR REPLACE TRIGGER "OFFENDER_EXTERNAL_MOVEMENTS_T1"
AFTER INSERT OR UPDATE OF movement_reason_code, movement_type
  ON offender_external_movements
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
  DECLARE
    v_rowid ROWID;

    CURSOR lock_cur (
      p_offender_book_id offender_bookings.offender_book_id%TYPE
    )
    IS
      SELECT ROWID
      FROM offender_bookings
      WHERE offender_book_id = p_offender_book_id
      FOR UPDATE OF status_reason NOWAIT;
    --@@@ Sunil 31/05/2017 QC#20526 Key Worker changes
    CURSOR lock_key_workers (
      p_offender_book_id offender_bookings.offender_book_id%TYPE)
    IS
      SELECT ROWID
      FROM offender_key_workers
      WHERE offender_book_id = p_offender_book_id
            AND active_flag = 'Y'
      FOR UPDATE OF active_flag, expiry_date NOWAIT;
    --@@@ Sunil 31/05/2017 QC#20526 End of changes
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) IN ('MERGE', 'XTAG_MOVEMENT_TRANSACTIONS')
    THEN
      RETURN;
    END IF;
    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not
    /*=========================================================
      Version Number = 5.1   Date Modified = 31-MAY-2017
    ==========================================================*/
    /* MODIFICATION HISTORY
       Person        Date        Version         Comments
       ---------  -----------    ------------    -----------------------------------------------
       SunilG      31/05/2017   5.1              QC#20526 Key Worker changes
       Laurence    09/04/2009   2.3              QC#14987 Performance.  Now does not fire if
                                                 context in 'XTAG_MOVMENT_TRANSACTION' i.e.
                                                 Migration / IEDT.
       Graham      21/12/2007   2.2              #7775: Code added for Merge, fix versioning
       Surya       18/04/2007   2.1              TD6389:Enqueue TX locks fix.
                                         Created the version history as it was not there.
    */
    IF INSERTING
    THEN
      IF :NEW.movement_type IS NOT NULL
         AND :NEW.movement_reason_code IS NOT NULL
      THEN
        OPEN lock_cur(:NEW.offender_book_id);

        FETCH lock_cur
        INTO v_rowid;

        CLOSE lock_cur;

        UPDATE offender_bookings
        SET status_reason =
        :NEW.movement_type || '-'
        || :NEW.movement_reason_code
        WHERE ROWID = v_rowid;
      END IF;

      --@@@ Sunil 31/05/2017 QC#20526 Key Worker changes
      BEGIN
        IF :NEW.movement_type IN ('TRN', 'REL', 'ADM')
        THEN
          v_rowid := NULL;

          OPEN lock_key_workers(:NEW.offender_book_id);
          FETCH lock_key_workers
          INTO v_rowid;

          CLOSE lock_key_workers;

          IF v_rowid IS NOT NULL
          THEN
            UPDATE offender_key_workers
            SET active_flag = 'N',
              expiry_date   = SYSDATE
            WHERE ROWID = v_rowid;
          END IF;
        END IF;
        -- Do not raise an error if this process failed
        EXCEPTION WHEN OTHERS THEN
        NULL;
      END;
      --@@@ Sunil 31/05/2017 QC#20526 End of changes
    ELSIF UPDATING
      THEN
        OPEN lock_cur(:OLD.offender_book_id);

        FETCH lock_cur
        INTO v_rowid;

        CLOSE lock_cur;

        UPDATE offender_bookings
        SET status_reason =
        NVL(:NEW.movement_type, :OLD.movement_type)
        || '-'
        || NVL(:NEW.movement_reason_code, :OLD.movement_reason_code)
        WHERE ROWID = v_rowid;
    END IF;
    EXCEPTION
    WHEN OTHERS THEN
    IF SQLCODE = -54
    THEN
      tag_error.raise_app_error(-20951, 'Booking is locked');
    ELSE
      tag_error.handle();
    END IF;

  END;
/



CREATE OR REPLACE TRIGGER "OFF_EXT_MOV_API_EVENT"
BEFORE INSERT
  ON offender_external_movements
REFERENCING new AS new old AS old
FOR EACH ROW
  DECLARE
    /******************************************************************************
       Name:  off_ext_mov_api_event
       PURPOSE: This trigger is part of the api_events interface

       Revisions:
       Ver        Date          Author           Description
       ---------  ------------  ---------------  ------------------------------------
       1.0        29/07/2016    Paul Morris      Initial version
    ******************************************************************************/

      cannot_find_program EXCEPTION;
    PRAGMA EXCEPTION_INIT (cannot_find_program, -06508);

    v_prev_agy_loc_id      agency_locations.agy_loc_id%TYPE;
    v_prev_movement_time   offender_external_movements.movement_time%TYPE;
    v_prev_movement_type   offender_external_movements.movement_type%TYPE;
    v_prev_movement_reason offender_external_movements.movement_reason_code%TYPE;

    --
    -- Check whether the prison is a digital prison
    --
    FUNCTION is_digital_prison(p_agy_loc_id IN agency_locations.agy_loc_id%TYPE)
      RETURN BOOLEAN
    IS
      l_dummy VARCHAR2(1);
      BEGIN
        SELECT 'x'
        INTO l_dummy
        FROM agy_loc_establishments ale
        WHERE ale.agy_loc_id = p_agy_loc_id
              AND ale.establishment_type = 'DIG';

        RETURN TRUE;
        EXCEPTION
        WHEN no_data_found THEN
        RETURN FALSE;
      END is_digital_prison;

    --
    -- Check whether the offendrs last out movement was a court even or temporary absence
    -- from a digital prison
    --
    PROCEDURE check_last_out_movement(p_offender_book_id IN  offender_bookings.offender_book_id%TYPE,
                                      p_prev_agy_loc_id  OUT agency_locations.agy_loc_id%TYPE,
                                      p_movement_time    OUT offender_external_movements.movement_time%TYPE,
                                      p_movement_type    OUT offender_external_movements.movement_type%TYPE,
                                      p_movement_reason  OUT offender_external_movements.movement_reason_code%TYPE)
    IS
      v_agy_loc_id agency_locations.agy_loc_id%TYPE;
      BEGIN
        SELECT
          agy_loc_id,
          movement_type,
          movement_time,
          movement_reason_code
        INTO v_agy_loc_id, p_movement_type, p_movement_time, p_movement_reason
        FROM (SELECT
                oem.from_agy_loc_id                                         agy_loc_id,
                oem.movement_type,
                oem.movement_reason_code,
                oem.movement_time,
                row_number()
                OVER (
                  ORDER BY oem.movement_date DESC, oem.movement_time DESC ) rown
              FROM offender_external_movements oem
              WHERE oem.offender_book_id = p_offender_book_id
                    AND oem.direction_code = 'OUT')
        WHERE rown = 1;

        IF p_movement_type IN ('CRT', 'TAP')
           AND api_owner.core_utils.is_digital_prison(v_agy_loc_id)
        THEN
          p_prev_agy_loc_id := v_agy_loc_id;
        END IF;
        EXCEPTION
        WHEN no_data_found THEN
        p_prev_agy_loc_id := NULL;
      END check_last_out_movement;

  BEGIN
    -- Trigger not to fire during merge.movemnt_type
    IF sys_context('NOMIS', 'AUDIT_MODULE_NAME', 50) IN ('MERGE')
    THEN
      RETURN;
    END IF;

    IF INSERTING
    THEN
      IF :new.direction_code = 'IN'
         AND :new.movement_type IN ('TRN', 'ADM')
      THEN
        IF api_owner.core_utils.is_digital_prison(:new.to_agy_loc_id)
        THEN
          --
          -- Post reception event
          --
          api_owner.pss_events.reception(
              p_offender_book_id => :new.offender_book_id,
              p_agy_loc_id       => :new.to_agy_loc_id,
              p_movement_date    => :new.movement_date,
              p_movement_time    => :new.movement_time);
          --
        END IF;
        --
        -- Check whether the offender was out at court from a digital prison
        --
        check_last_out_movement(p_offender_book_id  => :new.offender_book_id,
                                p_prev_agy_loc_id   => v_prev_agy_loc_id,
                                p_movement_time     => v_prev_movement_time,
                                p_movement_type     => v_prev_movement_type,
                                p_movement_reason   => v_prev_movement_reason);

        IF api_owner.core_utils.is_digital_prison(v_prev_agy_loc_id)
        THEN
          --
          -- Post discharge Event
          --
          api_owner.pss_events.discharge(
              p_offender_book_id => :new.offender_book_id,
              p_agy_loc_id       => v_prev_agy_loc_id,
              p_movement_date    => v_prev_movement_time,
              p_movement_time    => v_prev_movement_time,
              p_movement_type     => v_prev_movement_type,
              p_movement_reason  => v_prev_movement_reason);
        END IF;

      ELSIF :new.direction_code = 'OUT'
            AND :new.movement_type IN ('TRN', 'REL')
        THEN
          IF api_owner.core_utils.is_digital_prison(:new.from_agy_loc_id)
          THEN
            --
            -- Post discharge Event
            --
            api_owner.pss_events.discharge(
                p_offender_book_id => :new.offender_book_id,
                p_agy_loc_id       => :new.from_agy_loc_id,
                p_movement_date    => :new.movement_date,
                p_movement_time    => :new.movement_time,
                p_movement_type    => :new.movement_type,
                p_movement_reason  => :new.movement_reason_code);
          END IF;
      END IF;
    END IF;
    EXCEPTION
    WHEN cannot_find_program THEN
    -- this needs to propagate so it rectifies itself
    RAISE;
    WHEN OTHERS THEN
    api_owner.nomis_api_log.error(p_msg_module => 'OFF_EXT_MOV_API_EVENT',
                                  p_message    => sqlerrm);
  END;
/



CREATE OR REPLACE TRIGGER "OFFENDER_EXT_MOVEMENTS_TWF"
AFTER INSERT
  ON OFFENDER_EXTERNAL_MOVEMENTS
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
WHEN ( NEw.movement_type IN ('ADM', 'REL')
)
  DECLARE
    lv_offender_book_id OFFENDER_EXTERNAL_MOVEMENTS.offender_book_id%TYPE
    := :NEW.offender_book_id;
    lv_from_agy         AGENCY_LOCATIONS.description%TYPE;
    lv_to_agy           AGENCY_LOCATIONS.description%TYPE;
    lv_reason           REFERENCE_CODES.description%TYPE;
    lv_event_date       DATE;
    lv_xml              XMLTYPE;
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;
    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not
    /*===================================================================
    Version Number = 2.2  Date Modified = 08-APR-2009
    ====================================================================*/
    /* MODIFICATION HISTORY
       Person      Date           Version       Comments
       ----------- ------------   ------------  ------------------------
       Laurence    08/04/2009		2.2           QC#14987 Performance.  Added WHEN clause.
                                                Now only fies when movement_type = 'ADM'
                                                or 'REL' since code was only conditionally
                                                on these values anyway while regardlessly
                                                initialising variables, calling packages etc.
       Erin        10/01/2008     2.1           Re-created trigger with new file name
       Graham      21/12/2007     2.6           #7775: Code added for Merge, fix versioning
       Surya       15-Mar-2007    2.5           #6247:Removed the 'TRN/OJ' condition, as UK doen't have
                                                OIDTROJU(Transfer outside the jurisdiction).
       NDB         05-Mar-2007    2.4           #6247 Pass movement date and time as event date for
                                                workflow trigger PRISON_ADMIN and PRISON_REL.
       GJC         07-Jun-2006    2.3           Fix version Label
       GJC         09-May-2006    2.1           Async version
       Claus       31-Mar-2006    2.0           Created.
    */
    lv_xml := Tag_Wfmsg.create_xml;
    lv_event_date :=
    TO_DATE(TO_CHAR(:NEW.movement_date, 'DD/MM/YYYY') || ' ' ||
            TO_CHAR(:NEW.movement_time, 'HH24:MI'), 'DD/MM/YYYY HH24:MI');

    IF :NEW.movement_type = 'ADM'
    THEN
      Tag_Wfmsg.append('movement_type'
      , :NEW.movement_type
      , lv_xml
      );
      Tag_Wfmsg.append('movement_rsn_code'
      , :NEW.movement_reason_code
      , lv_xml
      );
      Tag_Wfmsg.append('from_agy_loc_id'
      , :NEW.from_agy_loc_id
      , lv_xml
      );
      Tag_Wfmsg.append('to_agy_loc_id'
      , :NEW.to_agy_loc_id
      , lv_xml
      );
      Tag_Workflow.create_case_note(:NEW.offender_book_id
      , 'PRISON_ADMIN'
      , p_message                => lv_xml.getstringval()
      , p_event_id               => NULL
      , p_event_date             => lv_event_date
      , p_note_source_code       => 'AUTO'
      );
    ELSIF :NEW.movement_type = 'REL'
      THEN
        Tag_Wfmsg.append('movement_type'
        , :NEW.movement_type
        , lv_xml
        );
        Tag_Wfmsg.append('movement_rsn_code'
        , :NEW.movement_reason_code
        , lv_xml
        );
        Tag_Wfmsg.append('from_agy_loc_id'
        , :NEW.from_agy_loc_id
        , lv_xml
        );
        Tag_Workflow.create_case_note(:NEW.offender_book_id
        , 'PRISON_REL'
        , p_message                => lv_xml.getstringval()
        , p_event_id               => NULL
        , p_event_date             => lv_event_date
        , p_note_source_code       => 'AUTO'
        );
    END IF;
    EXCEPTION
    WHEN OTHERS
    THEN
    Tag_Error.handle();
  END;

/



CREATE OR REPLACE TRIGGER "OFFENDER_EXTERNAL_MOVEMENTS_TA"
BEFORE INSERT OR UPDATE OR DELETE ON OFFENDER_EXTERNAL_MOVEMENTS
REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW
  DECLARE
    l_scn NUMBER;
    l_tid VARCHAR2(32);
  BEGIN
    /*
    ============================================================
       Generated by 2.3  Date Generation 10-NOV-2008
    ============================================================
      MODIFICATION HISTORY
       Person       Date      version      Comments
    -----------  --------- -----------  -------------------------------
        GJC      05/03/2007  2.3          Allow application setting some columns
        GJC      23/10/2006  2.2          Audit DELETE statements
       David Ng  18/04/2006  2.0.1        Audit column trigger
    */
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
          'OFFENDER_EXTERNAL_MOVEMENTS',
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


