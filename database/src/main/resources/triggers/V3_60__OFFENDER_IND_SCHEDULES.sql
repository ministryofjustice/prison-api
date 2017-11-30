
CREATE OR REPLACE TRIGGER "OFFENDER_IND_SCHEDULES_T4"
AFTER
UPDATE OF END_TIME, AGY_LOC_ID, EVENT_DATE, START_TIME, COMMENT_TEXT, EVENT_OUTCOME, IN_CHARGE_STAFF_ID
  ON OFFENDER_IND_SCHEDULES
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW
  DECLARE
    p_offender_book_id   offender_case_notes.offender_book_id%TYPE;
    p_contact_date       offender_case_notes.contact_date%TYPE;
    p_contact_time       offender_case_notes.contact_time%TYPE;
    p_note_source_code   offender_case_notes.note_source_code%TYPE;
    p_case_note_type     offender_case_notes.case_note_type%TYPE;
    p_case_note_sub_type offender_case_notes.case_note_sub_type%TYPE;
    p_case_note_text     offender_case_notes.case_note_text%TYPE;
    p_date_creation      offender_case_notes.date_creation%TYPE;
    p_time_creation      offender_case_notes.time_creation%TYPE;
    p_staff_id           offender_case_notes.staff_id%TYPE;
    p_check_box1         offender_case_notes.check_box1%TYPE;
    p_check_box2         offender_case_notes.check_box2%TYPE;
    p_check_box3         offender_case_notes.check_box3%TYPE;
    p_check_box4         offender_case_notes.check_box4%TYPE;
    p_check_box5         offender_case_notes.check_box5%TYPE;
    p_event_id           offender_case_notes.event_id%TYPE;
    p_new_text           offender_case_notes.case_note_text%TYPE;
    lv_len_txt           NUMBER;
    lv_alert             NUMBER;
    lv_old_staff         VARCHAR2(10);
    lv_new_staff         VARCHAR2(10);
    lv_old_text          offender_case_notes.case_note_text%TYPE;
    lv_new_text          offender_case_notes.case_note_text%TYPE;
    lv_old_outcome       offender_ind_schedules.event_outcome%TYPE;
    lv_new_outcome       offender_ind_schedules.event_outcome%TYPE;
    lv_old_time          VARCHAR2(10);
    lv_new_time          VARCHAR2(10);

    CURSOR get_casenote_details_c
    IS
      SELECT
        ocn.offender_book_id,
        ocn.event_id,
        ocn.contact_date,
        ocn.contact_time,
        ocn.note_source_code,
        ocn.case_note_type,
        ocn.case_note_sub_type,
        ocn.case_note_text,
        ocn.date_creation,
        ocn.time_creation,
        ocn.staff_id,
        ocn.check_box1,
        ocn.check_box2,
        ocn.check_box3,
        ocn.check_box4,
        ocn.check_box5
      FROM offender_case_notes ocn
      WHERE event_id = :NEW.event_id
            AND case_note_id = (SELECT MAX(case_note_id)
                                FROM offender_case_notes ocn
                                WHERE event_id = :NEW.event_id);
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;
    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not
    /*
    ==============================================================================================================================
       Version Number = 2.0  Date Modified = 10-JAN-2008
    ==============================================================================================================================
      MODIFICATION HISTORY
       Person       Date      version      Comments
    -----------  ---------   -----------  ---------------------------------------------------------------------------------
      Erin       10/01/2008  2.0          Correct trigger file name
      Graham     21/12/2007  2.4          #7775: Code added for Merge, fix versioning
      Rajshree   25/01/2006  2.2          Changed the record date to date_creation,Added time creation.Also added code to
                                          enter 'BLANK' when null value is updated or any value is updated to null.
      Rajshree   06/01/2006  2.1          Fixed issue of closing the cursor.

      Rajshree   04/01/2006  2.0          This trigger is will create a new case note entry that includes the old details and
                                          the update done to the feild for records that triggered an autometic case note.
    */
    IF :OLD.event_date <> :NEW.event_date
       OR :OLD.start_time <> :NEW.start_time
       OR NVL(TO_CHAR(:OLD.end_time, 'HH24:MI'), '00:00') <>
          NVL(TO_CHAR(:NEW.end_time, 'HH24:MI'), '00:00')
       OR :OLD.agy_loc_id <> :NEW.agy_loc_id
       OR NVL(:OLD.in_charge_staff_id, 0) <> NVL(:NEW.in_charge_staff_id, 0)
       OR NVL(:OLD.event_outcome, '0') <> NVL(:NEW.event_outcome, '0')
       OR NVL(:OLD.comment_text, '0') <> NVL(:NEW.comment_text, '0')
    THEN
      OPEN get_casenote_details_c;

      FETCH get_casenote_details_c
      INTO p_offender_book_id, p_event_id, p_contact_date, p_contact_time,
      p_note_source_code, p_case_note_type, p_case_note_sub_type,
      p_case_note_text, p_date_creation, p_time_creation, p_staff_id,
      p_check_box1, p_check_box2, p_check_box3, p_check_box4,
      p_check_box5;

      CLOSE get_casenote_details_c;

      IF p_event_id IS NOT NULL
      THEN
        IF :OLD.event_date <> :NEW.event_date
        THEN
          IF p_new_text IS NOT NULL
          THEN
            p_new_text :=
            p_new_text
            || :OLD.event_date
            || ' has been updated to '
            || :NEW.event_date;
          ELSE
            p_new_text :=
            :OLD.event_date || ' has been updated to '
            || :NEW.event_date;
          END IF;
        END IF;

        IF :OLD.start_time <> :NEW.start_time
        THEN
          IF p_new_text IS NOT NULL
          THEN
            p_new_text :=
            p_new_text
            || ' '
            || TO_CHAR(:OLD.start_time, 'HH24:MI')
            || ' has been updated to '
            || TO_CHAR(:NEW.start_time, 'HH24:MI');
          ELSE
            p_new_text :=
            TO_CHAR(:OLD.start_time, 'HH24:MI')
            || ' has been updated to '
            || TO_CHAR(:NEW.start_time, 'HH24:MI');
          END IF;
        END IF;

        IF NVL(TO_CHAR(:OLD.end_time, 'HH24:MI'), '00:00') <>
           NVL(TO_CHAR(:NEW.end_time, 'HH24:MI'), '00:00')
        THEN
          IF :OLD.end_time IS NULL
          THEN
            lv_old_time := 'BLANK';
          ELSE
            lv_old_time := TO_CHAR(:OLD.end_time, 'HH24:MI');
          END IF;

          IF :NEW.end_time IS NULL
          THEN
            lv_new_time := 'BLANK';
          ELSE
            lv_new_time := TO_CHAR(:NEW.end_time, 'HH24:MI');
          END IF;
          IF p_new_text IS NOT NULL
          THEN
            p_new_text :=
            p_new_text
            || ' '
            || lv_old_time
            || ' has been updated to '
            || lv_new_time;
          ELSE
            p_new_text :=
            lv_old_time
            || ' has been updated to '
            || lv_new_time;
          END IF;
        END IF;

        IF :OLD.agy_loc_id <> :NEW.agy_loc_id
        THEN
          IF p_new_text IS NOT NULL
          THEN
            p_new_text :=
            p_new_text
            || ' '
            || :OLD.agy_loc_id
            || ' has been updated to '
            || :NEW.agy_loc_id;
          ELSE
            p_new_text :=
            :OLD.agy_loc_id || ' has been updated to '
            || :NEW.agy_loc_id;
          END IF;
        END IF;

        IF NVL(:OLD.in_charge_staff_id, 0) <>
           NVL(:NEW.in_charge_staff_id, 0)
        THEN
          IF :OLD.in_charge_staff_id IS NULL
          THEN
            lv_old_staff := 'BLANK';
          ELSE
            lv_old_staff := TO_CHAR(:OLD.in_charge_staff_id);
          END IF;

          IF :NEW.in_charge_staff_id IS NULL
          THEN
            lv_new_staff := 'BLANK';
          ELSE
            lv_new_staff := TO_CHAR(:NEW.in_charge_staff_id);
          END IF;

          IF p_new_text IS NOT NULL
          THEN
            p_new_text :=
            p_new_text
            || ' '
            || lv_old_staff
            || ' has been updated to '
            || lv_new_staff;
          ELSE
            p_new_text :=
            lv_old_staff
            || ' has been updated to '
            || lv_new_staff;
          END IF;
        END IF;

        IF nvl(:OLD.event_outcome, 0) <> nvl(:NEW.event_outcome, 0)
        THEN
          IF :OLD.event_outcome IS NULL
          THEN
            lv_old_outcome := 'BLANK';
          ELSE
            lv_old_outcome := :OLD.event_outcome;
          END IF;

          IF :NEW.event_outcome IS NULL
          THEN
            lv_new_outcome := 'BLANK';
          ELSE
            lv_new_outcome := :NEW.event_outcome;
          END IF;

          IF p_new_text IS NOT NULL
          THEN
            p_new_text :=
            p_new_text
            || ' '
            || lv_old_outcome
            || ' has been updated to '
            || lv_new_outcome;
          ELSE
            p_new_text :=
            lv_old_outcome
            || ' has been updated to '
            || lv_new_outcome;
          END IF;
        END IF;

        IF nvl(:OLD.comment_text, 0) <> nvl(:NEW.comment_text, 0)
        THEN
          IF :OLD.comment_text IS NULL
          THEN
            lv_old_text := 'BLANK';
          ELSE
            lv_old_text := :OLD.comment_text;
          END IF;

          IF :NEW.comment_text IS NULL
          THEN
            lv_new_text := 'BLANK';
          ELSE
            lv_new_text := :NEW.comment_text;
          END IF;

          IF p_new_text IS NOT NULL
          THEN
            p_new_text :=
            p_new_text
            || ' '
            || lv_old_text
            || ' has been updated to '
            || lv_new_text;
          ELSE
            p_new_text :=
            lv_old_text
            || ' has been updated to '
            || lv_new_text;
          END IF;
        END IF;

        p_new_text := p_case_note_text || ' ' || p_new_text;
        lv_len_txt := NVL(LENGTH(p_new_text), 0);

        IF lv_len_txt > 4000
        THEN
          tag_error.raise_app_error
          (-20004,
           'The updated case note text is greater than the allowed limit of 4000 characters.'
          );
        END IF;

        INSERT INTO offender_case_notes
        (offender_book_id, event_id, case_note_id,
         contact_date, contact_time, note_source_code,
         case_note_type, case_note_sub_type, case_note_text,
         date_creation, time_creation, staff_id,
         check_box1, check_box2, check_box3, check_box4,
         check_box5
        )
        VALUES (p_offender_book_id, p_event_id, case_note_id.NEXTVAL,
                                    p_contact_date, p_contact_time, p_note_source_code,
                                    p_case_note_type, p_case_note_sub_type, p_new_text,
                                    p_date_creation, p_time_creation, p_staff_id,
                p_check_box1, p_check_box2, p_check_box3, p_check_box4,
                p_check_box5
        );
      END IF;
    END IF;
    EXCEPTION
    WHEN OTHERS
    THEN
    tag_error.handle;
  END;

