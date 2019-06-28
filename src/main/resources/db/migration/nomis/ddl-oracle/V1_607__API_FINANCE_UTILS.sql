create or replace package api_owner.api_finance_utils
as
   function show_version return varchar2;

   procedure insert_offender_transaction (
      p_txn_id                 in offender_transactions.txn_id%type,
      p_txn_entry_seq          in offender_transactions.txn_entry_seq%type,
      p_caseload_id            in offender_transactions.caseload_id%type,
      p_offender_id            in offender_transactions.offender_id%type,
      p_offender_book_id       in offender_transactions.offender_book_id%type,
      p_posting_type           in offender_transactions.txn_posting_type%type,
      p_txn_type               in offender_transactions.txn_type%type,
      p_txn_entry_desc         in offender_transactions.txn_entry_desc%type,
      p_txn_entry_amount       in offender_transactions.txn_entry_amount%type,
      p_txn_entry_date         in offender_transactions.txn_entry_date%type,
      p_sub_act_type           in offender_transactions.sub_account_type%type,
      p_txn_reference_number   in offender_transactions.txn_reference_number%type,
      p_receipt_number         in offender_transactions.receipt_number%type,
      p_deduction_flag         in offender_transactions.deduction_flag%type default 'N',
      p_remitter_name          in offender_transactions.remitter_name%type,
      p_remitter_id            in offender_transactions.remitter_id%type,
      p_pre_withhold_amount    in offender_transactions.pre_withhold_amount%type default null,
      p_hold_number            in offender_transactions.hold_number%type default null,
      p_slip_printed_flag      in offender_transactions.slip_printed_flag%type default 'Y',
      p_receipt_printed_flag   in offender_transactions.receipt_printed_flag%type default 'N',
      p_receipt_pending_print_flag   in offender_transactions.receipt_pending_print_flag%type default 'N',
      p_gross_net_flag         in offender_transactions.gross_net_flag%type default 'N',
      p_txn_adjusted_flag      in offender_transactions.txn_adjusted_flag%type default 'N',
      p_hold_clear_flag        in offender_transactions.hold_clear_flag%type default 'N',
      p_hold_until_date        in offender_transactions.hold_until_date%type default null,
      p_payee_name_text        in offender_transactions.payee_name_text%type default null,
      p_client_unique_ref      in offender_transactions.client_unique_ref%type default null);

   procedure reopen_trust_account(p_caseload_id      in offender_trust_accounts.caseload_id%type,
                                  p_offender_id      in offender_trust_accounts.offender_id%type,
                                  p_offender_book_id in offender_transactions.offender_book_id%type,
                                  p_deduction_flag   in offender_transactions.deduction_flag%type);
                                  
   
   procedure transfer_funds(p_caseload_id      in offender_transactions.caseload_id%type,
                            p_offender_id      in offender_transactions.offender_id%type,
                            p_offender_book_id in offender_transactions.offender_book_id%type);

   procedure close_account(p_caseload_id      in offender_transactions.caseload_id%type,
                           p_offender_id      in offender_transactions.offender_id%type);

   procedure transfer_funds_to_welfare ( p_txn_id           in offender_transactions.txn_id%type,
                                         p_txn_entry_seq    in offender_transactions.txn_entry_seq%type,
                                         p_caseload_id      in offender_transactions.caseload_id%type,
                                         p_offender_id      in offender_transactions.offender_id%type,
                                         p_offender_book_id in offender_transactions.offender_book_id%type,
                                         p_payee_name       in offender_transactions.payee_name_text%type);
   
   procedure zero_sub_account_amount  ( p_txn_id         in offender_transactions.txn_id%type,
                                        p_txn_entry_seq  in offender_transactions.txn_entry_seq%type,
                                        p_caseload_id    in offender_transactions.caseload_id%type,
                                        p_offender_id    in offender_transactions.offender_id%type,
                                        p_txn_entry_date in offender_transactions.txn_entry_date%type,
                                        p_account_code   in offender_sub_accounts.trust_account_code%type);

   function get_transaction_type_desc(p_txn_type varchar2) return varchar2;
