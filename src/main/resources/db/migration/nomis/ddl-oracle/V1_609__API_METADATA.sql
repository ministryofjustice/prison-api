create or replace package api_owner.api_metadata
as
   
   function show_version return varchar2;

   procedure get_db_system_profiles(p_profile_csr out sys_refcursor);
   
   procedure get_package_details (p_owner         in     varchar2,
                                   p_package_csr   out sys_refcursor);

   function get_database_name return varchar2;
end api_metadata;
/
sho err
create or replace package body api_owner.api_metadata
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.1   30-Jun-2017';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      -------------------------------------------------------------------------------
      Person      Date           Version     Comments
      ---------   -----------    ---------   ----------------------------------------
      Paul M      30-Jun-2016     1.1        Corrected Error regarding system profiles 
      Paul M      18-May-2016     1.0        Initial version

   */

   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;

   procedure get_package_details (p_owner         in     varchar2,
                                   p_package_csr   out sys_refcursor)
   is
      v_sql varchar2(32767);
   begin
      v_sql := null;
      for prec in ( select ap.owner, ap.object_name, ap.procedure_name, ao.status, ao.last_ddl_time,
                      'select '||ap.owner||'.'||ap.object_name||'.'||ap.procedure_name||' from dual' version_sql
                      from all_procedures ap
                      join all_objects ao
                        on ao.object_name = ap.object_name
                     where ap.owner = p_owner
                       and ap.procedure_name = 'SHOW_VERSION'
                       and ao.object_type = 'PACKAGE BODY'
                     order by ap.object_name)
      loop
         if v_sql is null then 
            v_sql := 'select ';
         else 
            v_sql := v_sql || chr(10) || 'union' || chr(10) || 'select ';
         end if;
         
         v_sql := v_sql||''''||prec.owner||''' package_owner,'''||prec.object_name||''' package_name,'||
                  ''''||prec.status||''' status, ';
         if prec.status = 'VALID' then
            v_sql := v_sql|| '('||prec.version_sql||')';
         else
            v_sql := v_sql|| 'null ';
         end if;
         v_sql := v_sql ||' version from dual';

      end loop;

      if v_sql is not null then
         open p_package_csr for v_sql;
      end if;

   end get_package_details;

   procedure get_db_system_profiles(p_profile_csr out sys_refcursor)
   is
   begin
      open p_profile_csr for 
           select nvl(description, profile_code) description, profile_value 
             from system_profiles 
            where profile_type = 'DB'
              and profile_code in ('API','TAG','NOMIS');

   end get_db_system_profiles;

   function get_database_name return varchar2
   is
      v_database_name varchar2(30);
   begin
      select name
        into v_database_name 
        from v$database;

      return v_database_name;
   end get_database_name;

end api_metadata;
/
sho err
