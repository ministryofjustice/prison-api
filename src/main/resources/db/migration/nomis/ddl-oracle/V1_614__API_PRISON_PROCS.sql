create or replace package api_owner.api_prison_procs
as
   function show_version return varchar2;
   
   procedure get_prison_roll(p_agy_loc_id    in offender_bookings.agy_loc_id%type,
                             p_roll_csr     out sys_refcursor);
   
end api_prison_procs;
/
sho err
create or replace package body api_owner.api_prison_procs
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.0   15-Nov-2016';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      -------------------------------------------------------------------------------
      Person      Date           Version     Comments
      ---------   -----------    ---------   ----------------------------------------
      Paul M      15-Nov-2016     1.0        Initial version

   */

   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;

   procedure get_prison_roll(p_agy_loc_id    in offender_bookings.agy_loc_id%type,
                             p_roll_csr     out sys_refcursor)
   is
   begin
      if not core_utils.is_digital_prison ( p_agy_loc_id) then
         raise_application_error(-20010,'Not a Digital Prison');
      end if;

      open p_roll_csr for
           select o.offender_id_display offender_id_display
             from offender_bookings ob
             join offenders o
               on o.offender_id = ob.offender_id
            where ob.agy_loc_id = p_agy_loc_id
              and ob.active_flag = 'Y';

   end get_prison_roll;
end api_prison_procs;
/
sho err