end api_finance_utils;
/
show err
create or replace package body api_owner.api_finance_utils
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.0   21-Sep-2017';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      ------------------------------------------------------------------------------------------
      Person      Date           Version     Comments
      ---------   -----------    ---------   ---------------------------------------------------
      Paul M      21-Sep-2017     1.0        Initial version
   */

   gk_inst_caseload_type     constant varchar2(12) := 'INST';

   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;


   procedure insert_offender_transaction (
      p_txn_id                 in offender_transactions.txn_id%type,
      p_txn_entry_seq          in offender_transactions.txn_entry_seq%type,
      p_caseload_id            in offender_transactions.caseload_id%type,
      p_offender_id            in offender_transactions.offender_id%type,
      p_offender_book_id       in offender_transactions.offender_book_id%type,
      p_posting_type           in offender_transactions.txn_posting_type%type,
      p_txn_type               in offender_transactions.txn_type%type,
      p_txn_entry_desc         in offender_transactions.txn_entry_desc%type,
      p_txn_entry_amount       in offender_transactions.txn_entry_amount%type,
      p_txn_entry_date         in offender_transactions.txn_entry_date%type,
      p_sub_act_type           in offender_transactions.sub_account_type%type,
      p_txn_reference_number   in offender_transactions.txn_reference_number%type,
      p_receipt_number         in offender_transactions.receipt_number%type,
      p_deduction_flag         in offender_transactions.deduction_flag%type default 'N',
      p_remitter_name          in offender_transactions.remitter_name%type,
      p_remitter_id            in offender_transactions.remitter_id%type,
      p_pre_withhold_amount    in offender_transactions.pre_withhold_amount%type default null,
      p_hold_number            in offender_transactions.hold_number%type default null,
      p_slip_printed_flag      in offender_transactions.slip_printed_flag%type default 'Y',
      p_receipt_printed_flag   in offender_transactions.receipt_printed_flag%type default 'N',
      p_receipt_pending_print_flag   in offender_transactions.receipt_pending_print_flag%type default 'N',
      p_gross_net_flag         in offender_transactions.gross_net_flag%type default 'N',
      p_txn_adjusted_flag      in offender_transactions.txn_adjusted_flag%type default 'N',
      p_hold_clear_flag        in offender_transactions.hold_clear_flag%type default 'N',
      p_hold_until_date        in offender_transactions.hold_until_date%type default null,
      p_payee_name_text        in offender_transactions.payee_name_text%type default null,
      p_client_unique_ref      in offender_transactions.client_unique_ref%type default null)
   is
      
      v_txn_entry_desc         offender_transactions.txn_entry_desc%type;
   begin

      if p_txn_entry_desc is null then
         v_txn_entry_desc := get_transaction_type_desc(p_txn_type);
      else
         v_txn_entry_desc := p_txn_entry_desc;
      end if;

      insert into offender_transactions
                  (txn_id, txn_entry_seq, caseload_id, offender_id,
                   offender_book_id, txn_posting_type, txn_type,
                   txn_entry_desc, txn_entry_amount, txn_entry_date,
                   sub_account_type, txn_reference_number, modify_date,
                   modify_user_id, receipt_number, slip_printed_flag,
                   transfer_caseload_id, receipt_printed_flag,
                   pre_withhold_amount, deduction_flag,
                   closing_cheque_number, remitter_name, payee_code,
                   payee_name_text, payee_corporate_id, payee_person_id,
                   adjust_txn_id, adjust_txn_entry_id, adjust_offender_id,
                   adjust_account_code, txn_adjusted_flag, deduction_type,
                   info_number, hold_clear_flag, hold_until_date,
                   hold_number, gross_amount,  gross_net_flag, remitter_id,
                   apply_spending_limit_amount, receipt_pending_print_flag,
                   client_unique_ref
                  )
           values (p_txn_id, p_txn_entry_seq, p_caseload_id, p_offender_id,
                   p_offender_book_id, p_posting_type, p_txn_type,
                   v_txn_entry_desc, p_txn_entry_amount, p_txn_entry_date,
                   p_sub_act_type, p_txn_reference_number, p_txn_entry_date,
                   user, p_receipt_number, p_slip_printed_flag,
                   null, p_receipt_printed_flag,
                   p_pre_withhold_amount, p_deduction_flag,
                   null, p_remitter_name, null,
                   p_payee_name_text, null, null,
                   null, 99, null,
                   null, p_txn_adjusted_flag, null,
                   null, p_hold_clear_flag, p_hold_until_date,
                   p_hold_number, null, p_gross_net_flag, p_remitter_id,
                   null, p_receipt_pending_print_flag,
                   p_client_unique_ref
                  );
   end insert_offender_transaction;

   procedure reopen_trust_account(p_caseload_id      in offender_trust_accounts.caseload_id%type,
                                  p_offender_id      in offender_trust_accounts.offender_id%type,
                                  p_offender_book_id in offender_transactions.offender_book_id%type,
                                  p_deduction_flag   in offender_transactions.deduction_flag%type)
                                  
   is
      v_txn_type               offender_transactions.txn_type%type;
      v_sub_account_type       offender_transactions.sub_account_type%type;
      v_txn_posting_type       offender_transactions.txn_posting_type%type;
   begin

      update offender_trust_accounts
         set account_closed_flag = 'N'
       where offender_id = p_offender_id 
         and caseload_id = p_caseload_id;
  
      begin
         select tro.txn_type, ac.sub_account_type, ac.txn_posting_type
           into v_txn_type, v_sub_account_type, v_txn_posting_type
           from transaction_operations tro
           join account_codes ac
             on ac.account_code = tro.cr_account_code
                and ac.caseload_type = gk_inst_caseload_type     
          where tro.module_name = 'OTDOPCTA'
            and tro.caseload_id = p_caseload_id;
      exception 
         when no_data_found then
            raise_application_error(-20039,'Financial setup error');
            
         when dup_val_on_index then
            raise_application_error(-20039,'Financial setup error');
      end; 

      insert_offender_transaction (p_txn_id                => txn_id.nextval,
                                   p_txn_entry_seq         => 1,
                                   p_caseload_id           => p_caseload_id,
                                   p_offender_id           => p_offender_id,
                                   p_offender_book_id      => p_offender_book_id,
                                   p_posting_type          => v_txn_posting_type,
                                   p_txn_type              => v_txn_type,
                                   p_txn_entry_desc        => 'Re-Open Closed Account',
                                   p_txn_entry_amount      => 0,
                                   p_txn_entry_date        => trunc(sysdate),
                                   p_sub_act_type          => v_sub_account_type,
                                   p_txn_reference_number  => null,
                                   p_receipt_number        => null,
                                   p_deduction_flag        => p_deduction_flag,
                                   p_remitter_name         => null,
                                   p_remitter_id           => null,
                                   p_pre_withhold_amount   => null,
                                   p_txn_adjusted_flag     => 'N',
                                   p_client_unique_ref      => null);
   end reopen_trust_account;

   procedure transfer_funds(p_caseload_id      in offender_transactions.caseload_id%type,
                            p_offender_id      in offender_transactions.offender_id%type,
                            p_offender_book_id in offender_transactions.offender_book_id%type)
   is

      v_payee_name       offender_transactions.payee_name_text%type;
      v_txn_id           offender_transactions.txn_id%type;
      v_txn_entry_seq    offender_transactions.txn_entry_seq%type;
      v_gl_txn_entry_seq offender_transactions.txn_entry_seq%type;
      v_gl_entry_seq     gl_transactions.gl_entry_seq%type;
      v_txn_entry_desc   offender_transactions.txn_entry_desc%type;
      v_sqlcode          number;
      v_sqlerrm          varchar2(512);
   begin
      
      begin
         select substr(( o.first_name || '  ' || o.last_name ), 1, 45 )
           into v_payee_name
           from offenders o
           join offender_bookings ob
             on ob.offender_id = o.offender_id
          where ob.offender_book_id = p_offender_book_id;
      exception
         when no_data_found then
            v_payee_name := null;
      end;

      -- Only One Transaction_Id should be created for the whole process.

      v_txn_id        := txn_id.nextval;
      v_txn_entry_seq := 0;

      v_txn_entry_desc := get_transaction_type_desc('OT');

      -- Loop through all sub accounts which are setup on Transaction Operations except for REG.

      for sub_acc_rec in ( select osa.trust_account_code, osa.balance, ac.sub_account_type
                             from offender_sub_accounts osa 
                             join account_codes ac
                               on osa.trust_account_code = ac.account_code
                                  and ac.caseload_type = gk_inst_caseload_type
                                  and ac.sub_account_type  != 'REG'   
                            where osa.offender_id = p_offender_id
                              and osa.caseload_id = p_caseload_id
                              and osa.trust_account_code in ( select dr_account_code
                                                                from transaction_operations
                                                               where caseload_id = p_caseload_id
                                                                 and module_name = 'OTDCLINA'
                                                                 and txn_type = 'OT' ))
      loop
            
         if sub_acc_rec.balance > 0 then                                    

            v_txn_entry_seq    := v_txn_entry_seq + 1;
            v_gl_txn_entry_seq := v_txn_entry_seq;

            v_gl_entry_seq := 0;
 
            insert_offender_transaction (p_txn_id                => v_txn_id,
                                         p_txn_entry_seq         => v_txn_entry_seq,
                                         p_caseload_id           => p_caseload_id,
                                         p_offender_id           => p_offender_id,
                                         p_offender_book_id      => p_offender_book_id,
                                         p_posting_type          => 'DR',
                                         p_txn_type              => 'OT',
                                         p_txn_entry_desc        => v_txn_entry_desc,
                                         p_txn_entry_amount      => sub_acc_rec.balance,
                                         p_txn_entry_date        => trunc(sysdate),
                                         p_sub_act_type          => sub_acc_rec.sub_account_type,
                                         p_txn_reference_number  => null,
                                         p_receipt_number        => null,
                                         p_deduction_flag        => null,
                                         p_remitter_name         => null,
                                         p_remitter_id           => null,
                                         p_pre_withhold_amount   => null,
                                         p_slip_printed_flag     => 'N',
                                         p_txn_adjusted_flag     => 'N',
                                         p_hold_clear_flag       => 'N',
                                         p_payee_name_text       => v_payee_name,
                                         p_client_unique_ref      => null);

            trust.update_offender_balance (p_csld_id          => p_caseload_id,
                                           p_off_id           => p_offender_id,
                                           p_trans_post_type  => 'DR',
                                           p_trans_date       => trunc(sysdate),
                                           p_trans_number     => v_txn_id,
                                           p_trans_type       => 'OT',
                                           p_trans_amount     => sub_acc_rec.balance,
                                           p_sub_act_type     => sub_acc_rec.sub_account_type,
                                           p_allow_overdrawn  => 'N');
      
            v_txn_entry_seq    := v_txn_entry_seq + 1;

            insert_offender_transaction (p_txn_id                => v_txn_id,
                                         p_txn_entry_seq         => v_txn_entry_seq,
                                         p_caseload_id           => p_caseload_id,
                                         p_offender_id           => p_offender_id,
                                         p_offender_book_id      => p_offender_book_id,
                                         p_posting_type          => 'CR',
                                         p_txn_type              => 'OT',
                                         p_txn_entry_desc        => v_txn_entry_desc,
                                         p_txn_entry_amount      => sub_acc_rec.balance,
                                         p_txn_entry_date        => trunc(sysdate),
                                         p_sub_act_type          => 'REG',
                                         p_txn_reference_number  => null,
                                         p_receipt_number        => null,
                                         p_deduction_flag        => null,
                                         p_remitter_name         => null,
                                         p_remitter_id           => null,
                                         p_pre_withhold_amount   => null,
                                         p_slip_printed_flag     => 'N',
                                         p_txn_adjusted_flag     => 'N',
                                         p_hold_clear_flag       => 'N',
                                         p_payee_name_text       => v_payee_name,
                                         p_client_unique_ref      => null);

            trust.update_offender_balance (p_csld_id          => p_caseload_id,
                                           p_off_id           => p_offender_id,
                                           p_trans_post_type  => 'CR',
                                           p_trans_date       => trunc(sysdate),
                                           p_trans_number     => v_txn_id,
                                           p_trans_type       => 'OT',
                                           p_trans_amount     => sub_acc_rec.balance,
                                           p_sub_act_type     => 'REG',
                                           p_allow_overdrawn  => 'N');
      
            trust.process_gl_trans_new (p_csld_id          => p_caseload_id,
                                        p_trans_type       => 'OT',
                                        p_operation_type   => null,
                                        p_trans_amount     => sub_acc_rec.balance,
                                        p_trans_number     => v_txn_id,
                                        p_trans_date       => trunc(sysdate),
                                        p_trans_desc       => v_txn_entry_desc,
                                        p_trans_seq        => v_gl_txn_entry_seq,
                                        p_module_name      => 'OTDCLINA',
                                        p_off_id           => p_offender_id,
                                        p_off_book_id      => p_offender_book_id,
                                        p_sub_act_type_dr  => sub_acc_rec.sub_account_type,
                                        p_sub_act_type_cr  => 'REG',
                                        p_payee_pers_id    => null,
                                        p_payee_corp_id    => null,
                                        p_payee_name_text  => null,
                                        p_gl_sqnc          => v_gl_entry_seq,
                                        p_off_ded_id       => null);
          
                 
            zero_sub_account_amount  ( p_txn_id         => v_txn_id,
                                       p_txn_entry_seq  => v_gl_txn_entry_seq,
                                       p_caseload_id    => p_caseload_id,
                                       p_offender_id    => p_offender_id,
                                       p_txn_entry_date => trunc(sysdate),
                                       p_account_code   => sub_acc_rec.trust_account_code);
         end if;
      end loop;

      -- Close the a/c before transferring funds to Welfare.
   
      close_account(p_caseload_id => p_caseload_id,
                    p_offender_id  => p_offender_id);
 
      -- Transfer funds from REG sub a/c to Welfare A/C. 
      transfer_funds_to_welfare( p_txn_id           => v_txn_id,
                                 p_txn_entry_seq    => v_txn_entry_seq + 1,
                                 p_caseload_id      => p_caseload_id,
                                 p_offender_id      => p_offender_id,
                                 p_offender_book_id => p_offender_book_id,
                                 p_payee_name       => v_payee_name);

   /*
   exception
      when others then 
         v_sqlcode := sqlcode;
         v_sqlerrm := sqlerrm;
         --
         -- Remap user exceptions raised by the nomis trust package in the range -20999 to -20000  
         -- To avoid conflict with the Nomis API usage of the user exception range.
         --
         case
            when v_sqlcode = -20009 and v_sqlerrm like '%Overdrawn transaction%' then 
               raise_application_error (-20009, 'Insufficient funds');
            when v_sqlcode = -20000 and v_sqlerrm like '%No setup been found in Maintain Transaction Operations%' then 
               raise_application_error (-20018, 'Invalid transaction type');
            when v_sqlcode between -20999 and -20000 then 
               -- Map any other user defined exceptions to -20013
               nomis_api_log.error('API_FINANCE_PROCS.TRANSFER_FUNDS',v_sqlerrm);
               raise_application_error (-20013, v_sqlerrm);
            else
               raise;
         end case;
   */
   end transfer_funds;

   -- This Procedure updates offender_sub_accounts balance = 0.
   procedure zero_sub_account_amount  ( p_txn_id         in offender_transactions.txn_id%type,
                                        p_txn_entry_seq  in offender_transactions.txn_entry_seq%type,
                                        p_caseload_id    in offender_transactions.caseload_id%type,
                                        p_offender_id    in offender_transactions.offender_id%type,
                                        p_txn_entry_date in offender_transactions.txn_entry_date%type,
                                        p_account_code   in offender_sub_accounts.trust_account_code%type)
   is
   begin

      update offender_sub_accounts 
         set balance        = 0 ,
             last_txn_id    = p_txn_id,
             list_seq       = p_txn_entry_seq,
		   	 modify_date    = p_txn_entry_date
       where caseload_id = p_caseload_id
         and offender_id = p_offender_id
         and trust_account_code = p_account_code;

   end zero_sub_account_amount;

   procedure close_account(p_caseload_id      in offender_transactions.caseload_id%type,
                           p_offender_id      in offender_transactions.offender_id%type)
   is
   begin

      update offender_trust_accounts
         set account_closed_flag = 'Y' 
       where caseload_id = p_caseload_id
         and offender_id = p_offender_id;

   end close_account;

   -- This Procedure moves the money from regular account to welfare a/c
   --  then inserts records into offender_transaction and gl_transactions table for cash and
   --  check amount entered and updates gl balance.

   procedure transfer_funds_to_welfare ( p_txn_id           in offender_transactions.txn_id%type,
                                         p_txn_entry_seq    in offender_transactions.txn_entry_seq%type,
                                         p_caseload_id      in offender_transactions.caseload_id%type,
                                         p_offender_id      in offender_transactions.offender_id%type,
                                         p_offender_book_id in offender_transactions.offender_book_id%type,
                                         p_payee_name       in offender_transactions.payee_name_text%type)
   is
      v_trust_account_code offender_sub_accounts.trust_account_code%type; 
      v_balance            offender_sub_accounts.balance%type;
      v_txn_entry_desc   offender_transactions.txn_entry_desc%type;
      v_gl_entry_seq     gl_transactions.gl_entry_seq%type;

   begin
      v_txn_entry_desc := get_transaction_type_desc('WFT');
   
      select osa.trust_account_code, osa.balance
        into v_trust_account_code, v_balance
        from offender_sub_accounts osa 
        join account_codes ac
          on osa.trust_account_code = ac.account_code
             and ac.caseload_type = gk_inst_caseload_type
             and ac.sub_account_type  = 'REG'   
             where osa.offender_id = p_offender_id
             and osa.caseload_id = p_caseload_id;

      if v_balance > 0 then

         insert_offender_transaction (p_txn_id                => p_txn_id,
                                      p_txn_entry_seq         => p_txn_entry_seq,
                                      p_caseload_id           => p_caseload_id,
                                      p_offender_id           => p_offender_id,
                                      p_offender_book_id      => p_offender_book_id,
                                      p_posting_type          => 'DR',
                                      p_txn_type              => 'WFT',
                                      p_txn_entry_desc        => v_txn_entry_desc,
                                      p_txn_entry_amount      => v_balance,
                                      p_txn_entry_date        => trunc(sysdate),
                                      p_sub_act_type          => 'REG',
                                      p_txn_reference_number  => null,
                                      p_receipt_number        => null,
                                      p_deduction_flag        => null,
                                      p_remitter_name         => null,
                                      p_remitter_id           => null,
                                      p_pre_withhold_amount   => null,
                                      p_slip_printed_flag     => 'N',
                                      p_txn_adjusted_flag     => 'N',
                                      p_hold_clear_flag       => 'N',
                                      p_payee_name_text       => p_payee_name,
                                      p_client_unique_ref      => null);

         trust.update_offender_balance (p_csld_id          => p_caseload_id,
                                        p_off_id           => p_offender_id,
                                        p_trans_post_type  => 'DR',
                                        p_trans_date       => trunc(sysdate),
                                        p_trans_number     => p_txn_id,
                                        p_trans_type       => 'WFT',
                                        p_trans_amount     => v_balance,
                                        p_sub_act_type     => 'REG',
                                        p_allow_overdrawn  => 'N');
         v_gl_entry_seq := 0;
         trust.process_gl_trans_new (p_csld_id          => p_caseload_id,
                                     p_trans_type       => 'WFT',
                                     p_operation_type   => null,
                                     p_trans_amount     => v_balance,
                                     p_trans_number     => p_txn_id,
                                     p_trans_date       => trunc(sysdate),
                                     p_trans_desc       => v_txn_entry_desc,
                                     p_trans_seq        => p_txn_entry_seq,
                                     p_module_name      => 'OTDCLINA',
                                     p_off_id           => p_offender_id,
                                     p_off_book_id      => p_offender_book_id,
                                     p_sub_act_type_dr  => 'REG',
                                     p_sub_act_type_cr  => null,
                                     p_payee_pers_id    => null,
                                     p_payee_corp_id    => null,
                                     p_payee_name_text  => null,
                                     p_gl_sqnc          => v_gl_entry_seq,
                                     p_off_ded_id       => null);

            zero_sub_account_amount  ( p_txn_id         => p_txn_id,
                                       p_txn_entry_seq  => p_txn_entry_seq,
                                       p_caseload_id    => p_caseload_id,
                                       p_offender_id    => p_offender_id,
                                       p_txn_entry_date => trunc(sysdate),
                                       p_account_code   => v_trust_account_code);
      
         -- After moving the funds to Welfare we now update the Current_Balance in 
         -- Offender_Trust_Accounts table with sum of balance in offender_sub_accounts.

         update offender_trust_accounts ota
            set current_balance = (select sum(osa.balance)
                                     from offender_sub_accounts osa
                                    where osa.caseload_id = ota.caseload_id
                                      and osa.offender_id = ota.offender_id)
          where offender_id = p_offender_id
            and caseload_id = p_caseload_id;
      end if;
   end transfer_funds_to_welfare;

   function get_transaction_type_desc(p_txn_type varchar2) return varchar2
   is
      v_txn_type_desc transaction_types.description%type;
   begin
      select description
        into v_txn_type_desc         
        from transaction_types
       where txn_type = p_txn_type;
      return v_txn_type_desc;
   exception
      when no_data_found then 
         raise_application_error (-20018, 'Invalid transaction type');
   end get_transaction_type_desc;

end api_finance_utils;
/
show err
