create or replace package api_owner.api_finance_procs
as
   function show_version return varchar2;

   procedure account_balances(p_noms_id              in offenders.offender_id_display%type default null,
                              p_root_offender_id     in offenders.root_offender_id%type default null,
                              p_single_offender_id   in varchar2 default null,
                              p_agy_loc_id           in agency_locations.agy_loc_id%type default null,
                              p_cash_balance     out offender_sub_accounts.balance%type,
                              p_spends_balance   out offender_sub_accounts.balance%type,
                              p_savings_balance  out offender_sub_accounts.balance%type);

   procedure transaction_history(p_noms_id              in offenders.offender_id_display%type default null,
                                 p_root_offender_id     in offenders.root_offender_id%type default null,
                                 p_single_offender_id   in varchar2 default null,
                                 p_agy_loc_id           in agency_locations.agy_loc_id%type default null,
                                 p_account_type         in account_codes.sub_account_type%type default null,
                                 p_from_date            in date default null,
                                 p_to_date              in date default null,
                                 p_client_unique_ref    in offender_transactions.client_unique_ref%type default null,
                                 p_trans_csr           out sys_refcursor);
                              
   procedure post_transaction(p_noms_id              in offenders.offender_id_display%type default null,
                              p_root_offender_id     in offenders.root_offender_id%type default null,
                              p_single_offender_id   in varchar2 default null,
                              p_agy_loc_id           in agency_locations.agy_loc_id%type default null,
                              p_txn_type             in offender_transactions.txn_type%type,
                              p_txn_reference_number in offender_transactions.txn_reference_number%type,
                              p_txn_entry_date       in offender_transactions.txn_entry_date%type,
                              p_txn_entry_desc       in offender_transactions.txn_entry_desc%type,
                              p_txn_entry_amount     in offender_transactions.txn_entry_amount%type,
                              p_client_unique_ref     in offender_transactions.client_unique_ref%type default null,
                              p_txn_id              out offender_transactions.txn_id%type,
                              p_txn_entry_seq       out offender_transactions.txn_entry_seq%type);

   procedure post_transfer(p_noms_id              in offenders.offender_id_display%type default null,
                              p_root_offender_id     in offenders.root_offender_id%type default null,
                              p_single_offender_id   in varchar2 default null,
                              p_from_agy_loc_id      in agency_locations.agy_loc_id%type default null,
                              p_txn_type             in offender_transactions.txn_type%type,
                              p_txn_reference_number in offender_transactions.txn_reference_number%type,
                              p_txn_entry_date       in offender_transactions.txn_entry_date%type,
                              p_txn_entry_desc       in offender_transactions.txn_entry_desc%type,
                              p_txn_entry_amount     in offender_transactions.txn_entry_amount%type,
                              p_client_unique_ref    in offender_transactions.client_unique_ref%type default null,
                              p_current_agy_loc_id  out agency_locations.agy_loc_id%type,
                              p_current_agy_desc    out agency_locations.description%type,
                              p_txn_id              out offender_transactions.txn_id%type,
                              p_txn_entry_seq       out offender_transactions.txn_entry_seq%type);

   procedure post_hold(p_noms_id              in offenders.offender_id_display%type default null,
                       p_root_offender_id     in offenders.root_offender_id%type default null,
                       p_single_offender_id   in varchar2 default null,
                       p_agy_loc_id           in agency_locations.agy_loc_id%type,
                       p_txn_reference_number in offender_transactions.txn_reference_number%type,
                       p_txn_entry_date       in offender_transactions.txn_entry_date%type,
                       p_txn_entry_desc       in offender_transactions.txn_entry_desc%type,
                       p_txn_entry_amount     in offender_transactions.txn_entry_amount%type,
                       p_hold_until_date      in offender_transactions.hold_until_date%type default null,
                       p_client_unique_ref    in offender_transactions.client_unique_ref%type default null,
                       p_txn_id              out offender_transactions.txn_id%type,
                       p_txn_entry_seq       out offender_transactions.txn_entry_seq%type,
                       p_hold_number         out offender_transactions.hold_number%type);

   procedure release_hold(p_noms_id              in offenders.offender_id_display%type default null,
                          p_root_offender_id     in offenders.root_offender_id%type default null,
                          p_single_offender_id   in varchar2 default null,
                          p_agy_loc_id           in agency_locations.agy_loc_id%type,
                          p_txn_entry_desc       in offender_transactions.txn_entry_desc%type default null,
                          p_hold_number          in offender_transactions.hold_number%type default null);

   procedure holds(p_noms_id              in offenders.offender_id_display%type default null,
                   p_root_offender_id     in offenders.root_offender_id%type default null,
                   p_single_offender_id   in varchar2 default null,
                   p_agy_loc_id           in agency_locations.agy_loc_id%type,
                   p_client_unique_ref    in offender_visits.client_unique_ref%type default null,
                   p_holds_csr           out sys_refcursor);
   
   procedure store_payment(p_noms_id              in offenders.offender_id_display%type default null,
                           p_root_offender_id     in offenders.root_offender_id%type default null,
                           p_single_offender_id   in varchar2 default null,
                           p_agy_loc_id           in agency_locations.agy_loc_id%type default null,
                           p_txn_type             in offender_transactions.txn_type%type,
                           p_txn_reference_number in offender_transactions.txn_reference_number%type,
                           p_txn_entry_date       in offender_transactions.txn_entry_date%type,
                           p_txn_entry_desc       in offender_transactions.txn_entry_desc%type,
                           p_txn_entry_amount     in offender_transactions.txn_entry_amount%type);

   procedure create_transaction(p_root_offender_id     in offender_transactions.offender_id%type,
                                p_offender_book_id     in offender_transactions.offender_book_id%type,
                                p_caseload_id          in offender_transactions.caseload_id%type,
                                p_txn_type             in offender_transactions.txn_type%type,
                                p_txn_reference_number in offender_transactions.txn_reference_number%type,
                                p_txn_entry_date       in offender_transactions.txn_entry_date%type,
                                p_txn_entry_desc       in offender_transactions.txn_entry_desc%type,
                                p_txn_entry_amount     in offender_transactions.txn_entry_amount%type,
                                p_client_unique_ref     in offender_transactions.client_unique_ref%type default null,
                                p_receipt_only         in boolean default false,
                                p_disbursement_only    in boolean default false,
                                p_txn_id              out offender_transactions.txn_id%type,
                                p_txn_entry_seq       out offender_transactions.txn_entry_seq%type);


   procedure stored_payment_failed(
                  p_api_stored_payment_id in api_stored_payments.api_stored_payment_id%type,
                  p_err_msg               in varchar2);

   procedure stored_payments(p_agy_loc_id  in agency_locations.agy_loc_id%type,
                             p_from_date   in date,
                             p_to_date     in date default null,
                             p_failed_only in boolean default false,
                             p_payment_csr out sys_refcursor);

