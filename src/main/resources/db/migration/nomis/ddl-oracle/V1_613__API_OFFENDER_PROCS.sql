create or replace package api_owner.api_offender_procs
as
   function show_version return varchar2;
   
   procedure pss_offender_details(p_noms_id             in out offenders.offender_id_display%type,
                                  p_root_offender_id    in out offenders.root_offender_id%type,
                                  p_single_offender_id  in out varchar2,
                                  p_agy_loc_id          in out agency_locations.agy_loc_id%type,
                                  p_details_clob       out clob,
                                  p_timestamp          out timestamp);
   
   function pss_offender_details_json(p_offender_book_id in offender_bookings.offender_book_id%type,
                                      p_agy_loc_id       in agency_locations.agy_loc_id%type default null,
                                      p_movement_datetime in date default null)
      return json;

   procedure get_offender_image (p_noms_id      in offenders.offender_id_display%type default null,
                                 p_image       out blob);

   procedure get_offender_details (p_noms_id       in     varchar2,
                                   p_offender_csr  out sys_refcursor);

   procedure get_csr_status ( p_offender_book_id   in offender_bookings.offender_book_id%type,
                              p_csra_code         out reference_codes.code%type,
                              p_csra_description  out reference_codes.description%type);

   procedure get_categorisation ( p_offender_book_id    in offender_bookings.offender_book_id%type,
                                  p_cat_level          out offender_assessments.review_sup_level_type%type,
                                  p_cat_level_desc     out reference_codes.description%type);

