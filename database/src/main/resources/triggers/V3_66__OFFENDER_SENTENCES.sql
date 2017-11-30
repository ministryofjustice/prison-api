
CREATE OR REPLACE TRIGGER "OFFENDER_SENTENCES_T4"
AFTER
INSERT
  ON offender_sentences
REFERENCING NEW AS NEW OLD AS OLD
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;
    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not
    /*
       ============================================================
       Version Number = 2.2  Date Modified = 21-Dec-2007
       ============================================================
       Person       Date           Version      Comments
       --------     ----------     --------     -----------------
       Graham       21/12/2007     2.2          #7775: Code added for Merge, fix versioning
       Claus        23/01/2007     2.1          D# 5948. Fix.
       Claus        18/01/2007     2.0          D# 5948. Created.

    */
    tag_termination.insert_licences;
    tag_termination.flush_licences_tab;
    EXCEPTION
    WHEN OTHERS
    THEN
    tag_error.handle();
  END;

/



CREATE OR REPLACE TRIGGER "OFFENDER_SENTENCES_T2"
AFTER UPDATE OF status_update_reason
  ON offender_sentences
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
WHEN (NEW.status_update_date IS NOT NULL)
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;

    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not
    /*
       ============================================================
         Version Number = 3.1 Date Modified = 02-SEP-2011
       ============================================================
         MODIFICATION HISTORY
         Person       Date         Version      Comments
         Kumar        02/09/2011   3.1          #18619: Do not create Status record for aggregates
         Ragini       15/01/2009   2.7          D 13255: Removed the NVL clause from :OLD.status_update_comment
         SK           10/11/2008   2.6          10487-Added when clause.
         Claus        31/10/2008   2.5          D# 11526. Removed IF-condition for SAR.
         Claus        11/06/2008   2.4          D# 9215. Re-fix.
         Claus        04/06/2008   2.3          D# 9215. Added NVL to OLD.status_update_reason.
         Graham       21/12/2007   2.2          #7775: Code added for Merge, fix versioning
         Surya        23/04/2006   2.1          Modified for the oracle errors.
         Claus        19/04/2006   2.0          Created.
       */

    --@@@Kumar 02/09/11 #18619 Do not create Status record for aggregates
    IF :OLD.sentence_calc_type NOT LIKE '%AGG%'
    THEN
      INSERT INTO offender_sentence_statuses
      (offender_book_id,
       sentence_seq,
       status_update_reason,
       status_update_comment,
       status_update_date,
       status_update_staff_id,
       offender_sentence_status_id
      )
      VALUES (:OLD.offender_book_id,
              :OLD.sentence_seq,
              NVL(:OLD.status_update_reason, :NEW.status_update_reason),
              :OLD.status_update_comment,
              NVL(:OLD.status_update_date, :NEW.status_update_date),
              NVL(:OLD.status_update_staff_id, :NEW.status_update_staff_id),
              offender_sentence_status_id.NEXTVAL
      );
    END IF;

  END;
/