end api_finance_procs;
/
show err
create or replace package body api_owner.api_finance_procs
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.11   23-Apr-2018';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      ------------------------------------------------------------------------------------------
      Person      Date           Version                Comments
      ---------   -----------    ---------   ---------------------------------------------------
      Paul M      23-Apr-2018     1.11       SDU-134 - Raise -20011 from account_balances if cash bal is null
      Paul M      18-Apr-2018     1.10       SDU-124 - Replace call to otdrdtfu.get_txn_usage with
                                             select statement using module of NOMISAPI instead of
                                             OTDRDTFU  
      Paul M      07-Sep-2017     1.9        Allow for crediting a prisoner at a prison they are no longer at 
      Paul M      23-Jun-2017     1.8        Correct issue with remapping of error messages
      Paul M      14-Jun-2017     1.7        Add procedures to hold and release money
      Paul M      27-Mar-2017     1.6        Add client_unique_ref to transaction_history
      Paul M      15-Mar-2017     1.5        Populate offender_transactions.client_unique_ref
                                             for transactions from MTP
      Paul M      10-Mar-2017     1.4        Remove digital prison check on post_transaction for MTP
      Paul M      09-Dec-2017     1.3        Remap user exceptions raised from finance package
      Paul M      03-Nov-2016     1.2        QC#20413 - Remove balance from transaction_history
      Paul M      03-Nov-2016     1.1        Moved get_offender_ids to core_utils package
      Paul M      19-Aug-2016     1.0        Initial version

      This package raises the following user defined exceptions

         -20009: Insufficient funds
         -20010: Not a Digital Prison
         -20011: Offender Has No Trust Account at Prison
         -20013: Unexpected error, return sqlerrm
         -20017: Offender not in specified prison
         -20018: Invalid transaction type
         -20020: Sub account does not exist
         -20021: There is no balance available in the Sub account
         -20022: Hold Amount Cannot exceed the Balance Amount
         -20023: Hold date should be greater than current date
         -20036: Offender still in specified prison
         -20037: Offender never at prison
         -20038: Offender being transferred
         -20039: Financial setup error
         -20040: Sum of sub account balances not equal to current balance
         -20041: Sub account has negative balance
   */

   gk_module_name            constant varchar2(12) := 'NOMISAPI';
   gk_inst_caseload_type     constant varchar2(12) := 'INST';

   resource_busy                  exception;
   pragma exception_init (resource_busy, -54);

   -- Forward declarations
   
   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;

   procedure account_balances(p_noms_id              in offenders.offender_id_display%type default null,
                              p_root_offender_id     in offenders.root_offender_id%type default null,
                              p_single_offender_id   in varchar2 default null,
                              p_agy_loc_id           in agency_locations.agy_loc_id%type default null,
                              p_cash_balance     out offender_sub_accounts.balance%type,
                              p_spends_balance   out offender_sub_accounts.balance%type,
                              p_savings_balance  out offender_sub_accounts.balance%type)
   is
      v_noms_id                offenders.offender_id_display%type;
      v_root_offender_id       offenders.offender_id%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;
   begin
      -- 
      -- validate offender identification criteria.
      -- Until the implementation of the siingle offender_id, only
      -- the Noms Id and/or root_offender_id will be populated, with
      -- the root_offender_id taking precedence 
      --
      v_root_offender_id       := p_root_offender_id;
      v_noms_id                := p_noms_id;
      v_agy_loc_id             := p_agy_loc_id;

      core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                       p_noms_id          => v_noms_id,
                       p_agy_loc_id       => v_agy_loc_id,
                       p_offender_book_id => v_offender_book_id);

      select max(cash) cash_balance, max(spends) spends_balance, max(savings) savings_balance
        into p_cash_balance, p_spends_balance, p_savings_balance
        from ( select case when ac.sub_account_type = 'REG' then balance else null end   cash ,
                      case when ac.sub_account_type = 'SPND' then balance else null end   spends ,
                      case when ac.sub_account_type = 'SAV' then balance else null end   savings 
                 from offender_bookings ob
                 join offender_sub_accounts osa
                   on osa.offender_id = ob.root_offender_id
                 join account_codes ac
                   on ac.account_code = osa.trust_account_code 
                where osa.caseload_id = v_agy_loc_id
                  and ob.offender_book_id = v_offender_book_id
                  and ac.sub_account_type in ('REG','SPND','SAV'));
      -- 
      -- If the cash balance is null then the offender has no trust account for the specified prison
      -- 
      if p_cash_balance is null then
         raise_application_error(-20011,'Offender Has No Trust Account at Prison');
      end if;
   end account_balances;

   procedure transaction_history(p_noms_id              in offenders.offender_id_display%type default null,
                                 p_root_offender_id     in offenders.root_offender_id%type default null,
                                 p_single_offender_id   in varchar2 default null,
                                 p_agy_loc_id           in agency_locations.agy_loc_id%type default null,
                                 p_account_type         in account_codes.sub_account_type%type default null,
                                 p_from_date            in date default null,
                                 p_to_date              in date default null,
                                 p_client_unique_ref    in offender_transactions.client_unique_ref%type default null,
                                 p_trans_csr           out sys_refcursor)
   is
      v_noms_id                offenders.offender_id_display%type;
      v_root_offender_id       offenders.offender_id%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;
   begin

      -- 
      -- validate offender identification criteria.
      -- Until the implementation of the siingle offender_id, only
      -- the Noms Id and/or root_offender_id will be populated, with
      -- the root_offender_id taking precedence 
      --
      v_root_offender_id       := p_root_offender_id;
      v_noms_id                := p_noms_id;
      v_agy_loc_id            := p_agy_loc_id;

      core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                       p_noms_id          => v_noms_id,
                       p_agy_loc_id       => v_agy_loc_id,
                       p_offender_book_id => v_offender_book_id);

      if p_client_unique_ref is not null then

         open p_trans_csr for 
              select ot.txn_id txn_id,
                     ot.txn_entry_seq txn_entry_seq,
                     ot.txn_entry_date txn_entry_date,
                     ot.txn_type txn_type,
                     tt.description txn_type_desc,
                     ot.txn_entry_desc txn_entry_desc,
                     ot.txn_reference_number txn_reference_number,
                     ot.txn_entry_amount * decode(txn_posting_type,'DR',-1,1) txn_entry_amount 
                from offender_transactions ot
                left join transaction_types tt
                  on tt.txn_type = ot.txn_type 
               where ot.offender_id = v_root_offender_id
                 and ot.caseload_id = p_agy_loc_id -- always use the parameter
                 and ot.client_unique_ref = p_client_unique_ref;
      else
         open p_trans_csr for 
              select ot.txn_id txn_id,
                     ot.txn_entry_seq txn_entry_seq,
                     ot.txn_entry_date txn_entry_date,
                     ot.txn_type txn_type,
                     tt.description txn_type_desc,
                     ot.txn_entry_desc txn_entry_desc,
                     ot.txn_reference_number txn_reference_number,
                     ot.txn_entry_amount * decode(txn_posting_type,'DR',-1,1) txn_entry_amount 
                from offender_transactions ot
                left join transaction_types tt
                  on tt.txn_type = ot.txn_type 
               where ot.offender_id = v_root_offender_id
                 and ot.caseload_id = v_agy_loc_id
                 and ot.sub_account_type = p_account_type
                 and ot.txn_entry_date >= p_from_date
                 and (p_to_date is null or ot.txn_entry_date <= p_to_date)
               order by ot.txn_entry_date desc, ot.txn_id desc, ot.txn_entry_seq desc;    
      end if;
   end transaction_history;

   /*
    * this procedure is based on code from the form OTDRDTFU
    */
   procedure post_transaction(p_noms_id              in offenders.offender_id_display%type default null,
                              p_root_offender_id     in offenders.root_offender_id%type default null,
                              p_single_offender_id   in varchar2 default null,
                              p_agy_loc_id           in agency_locations.agy_loc_id%type default null,
                              p_txn_type             in offender_transactions.txn_type%type,
                              p_txn_reference_number in offender_transactions.txn_reference_number%type,
                              p_txn_entry_date       in offender_transactions.txn_entry_date%type,
                              p_txn_entry_desc       in offender_transactions.txn_entry_desc%type,
                              p_txn_entry_amount     in offender_transactions.txn_entry_amount%type,
                              p_client_unique_ref     in offender_transactions.client_unique_ref%type default null,
                              p_txn_id              out offender_transactions.txn_id%type,
                              p_txn_entry_seq       out offender_transactions.txn_entry_seq%type)
   is

      v_noms_id                offenders.offender_id_display%type;
      v_root_offender_id       offenders.offender_id%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;
      v_posting_type           offender_transactions.txn_posting_type%type;
      v_sub_act_type           offender_transactions.sub_account_type%type;
      v_txn_entry_amount       offender_transactions.txn_entry_amount%type;
      v_check_ind              varchar2(1);
      v_cheque_prod_flag       transaction_operations.cheque_production_flag%type;
      v_receipt_prod_flag      transaction_operations.receipt_production_flag%type;
      v_deduction_flag         offender_transactions.deduction_flag%type;
      v_gl_entry_seq           gl_transactions.gl_entry_seq%type;
   begin
      nomis_context.set_context('AUDIT_MODULE_NAME','API_FINANCE_PROCS.POST_TRANSACTION');
    
      -- 
      -- validate offender identification criteria.
      -- Until the implementation of the single offender_id, only
      -- the Noms Id and/or root_offender_id will be populated, with
      -- the root_offender_id taking precedence 
      --
      v_root_offender_id       := p_root_offender_id;
      v_noms_id                := p_noms_id;

      core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                       p_noms_id          => v_noms_id,
                       p_agy_loc_id       => v_agy_loc_id,
                       p_offender_book_id => v_offender_book_id);

      --
      -- Check whether the prison the offender is currently in is the
      -- same as the one specified
      --
      if (v_agy_loc_id != p_agy_loc_id) then 
            raise_application_error(-20017,'Offender not in specified prison');
      end if;

      create_transaction(p_root_offender_id     => v_root_offender_id,
                         p_offender_book_id     => v_offender_book_id,
                         p_caseload_id          => v_agy_loc_id,
                         p_txn_type             => p_txn_type,
                         p_txn_reference_number => p_txn_reference_number,
                         p_txn_entry_date       => p_txn_entry_date,
                         p_txn_entry_desc       => p_txn_entry_desc,
                         p_txn_entry_amount     => p_txn_entry_amount,
                         p_client_unique_ref     => p_client_unique_ref,
                         p_txn_id               => p_txn_id,
                         p_txn_entry_seq        => p_txn_entry_seq);       
   end post_transaction;

   /*
    * This procedure is based on code from the forms:
    * OTDRDTFU
    * OTDTTAC
    * OTDRTTFU
    * OTDCLOSE
    * OTDCLINA
    *
    */
   procedure post_transfer(p_noms_id              in offenders.offender_id_display%type default null,
                           p_root_offender_id     in offenders.root_offender_id%type default null,
                           p_single_offender_id   in varchar2 default null,
                           p_from_agy_loc_id      in agency_locations.agy_loc_id%type default null,
                           p_txn_type             in offender_transactions.txn_type%type,
                           p_txn_reference_number in offender_transactions.txn_reference_number%type,
                           p_txn_entry_date       in offender_transactions.txn_entry_date%type,
                           p_txn_entry_desc       in offender_transactions.txn_entry_desc%type,
                           p_txn_entry_amount     in offender_transactions.txn_entry_amount%type,
                           p_client_unique_ref    in offender_transactions.client_unique_ref%type default null,
                           p_current_agy_loc_id  out agency_locations.agy_loc_id%type,
                           p_current_agy_desc    out agency_locations.description%type,
                           p_txn_id              out offender_transactions.txn_id%type,
                           p_txn_entry_seq       out offender_transactions.txn_entry_seq%type)
   is

      v_account_closed         varchar2(1);
      v_noms_id                offenders.offender_id_display%type;
      v_root_offender_id       offenders.offender_id%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_active_flag            offender_bookings.active_flag%type;
      v_txn_type               offender_transactions.txn_type%type;
      v_sub_account_type       offender_transactions.sub_account_type%type;
      v_to_agy_loc_id          offender_bookings.agy_loc_id%type;
      v_to_agy_desc            agency_locations.description%type;
      v_txn_posting_type       offender_transactions.txn_posting_type%type;
      v_sub_act_type           offender_transactions.sub_account_type%type;
      v_txn_entry_amount       offender_transactions.txn_entry_amount%type;
      v_check_ind              varchar2(1);
      v_cheque_prod_flag       transaction_operations.cheque_production_flag%type;
      v_receipt_prod_flag      transaction_operations.receipt_production_flag%type;
      v_deduction_flag         offender_transactions.deduction_flag%type;
      v_gl_entry_seq           gl_transactions.gl_entry_seq%type;
      v_current_balance        offender_trust_accounts.current_balance%type; 
      v_sub_account_total      offender_sub_accounts.balance%type; 
      v_min_balance            offender_sub_accounts.balance%type;
   begin
      nomis_context.set_context('AUDIT_MODULE_NAME','API_FINANCE_PROCS.POST_TRANSFER');
    
      -- 
      -- validate offender identification criteria.
      -- Until the implementation of the single offender_id, only
      -- the Noms Id and/or root_offender_id will be populated, with
      -- the root_offender_id taking precedence 
      --
      v_root_offender_id       := p_root_offender_id;
      v_noms_id                := p_noms_id;

      -- get offender and booking details

      begin
         if p_root_offender_id is not null then 

            select o.offender_id_display, ob.offender_book_id, ob.agy_loc_id, ob.active_flag, al.description
              into v_noms_id, v_offender_book_id, v_to_agy_loc_id, v_active_flag, v_to_agy_desc
              from offenders o
              join offender_bookings ob
                on ob.offender_id = o.offender_id
                   and ob.booking_seq = 1
              join agency_locations al
                on al.agy_loc_id = ob.agy_loc_id
             where o.root_offender_id = p_root_offender_id;

            if p_noms_id is not null and upper(p_noms_id) != v_noms_id then
               raise_application_error(-20002,'Offender Identifier inconsistancy');
            end if;

         elsif p_noms_id is not null then 

            select ob.root_offender_id, ob.offender_book_id, ob.agy_loc_id, ob.active_flag, al.description
              into v_root_offender_id, v_offender_book_id, v_to_agy_loc_id, v_active_flag, v_to_agy_desc
              from offenders o
              join offender_bookings ob
                on ob.offender_id = o.offender_id
                   and ob.booking_seq = 1
              join agency_locations al
                on al.agy_loc_id = ob.agy_loc_id
             where o.offender_id_display = upper(p_noms_id);

            if p_root_offender_id is not null and p_root_offender_id != v_root_offender_id then
               raise_application_error(-20002,'Offender Identifier inconsistancy');
            end if;
         else
            raise_application_error(-20003,'No Offender Identifier provided');
         end if;
      exception
         when no_data_found then
            raise_application_error(-20001,'Offender Not Found');
      end;

      if v_to_agy_loc_id = 'TRN' then
         raise_application_error(-20038,'Offender being transferred');
      end if;
         
      --
      -- Check whether the prison the offender is currently in is the
      -- same as the one specified
      --
      if (v_to_agy_loc_id = p_from_agy_loc_id) then 
         raise_application_error(-20036,'Offender still in specified prison');
      end if;


      -- Check whether the offender has ever been at the specified prison
      -- i.e. has ever had a trust account there.
      -- and wether the account is open or closed
      -- Code derived from trust.chk_account_status

      begin
         select account_closed_flag
           into v_account_closed
           from offender_trust_accounts
          where offender_id = v_root_offender_id 
            and caseload_id = p_from_agy_loc_id
            for update of account_closed_flag, current_balance;
      exception
         when no_data_found then
            raise_application_error(-20037,'Offender never at prison');
         when resource_busy then 
            raise_application_error (-20006, 'Resource is locked');
      end;

      if v_account_closed = 'Y' then

         -- re-open the trust account at the specified prison

         api_finance_utils.reopen_trust_account(p_caseload_id      => p_from_agy_loc_id,
                                                p_offender_id      => v_root_offender_id,
                                                p_offender_book_id => v_offender_book_id,
                                                p_deduction_flag   => v_deduction_flag);
      end if; 

      -- Post transaction to specified historic prison

      create_transaction(p_root_offender_id     => v_root_offender_id,
                         p_offender_book_id     => v_offender_book_id,
                         p_caseload_id          => p_from_agy_loc_id,
                         p_txn_type             => p_txn_type,
                         p_txn_reference_number => p_txn_reference_number,
                         p_txn_entry_date       => p_txn_entry_date,
                         p_txn_entry_desc       => p_txn_entry_desc,
                         p_txn_entry_amount     => p_txn_entry_amount,
                         p_client_unique_ref    => p_client_unique_ref,
                         p_receipt_only         => true,
                         p_txn_id               => p_txn_id,
                         p_txn_entry_seq        => p_txn_entry_seq);

      if v_to_agy_loc_id != 'OUT' then

         -- Transfer Trust Funds
         -- 
         trust_auto_transfer.transfer_account(p_offender_id        => v_root_offender_id,
                                              p_off_book_id        => v_offender_book_id,
                                              p_send_caseload_id   => p_from_agy_loc_id,
                                              p_txn_date           => trunc(sysdate),
                                              p_receive_csld_id    => v_to_agy_loc_id);

         deductions.get_ac_and_set_ind_date(p_off_id  => v_root_offender_id,
                                            p_csld_id => p_from_agy_loc_id);
         

         if  v_account_closed = 'Y' then
         
            -- If the account was previously closed then close it again
            api_finance_utils.close_account(p_caseload_id => p_from_agy_loc_id,
                                            p_offender_id  => v_root_offender_id);

         end if;

      elsif v_active_flag = 'N' then
         
         -- Transfer funds to NACRO
         -- Based on form OTDCLINA
         --
         begin
            select ota.current_balance,sum(osa.balance), min(osa.balance)
              into v_current_balance, v_sub_account_total, v_min_balance
              from offender_trust_accounts ota
              join offender_sub_accounts osa
                on osa.caseload_id = ota.caseload_id
                   and osa.offender_id = ota.offender_id
             where ota.offender_id = v_root_offender_id
               and ota.caseload_id = p_from_agy_loc_id
               and osa.trust_account_code in (select dr_account_code
                                                from transaction_operations
                                               where caseload_id = p_from_agy_loc_id
                                                 and module_name = 'OTDCLINA')
             group by ota.current_balance;
         exception
            when no_data_found then
               raise_application_error(-20011,'Offender Has No Trust Account at Prison');
         end;      

         -- Check if the sum of balances in offender_sub_accounts tally with
         -- Current_Balance in Offender_Trust_Accounts.

         if  v_current_balance != v_sub_account_total then
            raise_application_error(-20040,'Sum of sub account balances not equal to current balance');
         end if;

         -- Check if any sub account has a negative balance

         if v_min_balance < 0 then
            raise_application_error(-20041,'Sub account has negative balance');
         end if;

         -- Transfer funds procedure involves following steps :
         -- Transferring funds to REG sub a/c from other non-zero balance Offender sub accounts
         -- which are setup in Transaction Operation Screen. 
         -- Close the Offenders_Trust_Account.    
         -- Transferring funds from REG sub a/c to Welfare a/c.    

         api_finance_utils.transfer_funds(p_caseload_id      => p_from_agy_loc_id,
                                          p_offender_id      => v_root_offender_id,
                                          p_offender_book_id => v_offender_book_id);


      end if;

      p_current_agy_loc_id   := v_to_agy_loc_id;
      p_current_agy_desc     := v_to_agy_desc;
   end post_transfer;

   /*
    * this procedure is based on code from the form OTDHOLDT
    */
   procedure post_hold(p_noms_id              in offenders.offender_id_display%type default null,
                       p_root_offender_id     in offenders.root_offender_id%type default null,
                       p_single_offender_id   in varchar2 default null,
                       p_agy_loc_id           in agency_locations.agy_loc_id%type,
                       p_txn_reference_number in offender_transactions.txn_reference_number%type,
                       p_txn_entry_date       in offender_transactions.txn_entry_date%type,
                       p_txn_entry_desc       in offender_transactions.txn_entry_desc%type,
                       p_txn_entry_amount     in offender_transactions.txn_entry_amount%type,
                       p_hold_until_date      in offender_transactions.hold_until_date%type default null,
                       p_client_unique_ref    in offender_transactions.client_unique_ref%type default null,
                       p_txn_id              out offender_transactions.txn_id%type,
                       p_txn_entry_seq       out offender_transactions.txn_entry_seq%type,
                       p_hold_number         out offender_transactions.hold_number%type)
   is
      k_hold_txn_type          constant varchar2(3) := 'HOA';

      v_noms_id                offenders.offender_id_display%type;
      v_root_offender_id       offenders.offender_id%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;
      v_posting_type           offender_transactions.txn_posting_type%type;
      v_sub_act_type           offender_transactions.sub_account_type%type;
      v_txn_entry_amount       offender_transactions.txn_entry_amount%type;
      v_txn_entry_desc         offender_transactions.txn_entry_desc%type;
      v_check_ind              varchar2(1);
      v_cheque_prod_flag       transaction_operations.cheque_production_flag%type;
      v_receipt_prod_flag      transaction_operations.receipt_production_flag%type;
      v_deduction_flag         offender_transactions.deduction_flag%type;
      v_gl_entry_seq           gl_transactions.gl_entry_seq%type;
      v_balance                offender_sub_accounts.balance%type;
      v_hold_balance           offender_sub_accounts.hold_balance%type;
      v_txn_adjusted_flag      offender_transactions.txn_adjusted_flag%type;
      v_hold_clear_flag        offender_transactions.hold_clear_flag%type;
      v_trust_account_code     offender_sub_accounts.trust_account_code%type;
      v_sqlcode                number;
      v_sqlerrm                varchar2(512);
   begin
      nomis_context.set_context('AUDIT_MODULE_NAME','API_FINANCE_PROCS.POST_HOLD');

      p_txn_id        := txn_id.nextval;
      p_txn_entry_seq := 1;
      p_hold_number   := txn_id.nextval;

      v_txn_adjusted_flag  := 'N';
      v_hold_clear_flag    := 'N';
      v_posting_type       := 'DR';
    
      -- 
      -- validate offender identification criteria.
      -- Until the implementation of the single offender_id, only
      -- the Noms Id and/or root_offender_id will be populated, with
      -- the root_offender_id taking precedence 
      --
      v_root_offender_id       := p_root_offender_id;
      v_noms_id                := p_noms_id;

      core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                       p_noms_id          => v_noms_id,
                       p_agy_loc_id       => v_agy_loc_id,
                       p_offender_book_id => v_offender_book_id);

      --
      -- Check whether the prison the offender is currently in is the
      -- same as the one specified
      --
      if (v_agy_loc_id != p_agy_loc_id) then 
            raise_application_error(-20017,'Offender not in specified prison');
      end if;

      -- Get sub acount details and balances
      begin
         select nvl(balance, 0),
                nvl(hold_balance, 0),
                ac.sub_account_type,
                osa.trust_account_code
           into v_balance,
                v_hold_balance,
                v_sub_act_type,
                v_trust_account_code
           from offender_sub_accounts osa
           join transaction_operations tro
             on tro.caseload_id = osa.caseload_id
                and tro.dr_account_code = osa.trust_account_code
                and tro.module_name = gk_module_name
                and tro.txn_type = k_hold_txn_type
           join account_codes ac
             on ac.account_code = tro.dr_account_code
          where osa.caseload_id = p_agy_loc_id
            and osa.offender_id = v_root_offender_id;
         --
         -- Check there is sufficient money in the account
         --
         if v_balance <= 0 then
            raise_application_error(-20021,'There is no balance available in the Sub account');
         end if;
         if p_txn_entry_amount > v_balance  then
            raise_application_error(-20022,'Hold Amount Cannot exceed the Balance Amount');
         end if;
      exception
         when no_data_found then
            raise_application_error(-20020,'Sub account does not exist');
      end;

      if p_hold_until_date is not null and p_hold_until_date <= trunc(sysdate) then
         raise_application_error(-20023,'Hold date should be greater than current date');
      end if;

      if p_txn_entry_desc is null then
         v_txn_entry_desc := 'HOLD';
      else
         v_txn_entry_desc := p_txn_entry_desc;
      end if;

      api_finance_utils.insert_offender_transaction (p_txn_id                => p_txn_id,
                                   p_txn_entry_seq         => p_txn_entry_seq,
                                   p_caseload_id           => v_agy_loc_id,
                                   p_offender_id           => v_root_offender_id,
                                   p_offender_book_id      => v_offender_book_id,
                                   p_posting_type          => v_posting_type,
                                   p_txn_type              => k_hold_txn_type,
                                   p_txn_entry_desc        => v_txn_entry_desc,
                                   p_txn_entry_amount      => p_txn_entry_amount,
                                   p_txn_entry_date        => p_txn_entry_date,
                                   p_sub_act_type          => v_sub_act_type,
                                   p_txn_reference_number  => p_txn_reference_number,
                                   p_receipt_number        => null,
                                   p_deduction_flag        => 'N',
                                   p_remitter_name         => null,
                                   p_remitter_id           => null,
                                   p_hold_number           => p_hold_number,
                                   p_slip_printed_flag     => 'N',
                                   p_txn_adjusted_flag     => v_txn_adjusted_flag,
                                   p_hold_clear_flag       => v_hold_clear_flag,
                                   p_hold_until_date       => p_hold_until_date,
                                   p_client_unique_ref     => p_client_unique_ref);
      begin 
         trust.update_offender_balance (p_csld_id          => v_agy_loc_id,
                                        p_off_id           => v_root_offender_id,
                                        p_trans_post_type  => v_posting_type,
                                        p_trans_date       => p_txn_entry_date,
                                        p_trans_number     => p_txn_id,
                                        p_trans_type       => k_hold_txn_type,
                                        p_trans_amount     => p_txn_entry_amount,
                                        p_sub_act_type     => v_sub_act_type,
                                        p_allow_overdrawn  => 'N');

         trust.process_gl_trans_new (p_csld_id          => v_agy_loc_id,
                                     p_trans_type       => k_hold_txn_type,
                                     p_operation_type   => null,
                                     p_trans_amount     => p_txn_entry_amount,
                                     p_trans_number     => p_txn_id,
                                     p_trans_date       => p_txn_entry_date,
                                     p_trans_desc       => v_txn_entry_desc,
                                     p_trans_seq        => p_txn_entry_seq,
                                     p_module_name      => gk_module_name,
                                     p_off_id           => v_root_offender_id,
                                     p_off_book_id      => v_offender_book_id,
                                     p_sub_act_type_dr  => v_sub_act_type, 
                                     p_sub_act_type_cr  => null,
                                     p_payee_pers_id    => null,
                                     p_payee_corp_id    => null,
                                     p_payee_name_text  => null,
                                     p_gl_sqnc          => v_gl_entry_seq,
                                     p_off_ded_id       => null);
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
                  nomis_api_log.error('API_FINANCE_PROCS.CREATE_TRANSACTION',v_sqlerrm);
                  raise_application_error (-20013, v_sqlerrm);
               else
                  raise;
            end case;
       end;
         

      update offender_sub_accounts
         set hold_balance = nvl ( hold_balance, 0 ) + p_txn_entry_amount
       where offender_id = v_root_offender_id
         and caseload_id = v_agy_loc_id
         and trust_account_code = v_trust_account_code;

      update offender_trust_accounts
         set hold_balance = nvl ( hold_balance, 0 ) + p_txn_entry_amount
       where offender_id = v_root_offender_id
         and caseload_id = v_agy_loc_id;

   exception
      when others then 
         v_sqlcode := sqlcode;
         v_sqlerrm := sqlerrm;
         --
         if v_sqlcode = -1 and instr(v_sqlerrm,'OMS_OWNER.OFFENDER_TRANSACTIONS_UK1') > 0 then
            raise_application_error (-20019, 'Duplicate post');
         else
            raise;
         end if;
   end post_hold;

   procedure release_hold(p_noms_id              in offenders.offender_id_display%type default null,
                          p_root_offender_id     in offenders.root_offender_id%type default null,
                          p_single_offender_id   in varchar2 default null,
                          p_agy_loc_id           in agency_locations.agy_loc_id%type,
                          p_txn_entry_desc       in offender_transactions.txn_entry_desc%type default null,
                          p_hold_number          in offender_transactions.hold_number%type)
   is
      k_hold_txn_type          constant varchar2(3) := 'HOA';
      k_rel_hold_txn_type      constant varchar2(3) := 'HOR';

      v_noms_id                   offenders.offender_id_display%type;
      v_root_offender_id          offenders.offender_id%type;
      v_offender_book_id          offender_bookings.offender_book_id%type;
      v_agy_loc_id                offender_bookings.agy_loc_id%type;
      v_txn_date                  offender_transactions.txn_entry_date%type;
      v_txn_id                    offender_transactions.txn_id%type;
      v_txn_entry_seq             offender_transactions.txn_entry_seq%type;
      v_txn_desc                  offender_transactions.txn_entry_desc%type;
      v_hold_txn_reference_number offender_transactions.txn_reference_number%type;
      v_hold_txn_entry_amount     offender_transactions.txn_entry_amount%type;
      v_hold_sub_account_type     offender_transactions.sub_account_type%type;
      v_hold_clear_flag           offender_transactions.hold_clear_flag%type;
      v_hold_rowid                urowid;
      v_txn_adjusted_flag         offender_transactions.txn_adjusted_flag%type;
      v_txn_posting_type          offender_transactions.txn_posting_type%type;
      v_trust_account_code     offender_sub_accounts.trust_account_code%type;
      v_sqlcode                number;
      v_sqlerrm                varchar2(512);
   begin
      nomis_context.set_context('AUDIT_MODULE_NAME','API_FINANCE_PROCS.RELEASE_HOLD');

      -- 
      -- validate offender identification criteria.
      -- Until the implementation of the single offender_id, only
      -- the Noms Id and/or root_offender_id will be populated, with
      -- the root_offender_id taking precedence 
      --
      v_root_offender_id       := p_root_offender_id;
      v_noms_id                := p_noms_id;

      core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                       p_noms_id          => v_noms_id,
                       p_agy_loc_id       => v_agy_loc_id,
                       p_offender_book_id => v_offender_book_id);

      --
      -- Check whether the prison the offender is currently in is the
      -- same as the one specified
      --
      if (v_agy_loc_id != p_agy_loc_id) then 
            raise_application_error(-20017,'Offender not in specified prison');
      end if;

      --
      -- Get hold transaction and lock for update
      --
      begin
         select ot.rowid,
                ot.txn_reference_number,
                ot.txn_entry_amount,
                ot.sub_account_type
           into v_hold_rowid,
                v_hold_txn_reference_number,
                v_hold_txn_entry_amount,
                v_hold_sub_account_type
           from offender_transactions ot
          where ot.offender_id = v_root_offender_id
            and ot.caseload_id = p_agy_loc_id -- always use the parameter
            and ot.txn_type = k_hold_txn_type
            and ot.hold_clear_flag = 'N'
            and ot.hold_number = p_hold_number
            for update of ot.hold_clear_flag nowait;
      exception
         when no_data_found then
            raise_application_error(-20024,'Hold transaction not found');
         when resource_busy then 
            raise_application_error (-20006, 'Resource is locked');
      end;
 
      select tro.cr_account_code
        into v_trust_account_code
        from transaction_operations tro
       where tro.module_name = gk_module_name
         and tro.caseload_id = p_agy_loc_id
         and tro.txn_type = k_rel_hold_txn_type;

      v_txn_date := trunc (sysdate);
      v_txn_entry_seq := 1;
      if p_txn_entry_desc is null then
         v_txn_desc := 'Remove Hold';
      else 
         v_txn_desc := p_txn_entry_desc;
      end if;
      v_txn_adjusted_flag := 'N';
      v_hold_clear_flag := 'Y';                  -- Hold Clear Flag is set to YES
      v_txn_posting_type := 'CR';
      v_txn_id := txn_id.nextval;

      api_finance_utils.insert_offender_transaction (p_txn_id                => v_txn_id,
                                   p_txn_entry_seq         => v_txn_entry_seq,
                                   p_caseload_id           => p_agy_loc_id,
                                   p_offender_id           => v_root_offender_id,
                                   p_offender_book_id      => v_offender_book_id,
                                   p_posting_type          => v_txn_posting_type,
                                   p_txn_type              => k_rel_hold_txn_type,
                                   p_txn_entry_desc        => v_txn_desc,
                                   p_txn_entry_amount      => v_hold_txn_entry_amount,
                                   p_txn_entry_date        => v_txn_date,
                                   p_sub_act_type          => v_hold_sub_account_type,
                                   p_txn_reference_number  => v_hold_txn_reference_number,
                                   p_receipt_number        => null,
                                   p_deduction_flag        => 'N',
                                   p_remitter_name         => null,
                                   p_remitter_id           => null,
                                   p_hold_number           => null,
                                   p_slip_printed_flag     => 'N',
                                   p_txn_adjusted_flag     => v_txn_adjusted_flag,
                                   p_hold_clear_flag       => v_hold_clear_flag,
                                   p_hold_until_date       => null,
                                   p_client_unique_ref     => null);
      begin
         trust.update_offender_balance (p_csld_id          => p_agy_loc_id,
                                        p_off_id           => v_root_offender_id,
                                        p_trans_post_type  => v_txn_posting_type,
                                        p_trans_date       => v_txn_date,
                                        p_trans_number     => v_txn_id,
                                        p_trans_type       => k_rel_hold_txn_type,
                                        p_trans_amount     => v_hold_txn_entry_amount,
                                        p_sub_act_type     => v_hold_sub_account_type,
                                        p_allow_overdrawn  => 'N');
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
                  nomis_api_log.error('API_FINANCE_PROCS.CREATE_TRANSACTION',v_sqlerrm);
                  raise_application_error (-20013, v_sqlerrm);
               else
                  raise;
            end case;
      end;

      -- 
      -- Update the original hold transaction to clear the hold
      --
      update offender_transactions 
         set hold_clear_flag = v_hold_clear_flag
       where rowid = v_hold_rowid;

      update offender_sub_accounts
         set hold_balance = hold_balance - v_hold_txn_entry_amount
       where offender_id = v_root_offender_id
         and caseload_id = p_agy_loc_id
         and trust_account_code = v_trust_account_code;

      update offender_trust_accounts
         set hold_balance = hold_balance - v_hold_txn_entry_amount
       where offender_id = v_root_offender_id
         and caseload_id = p_agy_loc_id;
   end release_hold;
      

   procedure holds(p_noms_id              in offenders.offender_id_display%type default null,
                   p_root_offender_id     in offenders.root_offender_id%type default null,
                   p_single_offender_id   in varchar2 default null,
                   p_agy_loc_id           in agency_locations.agy_loc_id%type,
                   p_client_unique_ref    in offender_visits.client_unique_ref%type default null,
                   p_holds_csr           out sys_refcursor)
   is
      k_hold_txn_type          constant varchar2(3) := 'HOA';

      v_noms_id                offenders.offender_id_display%type;
      v_root_offender_id       offenders.offender_id%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;
   begin
      nomis_context.set_context('AUDIT_MODULE_NAME','API_FINANCE_PROCS.HOLDS');

      -- 
      -- validate offender identification criteria.
      -- Until the implementation of the single offender_id, only
      -- the Noms Id and/or root_offender_id will be populated, with
      -- the root_offender_id taking precedence 
      --
      v_root_offender_id       := p_root_offender_id;
      v_noms_id                := p_noms_id;

      core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                       p_noms_id          => v_noms_id,
                       p_agy_loc_id       => v_agy_loc_id,
                       p_offender_book_id => v_offender_book_id);

      --
      -- Check whether the prison the offender is currently in is the
      -- same as the one specified
      --
      if (v_agy_loc_id != p_agy_loc_id) then 
            raise_application_error(-20017,'Offender not in specified prison');
      end if;

      if p_client_unique_ref is not null then
      
         open p_holds_csr for
              select ot.txn_id txn_id,
                     ot.txn_entry_seq txn_entry_seq,
                     ot.txn_entry_date txn_entry_date,
                     ot.txn_entry_desc txn_entry_desc,
                     ot.txn_reference_number txn_reference_number,
                     ot.txn_entry_amount txn_entry_amount,
                     ot.hold_number hold_number,
                     ot.hold_until_date hold_until_date,
                     ot.client_unique_ref
                from offender_transactions ot
               where ot.offender_id = v_root_offender_id
                 and ot.caseload_id = p_agy_loc_id -- always use the parameter
                 and ot.txn_type = k_hold_txn_type
                 and ot.hold_clear_flag = 'N'
                 and ot.hold_number is not null
                 and ot.client_unique_ref = p_client_unique_ref;

      else

         open p_holds_csr for

              select ot.txn_id txn_id,
                     ot.txn_entry_seq txn_entry_seq,
                     ot.txn_entry_date txn_entry_date,
                     ot.txn_entry_desc txn_entry_desc,
                     ot.txn_reference_number txn_reference_number,
                     ot.txn_entry_amount txn_entry_amount,
                     ot.hold_number hold_number,
                     ot.hold_until_date hold_until_date,
                     ot.client_unique_ref
                from offender_transactions ot
               where ot.offender_id = v_root_offender_id
                 and ot.caseload_id = p_agy_loc_id -- always use the parameter
                 and ot.txn_type = k_hold_txn_type
                 and ot.hold_clear_flag = 'N'
                 and ot.hold_number is not null;

      end if;

   end holds;
   
   procedure store_payment(p_noms_id              in offenders.offender_id_display%type default null,
                           p_root_offender_id     in offenders.root_offender_id%type default null,
                           p_single_offender_id   in varchar2 default null,
                           p_agy_loc_id           in agency_locations.agy_loc_id%type default null,
                           p_txn_type             in offender_transactions.txn_type%type,
                           p_txn_reference_number in offender_transactions.txn_reference_number%type,
                           p_txn_entry_date       in offender_transactions.txn_entry_date%type,
                           p_txn_entry_desc       in offender_transactions.txn_entry_desc%type,
                           p_txn_entry_amount     in offender_transactions.txn_entry_amount%type)
   is
      v_noms_id                offenders.offender_id_display%type;
      v_root_offender_id       offenders.offender_id%type;
      v_offender_book_id       offender_bookings.offender_book_id%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;
   begin
      nomis_context.set_context('AUDIT_MODULE_NAME','API_FINANCE_PROCS.STORE_PAYMENT');
    
      -- 
      -- validate offender identification criteria.
      -- Until the implementation of the siingle offender_id, only
      -- the Noms Id and/or root_offender_id will be populated, with
      -- the root_offender_id taking precedence 
      --
      v_root_offender_id       := p_root_offender_id;
      v_noms_id                := p_noms_id;
      v_agy_loc_id            :=  p_agy_loc_id;

      core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                       p_noms_id          => v_noms_id,
                       p_agy_loc_id       => v_agy_loc_id,
                       p_offender_book_id => v_offender_book_id);

      --
      -- Only payments for digital prisons may be posted
      --
      if not core_utils.is_digital_prison(v_agy_loc_id) then
         raise_application_error(-20010,'Not a Digital Prison');
      end if;
      -- 
      -- Check if the offender has a trust account for the specified prison
      -- 
      if not core_utils.trust_account_exists(v_root_offender_id, v_agy_loc_id) then
         raise_application_error(-20011,'Offender Has No Trust Account at Prison');
      end if;

      insert into api_stored_payments(
               api_stored_payment_id,
               posted_timestamp,
               root_offender_id,
               offender_book_id,
               caseload_id,
               txn_type,
               txn_reference_number,
               txn_entry_date,
               txn_entry_desc,
               txn_entry_amount,
               processed_status)
      values (api_stored_payment_id.nextval,
              systimestamp,
              v_root_offender_id,
              v_offender_book_id,
              v_agy_loc_id,
              p_txn_type,
              p_txn_reference_number,
              p_txn_entry_date,
              p_txn_entry_desc,
              p_txn_entry_amount,
              'NEW');
              
   end store_payment;


   procedure create_transaction(p_root_offender_id     in offender_transactions.offender_id%type,
                                p_offender_book_id     in offender_transactions.offender_book_id%type,
                                p_caseload_id          in offender_transactions.caseload_id%type,
                                p_txn_type             in offender_transactions.txn_type%type,
                                p_txn_reference_number in offender_transactions.txn_reference_number%type,
                                p_txn_entry_date       in offender_transactions.txn_entry_date%type,
                                p_txn_entry_desc       in offender_transactions.txn_entry_desc%type,
                                p_txn_entry_amount     in offender_transactions.txn_entry_amount%type,
                                p_client_unique_ref    in offender_transactions.client_unique_ref%type default null,
                                p_receipt_only         in boolean default false,
                                p_disbursement_only    in boolean default false,
                                p_txn_id              out offender_transactions.txn_id%type,
                                p_txn_entry_seq       out offender_transactions.txn_entry_seq%type)
   is

      v_txn_usage              varchar2(1);
      v_posting_type           offender_transactions.txn_posting_type%type;
      v_sub_act_type           offender_transactions.sub_account_type%type;
      v_txn_entry_amount       offender_transactions.txn_entry_amount%type;
      v_check_ind              varchar2(1);
      v_cheque_prod_flag       transaction_operations.cheque_production_flag%type;
      v_receipt_prod_flag      transaction_operations.receipt_production_flag%type;
      v_deduction_flag         offender_transactions.deduction_flag%type;
      v_gl_entry_seq           gl_transactions.gl_entry_seq%type;
      v_sqlcode                number;
      v_sqlerrm                varchar2(512);
   begin
      p_txn_id := txn_id.nextval;
      p_txn_entry_seq := 1;
      v_check_ind     := 'Y';

      -- 
      -- SDU-124
      -- This select statement had to be extracted 
      -- from otdrdtfu.get_txn_usage which used a hardcoded
      -- module_name of OTDRDTFU
      -- 
      begin
         select tt.txn_usage, 
                nvl (tno.cheque_production_flag, 'N'),
                nvl (tno.receipt_production_flag, 'N')
           into v_txn_usage,
                v_cheque_prod_flag,
                v_receipt_prod_flag
           from transaction_types tt 
           join transaction_operations tno
             on tt.txn_type = tno.txn_type
                and tno.module_name = gk_module_name
          where tt.txn_type = p_txn_type
            and tt.caseload_type = gk_inst_caseload_type
            and tno.caseload_id = p_caseload_id;
      exception
         when no_data_found then
            raise_application_error (-20018, 'Invalid transaction type');
      end;

      if p_disbursement_only and v_txn_usage != 'D' then
         raise_application_error (-20041, 'Only disbursement transaction types allowed');
      end if;

      if p_receipt_only and v_txn_usage != 'R' then
         raise_application_error (-20042, 'Only receipt transaction types allowed');
      end if;

      if v_txn_usage = 'R' then
         --
         -- Check for Offender Deductions for Receipt type transaction.
         --
         deductions.chk_offender_deductions (p_csld_id     => p_caseload_id,
                                             p_off_id      => p_root_offender_id,
                                             p_trans_type  => p_txn_type,
                                             p_shadow_id   => null,
                                             p_ded_flag    => v_deduction_flag);
      end if;

      -- 
      -- from procedure main_process
      -- 
      trust.get_sub_act_type (p_module_name     => gk_module_name,
                              p_txn_type        => p_txn_type,        
                              p_txn_usage       => v_txn_usage,       
                              p_txn_post_type   => v_posting_type,           
                              p_sub_act_type    => v_sub_act_type,           
                              csld_id           => p_caseload_id);

      if otdrdtfu.get_txn_count (p_txn_id, p_txn_entry_seq) > 0 then
         p_txn_entry_seq := p_txn_entry_seq + 1;
      end if;

      api_finance_utils.insert_offender_transaction (p_txn_id                => p_txn_id,
                               p_txn_entry_seq         => p_txn_entry_seq,
                               p_caseload_id           => p_caseload_id,
                               p_offender_id           => p_root_offender_id,
                               p_offender_book_id      => p_offender_book_id,
                               p_posting_type          => v_posting_type,
                               p_txn_type              => p_txn_type,
                               p_txn_entry_desc        => p_txn_entry_desc,
                               p_txn_entry_amount      => p_txn_entry_amount,
                               p_txn_entry_date        => p_txn_entry_date,
                               p_sub_act_type          => v_sub_act_type,
                               p_txn_reference_number  => p_txn_reference_number,
                               p_receipt_number        => null,
                               p_deduction_flag        => v_deduction_flag,
                               p_remitter_name         => null,
                               p_remitter_id           => null,
                               p_pre_withhold_amount   => p_txn_entry_amount,
                               p_txn_adjusted_flag     => 'Y',
                               p_client_unique_ref      => p_client_unique_ref);
  

      v_gl_entry_seq := 0;

      if v_txn_usage = 'D' then

         trust.process_gl_trans_new (p_csld_id          => p_caseload_id,
                                     p_trans_type       => p_txn_type,
                                     p_operation_type   => null,
                                     p_trans_amount     => p_txn_entry_amount,
                                     p_trans_number     => p_txn_id,
                                     p_trans_date       => p_txn_entry_date,
                                     p_trans_desc       => p_txn_entry_desc,
                                     p_trans_seq        => p_txn_entry_seq,
                                     p_module_name      => gk_module_name,
                                     p_off_id           => p_root_offender_id,
                                     p_off_book_id      => p_offender_book_id,
                                     p_sub_act_type_dr  => v_sub_act_type, 
                                     p_sub_act_type_cr  => null,
                                     p_payee_pers_id    => null,
                                     p_payee_corp_id    => null,
                                     p_payee_name_text  => null,
                                     p_gl_sqnc          => v_gl_entry_seq,
                                     p_off_ded_id       => null);

         trust.process_transaction_fee (mod_name     => gk_module_name,
                                        csld_id      => p_caseload_id,
                                        off_id       => p_root_offender_id,
                                        off_bid      => p_offender_book_id,
                                        trans_id     => p_txn_id,
                                        trans_seq    => p_txn_entry_seq,
                                        disbu_type   => p_txn_type,
                                        txn_amount   => p_txn_entry_amount,
                                        trans_date   => p_txn_entry_date,
                                        trans_usage  => v_txn_usage);
      else
         trust.process_gl_trans_new (p_csld_id          => p_caseload_id,
                                     p_trans_type       => p_txn_type,
                                     p_operation_type   => null,
                                     p_trans_amount     => p_txn_entry_amount,
                                     p_trans_number     => p_txn_id,
                                     p_trans_date       => p_txn_entry_date,
                                     p_trans_desc       => p_txn_entry_desc,
                                     p_trans_seq        => p_txn_entry_seq,
                                     p_module_name      => gk_module_name,
                                     p_off_id           => p_root_offender_id,
                                     p_off_book_id      => p_offender_book_id,
                                     p_sub_act_type_dr  => null,
                                     p_sub_act_type_cr  => v_sub_act_type, 
                                     p_payee_pers_id    => null,
                                     p_payee_corp_id    => null,
                                     p_payee_name_text  => null,
                                     p_gl_sqnc          => v_gl_entry_seq,
                                     p_off_ded_id       => null);
      end if;

      trust.update_offender_balance (p_csld_id          => p_caseload_id,
                                     p_off_id           => p_root_offender_id,
                                     p_trans_post_type  => v_posting_type,
                                     p_trans_date       => p_txn_entry_date,
                                     p_trans_number     => p_txn_id,
                                     p_trans_type       => p_txn_type,
                                     p_trans_amount     => p_txn_entry_amount,
                                     p_sub_act_type     => v_sub_act_type,
                                     p_allow_overdrawn  => 'N');

      if v_txn_usage <> 'D' and p_txn_entry_amount > 0 then

         v_txn_entry_amount := p_txn_entry_amount;
         financial.do_deductions_financial (p_csld_id         => p_caseload_id,
                                            p_off_id          => p_root_offender_id,
                                            p_off_book_id     => p_offender_book_id,
                                            p_trans_type      => p_txn_type,
                                            p_trans_number    => p_txn_id,
                                            p_trans_date      => p_txn_entry_date,
                                            p_sub_act_type    => v_sub_act_type,
                                            p_ded_flag        => 'Y',
                                            p_receipt_amount  => p_txn_entry_amount,
                                            p_shadow_id       => null,
                                            p_ded_amount      => v_txn_entry_amount,
                                            txn_sequence      => p_txn_entry_seq,
                                            p_info_number     => null);
      end if;

      if v_txn_usage = 'R' then

         deductions.get_ac_and_set_ind_date (p_root_offender_id, p_caseload_id);

      elsif v_txn_usage = 'D' then

         if v_check_ind = 'Y' then

            deductions.get_ac_and_set_ind_date (p_root_offender_id, p_caseload_id);
         end if;
      end if;
   exception
      when others then 
         v_sqlcode := sqlcode;
         v_sqlerrm := sqlerrm;
         --
         -- Remap exception codes, in particular those user exceptions raised by the nomis trust and 
         -- finance packages in the range -20999 to -20000  To avoid conflict with the
         -- Nomis API usage of the user exception range.
         --
         case
            when v_sqlcode = -1 and instr(v_sqlerrm,'OMS_OWNER.OFFENDER_TRANSACTIONS_UK1') > 0 then
               raise_application_error (-20019, 'Duplicate post');
            when v_sqlcode = -20009 and v_sqlerrm like '%Overdrawn transaction%' then 
               raise_application_error (-20009, 'Insufficient funds');
            when v_sqlcode = -20000 and v_sqlerrm like '%No setup been found in Maintain Transaction Operations%' then 
               raise_application_error (-20018, 'Invalid transaction type');
            when v_sqlcode in (-2018, -20041, -20042) then
               raise;
            when v_sqlcode between -20999 and -20000 then 
               -- Map any other user defined exceptions to -20013
               nomis_api_log.error('API_FINANCE_PROCS.CREATE_TRANSACTION',v_sqlerrm);
               raise_application_error (-20013, v_sqlerrm);
            else
               raise;
         end case;
   end create_transaction;

   procedure stored_payment_failed(
                  p_api_stored_payment_id in api_stored_payments.api_stored_payment_id%type,
                  p_err_msg               in varchar2)
   is
      pragma autonomous_transaction;
   begin
      update api_stored_payments
         set processed_status = 'FAILED',
             error_message = p_err_msg
       where api_stored_payment_id = p_api_stored_payment_id;
      commit;
   end stored_payment_failed;

   procedure stored_payments(p_agy_loc_id  in agency_locations.agy_loc_id%type,
                             p_from_date   in date,
                             p_to_date     in date default null,
                             p_failed_only in boolean default false,
                             p_payment_csr out sys_refcursor)
   is
   begin
      if p_failed_only then
         open p_payment_csr for
              select api_stored_payment_id, posted_timestamp, root_offender_id, 
                     offender_book_id, caseload_id, txn_type, 
                     txn_reference_number, txn_entry_date, txn_entry_desc, 
                     txn_entry_amount, txn_id, txn_entry_seq, 
                     processed_status, error_message
                from api_owner.api_stored_payments
               where caseload_id = p_agy_loc_id
                 and posted_timestamp >= p_from_date
                 and posted_timestamp <= nvl(p_to_date, posted_timestamp)
                 and processed_status = 'FAILED'
               order by posted_timestamp;
      else
         open p_payment_csr for
              select api_stored_payment_id, posted_timestamp, root_offender_id, 
                     offender_book_id, caseload_id, txn_type, 
                   txn_reference_number, txn_entry_date, txn_entry_desc, 
                     txn_entry_amount, txn_id, txn_entry_seq, 
                     processed_status, error_message
                from api_owner.api_stored_payments
               where caseload_id = p_agy_loc_id
                 and posted_timestamp >= p_from_date
                 and trunc(posted_timestamp) <= nvl(p_to_date, posted_timestamp)
               order by posted_timestamp;
      end if;

   end stored_payments;

end api_finance_procs;
/
show err

