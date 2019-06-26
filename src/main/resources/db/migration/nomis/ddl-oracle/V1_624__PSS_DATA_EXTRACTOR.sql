create or replace package api_owner.pss_data_extractor
is
   function show_version return varchar2;

   procedure get_offender_identity(p_offender_book_id    in offender_bookings.offender_book_id%type,
                                   p_single_offender_id out varchar2,
                                   p_root_offender_id   out offenders.root_offender_id%type,
                                   p_noms_id            out offenders.offender_id_display%type,
                                   p_agy_loc_id         out offender_bookings.agy_loc_id%type);

   procedure get_offender_identity(p_offender_id        in offenders.offender_id%type,
                                   p_single_offender_id out varchar2,
                                   p_root_offender_id   out offenders.root_offender_id%type,
                                   p_noms_id            out offenders.offender_id_display%type);

   procedure get_offender_details(p_offender_book_id    in offender_bookings.offender_book_id%type,
                                  p_single_offender_id out varchar2,
                                  p_noms_id            out offenders.offender_id_display%type,
                                  p_root_offender_id   out offenders.root_offender_id%type,
                                  p_agy_loc_id         out offender_bookings.agy_loc_id%type,
                                  p_first_name         out offenders.first_name%type,
                                  p_last_name          out offenders.last_name%type,
                                  p_birth_date         out offenders.birth_date%type,
                                  p_sex_code           out offenders.sex_code%type,
                                  p_sex_desc           out varchar2,
                                  p_diet               out offender_profile_details.profile_code%type,
                                  p_diet_desc          out varchar2,
                                  p_religion           out offender_profile_details.profile_code%type,
                                  p_religion_desc      out varchar2,
                                  p_sec_category       out offender_assessments.review_sup_level_type%type,
                                  p_sec_category_desc  out varchar2,
                                  p_nationality        out offender_profile_details.profile_code%type,
                                  p_nationality_desc   out varchar2,
                                  p_language           out offender_languages.language_code%type,
                                  p_language_desc      out varchar2,
                                  p_ethnicity          out offenders.race_code%type,
                                  p_ethnicity_desc     out varchar2,
                                  p_internal_location  out agency_internal_locations.description%type,
                                  p_location_type      out agency_internal_locations.internal_location_type%type,
                                  p_convicted_status   out varchar2,
                                  p_imprisonment_status out offender_imprison_statuses.imprisonment_status%type,
                                  p_imprison_status_desc out varchar2);

   procedure get_iep_level(p_offender_book_id in offender_bookings.offender_book_id%type,
                           p_iep_level        out offender_iep_levels.iep_level%type,
                           p_iep_level_desc   out reference_codes.description%type);

   procedure get_offender_name(p_offender_id    in offenders.offender_id%type,
                               p_first_name    out offenders.first_name%type,
                               p_last_name     out offenders.last_name%type,
                               p_birth_date    out offenders.birth_date%type);

   procedure get_internal_location(p_internal_location_id in agency_internal_locations.internal_location_id%type,
                                   p_internal_location  out agency_internal_locations.description%type,
                                   p_location_type      out agency_internal_locations.internal_location_type%type);

   procedure get_account_balances(p_agy_loc_id        in agency_locations.agy_loc_id%type,
                                  p_offender_book_id  in offender_bookings.offender_book_id%type,
                                  p_cash_balance     out offender_sub_accounts.balance%type,
                                  p_spends_balance   out offender_sub_accounts.balance%type,
                                  p_savings_balance  out offender_sub_accounts.balance%type);

   function generate_guid return varchar2;

   procedure get_imp_status_desc(p_imprisonment_status in imprisonment_statuses.imprisonment_status%type,
                                 p_imprisonment_desc  out imprisonment_statuses.description%type,
                                 p_convicted_status   out varchar2);

   function get_profile_code_desc(p_profile_type in profile_codes.profile_type%type,
                                  p_profile_code in profile_codes.profile_code%type)
      return varchar2;

   function get_movement_reason(p_movement_type in movement_reasons.movement_type%type,
                                p_reason_code in movement_reasons.movement_reason_code%type)
      return varchar2;

   function has_canteen_sanction(p_offender_book_id in offender_bookings.offender_book_id%type) return boolean;

   procedure get_personal_officer(p_offender_book_id  in offender_bookings.offender_book_id%type,
                                  p_staff_id         out staff_members.staff_id%type,
                                  p_first_name       out staff_members.first_name%type,
                                  p_last_name        out staff_members.last_name%type,
                                  p_display_name     out varchar2);
