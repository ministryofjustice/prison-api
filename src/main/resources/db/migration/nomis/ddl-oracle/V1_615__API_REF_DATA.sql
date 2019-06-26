create or replace package api_owner.api_ref_data
as
   function show_version return varchar2;

   procedure get_domain_values(p_domain      in reference_codes.domain%type,
                               p_active_only in varchar2 default 'Y',
                               p_domain_csr out sys_refcursor);

   procedure get_child_domain_values(p_parent_domain  in reference_codes.parent_domain%type,
                                     p_parent_code    in reference_codes.parent_code%type,
                                     p_domain         in reference_codes.domain%type,
                                     p_active_only    in varchar2 default 'Y',
                                     p_domain_csr    out sys_refcursor);

   function get_description(p_domain      in reference_codes.domain%type,
                            p_code        in reference_codes.code%type)
      return varchar2 result_cache;
                     
end api_ref_data;
/
sho err
create or replace package body api_owner.api_ref_data
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.1   04-May-2018';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      -------------------------------------------------------------------------------
      Person      Date           Version     Comments
      ---------   -----------    ---------   ----------------------------------------
      Paul M      04-May-2018     1.1        Added result_cache to get_description
      Paul M      15-Nov-2016     1.0        Initial version

   */

   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;

   procedure get_domain_values(p_domain      in reference_codes.domain%type,
                               p_active_only in varchar2 default 'Y',
                               p_domain_csr out sys_refcursor)
   is
   begin
      if p_active_only = 'Y' then
         open p_domain_csr for
              select code,
                     description,
                     active_flag,
                     system_data_flag,
                     expired_date
                from reference_codes
               where domain = p_domain
                 and active_flag = 'Y'
               order by code;
      else 
         open p_domain_csr for
              select code,
                     description,
                     active_flag,
                     system_data_flag,
                     expired_date
                from reference_codes
               where domain = p_domain
               order by code;
      end if;
   end get_domain_values;

   procedure get_child_domain_values(p_parent_domain  in reference_codes.parent_domain%type,
                                     p_parent_code    in reference_codes.parent_code%type,
                                     p_domain         in reference_codes.domain%type,
                                     p_active_only    in varchar2 default 'Y',
                                     p_domain_csr    out sys_refcursor)
   is
   begin
      if p_active_only = 'Y' then
         open p_domain_csr for
              select code,
                     description,
                     active_flag,
                     system_data_flag,
                     expired_date
                from reference_codes
               where parent_domain = p_parent_domain
                 and parent_code = p_parent_code
                 and domain = p_domain
                 and active_flag = 'Y'
               order by code;
      else 
         open p_domain_csr for
              select code,
                     description,
                     active_flag,
                     system_data_flag,
                     expired_date
                from reference_codes
               where parent_domain = p_parent_domain
                 and parent_code = p_parent_code
                 and domain = p_domain
               order by code;
      end if;
   end get_child_domain_values;

   function get_description(p_domain      in reference_codes.domain%type,
                            p_code        in reference_codes.code%type)
      return varchar2 result_cache
   is
      v_description reference_codes.description%type;
   begin
      select description
        into v_description
        from reference_codes
       where domain = p_domain
         and code =   p_code;

      return v_description;

   exception
      when no_data_found then
         return null;
   end get_description;

end api_ref_data;
/
sho err
