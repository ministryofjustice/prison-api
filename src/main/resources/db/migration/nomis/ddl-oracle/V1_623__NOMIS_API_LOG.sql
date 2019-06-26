create or replace package api_owner.nomis_api_log
as

   function show_version return varchar2;

   procedure error(p_msg_module in varchar2, 
                   p_message in varchar2);

   procedure warning(p_msg_module in varchar2, 
                     p_message in varchar2);

   procedure info(p_msg_module in varchar2, 
                  p_message in varchar2);

   procedure debug(p_msg_module in varchar2, 
                   p_message in varchar2);
end nomis_api_log;
/
show err
create or replace package body api_owner.nomis_api_log

as
   -- =============================================================
   v_version   CONSTANT VARCHAR2 (60) := '1.0   21-Oct-2016';
   -- =============================================================
   /*
   MODIFICATION HISTORY
   ------------------------------------------------------------------------------
   Person      Date           Version                Comments
   ---------   -----------    ---------   ---------------------------------------
   Paul M      21-Oct-2016     1.0        Initial version
   ------------------------------------------------------------------------------
   */
   function show_version return varchar2
   is
   begin
      return (v_version);
   end show_version;

   procedure log(p_msg_type in varchar2, 
                 p_msg_module in varchar2, 
                 p_message in varchar2)
   is
      pragma autonomous_transaction;
   begin
      insert into nomis_api_logs
             (log_id, msg_type,msg_module, message)
      values (log_id.nextval, p_msg_type, p_msg_module, p_message);
      commit;
   end log;
      

   procedure error(p_msg_module in varchar2, 
                   p_message in varchar2)
   is
   begin
      log('ERROR', p_msg_module, p_message);
   end error;

   procedure warning(p_msg_module in varchar2, 
                     p_message in varchar2)
   is
   begin
      log('WARNING', p_msg_module, p_message);
   end warning;

   procedure info(p_msg_module in varchar2, 
                  p_message in varchar2)
   is
   begin
      log('INFO', p_msg_module, p_message);
   end info;

   procedure debug(p_msg_module in varchar2, 
                   p_message in varchar2)
   is
   begin
      log('DEBUG', p_msg_module, p_message);
   end debug;

end nomis_api_log;
/
show err
