create or replace package api_owner.api_data_extractor
is
   function show_version return varchar2;

   procedure get_offender_identity(p_offender_book_id    in offender_bookings.offender_book_id%type,
                                   p_single_offender_id out varchar2,
                                   p_root_offender_id   out offenders.root_offender_id%type,
                                   p_noms_id            out offenders.offender_id_display%type,
                                   p_agy_loc_id         out offender_bookings.agy_loc_id%type);

   procedure get_staff_details( p_staff_id          in staff_members.staff_id%type,
                                p_first_name       out staff_members.first_name%type,
                                p_last_name        out staff_members.last_name%type,
                                p_display_name     out varchar2,
                                p_userid_gen       out varchar2,
                                p_userid_adm       out varchar2);

end api_data_extractor;
/
show err
create or replace package body api_owner.api_data_extractor
is
   -- =============================================================
   v_version   CONSTANT VARCHAR2 (60) := '1.0   10-Apr-2017';
   -- =============================================================
   /*
   MODIFICATION HISTORY
   ------------------------------------------------------------------------------
   Person      Date           Version                Comments
   ---------   -----------    ---------   ---------------------------------------
   Paul M      10-Apr-2017     1.0        Initial version
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

   procedure get_staff_details( p_staff_id          in staff_members.staff_id%type,
                                p_first_name       out staff_members.first_name%type,
                                p_last_name        out staff_members.last_name%type,
                                p_display_name     out varchar2,
                                p_userid_gen       out varchar2,
                                p_userid_adm       out varchar2)
   is
   begin
      for staff_rec in (select sm.first_name,
                               sm.last_name,
                               initcap (sm.last_name) || ', ' || initcap (sm.first_name) display_name,
                               sua.username,
                               sua.staff_user_type
                          from staff_members sm
                          join staff_user_accounts sua
                            on sua.staff_id = sm.staff_id
                         where sm.staff_id = p_staff_id)
      loop
         p_first_name := staff_rec.first_name;
         p_last_name := staff_rec.last_name;
         p_display_name := staff_rec.display_name;
         if staff_rec.staff_user_type = 'GENERAL' then
            p_userid_gen := staff_rec.username;
         elsif staff_rec.staff_user_type = 'ADMIN' then
            p_userid_adm := staff_rec.username;
         end if;
      end loop;

   end get_staff_details;


end api_data_extractor;
/
show err
