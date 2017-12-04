CREATE OR REPLACE TRIGGER "OFFENDER_BOOKINGS_T3"
BEFORE DELETE
  ON offender_bookings
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
       Version Number = 2.1  Date Modified = 21-DEC-2007
    ============================================================
      MODIFICATION HISTORY
       Person       Date      version      Comments
    -----------  ---------- -----------  -------------------------------
      Graham     21/12/2007  2.1          #7775: Code added for Merge, fix versioning
      Neil B.    08/08/2005  2.0          Created.
    */
    IF :OLD.living_unit_id IS NOT NULL
    THEN
      tag_establishment.adjust_occupants(:OLD.living_unit_id, -1);
    END IF;
    EXCEPTION
    WHEN OTHERS
    THEN
    tag_error.handle;
  END;

/



CREATE OR REPLACE TRIGGER "OFFENDER_BOOKINGS_T4"
AFTER INSERT OR UPDATE
  ON OFFENDER_BOOKINGS
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;

    /*
    ============================================================
       Version Number = 2.4  Date Modified = 01-Jun-2011
    ============================================================
      MODIFICATION HISTORY
       Person       Date      version      Comments
    -----------  ---------- -----------  -------------------------------
       Neil      01/06/2011   2.4          18469-Added code to unsuspend an offender program when moving in.
       Surya     20/03/2009   2.3          14570-Create release schedules for inactive bookings but not transfer offenders.
       Surya     19/03/2009   2.2          14570-Create release schedules for old booking.
       Graham    21/12/2007   2.1          #7775: Code added for Merge, fix versioning
       David Ng  9 Feb 2006   2.0          Create offender Booking Details
    */
    IF INSERTING
    THEN
      INSERT INTO offender_booking_details
      (offender_book_id
      )
      VALUES (:NEW.offender_book_id
      );
    ELSE
      --@@@Surya 19-Mar-09:14570-It should trigger only upon re-activating an Offender.
      IF :OLD.active_flag = 'N'
         AND :OLD.in_out_status <> 'TRN'
         AND :NEW.active_flag = 'Y'
      THEN
        --@@@Surya 19-Mar-2009:14561-Re-create schedule for old booking.
        tag_booking.old_booking_release_schedules(:NEW.offender_book_id);
      END IF;

      -- NDB 01/06/2011: 18469: Unsuspend any active activities when moving in.

      IF :NEW.in_out_status != :OLD.in_out_status
         AND :NEW.in_out_status = 'IN'
      THEN
        tag_schedule.unsuspend_offender_program(
            :NEW.offender_book_id,
            :NEW.agy_loc_id);
      END IF;
    END IF;
    EXCEPTION
    WHEN OTHERS
    THEN
    tag_error.handle;
  END;
/



CREATE OR REPLACE TRIGGER "OFFENDER_BOOKINGS_T6"
BEFORE INSERT OR UPDATE
  ON offender_bookings
