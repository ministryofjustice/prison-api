create or replace package api_owner.api_warnings
as
   
   function show_version return varchar2;

   procedure clear;

   procedure add(p_warning_msg varchar2);

   function get_table return varchar100_table;
   
   function get_delimited_list(p_delimiter varchar2 default '|') return varchar2;

   function logged return boolean;

end api_warnings;
/
sho err
create or replace package body api_owner.api_warnings
as
   -- =============================================================
      v_version   CONSTANT VARCHAR2 ( 60 ) := '1.0   09-Jun-2017';
   -- =============================================================
   /*
     MODIFICATION HISTORY
      -------------------------------------------------------------------------------
      Person      Date           Version     Comments
      ---------   -----------    ---------   ----------------------------------------
      Paul M      09-Jun-2017     1.0        Initial version

   */

   g_warnings api_owner.varchar100_table;

   function show_version return varchar2
   is
   begin
      return ( v_version );
   end show_version;

   procedure clear
   is
   begin
      g_warnings.delete;
   end clear;

   procedure add(p_warning_msg varchar2)
   is
   begin
      g_warnings.extend;
      g_warnings(g_warnings.last) := p_warning_msg;
   end add;

   function get_table return varchar100_table
   is
   begin
      return g_warnings;
   end get_table;

   function get_delimited_list(p_delimiter varchar2 default '|') return varchar2
   is
      v_message_list varchar2(2000);
   begin
      v_message_list := null;
      for ix in 1..g_warnings.count loop
         v_message_list := v_message_list||
                           case when ix > 1 then p_delimiter else null end||
                           g_warnings(ix);
      end loop;
      return v_message_list;
   end get_delimited_list;
   
   function logged return boolean
   IS
   begin
      return (g_warnings.count > 0);
   end logged;

begin
   g_warnings := varchar100_table();
end api_warnings;
/
sho err
