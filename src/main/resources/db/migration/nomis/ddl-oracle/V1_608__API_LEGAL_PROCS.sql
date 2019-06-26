create or replace package api_owner.api_legal_procs
as
   function show_version return varchar2;
   
   procedure get_booking_cases (p_offender_book_id  in offender_cases.offender_book_id%type,
                                p_cases_csr      out sys_refcursor);

   procedure get_case_charges (p_case_id         in offender_cases.case_id%type,
                               p_charges_csr    out sys_refcursor);
end api_legal_procs;
/
sho err
create or replace package body api_owner.api_legal_procs
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.1   24-Jan-2017';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      -------------------------------------------------------------------------------
      Person      Date           Version     Comments
      ---------   -----------    ---------   ----------------------------------------
      Paul M      24-Jan-2017     1.1        Added imprison_status_mappings.active_flag
                                             to get_case_charges cursor.
      Paul M      11-Jan-2017     1.0        Initial version

   */

   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;

   procedure get_booking_cases (p_offender_book_id  in offender_cases.offender_book_id%type,
                                p_cases_csr      out sys_refcursor)
   is
   begin
        open p_cases_csr for
             select ocs.case_id,
                    ocs.case_info_number,
                    ocs.begin_date,
                    ocs.case_status,
                    ocs.agy_loc_id court_code,
                    al.description court_desc,
                    ocs.case_type case_type_code,
                    rc.description case_type_desc
               from offender_cases ocs
               join agency_locations al
                 on al.agy_loc_id = ocs.agy_loc_id
               left join reference_codes rc
                 on rc.code = ocs.case_type
                    and rc.domain = 'LEG_CASE_TYP'    
              where ocs.offender_book_id = p_offender_book_id
              order by ocs.case_status asc, ocs.begin_date desc;
   end get_booking_cases;

   procedure get_case_charges (p_case_id         in offender_cases.case_id%type,
                               p_charges_csr    out sys_refcursor)
   is
   begin
        open p_charges_csr for
             select och.offender_charge_id,
                    och.statute_code,
                    och.offence_code,
                    och.no_of_offences,
                    och.most_serious_flag,
                    och.charge_status,                     
                    offs.severity_ranking,
                    offs.description offence_desc,
                    s.description statute_desc,
                    orc.result_code,
                    orc.description result_desc,
                    orc.disposition_code, 
                    r1.description disposition_desc,
                    orc.conviction_flag,
                    ist.imprisonment_status,
                    ist.description imprisonment_status_desc,
                    ist.band_code,
                    r2.description band_desc
               from offender_charges och
               join offences offs
                 on offs.offence_code = och.offence_code
                    and offs.statute_code = och.statute_code
               join statutes s
                 on s.statute_code = offs.statute_code
               left join  offence_result_codes orc
                 on och.result_code_1 = orc.result_code
               left join reference_codes r1
                 on r1.code = orc.disposition_code
                    and r1.domain = 'OFF_RESULT'
               left join  imprison_status_mappings ism
                 on  ism.offence_result_code = orc.result_code
                    and ism.active_flag = 'Y'
               left join  imprisonment_statuses ist
                 on ist.imprisonment_status_id = ism.imprisonment_status_id
               left join reference_codes r2
                 on r2.code = ist.band_code
                    and r2.domain = 'IMPSBAND'
              where och.case_id = p_case_id
              order by och.charge_status asc, och.most_serious_flag desc,offs.severity_ranking;
   end get_case_charges;
end api_legal_procs;
/
sho err
