create or replace package api_owner.nomis_api_batch
as
   function show_version return varchar2;

   procedure process_pay_batch(p_date             in date default trunc (sysdate),
                               p_check_point      in number default 500,
                               p_job_control_id   in number default null);

   procedure process_stored_payments (
      p_caseload_id      in caseloads.caseload_id%type,
      p_date             in date default trunc (sysdate),
      p_check_point      in number default 500,
      p_job_control_id   in job_controls.job_control_id%type default null,
      p_last_key         in job_controls.last_key%type default null);

   procedure remove_old_events (p_date       in date default trunc (sysdate), 
                                p_checkpoint in number default 1000);
end nomis_api_batch;
/
sho err

create or replace package body api_owner.nomis_api_batch
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.2   21-Jul-2017';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      ------------------------------------------------------------------------------------------
      Person      Date           Version                Comments
      ---------   -----------    ---------   ---------------------------------------------------
      Paul M      21-Jul-2017     1.2        Add housekeeping job remove_old_events 
      Paul M      30-Nov-2016     1.1        Use current date as payment date and add version
                                             and modification history
      Paul M      03-Nov-2016     1.0        Initial version

   */

   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;
   procedure process_pay_batch(p_date             in date default trunc (sysdate),
                               p_check_point      in number default 500,
                               p_job_control_id   in number default null)
   is

      v_id             job_controls.job_control_id%TYPE;
      v_last_key       job_controls.last_key%TYPE;
      v_status         job_controls.job_status%TYPE;
      v_caseload_id   caseloads.caseload_id%TYPE;
   begin

      nomis_context.set_context
                      ('AUDIT_MODULE_NAME', 'NOMIS_API_BATCH.PROCESS_PAY_BATCH');
      -- calling this procedure to process steps from 2 to 4.
      v_id := tag_batch_job_control.register_module (
                  p_module_name     => 'NOMIS_API_BATCH.PROCESS_PAY_BATCH', 
                  p_parameters      => 'p_date=>'||p_date||':p_check_point=>' || p_check_point, 
                  p_status          => v_status,
                  p_last_key        => v_last_key,
                  p_job_control_id  => p_job_control_id);

      if p_job_control_id is not null then
         tag_application_log.log_message('NOMIS_API_BATCH.PROCESS_PAY_BATCH has been restarted', v_id);

         if v_last_key is not null or v_last_key != ' ' then
            v_caseload_id := v_last_key;
         end if;
      else
         tag_application_log.log_message('NOMIS_API_BATCH.PROCESS_PAY_BATCH has been started', v_id);
      end if;

      for each_caseload IN (select c.caseload_id
                              from caseloads c
		                        join agy_loc_establishments ale
		                          on ale.agy_loc_id = c.caseload_id
		                             and ale.establishment_type = 'DIG'
                             where trust_accounts_flag = 'Y'
                               and active_flag = 'Y'
                               and (v_caseload_id is null or caseload_id >= v_caseload_id)
                             order by caseload_id)
      loop
         process_stored_payments (each_caseload.caseload_id,
                                  p_date,
                                  p_check_point,
                                  v_id,
                                  v_last_key);
         v_last_key := NULL;
      end loop;

      tag_application_log.log_message('NOMIS_API_BATCH.PROCESS_PAY_BATCH completed', v_id);
      tag_batch_job_control.register_completion (p_job_control_id => v_id);
      commit;
   end;

   procedure process_stored_payments (
      p_caseload_id      in caseloads.caseload_id%type,
      p_date             in date default trunc (sysdate),
      p_check_point      in number default 500,
      p_job_control_id   in job_controls.job_control_id%type default null,
      p_last_key         in job_controls.last_key%type default null)
   is
      v_txn_id              offender_transactions.txn_id%type;
      v_txn_entry_seq       offender_transactions.txn_entry_seq%type;
      v_payment_date        date;
      v_err_msg             varchar2(1024);
      v_control_count       PLS_INTEGER                                := 0;
   begin
      v_payment_date := trunc(sysdate);
      for pay_rec in (select rowid,
                             api_stored_payment_id,
                             root_offender_id, 
                             offender_book_id,
                             caseload_id, txn_type, txn_reference_number, 
                             txn_entry_date, txn_entry_desc, txn_entry_amount
                        from api_owner.api_stored_payments
                       where caseload_id = p_caseload_id
                         and processed_status = 'NEW'
                         and trunc(posted_timestamp) <= p_date
                       order by posted_timestamp)
      loop
         
         begin
            savepoint start_txn;

            --
            -- If the trust account is closed then re-open it as is currently 
            -- done with inmate_payroll_process.post_pay_transactions 
            -- 
            update offender_trust_accounts
               set account_closed_flag = 'N'
             where offender_id = pay_rec.root_offender_id
               and caseload_id = pay_rec.caseload_id
               and account_closed_flag = 'Y';

            api_finance_procs.create_transaction(
                               p_root_offender_id     => pay_rec.root_offender_id,
                               p_offender_book_id     => pay_rec.offender_book_id,
                               p_caseload_id          => pay_rec.caseload_id,
                               p_txn_type             => pay_rec.txn_type,
                               p_txn_reference_number => pay_rec.txn_reference_number,
                               p_txn_entry_date       => v_payment_date,
                               p_txn_entry_desc       => pay_rec.txn_entry_desc,
                               p_txn_entry_amount     => pay_rec.txn_entry_amount,
                               p_txn_id               => v_txn_id,
                               p_txn_entry_seq        => v_txn_entry_seq);
                               
            update api_stored_payments
               set txn_id = v_txn_id,
                   txn_entry_seq = v_txn_entry_seq,
                   processed_status = 'PROCESSED'
             where rowid = pay_rec.rowid;                                               

            v_control_count := v_control_count + 1;

            if mod (v_control_count, p_check_point) = 0 then
               tag_batch_job_control.register_checkpoint
                             (p_job_control_id      => p_job_control_id,
                              p_last_key            => pay_rec.caseload_id);
               commit;
            end if;
         exception
            when others then
               rollback to start_txn;
               v_err_msg := substr(sqlerrm,1,1024);
               --
               -- Update the api_stored_payments table with failed status
               -- via an autonomous transaction
               --
               api_finance_procs.stored_payment_failed(
                     p_api_stored_payment_id => pay_rec.api_stored_payment_id ,
                     p_err_msg               => v_err_msg);
               --
               -- Update the tag_error_log
               --
               tag_application_log.log_message (
                      'NOMIS_API_BATCH.PROCESS_PAY_BATCH has error when processing stored_payment_id = '||
                      pay_rec.api_stored_payment_id|| ' Error: ' || v_err_msg, p_job_control_id);
         end;
      end loop;

   exception
     when others then
         tag_application_log.log_message
                           (p_message              => 'Process failed exception '||sqlerrm,
                            p_job_control_id       => p_job_control_id);
         tag_batch_job_control.register_failure(p_job_control_id => p_job_control_id);
         raise;
   end process_stored_payments ;

   procedure remove_old_events (p_date       in date default trunc (sysdate), 
                                p_checkpoint in number default 1000)
   is
      k_module_name      job_controls.module_name%type := 'NOMIS_API_BATCH.REMOVE_OLD_EVENTS';
      v_days             number (6);      
      v_count            pls_integer;
      v_job_control_id   job_controls.job_control_id%type;
      v_status           job_controls.job_status%type;
      v_last_key         job_controls.last_key%type;
      v_stage            varchar2(12);
      v_error            varchar2(12);
   begin
      v_stage := 'INIT_10';
      nomis_context.set_context ('AUDIT_MODULE_NAME', k_module_name);
      v_job_control_id :=
         tag_batch_job_control.register_module (p_module_name      => k_module_name,
                                                p_parameters       => 'p_date=>' || p_date || '::p_checkpoint=>' || p_checkpoint,
                                                p_status           => v_status,
                                                p_last_key         => v_last_key);

      if v_status is not null then
         tag_application_log.log_message (p_message        => 'Restarted after key value ' || v_last_key, 
                                          p_job_control_id => v_job_control_id);
      else
         tag_application_log.log_message (p_message        => 'Started', 
                                          p_job_control_id => v_job_control_id);
      end if;

      v_stage := 'INIT_20';
      v_days := oms_miscellaneous.get_profile_value ('NOMISAPI', 'EVENT_LIFE');

      if nvl(v_days, 0) = 0 then
         tag_error.raise_app_error (-20009, 'Error: System Profile EVENT_LIFE not defined. ' || sqlerrm);
      end if;


      v_count := 0;
      loop
         delete from api_owner.api_offender_events
          where event_timestamp < trunc(p_date) - v_days
            and rownum <= p_checkpoint;

         v_count := v_count + sql%rowcount;
         exit when sql%notfound or sql%rowcount < p_checkpoint;
         commit;
      end loop;

      v_stage := 'CW_30';
      tag_batch_job_control.register_completion (p_job_control_id => v_job_control_id);
      commit;
      tag_application_log.log_message (p_message => 'Completed. Records deleted: ' || v_count, 
                                       p_job_control_id => v_job_control_id);
   exception
      when others then        
    	  tag_application_log.log_message (p_message         => 'Process failed at stage '||v_stage || ' error:'||sqlerrm,
                                         p_job_control_id  => v_job_control_id);
        tag_batch_job_control.register_failure(p_job_control_id => v_job_control_id);
        raise;
   END remove_old_events;
end nomis_api_batch;
/
sho err

