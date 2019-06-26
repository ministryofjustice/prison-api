create or replace package api_owner.api_alerts
as
   function show_version return varchar2;
   
   procedure get_alerts (p_noms_id          in offenders.offender_id_display%type,
                         p_include_inactive in varchar2 default 'N',
                         p_modified_since   in timestamp default null,
                         p_alerts_csr      out sys_refcursor);


end api_alerts;
/
sho err
create or replace package body api_owner.api_alerts
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.0   05-Jan-2018';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      -------------------------------------------------------------------------------
      Person      Date           Version     Comments
      ---------   -----------    ---------   ----------------------------------------
      Paul M      05-Jan-2018     1.0        Initial version

   */

   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;

   procedure get_alerts (p_noms_id          in offenders.offender_id_display%type,
                         p_include_inactive in varchar2 default 'N',
                         p_modified_since   in timestamp default null,
                         p_alerts_csr       out sys_refcursor)
   is
   begin
      nomis_api_log.debug('API_ALERTS.GET_ALERTS','p_noms_id='||p_noms_id||', p_modified_since='||to_char(p_modified_since,'YYYY-MM-DD HH24:MI:SS'));
      if p_include_inactive = 'Y' then
         open p_alerts_csr for
              select ob.root_offender_id,
                     ob.offender_book_id,
                     ob.active_flag,
                     ob.agy_loc_id,
                     oa.alert_seq,
                     oa.alert_type,
                     rc1.description alert_type_desc,
                     oa.alert_code,
                     rc2.description alert_code_desc,
                     oa.alert_date,
                     oa.expiry_date,
                     oa.alert_status,
                     oa.comment_text,
                     oa.authorize_person_text,
                     oa.caseload_id,
                     oa.verified_flag
                from offenders o
                join offender_bookings ob
                  on ob.offender_id = o.offender_id
                     and ob.booking_seq = 1
                left join offender_alerts oa
                  on oa.offender_book_id = ob.offender_book_id
                     and (p_modified_since is null 
                          or oa.audit_timestamp >= p_modified_since)
                left join reference_codes rc1
                  on rc1.code = oa.alert_type
                     and rc1.domain = 'ALERT'
                left join reference_codes rc2
                  on rc2.code = oa.alert_code
                     and rc2.domain = 'ALERT_CODE'
                     and rc2.parent_domain = rc1.domain
                     and rc2.parent_code = rc1.code
               where o.offender_id_display = p_noms_id
                 and ob.active_flag = 'Y'
               order by oa.alert_status, oa.alert_date;
      else            
         open p_alerts_csr for
              select ob.root_offender_id,
                     ob.offender_book_id,
                     ob.active_flag,
                     ob.agy_loc_id,
                     oa.alert_seq,
                     oa.alert_type,
                     rc1.description alert_type_desc,
                     oa.alert_code,
                     rc2.description alert_code_desc,
                     oa.alert_date,
                     oa.expiry_date,
                     oa.alert_status,
                     oa.comment_text,
                     oa.authorize_person_text,
                     oa.caseload_id,
                     oa.verified_flag
                from offenders o
                join offender_bookings ob
                  on ob.offender_id = o.offender_id
                     and ob.booking_seq = 1
                left join offender_alerts oa
                  on oa.offender_book_id = ob.offender_book_id
                     and oa.alert_status = 'ACTIVE'
                     and (p_modified_since is null 
                          or oa.audit_timestamp >= p_modified_since)
                left join reference_codes rc1
                  on rc1.code = oa.alert_type
                     and rc1.domain = 'ALERT'
                left join reference_codes rc2
                  on rc2.code = oa.alert_code
                     and rc2.domain = 'ALERT_CODE'
                     and rc2.parent_domain = rc1.domain
                     and rc2.parent_code = rc1.code
               where o.offender_id_display = p_noms_id
                 and ob.active_flag = 'Y'
               order by oa.alert_date;
         end if;
   
   end get_alerts;


end api_alerts;
/
sho err