REFERENCING NEW AS NEW OLD AS OLD
FOR EACH ROW
  DECLARE
    /*===================================================================
     Version Number = 3.2  Date Modified = 06-Oct-2010
     ====================================================================*/
    /* MODIFICATION HISTORY
       Person      Date          Version    Comments
       ----------  ----------    ------     --------------------------
       P Thakur    06/10/2011    3.2        D#18645: Version number corrected.
       P Thakur    06/10/2011    3.1        D#18645: root_offender_id should used for booking resequence.
       Surya       09/04/2009    2.4        14590-Added booking_begin_date into sort order list.
       Igor       05/06/2008     2.3        #8428: Add code for UPDATE transaction
       Graham     21/12/2007     2.2        #7775: Code added for Merge, fix versioning
       Surya      21/08/2007     2.1        Removed booking_end_date from select list,
                                            as it was not used.
       Surya      16/08/2007     2.0        Initial Draft.
    */
    CURSOR book_cur
    IS
      SELECT ROWID
      FROM offender_bookings
      WHERE root_offender_id = :NEW.root_offender_id
      FOR UPDATE OF offender_book_id WAIT 1
      ORDER BY booking_end_date DESC NULLS FIRST,
        booking_begin_date DESC;

    v_seq offender_bookings.booking_seq%TYPE := 1;
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;

    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF INSERTING
    THEN
      FOR book_rec IN book_cur
      LOOP
        v_seq := v_seq + 1;

        UPDATE offender_bookings
        SET booking_seq = v_seq
        WHERE ROWID = book_rec.ROWID;
      END LOOP;

      :NEW.booking_seq := 1;
    ELSIF UPDATING
      THEN
        IF :OLD.booking_status = 'C' AND :NEW.booking_status = 'O'
        THEN
          oidadmis.v_booking_seq_change.offender_book_id := :OLD.offender_book_id;
          oidadmis.v_booking_seq_change.offender_id := :OLD.root_offender_id; -- @@@ PThakur 06/10/2011 Defect#18645: Changed from OLD.offender_id;
          oidadmis.v_booking_seq_change.old_booking_seq := :OLD.booking_seq;
          :NEW.booking_seq := 1;
        END IF;
    END IF;
    EXCEPTION
    WHEN OTHERS
    THEN
    tag_error.handle;
  END offender_bookings_t6;
/



CREATE OR REPLACE TRIGGER "OFFENDER_BOOKINGS_TA"
BEFORE INSERT OR UPDATE OR DELETE ON OFFENDER_BOOKINGS
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
          'OFFENDER_BOOKINGS',
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



CREATE OR REPLACE TRIGGER "OFFENDER_BOOKINGS_T5"
AFTER
INSERT OR UPDATE OF AGY_LOC_ID
  ON OFFENDER_BOOKINGS
