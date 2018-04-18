
CREATE OR REPLACE TRIGGER "OFFENDER_SENT_CALCULATIONS_T1"
AFTER
INSERT
  ON OFFENDER_SENT_CALCULATIONS
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF NVL(SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50), 'X') IN
       ('MERGE', 'XTAG_HMPS_LEGAL_TRANSACTION')
    THEN
      RETURN;
    END IF;
    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not
    /*
     ============================================================
     Version Number = 2.8  Date Modified = 24-Nov-2008
     ============================================================
       MODIFICATION HISTORY
        Person       Date           Version      Comments
        -----------  ------------   -----------  -----------------------------------------
        Paul M       24/11/2008     2.8          #11945: Don't execute for migration
        Graham       21/12/2007     2.7          #7775: Code added for Merge, fix versioning
        Surya        05-Sep-2006    2.6          Modified for the Integration of Sentence Calc.
   */
    tag_sentence_calc.create_offender_curfews(:NEW.offender_book_id,
                                              :NEW.hdced_overrided_date,
                                              :NEW.hdced_calculated_date,
                                              :NEW.crd_overrided_date,
                                              :NEW.crd_calculated_date,
                                              :NEW.ard_overrided_date,
                                              :NEW.ard_calculated_date
    );
    EXCEPTION
    WHEN OTHERS
    THEN
    tag_error.handle;
  END;

/



CREATE OR REPLACE TRIGGER "OFFENDER_SENT_CALCULATIONS_TWF"
BEFORE INSERT
  ON OFFENDER_SENT_CALCULATIONS
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
  DECLARE
    lv_trg_date           offender_sent_calculations.hdced_calculated_date%TYPE;
    lv_xml                XMLTYPE;
    v_days                PLS_INTEGER := 0;
    v_workflow_history_id offender_sent_calculations.workflow_history_id%TYPE;
    v_old_hdced           offender_sent_calculations.hdced_calculated_date%TYPE;

    CURSOR days_cur
    IS
      SELECT NVL(MAX(days), 0) days
      FROM work_triggers
      WHERE trigger_name = 'HDC_ELIGIBLE'
            AND active_flag = 'Y';

    CURSOR id_cur
    IS
      SELECT MAX(workflow_history_id)
      FROM offender_sent_calculations
      WHERE offender_book_id = :NEW.offender_book_id;
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;
    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not
    /*=========================================================
       Version Number = 2.7   Date Modified =  21-DEC-2007
     ==========================================================*/

    /* MODIFICATION HISTORY
       Person        Date   Version         Comments
     ---------  ---------   ------------    -----------------------------------------------
      Graham    21-Dec-2007  2.7            #7775: Code added for Merge, fix versioning
      Surya     09-Aug-2007  2.6            TD6413: Modified the trigger completely for when to crate HDCED
                                            work flow.
                                            1.The HDCED task should be created only when HDCED is with in the
                                            range(x days plus sysdate)
                                            2.The HDCED should be created upon update of HDCED_OVERRIDED_DATE
                                            as well.
                                            3.Added version history properly.
      GJC       07-Jun-2006  2.5           Fix version
      GJC       09-May-2006  2.3           Async version
      Claus     23-Apr-2006  2.2           D# 1492. Changed lv_trg_date to sysdate when creating workflow as per DD.
      Krishna   06-Apr-2006  2.1           Changed to create a task based on the days
      Krishna   05-Apr-2006  2.0           Initial version to create message
     */
    lv_trg_date := NVL(:NEW.hdced_overrided_date, :NEW.hdced_calculated_date);

    IF lv_trg_date IS NOT NULL
    THEN
      OPEN days_cur;
      FETCH days_cur INTO v_days;
      CLOSE days_cur;

      IF lv_trg_date <= (v_days + TRUNC(SYSDATE))
      THEN
        lv_xml := tag_wfmsg.create_xml;
        tag_wfmsg.append('trigger_date', lv_trg_date, lv_xml);
        tag_wfmsg.append('offender_sent_calculation_id',
                         :NEW.offender_sent_calculation_id,
                         lv_xml
        );

        OPEN id_cur;
        FETCH id_cur INTO v_workflow_history_id;
        CLOSE id_cur;

        IF v_workflow_history_id IS NULL
        THEN
          tag_workflow.create_workflow
          (p_offender_book_id       => :NEW.offender_book_id,
           p_trigger_name           => 'HDC_ELIGIBLE',
           p_params                 => lv_xml,
           p_event_date             => SYSDATE,
           p_key                    => TO_CHAR
           (:NEW.offender_sent_calculation_id
           ),
           p_override_due_date      => SYSDATE
          );
          :NEW.workflow_history_id := :NEW.offender_sent_calculation_id;
        ELSE
          SELECT NVL(NVL(hdced_overrided_date, hdced_calculated_date),
                     TO_DATE('01/01/3999', 'DD/MM/YYYY')
          )
          INTO v_old_hdced
          FROM offender_sent_calculations
          WHERE offender_sent_calculation_id = v_workflow_history_id;

          IF TRUNC(v_old_hdced) <> TRUNC(lv_trg_date)
          THEN
            tag_workflow.create_workflow
            (p_offender_book_id       => :NEW.offender_book_id,
             p_trigger_name           => 'HDC_ELIGIBLE',
             p_params                 => lv_xml,
             p_event_date             => SYSDATE,
             p_key                    => TO_CHAR
             (:NEW.offender_sent_calculation_id
             ),
             p_override_due_date      => SYSDATE
            );
            :NEW.workflow_history_id := :NEW.offender_sent_calculation_id;
          END IF;
        END IF;
      END IF;
    END IF;
    EXCEPTION
    WHEN OTHERS
    THEN
    tag_error.handle();
  END;

