create or replace package api_owner.api_booking_procs
as
   function show_version return varchar2;
   
   procedure get_offender_bookings (p_noms_id            in offenders.offender_id_display%type,
                                    p_booking_csr      out sys_refcursor);

   procedure get_latest_booking (p_noms_id           in offenders.offender_id_display%type,
                                 p_booking_csr      out sys_refcursor);

   function get_location_levels_delim(p_internal_location_id in agency_internal_locations.internal_location_id%type)
      return varchar2;

end api_booking_procs;
/
sho err
create or replace package body api_owner.api_booking_procs
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.2   14-Dec-2017';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      -------------------------------------------------------------------------------
      Person      Date           Version     Comments
      ---------   -----------    ---------   ----------------------------------------
      Paul M      14-Dec-2017     1.2        Add housing levels
      Paul M      03-Nov-2017     1.1        Add internal location to get_offender_bookings and 
                                             get_latest_booking
      Paul M      11-Jan-2017     1.0        Initial version

   */

   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;

   procedure get_offender_bookings (p_noms_id            in offenders.offender_id_display%type,
                                    p_booking_csr      out sys_refcursor)
   is
   begin
      open p_booking_csr for
           select ob.offender_book_id,
                  ob.booking_no,
                  ob.booking_begin_date,
                  ob.booking_end_date,
                  ob.active_flag,
                  ob.agy_loc_id,
                  al.description agy_loc_desc,
                  ail.description housing_location,
                  case when ob.booking_seq = 1 then 'Y'
                       else 'N' end latest_booking,
                  nvl(ord.release_date, ord.auto_release_date) rel_date,
                  case 
                     when ob.booking_seq =1 then
                        api_owner.api_booking_procs.get_location_levels_delim(ob.living_unit_id)
                     else
                        null
                  end housing_levels 
             from offenders o1
             join offender_bookings ob
               on ob.offender_id = o1.offender_id
             join agency_locations al
               on al.agy_loc_id = ob.agy_loc_id
             left join agency_internal_locations ail
               on ail.internal_location_id = ob.living_unit_id
             left join offender_release_details ord
               on ord.offender_book_id = ob.offender_book_id
            where o1.offender_id_display = p_noms_id
            order by ob.booking_seq asc;
   end get_offender_bookings;

   procedure get_latest_booking (p_noms_id           in offenders.offender_id_display%type,
                                 p_booking_csr      out sys_refcursor)
   is
   begin
      open p_booking_csr for
           select ob.offender_book_id,
                  ob.booking_no,
                  ob.booking_begin_date,
                  ob.booking_end_date,
                  ob.active_flag,
                  ob.agy_loc_id,
                  al.description agy_loc_desc,
                  'Y' latest_booking,
                  ail.description housing_location,
                  nvl(ord.release_date, ord.auto_release_date) rel_date,
                  api_owner.api_booking_procs.get_location_levels_delim(ob.living_unit_id) housing_levels 
             from offenders o1
             join offender_bookings ob
               on ob.offender_id = o1.offender_id
             join agency_locations al
               on al.agy_loc_id = ob.agy_loc_id
             left join agency_internal_locations ail
               on ail.internal_location_id = ob.living_unit_id
             left join offender_release_details ord
               on ord.offender_book_id = ob.offender_book_id
            where o1.offender_id_display = p_noms_id
              and ob.booking_seq = 1;
   end get_latest_booking;

   function get_location_levels_delim(p_internal_location_id in agency_internal_locations.internal_location_id%type)
      return varchar2
   is
      v_location_levels varchar2(1000);
   begin
      with loc_levels as 
           (select ltrim(max(sys_connect_by_path(rc.description||','||internal_location_code, '|')),'|')
              from agency_internal_locations ail
              join reference_codes rc
                on rc.code = ail.internal_location_type
                   and rc.domain = 'LIVING_UNIT'
              connect by internal_location_id = prior parent_internal_location_id 
                start with internal_location_id = p_internal_location_id)
      select ltrim(max(sys_connect_by_path(sub_path, '|')),'|')  
        into v_location_levels 
        from (select regexp_substr((select * from loc_levels),'[^|]+', 1, rownum) sub_path, rownum rn
                                      from dual  
                                      connect by level <= length(regexp_replace((select * from loc_levels), '[^|]','')) + 1)
        connect by prior rn = rn + 1
          start with rn = length(regexp_replace((select * from loc_levels), '[^|]','')) + 1;
      return v_location_levels;
   exception
      when no_data_found then
         null;
   end get_location_levels_delim;


end api_booking_procs;
/
sho err