REFERENCING OLD AS OLD NEW AS NEW
FOR EACH ROW
  DECLARE
    /* =========================================================
       Version Number = 2.4  Date Modified = 21-DEC-2007
       ========================================================= */

    /* MODIFICATION HISTORY
       Person      Date            Version       Comments
       ---------   ------       ------------  ------------------------------
       Graham      21/12/2007    2.4         #7775: Code added for Merge, fix versioning
       Krishna     04/07/2006    2.3         Fix for #3076, passing sysdate instead of effective_date
       Krishna     26/04/2006    2.2         Fix for #1613, added updating code
       Krishna     23/04/2006    2.1         Fix for #1417; removed after update for the trigger and code will execute for insert only
       Krishna     20/03/2006    2.0         Created
    */

    CURSOR get_agy_loc_team_funs_cur (
      p_agy_loc_id agy_loc_team_functions.agy_loc_id%TYPE
    )
    IS
      SELECT
        function_type,
        team_id,
        overwritten_flag,
        effective_date
      FROM agy_loc_team_functions
      WHERE agy_loc_id = p_agy_loc_id AND active_flag = 'Y';

    FUNCTION is_off_team_assign_exists(
      p_off_book_id   IN offender_team_assignments.offender_book_id%TYPE,
      p_function_type IN offender_team_assignments.function_type%TYPE
    )
      RETURN NUMBER
    IS
      CURSOR get_team_assign_cur
      IS
        SELECT team_id
        FROM offender_team_assignments
        WHERE offender_book_id = p_off_book_id
              AND function_type = p_function_type;

      lv_team_id offender_team_assignments.team_id%TYPE := 0;
      BEGIN
        OPEN get_team_assign_cur;

        FETCH get_team_assign_cur
        INTO lv_team_id;

        RETURN (lv_team_id);
        EXCEPTION
        WHEN OTHERS
        THEN
        tag_error.handle();
      END is_off_team_assign_exists;

    --@@@ Procedure to create new function and team details for the offender
    --@@@ in the offender_team_assignments table
    PROCEDURE ins_offender_team_assignments(
      p_off_book_id   offender_team_assignments.offender_book_id%TYPE,
      p_function_type offender_team_assignments.function_type%TYPE,
      p_team_id       offender_team_assignments.team_id%TYPE,
      p_assign_date   offender_team_assignments.assignment_date%TYPE
    )
    IS
      BEGIN
        --@@@ create a new record in the offender team assignments table
        INSERT INTO offender_team_assignments
        (offender_book_id, function_type, team_id, assignment_date
        )
        VALUES (p_off_book_id, p_function_type, p_team_id, p_assign_date
        );
        EXCEPTION
        WHEN OTHERS
        THEN
        tag_error.handle();
      END ins_offender_team_assignments;
  BEGIN
    -- Lines added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;
    -- Change added by GJC 21/12/2007 to check if the trigger code should be executed or not
    IF INSERTING
    THEN
      IF :NEW.agy_loc_id IS NOT NULL
      THEN
        FOR item IN get_agy_loc_team_funs_cur(:NEW.agy_loc_id)
        LOOP
          --@@@ check if there is a record exists with the function for the offender
          --@@@ in the offender_team_assignments table
          IF is_off_team_assign_exists(:NEW.offender_book_id,
                                       item.function_type
             ) > 0
          THEN
            IF item.overwritten_flag = 'Y'
            THEN
              --@@@ delete the existing function and team details
              --@@@ this will move the details to history table
              DELETE FROM offender_team_assignments
              WHERE offender_book_id = :NEW.offender_book_id
                    AND function_type = item.function_type;

              --@@@ finally insert the new function and team details for the offender
              ins_offender_team_assignments(:NEW.offender_book_id,
                                            item.function_type,
                                            item.team_id,
                                            --item.effective_date
                                            sysdate
              );
            END IF;
          ELSE
            --@@@ since there is no function for the offender
            --@@@ insert the function and team details straigh away
            ins_offender_team_assignments(:NEW.offender_book_id,
                                          item.function_type,
                                          item.team_id,
                                          --item.effective_date
                                          sysdate
            );
          END IF;
        END LOOP;
      END IF;
    ELSIF UPDATING
      THEN
        IF :NEW.agy_loc_id IS NOT NULL
           AND :NEW.agy_loc_id <> NVL(:OLD.agy_loc_id, 'x')
        THEN
          FOR item IN get_agy_loc_team_funs_cur(:NEW.agy_loc_id)
          LOOP
            --@@@ check if there is a record exists with the function for the offender
            --@@@ in the offender_team_assignments table
            IF is_off_team_assign_exists(:OLD.offender_book_id,
                                         item.function_type
               ) > 0
            THEN
              IF item.overwritten_flag = 'Y'
              THEN
                --@@@ delete the existing function and team details
                --@@@ this will move the details to history table
                DELETE FROM offender_team_assignments
                WHERE offender_book_id = :OLD.offender_book_id
                      AND function_type = item.function_type;

                --@@@ finally insert the new function and team details for the offender
                ins_offender_team_assignments(:OLD.offender_book_id,
                                              item.function_type,
                                              item.team_id,
                                              --item.effective_date
                                              sysdate
                );
              END IF;
            ELSE
              --@@@ since there is no function for the offender
              --@@@ insert the function and team details straigh away
              ins_offender_team_assignments(:OLD.offender_book_id,
                                            item.function_type,
                                            item.team_id,
                                            --item.effective_date
                                            sysdate
              );
            END IF;
          END LOOP;
        END IF;
    END IF;
    EXCEPTION
    WHEN OTHERS
    THEN
    tag_error.handle();
  END;

/



