create or replace package api_owner.core_utils
is

   function show_version return varchar2;

   procedure get_offender_details(p_noms_number         in offenders.offender_id_display%type,
                                  p_birth_date          in offenders.birth_date%type,
                                  p_offender_id        out offenders.offender_id%type,
                                  p_root_offender_id   out offenders.root_offender_id%type,
                                  p_offender_book_id   out offender_bookings.offender_book_id%type,
                                  p_agy_loc_id         out offender_bookings.agy_loc_id%type,
                                  p_first_name         out offenders.first_name%type,
                                  p_last_name          out offenders.last_name%type,
                                  p_active_flag        out offender_bookings.active_flag%type,
                                  p_booking_status     out offender_bookings.booking_status%type);

   procedure get_offender_details(p_root_offender_id    in offenders.root_offender_id%type,
                                  p_offender_book_id   out offender_bookings.offender_book_id%type,
                                  p_agy_loc_id         out offender_bookings.agy_loc_id%type,
                                  p_first_name         out offenders.first_name%type,
                                  p_last_name          out offenders.last_name%type,
                                  p_middle_name        out offenders.middle_name%type,
                                  p_middle_name_2      out offenders.middle_name_2%type,
                                  p_birth_date         out offenders.birth_date%type,
                                  p_active_flag        out offender_bookings.active_flag%type,
                                  p_booking_status     out offender_bookings.booking_status%type);
   
   procedure get_offender_details(p_root_offender_id    in offenders.root_offender_id%type,
                                  p_offender_book_id   out offender_bookings.offender_book_id%type,
                                  p_agy_loc_id         out offender_bookings.agy_loc_id%type,
                                  p_first_name         out offenders.first_name%type,
                                  p_last_name          out offenders.last_name%type,
                                  p_active_flag        out offender_bookings.active_flag%type,
                                  p_booking_status     out offender_bookings.booking_status%type);

   function is_offender_convicted (p_offender_book_id   in offender_bookings.offender_book_id%type) 
      return boolean;

	function is_digital_prison ( p_agy_loc_id in agency_locations.agy_loc_id%type) return boolean;

	function is_in_digital_prison ( p_offender_book_id in offender_bookings.offender_book_id%type) return boolean;

   procedure get_offender_ids(p_root_offender_id in out offenders.root_offender_id%type,
                             p_noms_id          in out offenders.offender_id_display%type,
                             p_agy_loc_id       in out offender_bookings.agy_loc_id%type,
                             p_offender_book_id in out offender_bookings.offender_book_id%type);

   function get_reception_datetime(p_offender_book_id in offender_bookings.offender_book_id%type, 
                                   p_agy_loc_id       in out offender_bookings.agy_loc_id%type)
      return date;

   function trust_account_exists(p_root_offender_id in offender_trust_accounts.offender_id%type,
                                 p_caseload_id      in offender_trust_accounts.caseload_id%type)
      return boolean;

   function prison_exists(p_agy_loc_id in agency_locations.agy_loc_id%type) return boolean;

   function is_reference_code_valid(p_domain reference_codes.domain%type, p_code reference_codes.code%type) return boolean;

   procedure get_account_code_type(p_account_code      in account_codes.account_code%type,
                                   p_sub_account_type out account_codes.sub_account_type%type,
                                   p_account_name     out account_codes.account_name%type);
                  

