create or replace package api_owner.pss_events
is
   
   function show_version return varchar2;

   procedure reception(p_offender_book_id in offender_bookings.offender_book_id%type,
                       p_agy_loc_id       in agency_locations.agy_loc_id%type default null,
                       p_movement_date    in offender_external_movements.movement_date%type,
                       p_movement_time    in offender_external_movements.movement_time%type);

   procedure discharge(p_offender_book_id in offender_bookings.offender_book_id%type,
                       p_agy_loc_id       in agency_locations.agy_loc_id%type default null,
                       p_movement_date    in offender_external_movements.movement_date%type,
                       p_movement_time    in offender_external_movements.movement_time%type,
                       p_movement_type    in offender_external_movements.movement_type%type,
                       p_movement_reason  in offender_external_movements.movement_reason_code%type);


   procedure offender_update(p_offender_book_id      in offender_bookings.offender_book_id%type,
                             p_offender_id           in offender_bookings.offender_id%type default null,
                             p_root_offender_id      in offender_bookings.root_offender_id%type default null,
                             p_noms_id               in offenders.offender_id_display%type default null,
                             p_first_name            in offenders.first_name%type default null,
                             p_agy_loc_id            in offender_bookings.agy_loc_id%type default null,
                             p_new_offender_id       in offender_bookings.offender_id%type default null,
                             p_living_unit_id        in offender_bookings.living_unit_id%type default null,
                             p_sex_code              in offenders.sex_code%type default null,
                             p_sex_desc              in varchar2 default null,
                             p_diet                  in offender_profile_details.profile_code%type default null,
                             p_diet_desc             in varchar2 default null,
                             p_religion              in offender_profile_details.profile_code%type default null,
                             p_religion_desc         in varchar2 default null,
                             p_sec_category          in offender_assessments.review_sup_level_type%type default null,
                             p_sec_category_desc     in varchar2 default null,
                             p_nationality           in offender_profile_details.profile_code%type default null,
                             p_nationality_desc      in varchar2 default null,
                             p_language              in offender_languages.language_code%type default null,
                             p_language_desc         in varchar2 default null,
                             p_ethnicity             in offenders.race_code%type default null,
                             p_ethnicity_desc        in varchar2 default null,
                             p_imprison_status       in offender_imprison_statuses.imprisonment_status%type default null,
                             p_imprison_status_desc  in varchar2 default null,
                             p_convicted_status      in varchar2 default null,
                             p_iep_level             in offender_iep_levels.iep_level%type default null,
                             p_iep_level_desc        in varchar2 default null,
                             p_warning_type          in offender_alerts.alert_type%type default null,
                             p_warning_type_desc     in varchar2 default null,
                             p_warning_sub_type      in offender_alerts.alert_code%type default null,
                             p_warning_sub_type_desc in varchar2 default null,
                             p_warning_date          in offender_alerts.alert_date%type default null,
                             p_expiry_date           in offender_alerts.expiry_date%type default null,
                             p_canteen_sanction      in boolean default null,
                             p_personal_officer          in varchar2 default null,
                             p_status                in offender_alerts.alert_status%type default null);

   procedure sub_account_update( p_root_offender_id      in offender_bookings.root_offender_id%type default null,
                                 p_agy_loc_id            in offender_bookings.agy_loc_id%type default null,
                                 p_account_type          in account_codes.sub_account_type%type,
                                 p_account_type_desc     in varchar2,
                                 p_balance               in offender_sub_accounts.balance%type);

   procedure post_event(p_agy_loc_id        in api_offender_events.agy_loc_id%type,
                        p_root_offender_id  in api_offender_events.root_offender_id%type,
                        p_noms_id           in api_offender_events.noms_id%type,
                        p_event_type        in api_offender_events.event_type%type,
                        p_json              in json);

   procedure noms_id_update(p_old_noms_id        in offenders.offender_id_display%type,
                            p_new_noms_id        in offenders.offender_id_display%type,
                            p_root_offender_id   in offenders.root_offender_id%type,
                            p_single_offender_id in varchar2 default null,
                            p_agy_loc_id         in agency_locations.agy_loc_id%type default null);