CREATE OR REPLACE TRIGGER "OFFENDER_BOOKINGS_T2"
BEFORE UPDATE
  ON offender_bookings
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
       Version Number = 2.5  Date Modified = 15-JAN-2009
    ============================================================
      MODIFICATION HISTORY
       Person       Date      version      Comments
    -----------  --------- -----------  -------------------------------
       PThakur   15/01/2009  2.5        D#13280: Removed commented code.
       PThakur   15/01/2009  2.4        D#13280: Call to tag_bed_assignment.end_prev_bed_assg_hty commented and moved to
                                        trigger on bed_assignment_histories
       Graham    21/12/2007  2.3        #7775: Code added for Merge, fix versioning
       Patirck   05/10/2007  2.2        TD 7856.  Added code for populate end date/time in bed assignment history
       Neil B.   08/08/2005  2.1        Added adjustment of occupancy
       David Ng  16/06/2005  1.1        Audit column trigger
    */
    IF :NEW.agy_loc_id <> :NEW.create_agy_loc_id
       AND :NEW.agy_loc_id <> 'OUT'
    THEN
      :NEW.create_agy_loc_id := :NEW.agy_loc_id;
    END IF;

    IF NVL(:OLD.living_unit_id, 0) != NVL(:NEW.living_unit_id, 0)
    THEN
      IF :OLD.living_unit_id IS NOT NULL
      THEN
        tag_establishment.adjust_occupants(:OLD.living_unit_id, -1);
      END IF;

      IF :NEW.living_unit_id IS NOT NULL
      THEN
        tag_establishment.adjust_occupants(:NEW.living_unit_id, 1);
      END IF;
    END IF;
    EXCEPTION
    WHEN OTHERS
    THEN
    tag_error.handle;
  END;

/



CREATE OR REPLACE TRIGGER "OFFENDER_BOOKINGS_T1"
BEFORE INSERT
  ON offender_bookings
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
       Version Number = 2.5  Date Modified = 15-JAN-2009
    ============================================================
      MODIFICATION HISTORY
       Person       Date      version      Comments
    -----------  ---------- -----------  -------------------------------
       PThakur   15/01/2009  2.5         D#13280: Removed commented code.
       PThakur   15/01/2009  2.4         D#13280: Call to tag_bed_assignment.end_prev_bed_assg_hty commented and moved to
                                         trigger on bed_assignment_histories
       Graham    21/12/2007  2.3         #7775: Code added for Merge, fix versioning
       Patrick   05/10/2007  2.2         TD 7856.  Added code to populate bed assignment history end date/time
       Neil B.   08/08/2005  2.1         Adjust occupancy.
       David Ng  16/06/2005  1.1         Audit column trigger
    */
    :NEW.create_agy_loc_id := :NEW.agy_loc_id;

    IF :NEW.living_unit_id IS NOT NULL
    THEN
      tag_establishment.adjust_occupants(:NEW.living_unit_id, 1);
    END IF;
    EXCEPTION
    WHEN OTHERS
    THEN
    tag_error.handle;
  END;

/



CREATE OR REPLACE TRIGGER "OFFENDER_BOOKINGS_T7"
AFTER UPDATE
OF BOOKING_STATUS
  ON OFFENDER_BOOKINGS
REFERENCING NEW AS NEW OLD AS OLD
  DECLARE
    /*===================================================================
     Version Number = 2.1 Date Modified = 02-Apr-2009
     ====================================================================*/
    /* MODIFICATION HISTORY
       Person      Date          Version    Comments
       ----------  ----------    ------     --------------------------
       Ragini      02-Apr-2009    2.1        15029: Added code to stop the execution for merge
       Igor        04/06/2008     2.0        Initial Draft.
    */
  BEGIN
    -- Lines added by Ragini on 02-Apr-2009 to check if the trigger code should be executed or not
    IF SYS_CONTEXT('NOMIS', 'AUDIT_MODULE_NAME', 50) = 'MERGE'
    THEN
      RETURN;
    END IF;
    -- Call procedure to resequence bookings
    Oidadmis.resequence_bookings;
  END offender_bookings_t7;

/