/



CREATE OR REPLACE TRIGGER "OFFENDER_IND_SCHEDULES_TWF"
BEFORE
UPDATE OR INSERT
  ON OFFENDER_IND_SCHEDULES
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
  DECLARE
    /*===================================================================
     Version Number = 2.13 Date Modified = 21-DEC-2007
     ====================================================================*/
    /* MODIFICATION HISTORY
       Person      Date          Version    Comments
       ----------  ----------    ------     --------------------------
        Graham     21/12/2007   2.13       #7775: Code added for Merge, fix versioning
        GJC        12/06/2007   2.12       Include event_type in message
        Surya      12/03/2007   2.11       TD6247: CR223 - Amended for Event date fix for FIRST_FTA.
                      and corrected version history and version number.
                      Populate Global variable with Event date and the same is being used
                                           in Offender Sentences Trigger.
        NDB        06-Mar-2007  2.10       #6247 Added event date to casenote for trigger APPOINT_OUTC
        GJC        25-Jul-2006  2.8        Defect 3464
        GJC        25-Jul-2006  2.7        Defect 3464
        GJC        25-Jul-2006  2.6        Defect 3464
        GJC        24-Jul-2006  2.5        Defect 3464
        GJC        04-Jul-2006  2.4        FTA and FTC still not correct
        GJC        28-Jun-2006  2.3        Should be refering to event_outcome
        GJC        19-Jun-2006  2.2        Defect 2726
        GJC        12-Jun-2006  2.1        Release 1.1 workflow version
        GJC        30-May-2006  2.0        Initial version
       ---------------------------------------------------------------
       */
    lv_xml     XMLTYPE;
    l_event_id NUMBER(12);
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;
    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not

    IF (INSERTING AND :NEW.EVENT_TYPE != 'DRR' AND :NEW.EVENT_OUTCOME IS NOT NULL)
       OR (UPDATING AND :NEW.EVENT_TYPE != 'DRR' AND :NEW.EVENT_OUTCOME IS NOT NULL AND :OLD.EVENT_OUTCOME IS NULL)
    THEN
      lv_xml := Tag_Wfmsg.create_xml;
      Tag_Wfmsg.append('event_type', :NEW.EVENT_TYPE, lv_xml);
      Tag_Wfmsg.append('event_sub_type', :NEW.EVENT_SUB_TYPE, lv_xml);
      Tag_Wfmsg.append('event_outcome', :NEW.EVENT_OUTCOME, lv_xml);

      Tag_Workflow.create_case_note(p_offender_book_id => :NEW.offender_book_id,
                                    p_trigger_name => 'APPOINT_OUTC',
                                    p_message => lv_xml.getstringval(),
                                    p_event_id => NULL,
                                    p_event_date => :NEW.event_date,
                                    p_note_source_code =>'AUTO');

    END IF;

    IF ((UPDATING AND NVL(:NEW.event_outcome, '***') IN ('FTA', 'FTC') AND
         NVL(:OLD.event_outcome, '***') NOT IN ('FTA', 'FTC'))
        OR (INSERTING AND NVL(:NEW.event_outcome, '***') IN ('FTA', 'FTC')))
    THEN
      lv_xml := Tag_Wfmsg.create_xml;

      Tag_Wfmsg.append('event_id', :NEW.EVENT_ID, lv_xml);
      Tag_Wfmsg.append('event_type', 'OCDCLOGS', lv_xml);
      -- GJC Added next line 12/06/2007
      Tag_Wfmsg.append('source_event', :NEW.EVENT_TYPE, lv_xml);

      Tag_Workflow.create_workflow(p_offender_book_id => :NEW.OFFENDER_BOOK_ID,
                                   p_trigger_name => 'FIRST_FTA',
                                   p_key => TO_CHAR(:NEW.event_id),
                                   p_params => lv_xml,
                                   p_event_date=> :NEW.EVENT_DATE); --@@@Surya TD6247
    END IF;

    --@@@ GJC Added for defect 3464

    IF UPDATING AND NVL(:OLD.event_outcome, '***') IN ('FTA', 'FTC')
       AND NVL(:NEW.event_outcome, '***') NOT IN ('FTA', 'FTC')
    THEN

      lv_xml := Tag_Wfmsg.create_xml;

      Tag_Wfmsg.append('event_id', :NEW.EVENT_ID, lv_xml);

      Tag_Workflow.complete_workflow(p_trigger_name => 'FIRST_FTA',
                                     p_key => TO_CHAR(:NEW.event_id),
                                     p_params => lv_xml);
    ELSE
      --@@@Surya TD6247:Store Breach trigger event date in global variable and the same is
      --                being utilised in Offender Sentences work flow trigger.
      IF UPDATING AND NVL(:NEW.event_outcome, '***') IN ('UA', 'UB')
      THEN
        Tag_Wf_Enforcement.g_event_date := :NEW.EVENT_DATE;
      END IF;
    END IF;

    EXCEPTION
    WHEN OTHERS
    THEN
    Tag_Error.handle();
  END;

