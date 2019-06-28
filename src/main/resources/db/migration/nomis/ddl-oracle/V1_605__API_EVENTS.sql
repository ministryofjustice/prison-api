create or replace package api_owner.api_events
is
   
   function show_version return varchar2;

   procedure case_note( p_offender_book_id        in offender_case_notes.offender_book_id%type,
                        p_case_note_id            in offender_case_notes.case_note_id%type,
                        p_contact_date            in offender_case_notes.contact_date%type,
                        p_contact_time            in offender_case_notes.contact_time%type,
                        p_case_note_type          in offender_case_notes.case_note_type%type,
                        p_case_note_type_desc     in reference_codes.description%type,
                        p_case_note_sub_type      in offender_case_notes.case_note_sub_type%type,
                        p_case_note_sub_type_desc in reference_codes.description%type,
                        p_staff_id                in offender_case_notes.staff_id%type,
                        p_case_note_text          in offender_case_notes.case_note_text%type,
                        p_amendment_flag          in offender_case_notes.amendment_flag%type,
                        p_note_source_code        in offender_case_notes.note_source_code%type,
                        p_note_source_desc        in reference_codes.description%type);

   procedure post_event(p_agy_loc_id        in api_offender_events.agy_loc_id%type,
                        p_root_offender_id  in api_offender_events.root_offender_id%type,
                        p_noms_id           in api_offender_events.noms_id%type,
                        p_event_type        in api_offender_events.event_type%type,
                        p_json              in json);

end api_events;
/
show err
create or replace package body api_owner.api_events
is
   
   -- =============================================================
   v_version   CONSTANT VARCHAR2 (60) := '1.1   31-Aug-2017';
   -- =============================================================
   /*
   MODIFICATION HISTORY
   ------------------------------------------------------------------------------
   Person      Date           Version                Comments
   ---------   -----------    ---------   ---------------------------------------
   Paul M      31-Aug-2017     1.1        Change from clob to varchar2
   Paul M      10-Apr-2017     1.0        Initial version
   ------------------------------------------------------------------------------
   */

   function show_version return varchar2
   is
   begin
      return (v_version);
   end show_version;

   procedure case_note( p_offender_book_id        in offender_case_notes.offender_book_id%type,
                        p_case_note_id            in offender_case_notes.case_note_id%type,
                        p_contact_date            in offender_case_notes.contact_date%type,
                        p_contact_time            in offender_case_notes.contact_time%type,
                        p_case_note_type          in offender_case_notes.case_note_type%type,
                        p_case_note_type_desc     in reference_codes.description%type,
                        p_case_note_sub_type      in offender_case_notes.case_note_sub_type%type,
                        p_case_note_sub_type_desc in reference_codes.description%type,
                        p_staff_id                in offender_case_notes.staff_id%type,
                        p_case_note_text          in offender_case_notes.case_note_text%type,
                        p_amendment_flag          in offender_case_notes.amendment_flag%type,
                        p_note_source_code        in offender_case_notes.note_source_code%type,
                        p_note_source_desc        in reference_codes.description%type)
   is
      v_single_offender_id varchar2(36);
      v_noms_id            offenders.offender_id_display%type;
      v_root_offender_id   offenders.root_offender_id%type;
      v_agy_loc_id         offender_bookings.agy_loc_id%type;
      v_event_type         api_offender_events.event_type%type;
      v_staff_first_name   staff_members.first_name%type;
      v_staff_last_name    staff_members.last_name%type;
      v_staff_display_name varchar2(75);
      v_staff_userid_gen   varchar2(30);
      v_staff_userid_adm   varchar2(30);

      v_json json;
      v_json_cn json;
   begin
      v_event_type := 'CASE_NOTE';
 
      v_json := json();

      api_data_extractor.get_offender_identity(p_offender_book_id    => p_offender_book_id,
                                               p_single_offender_id  => v_single_offender_id,
                                               p_root_offender_id    => v_root_offender_id,
                                               p_noms_id             => v_noms_id,
                                               p_agy_loc_id          => v_agy_loc_id);

      api_data_extractor.get_staff_details( p_staff_id     => p_staff_id,
                                            p_first_name   => v_staff_first_name,
                                            p_last_name    => v_staff_last_name,
                                            p_display_name => v_staff_display_name,
                                            p_userid_gen   => v_staff_userid_gen,
                                            p_userid_adm   => v_staff_userid_adm);

      v_json_cn := json_builder.case_note(p_case_note_id  => p_case_note_id,
                                          p_contact_datetime => to_date(to_char(p_contact_date,'yyyymmdd')||
                                                                         to_char(p_contact_time,'hh24miss'),
                                                                         'yyyymmddhh24miss'),
                                           p_source_code      => p_note_source_code,
                                           p_source_desc      => p_note_source_desc,
                                           p_type_code        => p_case_note_type,
                                           p_type_desc        => p_case_note_type_desc,
                                           p_sub_type_code    => p_case_note_sub_type,
                                           p_sub_type_desc    => p_case_note_sub_type_desc,
                                           p_staff_id         => p_staff_id,
                                           p_staff_name       => v_staff_display_name,
                                           p_userid           => v_staff_userid_gen,
                                           p_text             => p_case_note_text,
                                           p_amended          => (p_amendment_flag = 'Y'));
             
      
      v_json.put('case_note', v_json_cn);
      
      post_event(p_agy_loc_id        => v_agy_loc_id,
                 p_root_offender_id  => v_root_offender_id,
                 p_noms_id           => v_noms_id,
                 p_event_type        => v_event_type,
                 p_json              => v_json);

   end case_note;

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

end api_events;
/
show err
