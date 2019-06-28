create or replace package api_owner.api_case_notes
as
   function show_version return varchar2;

   procedure get_case_notes_since(p_from_timestamp      in timestamp,
                                  p_case_note_type      in offender_case_notes.case_note_type%type default null,
                                  p_case_note_sub_type  in offender_case_notes.case_note_sub_type%type default null,
                                  p_case_note_csr     out sys_refcursor);

   procedure get_all_case_notes_since(p_from_timestamp      in timestamp,
                                      p_case_note_csr     out sys_refcursor);

   procedure get_offender_case_notes(p_noms_id            in offenders.offender_id_display%type,
                                     p_from_timestamp     in timestamp default null,
                                     p_case_note_csr     out sys_refcursor);
end api_case_notes;
/
sho err
create or replace package body api_owner.api_case_notes
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.0   18-Jul-2017';
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

   procedure get_case_notes_since(p_from_timestamp      in timestamp,
                                  p_case_note_type      in offender_case_notes.case_note_type%type default null,
                                  p_case_note_sub_type  in offender_case_notes.case_note_sub_type%type default null,
                                  p_case_note_csr     out sys_refcursor)
   is
   begin
      open p_case_note_csr for
           select o.offender_id_display,
                  oc.offender_book_id,
                  ob.agy_loc_id,
                  oc.case_note_id,
                  to_date(to_char(oc.contact_date,'yyyymmdd')||
                          to_char(oc.contact_time,'hh24miss'),
                          'yyyymmddhh24miss') contact_datetime,
                  oc.case_note_type,
                  oc.case_note_sub_type,
                  oc.staff_id,
                  initcap (sm.last_name) || ', ' || initcap (sm.first_name) staff_name,
                  oc.case_note_text,
                  oc.amendment_flag,       
                  oc.audit_timestamp
             from offender_case_notes oc
             join offender_bookings ob
               on ob.offender_book_id = oc.offender_book_id
             join offenders o
               on o.offender_id = ob.offender_id
             join staff_members sm
               on sm.staff_id = oc.staff_id
            where oc.audit_timestamp >= p_from_timestamp
              and oc.case_note_type = p_case_note_type 
              and oc.case_note_sub_type = p_case_note_sub_type 
            order by oc.audit_timestamp ;
     
   end get_case_notes_since;


   procedure get_all_case_notes_since(p_from_timestamp      in timestamp,
                                  p_case_note_csr     out sys_refcursor)
   is
   begin
      open p_case_note_csr for
           select o.offender_id_display,
                  oc.offender_book_id,
                  ob.agy_loc_id,
                  oc.case_note_id,
                  to_date(to_char(oc.contact_date,'yyyymmdd')||
                          to_char(oc.contact_time,'hh24miss'),
                          'yyyymmddhh24miss') contact_datetime,
                  oc.case_note_type,
                  oc.case_note_sub_type,
                  oc.staff_id,
                  initcap (sm.last_name) || ', ' || initcap (sm.first_name) staff_name,
                  oc.case_note_text,
                  oc.amendment_flag,       
                  oc.audit_timestamp
             from offender_case_notes oc
             join offender_bookings ob
               on ob.offender_book_id = oc.offender_book_id
             join offenders o
               on o.offender_id = ob.offender_id
             join staff_members sm
               on sm.staff_id = oc.staff_id
            where oc.audit_timestamp >= p_from_timestamp
            order by oc.audit_timestamp ;
     
   end get_all_case_notes_since;

   procedure get_offender_case_notes(p_noms_id            in offenders.offender_id_display%type,
                                     p_from_timestamp     in timestamp default null,
                                     p_case_note_csr     out sys_refcursor)
   is
      v_noms_id           offenders.offender_id_display%type;
      v_root_offender_id  offender_bookings.root_offender_id%type;
      v_offender_book_id  offender_bookings.offender_book_id%type;
      v_agy_loc_id        offender_bookings.agy_loc_id%type;
   begin
      v_noms_id := p_noms_id; 
      core_utils.get_offender_ids(p_root_offender_id => v_root_offender_id,
                                  p_noms_id          => v_noms_id,
                                  p_agy_loc_id       => v_agy_loc_id,
                                  p_offender_book_id => v_offender_book_id);

      if p_from_timestamp is null then
         open p_case_note_csr
          for select p_noms_id offender_id_display,
                     v_offender_book_id offender_book_id,
                     v_agy_loc_id agy_loc_id,
                     oc.case_note_id,
                     to_date(to_char(oc.contact_date,'yyyymmdd')||
                             to_char(oc.contact_time,'hh24miss'),
                             'yyyymmddhh24miss') contact_datetime,
                     oc.case_note_type,
                     oc.case_note_sub_type,
                     oc.staff_id,
                     initcap (sm.last_name) || ', ' || initcap (sm.first_name) staff_name,
                     oc.case_note_text,
                     oc.amendment_flag,       
                     oc.audit_timestamp
                from offender_case_notes oc
                join staff_members sm
                  on sm.staff_id = oc.staff_id
               where oc.offender_book_id = v_offender_book_id
               order by oc.contact_date, oc.contact_time;
      else
         open p_case_note_csr
          for select p_noms_id offender_id_display,
                     v_offender_book_id offender_book_id,
                     v_agy_loc_id agy_loc_id,
                     oc.case_note_id,
                     to_date(to_char(oc.contact_date,'yyyymmdd')||
                             to_char(oc.contact_time,'hh24miss'),
                             'yyyymmddhh24miss') contact_datetime,
                     oc.case_note_type,
                     oc.case_note_sub_type,
                     oc.staff_id,
                     initcap (sm.last_name) || ', ' || initcap (sm.first_name) staff_name,
                     oc.case_note_text,
                     oc.amendment_flag,       
                     oc.audit_timestamp
                from offender_case_notes oc
                join staff_members sm
                  on sm.staff_id = oc.staff_id
               where oc.offender_book_id = v_offender_book_id
                 and oc.audit_timestamp >= p_from_timestamp
               order by oc.contact_date, oc.contact_time;
      end if;

   end get_offender_case_notes;
end api_case_notes;
/
sho err