/



CREATE OR REPLACE TRIGGER "OFFENDER_IND_SCHEDULES_TA"
BEFORE INSERT OR UPDATE OR DELETE ON OFFENDER_IND_SCHEDULES
REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW
  DECLARE
    l_scn NUMBER;
    l_tid VARCHAR2(32);
  BEGIN
    /*
    ============================================================
       Generated by 2.3  Date Generation 25-NOV-2010
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
          'OFFENDER_IND_SCHEDULES',
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



CREATE OR REPLACE TRIGGER "OFFENDER_IND_SCHEDULES_T1"
BEFORE
INSERT OR UPDATE
  ON offender_ind_schedules
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;
    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not
    /*
    ============================================================
       Version Number = 2.3  Date Modified = 21-DEC-2007
    ============================================================
      MODIFICATION HISTORY
       Person       Date      version      Comments
    -----------  ---------   -----------  -------------------------------
       Graham    21/12/2007  2.3          #7775: Code added for Merge, fix versioning
       GJC       16/10/2006  2.2          Remove DBMS_OUTPUT calls
       David Ng  12/10/2005  2.0          Popluate reference ID
    */
    IF (:new.EVENT_TYPE = 'PRG')
    THEN
      :new.CRS_SCH_ID := :new.reference_id;
    ELSIF (:new.EVENT_TYPE = 'TAP')
      THEN
        :new.TEMP_ABS_SCH_ID := :new.REference_ID;
    END IF;

    EXCEPTION
    WHEN OTHERS THEN
    tag_error.handle();

  END;