end pss_events;
/
show err
create or replace package body api_owner.pss_events
is
   
   -- =============================================================
   v_version   CONSTANT VARCHAR2 (60) := '1.3   31-Aug-2017';
   -- =============================================================
   /*
   MODIFICATION HISTORY
   ------------------------------------------------------------------------------
   Person      Date           Version                Comments
   ---------   -----------    ---------   ---------------------------------------
   Paul M      31-Aug-2017     1.3        Populate varchar2 columns instead of clob 
   Paul M      17-Aug-2017     1.2        Added sub_account_update
   Paul M      16-Dec-2016     1.1        Added location type
   Paul M      21-Oct-2016     1.0        Initial version
   ------------------------------------------------------------------------------
   */

   function show_version return varchar2
   is
   begin
      return (v_version);
   end show_version;

   procedure reception(p_offender_book_id in offender_bookings.offender_book_id%type,
                       p_agy_loc_id       in agency_locations.agy_loc_id%type default null,
                       p_movement_date    in offender_external_movements.movement_date%type,
                       p_movement_time    in offender_external_movements.movement_time%type)
   is
      v_root_offender_id    offenders.root_offender_id%type;
      v_noms_id             offenders.offender_id_display%type;
      v_reception_datetime  date;
      v_json json;
   begin
      v_reception_datetime := to_date(to_char(p_movement_date,'YYYYMMDD')||
                                      to_char(p_movement_time,'HH24MI'),'YYYYMMDDHH24MI');

      v_root_offender_id   := null;

      select ob.root_offender_id, o.offender_id_display
        into v_root_offender_id, v_noms_id
        from offender_bookings ob
        join offenders o
          on o.offender_id = ob.offender_id 
       where ob.offender_book_id = p_offender_book_id;

      v_json :=  api_offender_procs.pss_offender_details_json(
                                                     p_offender_book_id  => p_offender_book_id,
                                                     p_agy_loc_id        => p_agy_loc_id,
                                                     p_movement_datetime => v_reception_datetime );

      post_event(p_agy_loc_id        => p_agy_loc_id,
                 p_root_offender_id  => v_root_offender_id,
                 p_noms_id           => v_noms_id,
                 p_event_type        => 'RECEPTION',
                 p_json              => v_json);
   end reception;

   procedure discharge(p_offender_book_id in offender_bookings.offender_book_id%type,
                       p_agy_loc_id       in agency_locations.agy_loc_id%type default null,
                       p_movement_date    in offender_external_movements.movement_date%type,
                       p_movement_time    in offender_external_movements.movement_time%type,
                       p_movement_type    in offender_external_movements.movement_type%type,
                       p_movement_reason  in offender_external_movements.movement_reason_code%type)
   is
      v_single_offender_id varchar2(36);
      v_noms_id            offenders.offender_id_display%type;
      v_root_offender_id   offenders.root_offender_id%type;
      v_agy_loc_id         offender_bookings.agy_loc_id%type;

      v_json json;
      v_json_od json;
      v_json_list json_list;
   begin

      v_json := json();

      pss_data_extractor.get_offender_identity(p_offender_book_id    => p_offender_book_id,
                                               p_single_offender_id  => v_single_offender_id,
                                               p_root_offender_id    => v_root_offender_id,
                                               p_noms_id             => v_noms_id,
                                               p_agy_loc_id          => v_agy_loc_id);


      v_json.put('discharge_status', 
                 json_builder.discharge_status(
                     p_discharge_date => p_movement_time,
                     p_discharge_reason => p_movement_reason,
                     p_reason_desc => pss_data_extractor.get_movement_reason(p_movement_type, p_movement_reason)));

      post_event(p_agy_loc_id        => p_agy_loc_id,
                 p_root_offender_id  => v_root_offender_id,
                 p_noms_id           => v_noms_id,
                 p_event_type        => 'DISCHARGE',
                 p_json              => v_json);
   end discharge;

                                             
   procedure offender_update(p_offender_book_id      in offender_bookings.offender_book_id%type,
                             p_offender_id           in offender_bookings.offender_id%type default null,
                             p_root_offender_id      in offender_bookings.root_offender_id%type default null,
                             p_noms_id               in offenders.offender_id_display%type default null,
                             p_first_name            in offenders.first_name%type default null,
                             p_agy_loc_id            in offender_bookings.agy_loc_id%type default null,
                             p_new_offender_id       in offender_bookings.offender_id%type default null,
                             p_living_unit_id        in offender_bookings.living_unit_id%type default null,
                             p_sex_code              in offenders.sex_code%type default null,
                             p_sex_desc              in varchar2 default null,
                             p_diet                  in offender_profile_details.profile_code%type default null,
                             p_diet_desc             in varchar2 default null,
                             p_religion              in offender_profile_details.profile_code%type default null,
                             p_religion_desc         in varchar2 default null,
                             p_sec_category          in offender_assessments.review_sup_level_type%type default null,
                             p_sec_category_desc     in varchar2 default null,
                             p_nationality           in offender_profile_details.profile_code%type default null,
                             p_nationality_desc      in varchar2 default null,
                             p_language              in offender_languages.language_code%type default null,
                             p_language_desc         in varchar2 default null,
                             p_ethnicity             in offenders.race_code%type default null,
                             p_ethnicity_desc        in varchar2 default null,
                             p_imprison_status       in offender_imprison_statuses.imprisonment_status%type default null,
                             p_imprison_status_desc  in varchar2 default null,
                             p_convicted_status      in varchar2 default null,
                             p_iep_level             in offender_iep_levels.iep_level%type default null,
                             p_iep_level_desc        in varchar2 default null,
                             p_warning_type          in offender_alerts.alert_type%type default null,
                             p_warning_type_desc     in varchar2 default null,
                             p_warning_sub_type      in offender_alerts.alert_code%type default null,
                             p_warning_sub_type_desc in varchar2 default null,
                             p_warning_date          in offender_alerts.alert_date%type default null,
                             p_expiry_date           in offender_alerts.expiry_date%type default null,
                             p_canteen_sanction      in boolean default null,
                             p_personal_officer          in varchar2 default null,
                             p_status                in offender_alerts.alert_status%type default null)
   is
      v_single_offender_id varchar2(36);
      v_noms_id            offenders.offender_id_display%type;
      v_root_offender_id   offenders.root_offender_id%type;
      v_agy_loc_id         offender_bookings.agy_loc_id%type;
      v_first_name         offenders.first_name%type;
      v_last_name          offenders.last_name%type;
      v_birth_date         offenders.birth_date%type;
      v_sex_code           offenders.sex_code%type;
      v_internal_location  agency_internal_locations.description%type;
      v_location_type      agency_internal_locations.internal_location_type%type;
      v_event_type         api_offender_events.event_type%type;

      v_json json;
      v_json_od json;
      v_json_list json_list;
   begin
      v_json := json();

      if p_offender_id is not null then
         if p_root_offender_id is null
            or p_noms_id is null
         then
            pss_data_extractor.get_offender_identity(p_offender_id    => p_offender_id,
                                                     p_single_offender_id  => v_single_offender_id,
                                                     p_root_offender_id    => v_root_offender_id,
                                                     p_noms_id             => v_noms_id);
         else
            v_root_offender_id := p_root_offender_id;
            v_noms_id := p_noms_id;
            v_single_offender_id := null;
         end if;
      elsif p_offender_book_id is not null then 
         pss_data_extractor.get_offender_identity(p_offender_book_id    => p_offender_book_id,
                                                  p_single_offender_id  => v_single_offender_id,
                                                  p_root_offender_id    => v_root_offender_id,
                                                  p_noms_id             => v_noms_id,
                                                  p_agy_loc_id          => v_agy_loc_id);
      end if;

      --
      -- Offender Details
      --
      v_json_od := json();

      if p_new_offender_id is not null 
         or p_first_name is not null
         or p_sex_code is not null
         or p_diet is not null
         or p_religion is not null
         or p_sec_category is not null
         or p_nationality is not null
         or p_language is not null
         or p_ethnicity is not null
      then

         if p_new_offender_id is not null then  
            pss_data_extractor.get_offender_name(p_offender_id    => p_new_offender_id,
                                                 p_first_name     => v_first_name,
                                                 p_last_name      => v_last_name,
                                                 p_birth_date     => v_birth_date);
         end if;

         v_json_od.put('personal_details',
                       json_builder.personal_details(
                           p_first_name        => nvl(p_first_name,v_first_name),
                           p_last_name         => v_last_name,
                           p_birth_date        => v_birth_date,
                           p_sex_code          => p_sex_code,
                           p_sex_desc          => p_sex_desc,
                           p_diet              => p_diet,
                           p_diet_desc         => p_diet_desc,
                           p_religion          => p_religion,
                           p_religion_desc     => p_religion_desc,
                           p_sec_category      => p_sec_category,
                           p_sec_category_desc => p_sec_category_desc,
                           p_nationality       => p_nationality,
                           p_nationality_desc  => p_nationality_desc,
                           p_language          => p_language,
                           p_language_desc     => p_language_desc,
                           p_ethnicity         => p_ethnicity,
                           p_ethnicity_desc    => p_ethnicity_desc));

         v_event_type := 'PERSONAL_DETAILS_CHANGED';
      end if;

      if p_imprison_status is not null then
         
         v_json_od.put('sentence_information',
                       json_builder.sentence_information(p_convicted_status     => p_convicted_status,
                                                         p_imprisonment_status  => p_imprison_status,
                                                         p_imprison_status_desc =>  p_imprison_status_desc));

         v_event_type := 'SENTENCE_INFORMATION_CHANGED';
      end if;

      if p_living_unit_id is not null then
         pss_data_extractor.get_internal_location(p_internal_location_id => p_living_unit_id,
                                                  p_internal_location    => v_internal_location,
                                                  p_location_type        => v_location_type);

         v_json_od.put('location', 
                       json_builder.location(p_internal_location => v_internal_location,
                                             p_location_type     => v_location_type));

         v_event_type := 'INTERNAL_LOCATION_CHANGED';
      end if;
     
      if p_warning_type is not null then 
         v_json_list := json_list();
         v_json_list.append(json_builder.warnings(p_warning_type     => p_warning_type,
                                                      p_type_desc        => p_warning_type_desc,
                                                      p_warning_sub_type => p_warning_sub_type,
                                                      p_sub_type_desc    => p_warning_sub_type_desc,
                                                      p_warning_date     => p_warning_date,
                                                      p_expiry_date      => p_expiry_date,
                                                      p_status           => p_status).to_json_value);

         if v_json_list.count > 0 then
            v_json_od.put('warnings', v_json_list);
         end if;

         v_event_type := 'ALERT';
      end if;

      if p_iep_level is not null
         or p_canteen_sanction is not null
      then
         v_json_od.put('entitlement', 
                       json_builder.entitlement(p_canteen_adjudication => p_canteen_sanction,
                                                p_iep_level            => p_iep_level,
                                                p_iep_level_desc       => p_iep_level_desc));

         v_event_type := 'IEP_CHANGED';
      end if;

      if p_personal_officer is not null then
         v_json_od.put('case_details',
                     json_builder.case_details(p_personal_officer => p_personal_officer));
         v_event_type := 'PERSONAL_OFFICER_CHANGED';
      end if;
          

      v_json.put('offender_details', v_json_od);
      
      post_event(p_agy_loc_id        => nvl(p_agy_loc_id,v_agy_loc_id),
                 p_root_offender_id  => v_root_offender_id,
                 p_noms_id           => v_noms_id,
                 p_event_type        => v_event_type,
                 p_json              => v_json);
   end offender_update;

   procedure sub_account_update( p_root_offender_id      in offender_bookings.root_offender_id%type default null,
                                 p_agy_loc_id            in offender_bookings.agy_loc_id%type default null,
                                 p_account_type          in account_codes.sub_account_type%type,
                                 p_account_type_desc     in varchar2,
                                 p_balance               in offender_sub_accounts.balance%type)
   is
      v_single_offender_id varchar2(36);
      v_root_offender_id   offender_bookings.root_offender_id%type;
      v_noms_id            offenders.offender_id_display%type;
      v_event_type         api_offender_events.event_type%type;
      v_json               json;
   begin
      v_event_type := 'BALANCE_UPDATE';
      v_json := json();
      v_single_offender_id := null;

      pss_data_extractor.get_offender_identity(p_offender_id         => p_root_offender_id,
                                               p_single_offender_id  => v_single_offender_id,
                                               p_root_offender_id    => v_root_offender_id,
                                               p_noms_id             => v_noms_id);

      v_json := json_builder.account_balance(p_account_type      => p_account_type,
                                             p_account_type_desc => p_account_type_desc,
                                             p_balance           => p_balance);
      
      post_event(p_agy_loc_id        => p_agy_loc_id,
                 p_root_offender_id  => p_root_offender_id,
                 p_noms_id           => v_noms_id,
                 p_event_type        => v_event_type,
                 p_json              => v_json);

   end sub_account_update;

   procedure post_event(p_agy_loc_id        in api_offender_events.agy_loc_id%type,
                        p_root_offender_id  in api_offender_events.root_offender_id%type,
                        p_noms_id           in api_offender_events.noms_id%type,
                        p_event_type        in api_offender_events.event_type%type,
                        p_json              in json)
   is
   begin

      api_offender_event.post_event(p_agy_loc_id        => p_agy_loc_id,
                                    p_root_offender_id  => p_root_offender_id,
                                    p_noms_id           => p_noms_id,
                                    p_event_type        => p_event_type,
                                    p_event_data        => p_json.to_char(false));


   end post_event;

   procedure noms_id_update(p_old_noms_id        in offenders.offender_id_display%type,
                            p_new_noms_id        in offenders.offender_id_display%type,
                            p_root_offender_id   in offenders.root_offender_id%type,
                            p_single_offender_id in varchar2 default null,
                            p_agy_loc_id         in agency_locations.agy_loc_id%type default null)
   is
      v_json           json;
      v_change         json;
   begin

      v_json := json();
      v_change := json();
      v_change.put('old_noms_id', p_old_noms_id);
      v_change.put('new_noms_id', p_new_noms_id);
      v_json.put('noms_id_change',v_change);
       
      post_event(p_agy_loc_id        => p_agy_loc_id,
                 p_root_offender_id  => p_root_offender_id,
                 p_noms_id           => p_old_noms_id,
                 p_event_type        => 'NOMS_ID_CHANGED',
                 p_json              => v_json);
     
       
   end noms_id_update;
end pss_events;
/
show err
