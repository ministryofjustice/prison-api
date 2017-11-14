CREATE OR REPLACE PROCEDURE "TAG_EXCEPTIONS" 
   (p_Procedure_Name  TAG_ERROR_LOGS.PROCEDURE_NAME%TYPE
    ,p_Error_Msg      TAG_ERROR_LOGS.ERROR_MESSAGE%TYPE
    ,p_Error_Place    TAG_ERROR_LOGS.ERROR_LOCATION%TYPE
    ,p_user_module   VARCHAR2    DEFAULT NULL
    ,p_user_location VARCHAR2    DEFAULT NULL
    ,p_user_message  VARCHAR2    DEFAULT NULL
    ,p_user_error_code NUMBER    DEFAULT NULL
  )
  IS
  /*===================================================================
   Version Number = 10.1.1  DATE MODIFIED = 21/06/2005
   ===================================================================*/
/* MODIFICATION HISTORY
   PERSON      DATE           VERSION       COMMENTS
   ---------   -----------    ------------  -----------------------------
   GJC         31-Mar-2005    10.1.2        Added USER attributes with defaults
   Venu        21-Jun-2005    10.1.1        Modified the Insert statement below added column list.
   SURYA       14-FEB-2002    4.12.0.0      CREATED THIS PROCEDURE FOR POPULATING
                                            THE EXCEPTIONS INTO LOG TABLE FROM PACKAGE SUBROUNTINES
   */
    PRAGMA          AUTONOMOUS_TRANSACTION;
    v_Error_Msg     VARCHAR2(260):= NULL;
    v_Module_Name   TAG_ERROR_LOGS.MODULE_NAME%TYPE:= NULL;
    v_Sid           TRUST_AUDITS_TMP.SID%TYPE:= NULL;
    v_Profile_Value SYSTEM_PROFILES.PROFILE_VALUE%TYPE:= NULL;
    /**FIND OUT THE SESSION ID**/
    CURSOR Sid_Cur IS
      SELECT USERENV('SESSIONID')
        FROM DUAL;
    /**FIND OUT THE MODULE_NAME**/
    CURSOR Module_Name_Cur IS
      SELECT Module_Name
        FROM TRUST_AUDITS_TMP
       WHERE Sid = v_Sid;
    /**FIND OUT THE SYSTEM PROFILE**/
    CURSOR System_Profiles_Cur IS
      SELECT Profile_Value
        FROM SYSTEM_PROFILES
       WHERE Profile_Type = 'CLIENT'
         AND Profile_Code = 'FINAN_AUDIT';
BEGIN
  
  v_Sid:= NULL;
  v_Module_Name:= NULL;
  v_Profile_Value:= NULL;
  
  OPEN System_Profiles_Cur;
  FETCH System_Profiles_Cur INTO v_Profile_Value;
  CLOSE System_Profiles_Cur;
  
  IF v_Profile_Value = 'Y' THEN
    BEGIN
      
      OPEN Sid_Cur;
      FETCH Sid_Cur INTO v_Sid;
      CLOSE Sid_Cur;
      
      OPEN Module_Name_Cur;
      FETCH Module_Name_Cur INTO v_Module_Name;
      CLOSE Module_Name_Cur;
      
      INSERT INTO TAG_ERROR_LOGS
                   (Tag_Error_Id
                    ,Sid
                    ,Module_Name
                    ,Procedure_Name
                    ,Error_Message
                    ,Error_Location
                    ,Modify_User_Id
                    ,Modify_Datetime
                    ,user_message
                    ,user_module
                    ,user_location
			  ,user_error_code )
         VALUES    (TAG_ERROR_ID.NEXTVAL --TAG_ERROR_ID
                    ,v_Sid               --SID
                    ,v_Module_Name       --MODULE_NAME
                    ,p_Procedure_Name    --PROCEDURE_NAME
                    ,p_Error_Msg         --ERROR_MESSAGE
                    ,p_Error_Place       --ERROR_LOCATION
                    ,USER                --MODIFY_USER_ID
                    ,systimestamp        --MODIFY_DATETIME
                    ,p_user_message
                    ,p_user_module
                    ,p_user_location
			  ,p_user_error_code );      
    EXCEPTION
        WHEN OTHERS THEN
           v_Error_Msg:= p_Error_Msg||'  '||SQLERRM;
           INSERT INTO TAG_ERROR_LOGS
                      (
                        TAG_ERROR_ID
                        ,SID
                        ,MODULE_NAME
                        ,PROCEDURE_NAME
                        ,ERROR_MESSAGE
                        ,ERROR_LOCATION
                        ,Modify_User_Id
                        ,Modify_Datetime
                       )
               VALUES (TAG_ERROR_ID.NEXTVAL
                       ,v_Sid
                       ,v_Module_Name
                       ,'TAG_EXCEPTIONS'
                       ,p_Error_Msg
                       ,'TAG_EXCEPTIONS PROCEDURE'
                       ,USER
                       ,SYSTIMESTAMP );
    END;
    COMMIT;
  END IF;
END TAG_EXCEPTIONS;--PROCEDURE
/
 