end core_utils;
/
sho err
create or replace package body api_owner.core_utils
as
   -- =============================================================
   v_version   CONSTANT VARCHAR2 (60) := '1.6   17-Aug-2017';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      ------------------------------------------------------------------------------------------
      Person      Date           Version                Comments
      ---------   -----------    ---------   ---------------------------------------------------
      Paul M      17-Aug-2017     1.6        Added procedure get_account_code_type
      Paul M      30-Jun-2017     1.5        Added function is_reference_code_valid
      Paul M      08-Mar-2017     1.4        Create another overloaded procedure for 
                                             get_offender_details
      Paul M      16-Feb-2017     1.3        Changed is_offender_convicted to use imprisonment_statuses
                                             band_code
      Paul M      03-Nov-2016     1.2        Added get_offender_ids and trust_account_exists
      Paul M      27-Oct-2016     1.1        Added functionality for PSS
      Paul M      14-Oct-2016     1.0        Initial version for PVB 

   */
   -- ==============================================================================
   -- Constants
   -- ==============================================================================

   -- ==============================================================================
   -- globals
   -- ==============================================================================
   g_debug boolean := false;

	resource_busy exception;
	pragma exception_init(resource_busy, -54);

   -- ==============================================================================
   -- Forward Declarations
   -- ==============================================================================
   
  
   -- ==============================================================================
   -- Implementations
   -- ==============================================================================
   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;
   -- ==============================================================================
   procedure get_offender_details(p_noms_number         in offenders.offender_id_display%type,
                                  p_birth_date          in offenders.birth_date%type,
                                  p_offender_id        out offenders.offender_id%type,
                                  p_root_offender_id   out offenders.root_offender_id%type,
                                  p_offender_book_id   out offender_bookings.offender_book_id%type,
                                  p_agy_loc_id         out offender_bookings.agy_loc_id%type,
                                  p_first_name         out offenders.first_name%type,
                                  p_last_name          out offenders.last_name%type,
                                  p_active_flag        out offender_bookings.active_flag%type,
                                  p_booking_status     out offender_bookings.booking_status%type)
   is
   begin
      select ob.offender_id,
             ob.root_offender_id,
             ob.offender_book_id,
             ob.agy_loc_id,
             o.first_name,
             o.last_name,
             ob.active_flag,
             ob.booking_status
        into p_offender_id,
             p_root_offender_id,
             p_offender_book_id,
             p_agy_loc_id,
             p_first_name,
             p_last_name,
             p_active_flag,
             p_booking_status
        from offenders o
        join offender_bookings ob
          on ob.offender_id = o.offender_id
             and ob.booking_seq = 1
       where o.offender_id_display = upper(p_noms_number)
         and exists (select null
                       from offenders o2 
                      where o2.root_offender_id = o.root_offender_id
                        and o2.birth_date = p_birth_date);
    exception
      when no_data_found then
         -- we return null values in output parameters
         -- so no action required here
         null;
        
   end get_offender_details;

   -- ==============================================================================
   procedure get_offender_details(p_root_offender_id    in offenders.root_offender_id%type,
                                  p_offender_book_id   out offender_bookings.offender_book_id%type,
                                  p_agy_loc_id         out offender_bookings.agy_loc_id%type,
                                  p_first_name         out offenders.first_name%type,
                                  p_last_name          out offenders.last_name%type,
                                  p_middle_name        out offenders.middle_name%type,
                                  p_middle_name_2      out offenders.middle_name_2%type,
                                  p_birth_date         out offenders.birth_date%type,
                                  p_active_flag        out offender_bookings.active_flag%type,
                                  p_booking_status     out offender_bookings.booking_status%type)
   is
   begin
      select ob.offender_book_id,
             ob.agy_loc_id,
             o.first_name,
             o.last_name,
             o.middle_name,
             o.middle_name_2,
             o.birth_date,
             ob.active_flag,
             ob.booking_status
        into p_offender_book_id,
             p_agy_loc_id,
             p_first_name,
             p_last_name,
             p_middle_name,
             p_middle_name_2,
             p_birth_date,
             p_active_flag,
             p_booking_status
        from offenders o
        join offender_bookings ob
          on ob.offender_id = o.offender_id
             and ob.booking_seq = 1
       where o.root_offender_id = p_root_offender_id;
    exception
      when no_data_found then
         -- we return null values in output parameters
         -- so no action required here
         null;
        
   end get_offender_details;

   -- ==============================================================================
   procedure get_offender_details(p_root_offender_id    in offenders.root_offender_id%type,
                                  p_offender_book_id   out offender_bookings.offender_book_id%type,
                                  p_agy_loc_id         out offender_bookings.agy_loc_id%type,
                                  p_first_name         out offenders.first_name%type,
                                  p_last_name          out offenders.last_name%type,
                                  p_active_flag        out offender_bookings.active_flag%type,
                                  p_booking_status     out offender_bookings.booking_status%type)
   is
      v_middle_name        offenders.middle_name%type;
      v_middle_name_2      offenders.middle_name_2%type;
      v_birth_date         offenders.birth_date%type;
   begin
      get_offender_details(p_root_offender_id   => p_root_offender_id,
                           p_offender_book_id   => p_offender_book_id,
                           p_agy_loc_id         => p_agy_loc_id,
                           p_first_name         => p_first_name,
                           p_last_name          => p_last_name,
                           p_middle_name        => v_middle_name,
                           p_middle_name_2      => v_middle_name_2,
                           p_birth_date         => v_birth_date,
                           p_active_flag        => p_active_flag,
                           p_booking_status     => p_booking_status);
        
   end get_offender_details;

   -- ==============================================================================
   --
   -- Uses the imprisonment_statuses band_code to determine whether convicted
   --
   --
   function is_offender_convicted (p_offender_book_id   in offender_bookings.offender_book_id%type) 
      return boolean
   is
      v_band_code    number;
   begin
      select to_number(ist.band_code)
        into v_band_code
        from offender_imprison_statuses ois
        join imprisonment_statuses ist
          on ist.imprisonment_status = ois.imprisonment_status
             and ist.active_flag = 'Y'
       where ois.offender_book_id = p_offender_book_id
         and ois.latest_status = 'Y';

      return (v_band_code <= 8);

   exception
      when no_data_found then
         return false;
   end is_offender_convicted;

   --
   -- Check whether the prison is a digital prison
   --
	function is_digital_prison ( p_agy_loc_id in agency_locations.agy_loc_id%type) return boolean
	is
		l_dummy varchar2(1);
	begin
		select 'x'
		  into l_dummy
		  from agy_loc_establishments ale
		 where ale.agy_loc_id = p_agy_loc_id
		   and ale.establishment_type = 'DIG';

		return true;
	exception
		when no_data_found then
			return false;
	end is_digital_prison;

   --
   -- Check whether the prisoner is in a digital prison
   --
	function is_in_digital_prison ( p_offender_book_id in offender_bookings.offender_book_id%type) return boolean
	is
		l_dummy varchar2(1);
	begin
		select 'x'
		  into l_dummy
		  from offender_bookings ob
        join agy_loc_establishments ale
		    on ale.agy_loc_id = ob.agy_loc_id
		       and ale.establishment_type = 'DIG'
       where ob.offender_book_id = p_offender_book_id;

		return true;
	exception
		when no_data_found then
			return false;
	end is_in_digital_prison;

   procedure get_offender_ids(p_root_offender_id in out offenders.root_offender_id%type,
                             p_noms_id          in out offenders.offender_id_display%type,
                             p_agy_loc_id       in out offender_bookings.agy_loc_id%type,
                             p_offender_book_id in out offender_bookings.offender_book_id%type)
   is
      v_root_offender_id       offenders.offender_id%type;
      v_noms_id                offenders.offender_id_display%type;
      v_agy_loc_id             offender_bookings.agy_loc_id%type;
   begin 
      if p_root_offender_id is not null then 
         select o.offender_id_display, ob.offender_book_id, ob.agy_loc_id
           into v_noms_id, p_offender_book_id, v_agy_loc_id
           from offenders o
           join offender_bookings ob
             on ob.offender_id = o.offender_id
                and ob.booking_seq = 1
          where o.root_offender_id = p_root_offender_id;

         if p_noms_id is not null and upper(p_noms_id) != v_noms_id then
            raise_application_error(-20002,'Offender Identifier inconsistancy');
         end if;
         p_noms_id := v_noms_id;
      elsif p_noms_id is not null then 
         select ob.root_offender_id, ob.offender_book_id, ob.agy_loc_id
           into v_root_offender_id, p_offender_book_id, v_agy_loc_id
           from offenders o
           join offender_bookings ob
             on ob.offender_id = o.offender_id
                and ob.booking_seq = 1
          where o.offender_id_display = upper(p_noms_id);

         if p_root_offender_id is not null and p_root_offender_id != v_root_offender_id then
            raise_application_error(-20002,'Offender Identifier inconsistancy');
         end if;
         p_root_offender_id := v_root_offender_id;
      elsif p_offender_book_id is not null then
         select ob.root_offender_id, o.offender_id_display, ob.agy_loc_id
           into p_root_offender_id, p_noms_id, v_agy_loc_id
           from offenders o
           join offender_bookings ob
             on ob.offender_id = o.offender_id
          where ob.offender_book_id = p_offender_book_id;
      else
         raise_application_error(-20003,'No Offender Identifier provided');
      end if;

      --
      -- If prison id is provided then it overrides the 
      -- current prison
      --
      if p_agy_loc_id is null then
         if v_agy_loc_id in  ('TRN','OUT') then
            raise_application_error(-20005,'Offender is out or in transit');
         else
            p_agy_loc_id := v_agy_loc_id;
         end if;
      end if;
   exception
      when no_data_found then
         raise_application_error(-20001,'Offender Not Found');
   end get_offender_ids;

   function get_reception_datetime(p_offender_book_id in offender_bookings.offender_book_id%type, 
                                   p_agy_loc_id       in out offender_bookings.agy_loc_id%type)
      return date
   is
      v_reception_datetime date;
   begin
      select movement_datetime
        into v_reception_datetime
        from ( select to_date(to_char(movement_date,'YYYYMMDD')||
                              to_char(movement_time,'HH24MI'),'YYYYMMDDHH24MI') movement_datetime,
                      row_number() over (order by to_date(to_char(movement_date,'YYYYMMDD')||
                                                                   to_char(movement_time,'HH24MI'),'YYYYMMDDHH24MI') desc) rnum
                 from offender_external_movements
                where offender_book_id = p_offender_book_id
                  and to_agy_loc_id = p_agy_loc_id
                  and movement_type = 'ADM' )
       where rnum = 1;

      return v_reception_datetime;
   exception
      when no_data_found then
         return null;
   end get_reception_datetime;

   function trust_account_exists(p_root_offender_id in offender_trust_accounts.offender_id%type,
                                 p_caseload_id      in offender_trust_accounts.caseload_id%type)
      return boolean
   is
      v_dummy varchar2(1);
   begin
      select 'X'
        into v_dummy
        from oms_owner.offender_trust_accounts
       where offender_id = p_root_offender_id
         and caseload_id = p_caseload_id;
      return true;
   exception
      when no_data_found then
         return false;
   end trust_account_exists;

   function prison_exists(p_agy_loc_id in agency_locations.agy_loc_id%type) return boolean
   is
      v_dummy varchar2(1);
   begin
      select 'X' 
        into v_dummy
        from agency_locations
       where agy_loc_id = upper(p_agy_loc_id)
         and agency_location_type = 'INST'
         and active_flag = 'Y';
      return true;
   exception
      when no_data_found then
         return false;
   end prison_exists;

   
   function is_reference_code_valid(p_domain reference_codes.domain%type, p_code reference_codes.code%type) return boolean
   is
      v_dummy varchar2(1);
   begin
      select 'Y'
        into v_dummy
        from reference_codes 
       where domain = p_domain
         and code = p_code
         and active_flag = 'Y';
      return true;
   exception
      when no_data_found then
         return false;
   end is_reference_code_valid;
   
   procedure get_account_code_type(p_account_code      in account_codes.account_code%type,
                                   p_sub_account_type out account_codes.sub_account_type%type,
                                   p_account_name     out account_codes.account_name%type)
   is
   begin
      select ac.sub_account_type, ac.account_name
        into p_sub_account_type, p_account_name
        from account_codes ac
       where ac.account_code = p_account_code;
   exception
      when no_data_found then
         p_sub_account_type := null;
         p_account_name := null;
   end get_account_code_type;

end core_utils;
/
sho err