/



CREATE OR REPLACE TRIGGER "OFFENDER_IND_SCHEDULES_T3"
BEFORE
INSERT
  ON offender_ind_schedules
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
  DECLARE V_numrows INTEGER;
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;
    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not

    /* =========================================================
       Version Number = 2.2  Date Modified = 21-DEC-2007
       ========================================================= */

    /* MODIFICATION HISTORY
       Person      Date            Version       Comments
       ---------   ------       ------------  ------------------------------
       Graham       21/12/2007   2.2           #7775: Code added for Merge, fix versioning
       GJC          15/06/2006   2.1           Ensure EVENT_ID is set if null
       David Ng     15/10/2005   2.0           NOMIS project(10.2.0)
    */
    IF (:new.Reference_ID IS NOT NULL)
    THEN
      SELECT count(*)
      INTO v_numrows
      FROM offender_ind_schedules
      WHERE offender_Book_ID = :new.offender_Book_ID
            AND Event_Type = :new.event_type
            AND Reference_ID = :New.Reference_ID;
    END IF;

    IF (v_Numrows > 0)
    THEN
      tag_error.raise_app_error(-20004, 'Schedule record already exists');
    END IF;

    IF :NEW.event_id IS NULL
    THEN
      SELECT event_id.nextval
      INTO :NEW.event_id
      FROM DUAL;
    END IF;

  END;

