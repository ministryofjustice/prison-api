create or replace package api_owner.json_builder
is
   function show_version return varchar2;

   function offender_identification (p_offender_identifier  in varchar2,
                                     p_establishment_code   in varchar2,
                                     p_single_offender_id   in varchar2 default null)
      return json;

   function personal_details(p_first_name        in varchar2 default null,
                             p_last_name         in varchar2 default null,
                             p_birth_date        in date default null,
                             p_sex_code          in varchar2 default null,
                             p_sex_desc          in varchar2 default null,
                             p_diet              in varchar2 default null,
                             p_diet_desc         in varchar2 default null,
                             p_religion          in varchar2 default null,
                             p_religion_desc     in varchar2 default null,
                             p_sec_category      in varchar2 default null,
                             p_sec_category_desc in varchar2 default null,
                             p_nationality       in varchar2 default null,
                             p_nationality_desc  in varchar2 default null,
                             p_language          in varchar2 default null,
                             p_language_desc     in varchar2 default null,
                             p_ethnicity         in varchar2 default null,
                             p_ethnicity_desc    in varchar2 default null)
      return json;
   
   function sentence_information(p_reception_date in date default null,
                                 p_convicted_status in varchar2 default null,
                                 p_imprisonment_status in varchar2 default null,
                                 p_imprison_status_desc in varchar2 default null)
      return json;

   function location(p_agency_location in varchar2 default null,
                     p_internal_location in varchar2 default null,
                     p_location_type in varchar2 default null)
      return json;

   function warnings(p_warning_type      in varchar2,
                     p_type_desc         in varchar2,
                     p_warning_sub_type  in varchar2,
                     p_sub_type_desc     in varchar2,
                     p_warning_date      in date,
                     p_expiry_date       in date default null,
                     p_status            in varchar2)
      return json;

   function entitlement(p_canteen_adjudication boolean default null,
                        p_iep_level            varchar2 default null,
                        p_iep_level_desc       varchar2 default null)
      return json;

   function discharge_status(p_discharge_date in date,
                             p_discharge_reason in varchar2,
                             p_reason_desc in varchar2)
      return json;


   function off_transaction(p_transaction_id    in integer,
                            p_transaction_seq   in integer,
                            p_counter           in integer default 1,
                            p_account_type      in varchar2,
                            p_account_type_desc in varchar2,
                            p_txn_type          in varchar2,
                            p_txn_type_desc     in varchar2,
                            p_entry_desc        in varchar2,
                            p_entry_amount      in number,
                            p_entry_date        in date)
      return json;

   function account_balance(p_account_type      in varchar2,
                            p_account_type_desc in varchar2,
                            p_balance           in number)
      return json;

   function case_details(p_keydate_status   in varchar2 default null,
                         p_hdced            in date default null,
                         p_ard              in date default null,
                         p_crd              in date default null,
                         p_ped              in date default null,
                         p_apd              in date default null,
                         p_npd              in date default null,
                         p_prrd             in date default null,
                         p_tariff           in integer default null,
                         p_eff_sent_length  in varchar2 default null,
                         p_personal_officer in varchar2 default null,
                         p_sentence_date    in date default null,
                         p_release_date     in date default null)
      return json;

   function case_note(p_case_note_id     in integer,
                      p_contact_datetime in date,
                      p_source_code      in varchar2,
                      p_source_desc      in varchar2,
                      p_type_code        in varchar2,
                      p_type_desc        in varchar2,
                      p_sub_type_code    in varchar2,
                      p_sub_type_desc    in varchar2,
                      p_staff_id         in integer,
                      p_staff_name       in varchar2,
                      p_userid           in varchar2,
                      p_text             in varchar2,
                      p_amended          in boolean)
      return json;
   
   function staff_member(p_staff_id in integer,
                         p_name     in varchar2,
                         p_userid   in varchar2)
      return json;

   function build_code_desc(p_code in varchar2, p_desc in varchar2) return json;