CREATE OR REPLACE TRIGGER "OFFENDER_SENTENCES_TWF"
AFTER UPDATE
  ON OFFENDER_SENTENCES
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
  DECLARE
    lv_xml               XMLTYPE;
    l_sentence_calc_type SENTENCE_CALC_TYPES.sentence_calc_type%TYPE;
    v_event_date         offender_ind_schedules.event_date%TYPE;
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;
    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not
    /*===================================================================
    Version Number = 5.1  Date Modified = 24-Jun-2014
    ====================================================================*/
    /* MODIFICATION HISTORY
       Person      Date           Version       Comments
       ------------------------------------------------------------------
       PThakur     24-Jun-2014    5.1           #19853: Added YOI_ORA sentence type
       Graham      21-Dec-2007    2.15          #7775: Code added for Merge, fix versioning
       Surya       12-Mar-2007    2.14          TD#6247:For BREACH and RECALL Triggers, Pass the UA, UB's
                                                schedules event date.
       NDB         05-Mar-2007    2.13          For trigger SENT_TERMIND, Pass the status_update_date as
                                                the event date for the caseload.
       GJC         25-Jul-2006    2.12          Change to RECALL and BREACH
       GJC         25-Jul-2006    2.11          Change to RECALL and BREACH
       GJC         24-Jul-2006    2.10          Change to RECALL and BREACH
       GJC         04-Jul-2006    2.9           Change to RECALL and BREACH
       GJC         28-Jun-2006    2.8           Check for nulls and ensure xml initialised
       GJC         13-Jun-2006    2.7           Defect 1883 Release 1.1 workflows, triggers 4.5 and 4.6
       GJC         12-Jun-2006    2.6           Remove 2.5 code
       GJC         07-Jun-2006    2.5           Defect 1883 Release 1.1 workflows
       GJC         09-May-2006    2.3           Async version
       Claus       22-Apr-2006    2.2           Removed reference to status_update_reasons.
       Claus       19-Apr-2006    2.1           Changed for Terminations.
       Claus       04-Apr-2006    2.0           Created.
    */
    IF UPDATING
    THEN
      IF :NEW.sentence_status = 'I'
         AND :OLD.sentence_status <> 'I'
      THEN
        lv_xml := Tag_Wfmsg.create_xml;

        Tag_Wfmsg.append('sentence_category', :NEW.sentence_calc_type, lv_xml);
        Tag_Wfmsg.append('status_update_reason', :NEW.status_update_reason, lv_xml);
        Tag_Wfmsg.append('sentence_start_date', :NEW.start_date, lv_xml);

        Tag_Workflow.create_case_note(p_offender_book_id => :NEW.offender_book_id,
                                      p_trigger_name => 'SENT_TERMIND',
                                      p_message => lv_xml.getstringval(),
                                      p_event_id => NULL,
                                      p_event_date => :NEW.status_update_date,
                                      p_note_source_code =>'AUTO');
      END IF;

      IF NVL(:NEW.no_of_unexcused_absence, 0) > NVL(:OLD.no_of_unexcused_absence, 0)
         AND NVL(:OLD.no_of_unexcused_absence, 0) > 0
      THEN

        SELECT sentence_type
        INTO l_sentence_calc_type
        FROM SENTENCE_CALC_TYPES
        WHERE sentence_category = :NEW.sentence_category
              AND sentence_calc_type = :NEW.sentence_calc_type;

        IF (:NEW.sentence_category IN ('1991', '2003') AND
            l_sentence_calc_type = 'COMM')
           OR (:NEW.sentence_category = 'LICENCE' AND
               l_sentence_calc_type = 'ALL' AND
               NVL(:NEW.sentence_calc_type, '**') IN ('YO', 'YN'))
           OR (:NEW.sentence_category IN ('1991', '2003') AND
               l_sentence_calc_type = 'ALL' AND
               NVL(:NEW.sentence_calc_type, '**') IN
               ('YOI', 'YOI_ORA')) -- @@@ PThakur 24/06/2014 D#19853: Added YOI_ORA
        THEN
          IF NVL(:NEW.no_of_unexcused_absence, 0) > 1
             AND NVL(:OLD.no_of_unexcused_absence, 0) = 1
          THEN
            lv_xml := Tag_Wfmsg.create_xml;
            v_event_date := tag_wf_enforcement.g_event_date; --TD6247
            tag_wf_enforcement.g_event_date := NULL;

            Tag_Workflow.create_workflow(p_offender_book_id => :NEW.OFFENDER_BOOK_ID,
                                         p_trigger_name => 'BREACH',
                                         p_key => 'OFFENDER_BOOK_ID=>' || :NEW.OFFENDER_BOOK_ID || ':SENTENCE_SEQ=>' ||
                                                  :NEW.SENTENCE_SEQ,
                                         p_params => lv_xml,
                                         p_event_date =>v_event_date--TD6247
            );

          END IF;
        END IF;

        IF (:NEW.sentence_category = 'LICENCE' AND
            l_sentence_calc_type = 'ALL' AND
            NVL(:NEW.sentence_calc_type, '**') NOT IN ('YO', 'YN'))
        THEN

          IF NVL(:NEW.no_of_unexcused_absence, 0) > 2
             AND NVL(:OLD.no_of_unexcused_absence, 0) = 2
          THEN
            lv_xml := Tag_Wfmsg.create_xml;
            v_event_date := tag_wf_enforcement.g_event_date; --TD6247
            tag_wf_enforcement.g_event_date := NULL;

            Tag_Workflow.create_workflow(p_offender_book_id => :NEW.OFFENDER_BOOK_ID,
                                         p_trigger_name => 'RECALL',
                                         p_key => 'OFFENDER_BOOK_ID=>' || :NEW.OFFENDER_BOOK_ID || ':SENTENCE_SEQ=>' ||
                                                  :NEW.SENTENCE_SEQ,
                                         p_params => lv_xml,
                                         p_event_date => v_event_date); --TD6247


          END IF;
        END IF;
      END IF;
    END IF;

    EXCEPTION
    WHEN OTHERS
    THEN
    Tag_Error.handle();
  END;
