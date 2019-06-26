create or replace package api_owner.api_staff
as
   function show_version return varchar2;
   
   procedure get_staff_details_from_email (p_email_address in internet_addresses.internet_address%type,
                                           p_staff_id     out staff_members.staff_id%type,
                                           p_last_name    out staff_members.last_name%type,
                                           p_first_name   out staff_members.first_name%type,
                                           p_status       out staff_members.status%type,
                                           p_status_desc  out reference_codes.description%type ,
                                           p_gen_username out staff_user_accounts.username%type,
                                           p_adm_username out staff_user_accounts.username%type);

end api_staff;
/
sho err
create or replace package body api_owner.api_staff
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.0   25-Apr-2018';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      -------------------------------------------------------------------------------
      Person      Date           Version     Comments
      ---------   -----------    ---------   ----------------------------------------
      Paul M      25-Apr-2018     1.0        Initial version

   */

   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;

   procedure get_staff_details_from_email (p_email_address in internet_addresses.internet_address%type,
                                           p_staff_id     out staff_members.staff_id%type,
                                           p_last_name    out staff_members.last_name%type,
                                           p_first_name   out staff_members.first_name%type,
                                           p_status       out staff_members.status%type,
                                           p_status_desc  out reference_codes.description%type ,
                                           p_gen_username out staff_user_accounts.username%type,
                                           p_adm_username out staff_user_accounts.username%type)
   is
   begin
      select sm.staff_id,
             sm.last_name,
             sm.first_name,
             sm.status,
             rc.description status_desc,
             sug.username,
             sua.username
        into p_staff_id,    
             p_last_name,
             p_first_name,
             p_status,
             p_status_desc,
             p_gen_username,
             p_adm_username
        from staff_members sm
        join internet_addresses ia
          on ia.owner_id = sm.staff_id
             and ia.owner_class = 'STF'
        left join staff_user_accounts sug
          on sug.staff_id = sm.staff_id
             and sug.staff_user_type = 'GENERAL'
        left join staff_user_accounts sua
          on sua.staff_id = sm.staff_id
             and sua.staff_user_type = 'ADMIN'
        left join reference_codes rc
          on rc.code = sm.status
             and rc.domain = 'STAFF_STATUS'
       where ia.internet_address_class = 'EMAIL'
         and upper(internet_address) = upper(p_email_address);
   exception
      when no_data_found then 
         null;
      when too_many_rows then
         null;
   end get_staff_details_from_email;

end api_staff;
/
sho err