end json_builder;
/
show err
create or replace package body api_owner.json_builder
is
   -- =============================================================
   v_version   CONSTANT VARCHAR2 (60) := '1.2   07-Apr-2017';
   -- =============================================================
   /*
   MODIFICATION HISTORY
   ------------------------------------------------------------------------------
   Person      Date           Version                Comments
   ---------   -----------    ---------   ---------------------------------------
   Paul M      07-Apr-2017     1.2        Added case_note
   Paul M      16-Dec-2016     1.1        Added type to location
   Paul M      21-Oct-2016     1.0        Initial version
   ------------------------------------------------------------------------------
   */
   function show_version return varchar2
   is
   begin
      return (v_version);
   end show_version;

   function offender_identification (p_offender_identifier  in varchar2,
                                     p_establishment_code   in varchar2,
                                     p_single_offender_id   in varchar2 default null)
      return json
   is 
      v_json json;
   begin
      v_json := json();
      if p_single_offender_id is not null then
         v_json.put('single_offender_id',p_single_offender_id);
      end if;

      v_json.put('noms_id',p_offender_identifier);
      v_json.put('establishment_code', p_establishment_code);

      return v_json;

   end offender_identification;

   function personal_details(p_first_name        in varchar2 default null,
                             p_last_name         in varchar2 default null,
                             p_birth_date        in date default null,
                             p_sex_code          in varchar2 default null,
                             p_sex_desc          in varchar2 default null,
                             p_diet              in varchar2 default null,
                             p_diet_desc         in varchar2 default null,
                             p_religion          in varchar2 default null,
                             p_religion_desc     in varchar2 default null,
                             p_sec_category      in varchar2 default null,
                             p_sec_category_desc in varchar2 default null,
                             p_nationality       in varchar2 default null,
                             p_nationality_desc  in varchar2 default null,
                             p_language          in varchar2 default null,
                             p_language_desc     in varchar2 default null,
                             p_ethnicity         in varchar2 default null,
                             p_ethnicity_desc    in varchar2 default null)
      return json
   is
      v_json json;
   begin
      v_json := json();
      if p_last_name is not null then
         v_json.put('offender_surname', p_last_name);
      end if;
      if p_first_name is not null then
         v_json.put('offender_given_name_1', p_first_name);
      end if;
      if p_birth_date is not null then
         v_json.put('offender_dob', json_ext.to_json_value(p_birth_date)); 
      end if;
      if p_sex_code is not null then
         v_json.put('gender', build_code_desc(p_sex_code, p_sex_desc));
      end if;
      if p_diet is not null then
         v_json.put('diet', build_code_desc(p_diet, p_diet_desc));
      end if;
      if p_religion     is not null then
         v_json.put('religion' ,build_code_desc(p_religion,p_religion_desc));
      end if;
      if p_sec_category is not null then
         v_json.put('security_category' , build_code_desc( p_sec_category,p_sec_category_desc));
      end if;
      if p_nationality  is not null then
         v_json.put('nationality',build_code_desc( p_nationality,p_nationality_desc));
      end if;
      if p_language     is not null then
         v_json.put('language', build_code_desc( p_language,p_language_desc));
      end if;
      if p_ethnicity    is not null then
         v_json.put('ethnicity', build_code_desc( p_ethnicity,p_ethnicity_desc));
      end if;

      return v_json;
   end personal_details;

   function sentence_information(p_reception_date in date default null,
                                 p_convicted_status in varchar2 default null,
                                 p_imprisonment_status in varchar2 default null,
                                 p_imprison_status_desc in varchar2 default null)
      return json
   is
      v_json json;
      v_imp_status_obj json;
   begin
      v_json := json();

      if p_reception_date is not null then
         v_json.put('reception_arrival_date_and_time',json_ext.to_json_value(p_reception_date));
      end if;
      if p_convicted_status is not null then
         v_json.put('status', p_convicted_status);
      end if;
      if p_imprisonment_status is not null then
         v_json.put('imprisonment_status',  build_code_desc( p_imprisonment_status, p_imprison_status_desc));
      end if;

      return v_json;
   end sentence_information;

   function location(p_agency_location in varchar2 default null,
                     p_internal_location in varchar2 default null,
                     p_location_type in varchar2 default null)
      return json
   is
      v_json json;
   begin
      v_json := json();
      if p_agency_location is not null then 
         v_json.put('agency_location', p_agency_location);
      end if;
      if p_internal_location is not null then
         v_json.put('internal_location', p_internal_location);
         v_json.put('location_type', p_location_type);
      end if;

      return v_json;
   end location;

   function warnings(p_warning_type     in varchar2,
                     p_type_desc        in varchar2,
                     p_warning_sub_type in varchar2,
                     p_sub_type_desc    in varchar2,
                     p_warning_date     in date,
                     p_expiry_date      in date default null,
                     p_status           in varchar2)
      return json
   is
      v_json json;
   begin
      v_json := json();
      v_json.put('warning_type', build_code_desc( p_warning_type,p_type_desc));
      v_json.put('warning_sub_type', build_code_desc( p_warning_sub_type,p_sub_type_desc));
      v_json.put('warning_date', json_ext.to_json_value(p_warning_date));
      if p_expiry_date is not null then
         v_json.put('expiry_date', json_ext.to_json_value(p_expiry_date));
      end if;
      v_json.put('status', p_status );
      return v_json;
   end warnings;

   function entitlement(p_canteen_adjudication boolean default null,
                        p_iep_level            varchar2 default null,
                        p_iep_level_desc       varchar2 default null)
      return json
   is
      v_json json;
      v_iep_obj json;
   begin
      v_json := json();
      v_iep_obj := json();
      if p_canteen_adjudication is not null then
         v_json.put('canteen_adjudication', p_canteen_adjudication);
      end if;
      if p_iep_level is not null then
         v_json.put('iep_level', build_code_desc( p_iep_level,p_iep_level_desc));
      end if;
      return v_json;
   end entitlement;

   function discharge_status(p_discharge_date in date,
                             p_discharge_reason in varchar2,
                             p_reason_desc in varchar2)
      return json
   is
      v_json json;
   begin
      v_json := json();
      v_json.put('discharge_datetime', json_ext.to_json_value(p_discharge_date));
      v_json.put('discharge_reason', build_code_desc(p_discharge_reason, p_reason_desc ));
      return v_json;
   end discharge_status;

   function off_transaction(p_transaction_id    in integer,
                            p_transaction_seq   in integer,
                            p_counter           in integer default 1,
                            p_account_type      in varchar2,
                            p_account_type_desc in varchar2,
                            p_txn_type          in varchar2,
                            p_txn_type_desc     in varchar2,
                            p_entry_desc        in varchar2,
                            p_entry_amount      in number,
                            p_entry_date        in date)
      return json
   is
      v_json json;
   begin
      v_json := json();
      v_json.put('transaction_id', p_transaction_id);
      v_json.put('transaction_seq', p_transaction_seq);
      v_json.put('counter', p_counter);
      v_json.put('account', build_code_desc(p_account_type, p_account_type_desc));
      v_json.put('transaction_type', build_code_desc(p_txn_type, p_txn_type_desc));
      v_json.put('description', p_entry_desc);
      v_json.put('amount', p_entry_amount);
      v_json.put('date', json_ext.to_json_value(p_entry_date));
      return v_json;
   end off_transaction;

   function account_balance(p_account_type      in varchar2,
                            p_account_type_desc in varchar2,
                            p_balance           in number)
      return json
   is
      v_json json;
   begin
      v_json := json();
      v_json.put('account', build_code_desc(p_account_type, p_account_type_desc));
      v_json.put('balance', p_balance);
      return v_json;
   end account_balance;

   function case_details(p_keydate_status   in varchar2 default null,
                         p_hdced            in date default null,
                         p_ard              in date default null,
                         p_crd              in date default null,
                         p_ped              in date default null,
                         p_apd              in date default null,
                         p_npd              in date default null,
                         p_prrd             in date default null,
                         p_tariff           in integer default null,
                         p_eff_sent_length  in varchar2 default null,
                         p_personal_officer in varchar2 default null,
                         p_sentence_date    in date default null,
                         p_release_date     in date default null)
      return json
   is
      v_json json;
   begin
      v_json := json();
		if p_keydate_status is not null then
         v_json.put('key_date_status', p_keydate_status);
      end if;
		if p_hdced is not null then
         v_json.put('hdced', json_ext.to_json_value(p_hdced));
      end if;
		if p_ard is not null then
         v_json.put('ard', json_ext.to_json_value(p_ard));
      end if;
		if p_crd is not null then
         v_json.put('crd', json_ext.to_json_value(p_crd));
      end if;
		if p_ped is not null then
         v_json.put('ped', json_ext.to_json_value(p_ped));
      end if;
		if p_apd is not null then
         v_json.put('apd', json_ext.to_json_value(p_apd));
      end if;
		if p_npd is not null then
         v_json.put('npd', json_ext.to_json_value(p_npd));
      end if;
		if p_prrd is not null then
         v_json.put('prrd', json_ext.to_json_value(p_prrd));
      end if;
		if p_tariff is not null then
         v_json.put('tariff', p_tariff);
      end if;
		if p_eff_sent_length is not null then
         v_json.put('effective_sentence_length', p_eff_sent_length);
      end if;
		if p_personal_officer is not null then
         v_json.put('personal_officer', p_personal_officer);
      end if;
		if p_sentence_date is not null then
         v_json.put('sentence_date', json_ext.to_json_value(p_sentence_date));
      end if;
		if p_release_date is not null then
         v_json.put('release_date', json_ext.to_json_value(p_release_date));
      end if;
      return v_json;
   end case_details;

   function case_note(p_case_note_id     in integer,
                      p_contact_datetime in date,
                      p_source_code      in varchar2,
                      p_source_desc      in varchar2,
                      p_type_code        in varchar2,
                      p_type_desc        in varchar2,
                      p_sub_type_code    in varchar2,
                      p_sub_type_desc    in varchar2,
                      p_staff_id         in integer,
                      p_staff_name       in varchar2,
                      p_userid           in varchar2,
                      p_text             in varchar2,
                      p_amended          in boolean)
      return json
   is
      v_json json;
   begin
      v_json := json();
      v_json.put('id', p_case_note_id);
      v_json.put('contact_datetime', json_ext.to_json_value(p_contact_datetime));
      v_json.put('source', build_code_desc(p_source_code, p_source_desc));
      v_json.put('type', build_code_desc(p_type_code, p_type_desc));
      v_json.put('sub_type', build_code_desc(p_sub_type_code, p_sub_type_desc));
      v_json.put('staff_member', staff_member(p_staff_id, p_staff_name, p_userid));
      v_json.put('text', p_text);
      v_json.put('amended', p_amended);
      return v_json;
   end case_note;

   function staff_member(p_staff_id in integer,
                         p_name     in varchar2,
                         p_userid   in varchar2)
      return json
   is
      v_json json;
   begin
      v_json := json();
      v_json.put('id', p_staff_id); 
      v_json.put('name', p_name);
      v_json.put('userid', p_userid);
      return v_json;
   end staff_member;

   function build_code_desc(p_code in varchar2, p_desc in varchar2) return json
   is
      v_json_obj json;
   begin
      v_json_obj := json();
      v_json_obj.put('code',p_code);
      v_json_obj.put('desc',p_desc);
      return v_json_obj;
   end build_code_desc;
      
end json_builder;
/
show err