/



CREATE OR REPLACE TRIGGER "OFFENDER_SENTENCES_T3"
AFTER INSERT OR UPDATE
  ON offender_sentences
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
  DECLARE
    v_exist VARCHAR2(1) := NULL;
    v_found VARCHAR2(1) := 'N';

    CURSOR is_drr_cur
    IS
      SELECT 'X'
      FROM sentence_calc_types
      WHERE sentence_category = :NEW.sentence_category
            AND sentence_calc_type = :NEW.sentence_calc_type
            AND program_method = 'DRR';

    CURSOR obl_cur
    IS
      SELECT 'Y'
      FROM offender_prg_obligations
      WHERE offender_book_id = :NEW.offender_book_id
            AND sentence_seq = :NEW.sentence_seq
            AND :NEW.sentence_level = 'IND'
            AND :NEW.sentence_status = 'A'
      FOR UPDATE OF offender_book_id WAIT 1;
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;
    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not
    /*
       ============================================================
       Version Number = 2.4  Date Modified = 21-Dec-2007
       ============================================================
       Person       Date           Version      Comments
       --------     ----------     --------     -----------------
       Graham       21/12/2007     2.4          #7775: Code added for Merge, fix versioning
       NDB          30/07/2007     2.3          D# 6872. Added locking checks. Removed update of end date.
       NDB          30/07/2007     2.2          D# 6872. Added locking checks. Removed update of end date.
       Claus        23/01/2007     2.1          D# 5948. Cut record in offender_licence_sentences
       Surya        07/09/2006     2.0          Initial Draft.
    */
    IF :NEW.sentence_category = '1991'
       AND :NEW.sentence_level = 'IND'
    THEN
      OPEN is_drr_cur;

      FETCH is_drr_cur
      INTO v_exist;

      IF is_drr_cur%FOUND
      THEN
        CLOSE is_drr_cur;

        IF INSERTING
        THEN
          INSERT INTO offender_prg_obligations
          (offender_prg_obligation_id,
           offender_book_id, offender_sent_condition_id,
           sentence_seq, referral_date, end_date,
           event_type)
          VALUES (offender_prg_obligation_id.NEXTVAL,
                  :NEW.offender_book_id, NULL,
                  :NEW.sentence_seq, :NEW.start_date, :NEW.end_date,
                  'DRR');
        ELSE
          IF :NEW.start_date != :OLD.start_date
          THEN
            OPEN obl_cur;
            FETCH obl_cur
            INTO v_found;
            IF v_found = 'Y'
            THEN
              -- #6872. Added locking checks. Removed update of referral and end dates.
              UPDATE offender_prg_obligations
              SET referral_date = :NEW.start_date
              WHERE CURRENT OF obl_cur;
            END IF;
            CLOSE obl_cur;
          END IF;
        END IF;
      ELSE
        CLOSE is_drr_cur;
      END IF;
      --@@@ Claus, 23-Jan-2007. D# 5948.
    ELSIF :NEW.sentence_category = 'LICENCE'
          AND INSERTING
      THEN
        tag_termination.populate_licences(:NEW.offender_book_id,
                                          :NEW.sentence_seq);
    END IF;
    --@@@ Claus, 23-Jan-2007. D# 5948. End of change.
    EXCEPTION
    WHEN OTHERS
    THEN
    IF SQLCODE = -30006
    THEN
      tag_error.raise_app_error(-20951,
                                'Obligation is locked.'
      );
    ELSE
      tag_error.handle;
    END IF;
  END;

/



CREATE OR REPLACE TRIGGER "OFFENDER_SENTENCES_TA"
BEFORE INSERT OR UPDATE OR DELETE ON OFFENDER_SENTENCES
REFERENCING NEW AS NEW OLD AS OLD FOR EACH ROW
  DECLARE
    l_scn NUMBER;
    l_tid VARCHAR2(32);
  BEGIN
    /*
    ============================================================
       Generated by 2.3  Date Generation 13-JUL-2016
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
          'OFFENDER_SENTENCES',
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