end api_offender_procs;
/
sho err
create or replace package body api_owner.api_offender_procs
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.9   16-Apr-2018';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      -------------------------------------------------------------------------------
      Person      Date           Version     Comments
      ---------   -----------    ---------   ----------------------------------------
      Paul M      16-Apr-2018     1.9        SDU-125 Add Security Category to Offender Details
                                             and correctly align version with PVCS
      Paul M      12-Jan-2017     1.7        Align version with PVCS
      Paul M      19-Oct-2017     1.5        Add sentence status, diet and IEP level to get_offender_details
      Paul M      18-Jul-2017     1.4        Add religion and ethnicity to get_offender_details cursor
      Paul M      20-Jan-2017     1.3        Performance changes following P and S testing
      Paul M      10-Jan-2017     1.2        Added get_offender_details and get_offender_image
                                              from api_core_procs
      Paul M      16-Dec-2016     1.1        Added location type to location object returned
                                             by pss_offender_details.
                                             Added procedures get_offender_images and get_offender_details
      Paul M      15-Nov-2016     1.0        Initial version

   */

   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;

   procedure pss_offender_details(p_noms_id             in out offenders.offender_id_display%type,
                                  p_root_offender_id    in out offenders.root_offender_id%type,
                                  p_single_offender_id  in out varchar2,
                                  p_agy_loc_id          in out agency_locations.agy_loc_id%type,
                                  p_details_clob       out clob,
                                  p_timestamp          out timestamp)
   is
      v_offender_book_id  offender_bookings.offender_book_id%type;
      v_reception_datetime     date;
      v_json json;
      v_json_clob clob;
   begin
      p_timestamp := systimestamp;
      -- 
      -- validate offender identification criteria.
      -- Until the implementation of the siingle offender_id, only
      -- the Noms Id and/or root_offender_id will be populated, with
      -- the root_offender_id taking precedence 
      --

      core_utils.get_offender_ids(p_root_offender_id => p_root_offender_id,
                       p_noms_id          => p_noms_id,
                       p_agy_loc_id       => p_agy_loc_id,
                       p_offender_book_id => v_offender_book_id);
    
      v_reception_datetime := core_utils.get_reception_datetime(
                                    p_offender_book_id => v_offender_book_id,
                                    p_agy_loc_id       => p_agy_loc_id);

      v_json :=  pss_offender_details_json(p_offender_book_id => v_offender_book_id,
                                           p_movement_datetime => v_reception_datetime );

      dbms_lob.createtemporary(v_json_clob, true);
      v_json.to_clob(v_json_clob, true);
      p_details_clob := v_json_clob;

   end pss_offender_details;

   function pss_offender_details_json(p_offender_book_id in offender_bookings.offender_book_id%type,
                                      p_agy_loc_id       in agency_locations.agy_loc_id%type default null,
                                      p_movement_datetime in date default null)
      return json
   is
      v_single_offender_id varchar2(36);
      v_noms_id            offenders.offender_id_display%type;
      v_root_offender_id   offenders.root_offender_id%type;
      v_agy_loc_id         offender_bookings.agy_loc_id%type;
      v_first_name         offenders.first_name%type;
      v_last_name          offenders.last_name%type;
      v_birth_date         offenders.birth_date%type;
      v_sex_code           offenders.sex_code%type;
      v_sex_desc           reference_codes.description%type;
      v_diet               offender_profile_details.profile_code%type;
      v_diet_desc          profile_codes.description%type;
      v_religion           offender_profile_details.profile_code%type;
      v_religion_desc      profile_codes.description%type;
      v_sec_category       offender_assessments.review_sup_level_type%type;
      v_sec_category_desc  reference_codes.description%type;
      v_nationality        offender_profile_details.profile_code%type;
      v_nationality_desc   profile_codes.description%type;
      v_language           offender_languages.language_code%type;
      v_language_desc      reference_codes.description%type;
      v_ethnicity          offenders.race_code%type;
      v_ethnicity_desc     reference_codes.description%type;
      v_internal_location  agency_internal_locations.description%type;
      v_location_type      agency_internal_locations.internal_location_type%type;
      v_convicted_status   varchar2(12);
      v_imprisonment_status imprisonment_statuses.description%type;
      v_imprison_status_desc imprisonment_statuses.description%type;
      v_iep_level          offender_iep_levels.iep_level%type;
      v_iep_level_desc     reference_codes.description%type;
      v_po_staff_id           staff_members.staff_id%type;
      v_po_first_name         staff_members.first_name%type;
      v_po_last_name          staff_members.last_name%type;
      v_po_display_name       varchar2(100);

      v_json json;
      v_json_od json;
      v_json_list json_list;
   begin

      v_json := json();

      pss_data_extractor.get_offender_details(p_offender_book_id    => p_offender_book_id,
                                              p_single_offender_id  => v_single_offender_id,
                                              p_noms_id             => v_noms_id,
                                              p_root_offender_id    => v_root_offender_id,
                                              p_agy_loc_id          => v_agy_loc_id,
                                              p_first_name          => v_first_name,
                                              p_last_name           => v_last_name,
                                              p_birth_date          => v_birth_date,
                                              p_sex_code            => v_sex_code,
                                              p_sex_desc            => v_sex_desc,
                                              p_diet                => v_diet,
                                              p_diet_desc           => v_diet_desc,
                                              p_religion            => v_religion,
                                              p_religion_desc       => v_religion_desc,
                                              p_sec_category        => v_sec_category,
                                              p_sec_category_desc   => v_sec_category_desc,
                                              p_nationality         => v_nationality,
                                              p_nationality_desc    => v_nationality_desc,
                                              p_language            => v_language,
                                              p_language_desc       => v_language_desc,
                                              p_ethnicity           => v_ethnicity,
                                              p_ethnicity_desc      => v_ethnicity_desc,
                                              p_internal_location   => v_internal_location,
                                              p_location_type       => v_location_type,
                                              p_convicted_status    => v_convicted_status,
                                              p_imprisonment_status => v_imprisonment_status,
                                              p_imprison_status_desc => v_imprison_status_desc);

      if p_agy_loc_id is not null then
         v_agy_loc_id := p_agy_loc_id;
      end if;

      --
      -- Offender Details
      --
      v_json_od := json();

      v_json_od.put('personal_details',
                    json_builder.personal_details(p_first_name        => v_first_name,
                                                      p_last_name         => v_last_name,
                                                      p_birth_date        => v_birth_date,
                                                      p_sex_code          => v_sex_code,
                                                      p_sex_desc          => v_sex_desc,
                                                      p_diet              => v_diet ,
                                                      p_diet_desc         => v_diet_desc ,
                                                      p_religion          => v_religion,
                                                      p_religion_desc     => v_religion_desc,
                                                      p_sec_category      => v_sec_category,
                                                      p_sec_category_desc => v_sec_category_desc,
                                                      p_nationality       => v_nationality,
                                                      p_nationality_desc  => v_nationality_desc,
                                                      p_language          => v_language,
                                                      p_language_desc     => v_language_desc,
                                                      p_ethnicity         => v_ethnicity,
                                                      p_ethnicity_desc    => v_ethnicity_desc));
      v_json_od.put('sentence_information',
                    json_builder.sentence_information(p_reception_date => p_movement_datetime,
                                                          p_convicted_status => v_convicted_status ,
                                                          p_imprisonment_status => v_imprisonment_status,
                                                          p_imprison_status_desc => v_imprison_status_desc ));

      v_json_od.put('location', 
                    json_builder.location(p_agency_location   => NVL(p_agy_loc_id, v_agy_loc_id),
                                          p_internal_location => v_internal_location,
                                          p_location_type     => v_location_type));

      v_json_list := json_list();
      for alert_rec in (select oa.alert_type,rc1.description type_desc,
                               oa.alert_code,rc2.description sub_type_desc,
                               oa.alert_date,oa.expiry_date,oa.alert_status
                          from offender_alerts oa
                          left join reference_codes rc1
                            on rc1.code = oa.alert_type
                               and rc1.domain = 'ALERT'
                          left join reference_codes rc2
                            on rc2.code = oa.alert_code
                               and rc2.parent_domain = rc1.domain
                               and rc2.parent_code = rc1.code
                         where offender_book_id = p_offender_book_id
                           and alert_status = 'ACTIVE')
      loop
         v_json_list.append(json_builder.warnings(p_warning_type     => alert_rec.alert_type,
                                                      p_type_desc        => alert_rec.type_desc,
                                                      p_warning_sub_type => alert_rec.alert_code,
                                                      p_sub_type_desc    => alert_rec.sub_type_desc,
                                                      p_warning_date     => alert_rec.alert_date,
                                                      p_expiry_date      => alert_rec.expiry_date,
                                                      p_status           => alert_rec.alert_status).to_json_value);
      end loop;

      if v_json_list.count > 0 then
         v_json_od.put('warnings', v_json_list);
      end if;

      pss_data_extractor.get_iep_level(p_offender_book_id => p_offender_book_id,
                                       p_iep_level        => v_iep_level,
                                       p_iep_level_desc   => v_iep_level_desc);

      v_json_od.put('entitlement', 
                    json_builder.entitlement(
                        p_canteen_adjudication => pss_data_extractor.has_canteen_sanction(p_offender_book_id),
                        p_iep_level            => v_iep_level,
                        p_iep_level_desc       => v_iep_level_desc));

      --
      -- Case Details
      --
      pss_data_extractor.get_personal_officer(p_offender_book_id  => p_offender_book_id, 
                                              p_staff_id         => v_po_staff_id,
                                              p_first_name       => v_po_first_name,
                                              p_last_name        => v_po_last_name,
                                              p_display_name     => v_po_display_name);
      if  v_po_display_name is not null then
         v_json_od.put('case_details',json_builder.case_details(p_personal_officer => v_po_display_name));
      end if;

      v_json.put('offender_details', v_json_od);

      return v_json;
   end pss_offender_details_json;

   procedure get_offender_image (p_noms_id      in offenders.offender_id_display%type default null,
                                 p_image       out blob)
   is
   begin
      select full_size_image
        into p_image
        from ( select full_size_image
                 from offenders o
                 join offender_bookings ob
                   on o.offender_id = ob.offender_id
                 left join offender_images oi
                   on ob.offender_book_id = oi.offender_book_id
                      and oi.image_object_type = 'OFF_BKG'
                      and oi.image_view_type = 'FACE'
                      and oi.active_flag = 'Y'
                where o.offender_id_display = p_noms_id
                  and ob.booking_seq = 1
                order by oi.create_datetime desc)
       where rownum = 1;
   exception
      when no_data_found then
         raise_application_error(-20001,'Offender Not Found');
   end get_offender_image;

   procedure get_offender_details (p_noms_id       in     varchar2,
                                   p_offender_csr  out sys_refcursor)
   is
      v_root_offender_id  offenders.root_offender_id%type;
      v_noms_id           offenders.offender_id_display%type;
      v_agy_loc_id        offender_bookings.agy_loc_id%type;
      v_offender_book_id  offender_bookings.offender_book_id%type;
      v_iep_level         offender_iep_levels.iep_level%type;
      v_iep_level_desc    reference_codes.description%type;
      v_csra_code         reference_codes.code%type;
      v_csra_description  reference_codes.description%type;
      v_cat_level         offender_assessments.review_sup_level_type%type;
      v_cat_level_desc    reference_codes.description%type;

   begin

      v_noms_id := p_noms_id;

      core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                                  p_noms_id          => v_noms_id,
                                  p_agy_loc_id       => v_agy_loc_id,
                                  p_offender_book_id => v_offender_book_id);
            
      pss_data_extractor.get_iep_level(p_offender_book_id => v_offender_book_id,
                                       p_iep_level        => v_iep_level,
                                       p_iep_level_desc   => v_iep_level_desc);

      get_csr_status ( p_offender_book_id  => v_offender_book_id,
                       p_csra_code         => v_csra_code,
                       p_csra_description  => v_csra_description );

      get_categorisation(p_offender_book_id  => v_offender_book_id,
                         p_cat_level         => v_cat_level,
                         p_cat_level_desc    => v_cat_level_desc );

      open p_offender_csr for
          select  o1.first_name,
                  o1.middle_name||
                     case when middle_name_2 is not null then ' '|| o1.middle_name_2 
                          else null end middle_names,
                  o1.last_name,
                  o1.birth_date,
                  o1.sex_code,
                  rc.description sex_desc,
                  o1.title,
                  o1.suffix,
                  (select oi1.identifier
                     from offender_identifiers oi1
                    where oi1.offender_id = ob.offender_id
                      and oi1.identifier_type = 'PNC'
                      and rownum = 1) pnc_number,
                  (select oi2.identifier
                     from offender_identifiers oi2
                    where oi2.offender_id = ob.offender_id
                      and oi2.identifier_type = 'CRO'
                      and rownum = 1) cro_number,
                  ois.imprisonment_status,
                  ist.description imprisonment_status_desc,
                  case 
                    when ist.band_code <= 8 then 'Convicted'
                    when ist.band_code > 8 then 'Remand'
                    else null
                  end convicted_status,
                  cursor (select o2.first_name,
                          o2.middle_name||
                             case when o2.middle_name_2 is not null then ' '|| o2.middle_name_2 
                                  else null end middle_names,
                          o2.last_name,
                          o2.birth_date
                     from offenders o2
                    where o2.root_offender_id = o1.root_offender_id
                      and o2.offender_id != o1.offender_id) aliases,
                  case 
                    when opd2.profile_code is not null then opd2.profile_code
                    else pc.description 
                  end nationalities,
                  opd3.profile_code religion_code,
                  pc3.description religion_desc,
                  opd4.profile_code diet_code,
                  pc4.description diet_desc,
                  o1.race_code ethnicity_code,
                  rc1.description ethnicity_desc,
                  v_iep_level iep_level,
                  v_iep_level_desc iep_level_desc,
                  ol.language_code spoken_language_code,
                  rc2.description spoken_language_desc,
                  ol.interpreter_requested_flag,
                  v_csra_code csra_code,
                  v_csra_description csra_description,
                  v_cat_level  cat_level,
                  v_cat_level_desc cat_level_desc
             from offender_bookings ob
             join offenders o1
               on o1.offender_id = ob.offender_id
             left join reference_codes rc
               on rc.code = o1.sex_code 
                  and rc.domain = 'SEX'
             left join offender_imprison_statuses ois
               on ois.offender_book_id = ob.offender_book_id
                  and ois.latest_status = 'Y'
             left join imprisonment_statuses ist
               on ist.imprisonment_status = ois.imprisonment_status    
             left join offender_profile_details opd1
               on opd1.offender_book_id = ob.offender_book_id
                  and opd1.profile_seq = 1
                  and opd1.profile_type = 'NAT'
             left join profile_codes pc
               on pc.profile_type = opd1.profile_type
                  and pc.profile_code = opd1.profile_code
             left join offender_profile_details opd2
               on opd2.offender_book_id = ob.offender_book_id
                  and opd2.profile_seq = 1
                  and opd2.profile_type = 'NATIO'                    
             left join offender_profile_details opd3
               on opd3.offender_book_id = ob.offender_book_id
                  and opd3.profile_seq = 1
                  and opd3.profile_type = 'RELF'
             left join profile_codes pc3
               on pc3.profile_type = opd3.profile_type
                  and pc3.profile_code = opd3.profile_code
             left join offender_profile_details opd4
               on opd4.offender_book_id = ob.offender_book_id
                  and opd4.profile_seq = 1
                  and opd4.profile_type = 'DIET'
             left join profile_codes pc4
               on pc4.profile_type = opd4.profile_type
                  and pc4.profile_code = opd4.profile_code
             left join reference_codes rc1
               on rc1.code = o1.race_code
                  and rc1.domain = 'ETHNICITY'
             left join offender_languages ol
               on ol.offender_book_id = ob.offender_book_id
                  and ol.language_type = 'PREF_SPEAK'
             left join reference_codes rc2
               on rc2.code = ol.language_code
                  and rc2.domain = 'LANG'
            where ob.offender_book_id = v_offender_book_id;
   end get_offender_details;


   procedure get_csr_status ( p_offender_book_id   in offender_bookings.offender_book_id%type,
                              p_csra_code         out reference_codes.code%type,
                              p_csra_description  out reference_codes.description%type)
   is
      k_caseload_type       constant varchar2(12) := 'INST';

   begin
      
      begin
        select csr_status, rc.description
          into p_csra_code, p_csra_description
          from (select case
                          when decode (off_ass.review_sup_level_type,
                                       null, decode (off_ass.overrided_sup_level_type,
                                                     null, off_ass.calc_sup_level_type,
                                                     off_ass.overrided_sup_level_type
                                                    ),
                                       off_ass.review_sup_level_type
                                      ) = 'PEND'
                             then null
                          else decode (off_ass.review_sup_level_type,
                                       null, decode (off_ass.overrided_sup_level_type,
                                                     null, off_ass.calc_sup_level_type,
                                                     off_ass.overrided_sup_level_type
                                                    ),
                                       off_ass.review_sup_level_type
                                      )
                       end csr_status,
                       off_ass.assessment_date,
                       off_ass.assessment_seq,
                       max (off_ass.assessment_date) over (partition by off_ass.offender_book_id) max_date,
                       max (off_ass.assessment_seq) over (partition by off_ass.offender_book_id, off_ass.assessment_date)
                                                                                                                        max_seq
                  from offender_assessments off_ass 
                  join assessments ass 
                    on off_ass.assessment_type_id = ass.assessment_id
                 where off_ass.offender_book_id = p_offender_book_id
                   and ass.caseload_type = k_caseload_type
                   and ass.cell_sharing_alert_flag = 'Y'
                   and off_ass.assess_status = 'A') vass
          left join reference_codes rc
            on rc.code = vass.csr_status
               and rc.domain = 'SUP_LVL_TYPE'
         where vass.assessment_date = vass.max_date
           and vass.assessment_seq = vass.max_seq;
      exception
         when no_data_found then
            p_csra_code := null;
            p_csra_description := null;
         when too_many_rows THEN   
            p_csra_code := null;
            p_csra_description := null;
      end;

   end get_csr_status;

   --
   -- Derived from tag_header.get_offender_assessment 
   --
   procedure get_categorisation ( p_offender_book_id    in offender_bookings.offender_book_id%type,
                                  p_cat_level          out offender_assessments.review_sup_level_type%type,
                                  p_cat_level_desc     out reference_codes.description%type)
   is

   begin

      begin
         select vass.review_sup_level_type,
                rc.description
           into p_cat_level,
                p_cat_level_desc
           from ( select off_ass.offender_book_id,
                         off_ass.review_sup_level_type,
                         off_ass.assessment_date,
                         off_ass.assessment_seq,
                         max (off_ass.assessment_date) over (partition by off_ass.offender_book_id) max_date,
                         max (off_ass.assessment_seq) over (partition by off_ass.offender_book_id,off_ass.assessment_date) max_seq
                    from offender_assessments off_ass
                    join assessments ass
                     on off_ass.assessment_type_id = ass.assessment_id
                   where off_ass.offender_book_id = p_offender_book_id
                     and ass.caseload_type = 'INST'
                     and ass.determine_sup_level_flag = 'Y'
                     and off_ass.evaluation_result_code = 'APP'
                     and off_ass.assess_status = 'A'
                ) vass
           left outer join reference_codes rc
             on rc.code = vass.review_sup_level_type
                and rc.domain = 'SUP_LVL_TYPE'
          where vass.assessment_date = vass.max_date
            and vass.assessment_seq = vass.max_seq;

      exception
         when no_data_found then
            begin
               select profile_value, profile_Value_2
                 into p_cat_level, p_cat_level_desc 
                 from system_profiles
                where profile_type = 'CLIENT'
                  and profile_code = 'PENDING_STAT';
            exception
               when no_data_found then
                  p_cat_level := 'Z';
                  p_cat_level_desc := oms_miscellaneous.getdesccode('SUP_LVL_TYPE', p_cat_level);
         end;
      end;

   end get_categorisation;
end api_offender_procs;
/
sho err