end pss_data_extractor;
/
show err
create or replace package body api_owner.pss_data_extractor
is
   -- =============================================================
   v_version   CONSTANT VARCHAR2 (60) := '1.1   16-Dec-2016';
   -- =============================================================
   /*
   MODIFICATION HISTORY
   ------------------------------------------------------------------------------
   Person      Date           Version                Comments
   ---------   -----------    ---------   ---------------------------------------
   Paul M      16-Dec-2016     1.1        Added location type
   Paul M      21-Oct-2016     1.0        Initial version
   ------------------------------------------------------------------------------
   */
   function show_version return varchar2
   is
   begin
      return (v_version);
   end show_version;

   procedure get_offender_identity(p_offender_book_id    in offender_bookings.offender_book_id%type,
                                   p_single_offender_id out varchar2,
                                   p_root_offender_id   out offenders.root_offender_id%type,
                                   p_noms_id            out offenders.offender_id_display%type,
                                   p_agy_loc_id         out offender_bookings.agy_loc_id%type)
   is
   begin
      p_single_offender_id := null;
      select o.offender_id_display,ob.agy_loc_id, o.root_offender_id
        into p_noms_id, p_agy_loc_id, p_root_offender_id
        from offender_bookings ob
        join offenders o
          on o.offender_id = ob.offender_id
       where ob.offender_book_id = p_offender_book_id;

   end  get_offender_identity;

   procedure get_offender_identity(p_offender_id        in offenders.offender_id%type,
                                   p_single_offender_id out varchar2,
                                   p_root_offender_id   out offenders.root_offender_id%type,
                                   p_noms_id            out offenders.offender_id_display%type)
   is
   begin

      p_single_offender_id := null;
      select o.offender_id_display, o.root_offender_id
        into p_noms_id, p_root_offender_id
        from offenders o
       where o.offender_id = p_offender_id;

   end  get_offender_identity;

   procedure get_offender_details(p_offender_book_id    in offender_bookings.offender_book_id%type,
                                  p_single_offender_id out varchar2,
                                  p_noms_id            out offenders.offender_id_display%type,
                                  p_root_offender_id   out offenders.root_offender_id%type,
                                  p_agy_loc_id         out offender_bookings.agy_loc_id%type,
                                  p_first_name         out offenders.first_name%type,
                                  p_last_name          out offenders.last_name%type,
                                  p_birth_date         out offenders.birth_date%type,
                                  p_sex_code           out offenders.sex_code%type,
                                  p_sex_desc           out varchar2,
                                  p_diet               out offender_profile_details.profile_code%type,
                                  p_diet_desc          out varchar2,
                                  p_religion           out offender_profile_details.profile_code%type,
                                  p_religion_desc      out varchar2,
                                  p_sec_category       out offender_assessments.review_sup_level_type%type,
                                  p_sec_category_desc  out varchar2,
                                  p_nationality        out offender_profile_details.profile_code%type,
                                  p_nationality_desc   out varchar2,
                                  p_language           out offender_languages.language_code%type,
                                  p_language_desc      out varchar2,
                                  p_ethnicity          out offenders.race_code%type,
                                  p_ethnicity_desc     out varchar2,
                                  p_internal_location  out agency_internal_locations.description%type,
                                  p_location_type      out agency_internal_locations.internal_location_type%type,
                                  p_convicted_status   out varchar2,
                                  p_imprisonment_status out offender_imprison_statuses.imprisonment_status%type,
                                  p_imprison_status_desc out varchar2)
   is
   begin

      p_single_offender_id := null;
      select o.offender_id_display,ob.agy_loc_id,
             o.root_offender_id,
             o.first_name, o.last_name, o.birth_date, 
             o.sex_code, rc2.description sex_desc,
             o.race_code,
             rc1.description race_desc,
             ail.description,
             ail.internal_location_type,
             case 
                when ist.band_code <= 8 then 'Convicted'
                when ist.band_code > 8 then 'Remand'
                else null
             end offender_status,
             ois.imprisonment_status,
             ist.description imprison_status_desc
        into p_noms_id,p_agy_loc_id,
             p_root_offender_id,
             p_first_name, p_last_name, p_birth_date, 
             p_sex_code, p_sex_desc,
             p_ethnicity,
             p_ethnicity_desc,
             p_internal_location,
             p_location_type,
             p_convicted_status,
             p_imprisonment_status,
             p_imprison_status_desc
        from offender_bookings ob
        join offenders o
          on o.offender_id = ob.offender_id
        left join offender_imprison_statuses ois
          on ois.offender_book_id = ob.offender_book_id
             and ois.latest_status = 'Y'
        left join imprisonment_statuses ist
          on ist.imprisonment_status = ois.imprisonment_status
        left join agency_internal_locations ail
          on ail.internal_location_id = ob.living_unit_id
        left join reference_codes rc1
          on rc1.code = o.race_code
             and domain = 'ETHNICITY'
        left join reference_codes rc2
          on rc2.code = o.sex_code
             and rc2.domain = 'SEX'
       where ob.offender_book_id = p_offender_book_id;

      --
      -- Religion, Diet and Nationality
      --
      for prof_rec in (select opd.profile_type,pc.description,opd.profile_code
                         from offender_profile_details opd
                         left join profile_codes pc
                           on pc.profile_type = opd.profile_type
                              and pc.profile_code = opd.profile_code
                        where opd.offender_book_id = p_offender_book_id
                          and opd.profile_seq = 1
                          and opd.profile_type in ('DIET','RELF','NAT','NATIO')
                          and opd.profile_code is not null)
      loop
         case prof_rec.profile_type
            when 'DIET' then 
               p_diet := prof_rec.profile_code;
               p_diet_desc := prof_rec.description;
            when 'RELF' then
               p_religion := prof_rec.profile_code;
               p_religion_desc := prof_rec.description;
            when 'NAT' then
               p_nationality := prof_rec.profile_code;
               p_nationality_desc := prof_rec.description;
            when 'NATIO' then
               p_nationality := 'MULTI';
               p_nationality_desc := prof_rec.profile_code;
         end case;
      end loop;
      --
      -- Prefered spoken language
      --
      begin
         select ol.language_code, rc.description
           into p_language, p_language_desc
           from offender_languages ol
           left join reference_codes rc
             on rc.code = ol.language_code
                and rc.domain = 'LANG'
          where offender_book_id = p_offender_book_id
            and language_type = 'PREF_SPEAK';
      exception
         when no_data_found then
            null;
      end;
      --
      -- Security Category
      --
      begin
         select review_sup_level_type,rc.description
           into p_sec_category, p_sec_category_desc
           from (select off_ass.review_sup_level_type,
                        row_number() over(order by off_ass.assessment_date desc,off_ass.assessment_seq desc) rnum 
                   from offender_assessments off_ass
                   join assessments ass
                     on off_ass.assessment_type_id = ass.assessment_id
                  where off_ass.offender_book_id = p_offender_book_id
                    and ass.caseload_type = 'INST'
                    and ass.determine_sup_level_flag = 'Y'
                    and off_ass.evaluation_result_code = 'APP'
                    and off_ass.assess_status = 'A') vass
           left join reference_codes rc
             on rc.code = vass.review_sup_level_type
                and rc.domain = 'SUP_LVL_TYPE'
          where vass.rnum = 1;
      exception
         when no_data_found then
            p_sec_category := 'Z';
            p_sec_category_desc := 'Unclass';
      end;

   end  get_offender_details;
   
   procedure get_iep_level(p_offender_book_id in offender_bookings.offender_book_id%type,
                           p_iep_level        out offender_iep_levels.iep_level%type,
                           p_iep_level_desc   out reference_codes.description%type)
   is
   begin
      select iep_level, description
        into p_iep_level, p_iep_level_desc 
        from ( select oil.iep_level,
                      rc.description,
                      oil.offender_book_id,
                      row_number() over(partition by oil.offender_book_id 
                                           order by oil.iep_date desc, oil.iep_level_seq desc) rnum,
                      il.remand_spend_limit,
                      il.convicted_spend_limit
                 from offender_iep_levels oil
                 join iep_levels il
                   on il.agy_loc_id = oil.agy_loc_id
                      and il.iep_level = oil.iep_level
                 left join reference_codes rc
                   on rc.code = oil.iep_level
                      and rc.domain = 'IEP_LEVEL'
                where oil.offender_book_id = p_offender_book_id) 
       where rnum = 1;
   exception
      when no_data_found then
         null;
   end get_iep_level;

   procedure get_offender_name(p_offender_id    in offenders.offender_id%type,
                               p_first_name    out offenders.first_name%type,
                               p_last_name     out offenders.last_name%type,
                               p_birth_date    out offenders.birth_date%type)
   is
   begin
      select o.first_name, o.last_name, o.birth_date
        into p_first_name, p_last_name, p_birth_date
        from offenders o
       where offender_id = p_offender_id;
   exception
      when no_data_found then
         -- return no values
         null;
   end get_offender_name;

   procedure get_internal_location(p_internal_location_id in agency_internal_locations.internal_location_id%type,
                                   p_internal_location  out agency_internal_locations.description%type,
                                   p_location_type      out agency_internal_locations.internal_location_type%type)
   is
   begin
      select ail.description,
             ail.internal_location_type
        into p_internal_location,
             p_location_type 
        from agency_internal_locations ail
       where ail.internal_location_id = p_internal_location_id;
   exception
      when no_data_found then
         -- return no values
         null;
   end get_internal_location;

   procedure get_account_balances(p_agy_loc_id        in agency_locations.agy_loc_id%type,
                                  p_offender_book_id  in offender_bookings.offender_book_id%type,
                                  p_cash_balance     out offender_sub_accounts.balance%type,
                                  p_spends_balance   out offender_sub_accounts.balance%type,
                                  p_savings_balance  out offender_sub_accounts.balance%type)
   is
   begin
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
                where osa.caseload_id = p_agy_loc_id
                  and ob.offender_book_id = p_offender_book_id
                  and ac.sub_account_type in ('REG','SPND','SAV'));

   end get_account_balances;
                
   function generate_guid return varchar2
   is
      v_guid raw(16);
   begin
      v_guid := sys_guid();
      return  substr (v_guid, 1, 8) || '-' || 
              substr (v_guid, 9, 4) || '-' || 
              substr (v_guid, 13, 4) || '-' || 
              substr (v_guid, 17, 4) || '-' || 
              substr (v_guid, 21);
   end generate_guid;

   procedure get_imp_status_desc(p_imprisonment_status in imprisonment_statuses.imprisonment_status%type,
                                 p_imprisonment_desc  out imprisonment_statuses.description%type,
                                 p_convicted_status   out varchar2)
   is
      v_band_code imprisonment_statuses.band_code%type;
   begin
      select ist.description, ist.band_code,
             case 
                when ist.band_code <= 8 then 'Convicted'
                else 'Remand'
             end offender_status
        into p_imprisonment_desc, v_band_code,
             p_convicted_status
        from imprisonment_statuses ist
       where ist.imprisonment_status = p_imprisonment_status;
   exception
      when no_data_found then
         null;
   end get_imp_status_desc;

   function get_profile_code_desc(p_profile_type in profile_codes.profile_type%type,
                                  p_profile_code in profile_codes.profile_code%type)
      return varchar2
   is
      v_description profile_codes.description%type;
   begin
      select description
        into v_description
        from profile_codes
       where profile_type = p_profile_type
         and profile_code = p_profile_code;

      return v_description;
   exception
      when no_data_found then
         return null;
   end get_profile_code_desc;

   function get_movement_reason(p_movement_type in movement_reasons.movement_type%type,
                                p_reason_code in movement_reasons.movement_reason_code%type)
      return varchar2
   is
      v_description movement_reasons.description%type;
   begin
      select description
        into v_description
        from movement_reasons
       where movement_type = p_movement_type
         and movement_reason_code = p_reason_code;

      return v_description;
   exception
      when no_data_found then
         return null;
   end get_movement_reason;

   function has_canteen_sanction(p_offender_book_id in offender_bookings.offender_book_id%type) return boolean
   is
      v_dummy varchar2(1);
   begin

      select 'X' 
        into v_dummy
        from dual
       where exists (select null
                       from offender_oic_sanctions
                      where offender_book_id = p_offender_book_id 
                        and oic_sanction_code in ('CANTEEN')
                        and effective_date <= trunc(sysdate)
                        and (add_months(effective_date,sanction_months) + sanction_days) >= trunc(sysdate));
      return true;
   exception
      when no_data_found then
         return false;
   end has_canteen_sanction;


   procedure get_personal_officer(p_offender_book_id  in offender_bookings.offender_book_id%type,
                                  p_staff_id         out staff_members.staff_id%type,
                                  p_first_name       out staff_members.first_name%type,
                                  p_last_name        out staff_members.last_name%type,
                                  p_display_name     out varchar2)
   is
   begin
      select staff_id, first_name, last_name, initcap (last_name) || ', ' || initcap (first_name) 
        into p_staff_id, p_first_name, p_last_name, p_display_name
        from (select sm.staff_id,
                     sm.first_name,
                     sm.last_name,
                     row_number() over(order by oco.case_assigned_date desc, oco.case_assigned_time desc) rnum1
                from offender_case_officers oco
                join staff_members sm
                  on oco.case_officer_id = sm.staff_id
               where oco.offender_book_id = p_offender_book_id
                 and oco.case_assigned_date <= sysdate)
       where rnum1 = 1;
   exception
      when no_data_found then
         null;
   end get_personal_officer;

end pss_data_extractor;
/
show err