/



CREATE OR REPLACE TRIGGER "OFFENDER_IND_SCHEDULES_T2"
BEFORE
INSERT OR UPDATE
  ON OFFENDER_IND_SCHEDULES
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
  DECLARE
    v_numrows INTEGER;
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;
    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not
    /* =========================================================
       Version Number = 2.7  Date Modified = 21-DEC-2007
       ========================================================= */

    /* MODIFICATION HISTORY
       Person      Date            Version       Comments
       ---------   ------       ------------  ------------------------------
       David Ng     15/10/2005  2.0           NOMIS project(10.2.0)
       David Ng     07/07/2006  2.1           Add Syn event_status and Event_Outcome
       Venu         07/09/2006  2.2           In case of Prison schedule when a schedule is confirmed it is updated as COMP
                                                 but the trigger was reverting it back to SCH, incorrect and it is fixed now.
       Rajshree    28/11/2006   2.3           Added condition to make sure if record is created in past(As status EXP) should
                                                 be changed to status 'SCH' when date is update to >= to current date.
       GJC         07/12/2006   2.4           If the EVENT_STATUS is set to DEL it should be left alone, defect 5393
       GJC         06/03/2007   2.5           Correct EVENT_STATUS processing when event_date <= sysdate-1, defect 6268
       Claus       09/08/2007   2.6           D# 7747. Added event_status 'PEN'.
       Graham      21/12/2007   2.7           #7775: Code added for Merge, fix versioning
       Igor        18/08/2007   2.8           #9886: Added DEN (Denied) event status to the list of the statuses
                                                     that should not be modified by trigger in regard to the event dates
    */
    SELECT COUNT(*)
    INTO v_numrows
    FROM REFERENCE_CODES
    WHERE code = :NEW.event_type
          AND domain IN ('MOVE_TYPE', 'INT_SCH_TYPE', 'EVENTS');

    IF (v_numrows = 0)
    THEN
      Tag_Error.raise_app_error(-20005, 'Invalid Event Type');
    END IF;

    SELECT COUNT(*)
    INTO v_numrows
    FROM REFERENCE_CODES
    WHERE code = :NEW.event_status AND domain IN ('EVENT_STS');

    IF (v_numrows = 0)
    THEN
      Tag_Error.raise_app_error(-20006, 'Invalid Event Status');
    END IF;

    -- GJC Added condition 07/12/2006
    IF :NEW.event_status = 'DEL'
    THEN
      RETURN;
    END IF;
    -- GJC End

    IF (:NEW.event_outcome IS NOT NULL)
    THEN
      IF (:OLD.event_outcome IS NULL AND :NEW.event_status IN ('SCH', 'EXP')
      )
      THEN
        :NEW.event_status := 'COMP';
      END IF;
    ELSE
      --@@@ Claus, 09-Aug-2007. D# 7747. Added event_status 'PEN'.
      IF (:NEW.event_status NOT IN
          ('CANC', 'COMP', 'PEN', 'DEN')) --- D#9886 Added 'DEN' to the list by Igor on 18/08/08
      --- @@@ Venu/Rajshree 28/11/2006,If by accident record is created in past(As expired)
      --- should be changed back to 'SCH' if date is updated to >=sysdate.Detected /Fixed in 5709
      THEN
        -- @@@ GJC Change next line 06/03/2007 was < TRUNC(SYSDATE) -1
        IF (:NEW.event_date <= TRUNC(SYSDATE) - 1)
        THEN
          :NEW.event_status := 'EXP';
        ELSE
          -- @@@ Venu/Rajshree 07/09/2006, In case of prison schedules once we confirm
          -- the schedule on OIDSCEXM we update the event_status to 'COMP' but the trigger
          -- was reverting it back to SCH. A bug that is fixed.
          :NEW.event_status := 'SCH';

        END IF;
      END IF;
    END IF;
  END;

/