/



CREATE OR REPLACE TRIGGER "OFFENDER_SENT_CALCULATIONS_TA"
BEFORE INSERT OR UPDATE OR DELETE ON OFFENDER_SENT_CALCULATIONS
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
          'OFFENDER_SENT_CALCULATIONS',
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



CREATE OR REPLACE TRIGGER "OFFENDER_SENT_CALCULATIONS_T2"
AFTER INSERT OR UPDATE
  ON offender_sent_calculations
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW
  DECLARE
    lv_count                 NUMBER;
    lv_approved_release_date DATE;
    lv_auto_release_date     DATE;
    lv_dto_approved_date     DATE;
    lv_dto_mid_term_date     DATE;
    lv_date                  DATE;
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) IN
       ('MERGE', 'VSC', 'CREATE_TASK_HDC_ELIGIBILITY')
    -- @@@ PThkaur 24/11/2009 D#16463: Added VSC, this trigger code should not executed for VSC
    --@@@Kumar 28/03/2012 #18133 Added CREATE_TASK_HDC_ELIGIBILITY. Changing workflow_history_id should not update release details.
    THEN
      RETURN;
    END IF;

    /*
     ============================================================
        Version Number = 3.5  Date Modified = 28-Mar-2012
     ============================================================
     MODIFICATION HISTORY
     Person      Date            Version       Comments
     ---------   ------       ------------  -------------------------------------------
     Kumar        28/03/2012   3.5          #18133: Added CREATE_TASK_HDC_ELIGIBILITY to sys_context check.
     Kumar        26/03/2012   3.4          #18133:Changing workflow_history_id should not update release details.
     Surya        20/07/2011   3.3          18533:DPRRD introduction for DTO approved date.
     Kumar       06/06/2011    3.2          18478: CR182/729 Indeterminate sentence related changes
     P Thakur    01/12/2009    3.1          16463: Added code to stop execution of trigger code for VSC
     Surya       18/03/2009    2.4          14546-MTD should not be considered for approved release date.
     Surya       18/03/2009    2.3          14546-If no value exists for MTD override then DTO mid term should
                                            be populated in dto_mid_term_date.Removed commented code.
     Graham      21/12/2007    2.2          #7775: Code added for Merge, fix versioning
     David Ng    07/07/2006    2.1          Update key dates in Offender_booking_details
   */

    --@@@Kumar 06/06/11 #18478 CR182/729 changes
    IF tag_sentence_calc.is_life_sentence(:NEW.offender_book_id)
    THEN
      lv_approved_release_date := NULL;
      lv_auto_release_date := NULL;
      lv_dto_approved_date := NULL;
      lv_dto_mid_term_date := NULL;
    ELSE
      lv_approved_release_date :=
      NVL(:NEW.hdcad_overrided_date,
          :NEW.hdcad_calculated_date);

      IF (lv_approved_release_date IS NULL)
      THEN
        lv_approved_release_date :=
        NVL(:NEW.apd_overrided_date,
            :NEW.apd_calculated_date);
      ELSE
        IF (NVL(:NEW.apd_overrided_date, :NEW.apd_calculated_date) IS NOT NULL
           )
           AND (NVL(:NEW.apd_overrided_date, :NEW.apd_calculated_date) >
                lv_approved_release_date
           )
        THEN
          lv_approved_release_date :=
          NVL(:NEW.apd_overrided_date,
              :NEW.apd_calculated_date);
        END IF;
      END IF;

      IF (lv_approved_release_date IS NULL)
      THEN
        lv_approved_release_date :=
        NVL(:NEW.prrd_overrided_date,
            :NEW.prrd_calculated_date);
      ELSE
        IF (NVL(:NEW.prrd_overrided_date, :NEW.prrd_calculated_date) IS NOT NULL
           )
           AND (NVL(:NEW.prrd_overrided_date, :NEW.prrd_calculated_date) >
                lv_approved_release_date
           )
        THEN
          lv_approved_release_date :=
          NVL(:NEW.prrd_overrided_date,
              :NEW.prrd_calculated_date);
        END IF;
      END IF;

      lv_auto_release_date :=
      NVL(:NEW.crd_overrided_date,
          :NEW.crd_calculated_date);

      IF (lv_auto_release_date IS NULL)
      THEN
        lv_auto_release_date :=
        NVL(:NEW.ard_overrided_date,
            :NEW.ard_calculated_date);
      ELSE
        IF (NVL(:NEW.ard_overrided_date, :NEW.ard_calculated_date) IS NOT NULL
           )
           AND (NVL(:NEW.ard_overrided_date, :NEW.ard_calculated_date) >
                lv_auto_release_date
           )
        THEN
          lv_auto_release_date :=
          NVL(:NEW.ard_overrided_date,
              :NEW.ard_calculated_date);
        END IF;
      END IF;

      IF (lv_auto_release_date IS NULL)
      THEN
        lv_auto_release_date :=
        NVL(:NEW.npd_overrided_date,
            :NEW.npd_calculated_date);
      ELSE
        IF (NVL(:NEW.npd_overrided_date, :NEW.npd_calculated_date) IS NOT NULL
           )
           AND (NVL(:NEW.npd_overrided_date, :NEW.npd_calculated_date) >
                lv_auto_release_date
           )
        THEN
          lv_auto_release_date :=
          NVL(:NEW.npd_overrided_date,
              :NEW.npd_calculated_date);
        END IF;
      END IF;

      --@@@Surya 18-Mar-2009:14546-MTD should not be considered for DTO Approved release date.
      lv_dto_approved_date :=
      NVL(:NEW.ltd_overrided_date,
          :NEW.ltd_calculated_date);

      IF (lv_dto_approved_date IS NULL)
      THEN
        lv_dto_approved_date :=
        NVL(:NEW.etd_overrided_date,
            :NEW.etd_calculated_date);
      ELSE
        IF (NVL(:NEW.etd_overrided_date, :NEW.etd_calculated_date) IS NOT NULL
           )
           AND (NVL(:NEW.etd_overrided_date, :NEW.etd_calculated_date) >
                lv_dto_approved_date
           )
        THEN
          lv_dto_approved_date :=
          NVL(:NEW.etd_overrided_date,
              :NEW.etd_calculated_date);
        END IF;
      END IF;

      --@@@Surya 20-Jul-2011:18533-DPRRD should be consider DTO Approved release date if it is greater than LTD/ETD calculated from above
      IF (lv_dto_approved_date IS NULL)
      THEN
        lv_dto_approved_date :=
        NVL(:NEW.dprrd_overrided_date,
            :NEW.dprrd_calculated_date);
      ELSE
        IF (NVL(:NEW.dprrd_overrided_date, :NEW.dprrd_calculated_date) IS NOT NULL
           )
           AND (NVL(:NEW.dprrd_overrided_date, :NEW.dprrd_calculated_date) >
                lv_dto_approved_date
           )
        THEN
          lv_dto_approved_date :=
          NVL(:NEW.dprrd_overrided_date,
              :NEW.dprrd_calculated_date);
        END IF;
      END IF;

      --@@@Surya 18-Mar-2009:14546-If no value exists for override then it should populate from mtd calculated date.
      lv_dto_mid_term_date :=
      NVL(:NEW.mtd_overrided_date,
          :NEW.mtd_calculated_date);
    END IF; --#18478

    SELECT COUNT(*)
    INTO lv_count
    FROM offender_release_details
    WHERE offender_book_id = :NEW.offender_book_id;

    IF (lv_count > 0)
    THEN
      UPDATE offender_release_details
      SET approved_release_date = lv_approved_release_date,
        auto_release_date       = lv_auto_release_date,
        dto_approved_date       = lv_dto_approved_date,
        dto_mid_term_date       = lv_dto_mid_term_date
      WHERE offender_book_id = :NEW.offender_book_id;
    ELSE
      INSERT INTO offender_release_details
      (event_id, offender_book_id, movement_type,
       approved_release_date, auto_release_date,
       dto_approved_date, dto_mid_term_date
      )
      VALUES (event_id.NEXTVAL, :NEW.offender_book_id, 'REL',
              lv_approved_release_date, lv_auto_release_date,
              lv_dto_approved_date, lv_dto_mid_term_date
      );
    END IF;
  END;
/

