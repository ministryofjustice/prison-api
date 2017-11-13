  CREATE OR REPLACE PACKAGE "OUMAGYHT"
  IS
-- Purpose: This package supports various functions for AGENCY_LOCATIONS_T1 trigger
--          For keeping the history of the AGENCY_LOCATIONS table.
--
-- MODIFICATION HISTORY
-- Person      Date         Comments
-- ---------   -----------  ------------------------------------------
-- GJC         14 Oct 2006    2.1     SHOW_VERSION changed from procedure to function                                  
-- Patrick     27-JUN-2005   1.2     Added function get_address_owner_code
-- Patrick     17-JUN-2005  Initial Version

  PROCEDURE insert_into_agy_loc_amendments ( p_agy_loc_id VARCHAR2, p_col_name VARCHAR2, p_old_value VARCHAR2, p_new_value VARCHAR2, p_domain varchar2, p_desc_type varchar2);
  FUNCTION check_changed (p_old_value varchar2, p_new_value varchar2) return boolean;
  FUNCTION get_address_owner_code (p_addr_id NUMBER) RETURN VARCHAR2;
   FUNCTION Show_Version RETURN VARCHAR2;
END; -- Package spec
/
CREATE OR REPLACE PACKAGE BODY "OUMAGYHT"
IS
/*
||   Purpose: This package supports various functions for AGENCY_LOCATIONS_T1 trigger
||            For keeping the history of the AGENCY_LOCATIONS table.
||
||    MODIFICATION HISTORY (Please put version history IN a REVERSE-chronological ORDER below)
||    --------------------
||    Person                     DATE       Version  Comments
||    ---------              ---------    ---------  -----------------------------------
||    GJC                     14 Oct 2006    2.1     SHOW_VERSION changed from procedure to function                                  
||    Patrick                 27-JUN-2005    1.2     Added function get_address_owner_code
||    Patrick                 21-JUN-2005    1.1     Fixed bug after peer review.
||    Patrick                 14-JUN-2005    1.0     Created the Package.
*/
-- ====================================================================================
   vcp_version   CONSTANT VARCHAR2 (60) := '2.1 14-Oct-2006';
-- ====================================================================================
   PROCEDURE insert_into_agy_loc_amendments (p_agy_loc_id VARCHAR2, p_col_name VARCHAR2, p_old_value VARCHAR2, p_new_value VARCHAR2,  p_domain varchar2, p_desc_type varchar2)
   IS
      lv_amendment_id   agency_location_amendments.agy_loc_amend_id%TYPE;
      lv_new_value agency_location_amendments.new_value%type;
      lv_old_value agency_location_amendments.original_value%type;
   BEGIN
      if p_desc_type = 'REF_CODE' then
         lv_new_value := oms_miscellaneous.getdesccode(p_domain, p_new_value);
         lv_old_value := oms_miscellaneous.getdesccode(p_domain, p_old_value);
      elsif p_desc_type ='DATE' then
         lv_new_value := to_char(to_date(p_new_value), 'DD/MM/YYYY');
         lv_old_value := to_char(to_date(p_old_value), 'DD/MM/YYYY');
      else
         lv_new_value := p_new_value;
         lv_old_value := p_old_value;
      end if;
      INSERT INTO agency_location_amendments
                  (agy_loc_amend_id, agy_loc_id, FIELD, original_value, new_value, amend_datetime, amend_user
                  )
           VALUES (agy_loc_amend_id.NEXTVAL, p_agy_loc_id, p_col_name, nvl(lv_old_value, p_old_value), nvl(lv_new_value, p_new_value), SYSDATE, USER
                  );
   EXCEPTION
      WHEN OTHERS
      THEN
         tag_error.handle;
   END;

--------------------------------------------------------------
   FUNCTION check_changed (p_old_value VARCHAR2, p_new_value VARCHAR2)
      RETURN BOOLEAN
   IS
/* This function determines wether 2 values are the same. */
   BEGIN
      IF p_old_value IS NOT NULL AND p_new_value IS NULL
      THEN
         RETURN TRUE;
      END IF;

      IF p_old_value IS NULL AND p_new_value IS NOT NULL
      THEN
         RETURN TRUE;
      END IF;

      IF p_old_value <> p_new_value
      THEN
         RETURN TRUE;
      END IF;

      RETURN FALSE;
   EXCEPTION
      WHEN OTHERS
      THEN
         tag_error.handle;
   END;

-------------------------------------------------------------
FUNCTION get_address_owner_code (p_addr_id NUMBER)
RETURN VARCHAR2 is
   lv_owner_code ADDRESSES.owner_code%TYPE;
   CURSOR get_agy_loc_id_cur IS
      SELECT OWNER_CODE
        FROM ADDRESSES
       WHERE ADDRESS_ID = p_addr_id
         AND OWNER_CLASS = 'AGY';
   BEGIN
      OPEN get_agy_loc_id_cur;
      FETCH get_agy_loc_id_cur INTO lv_owner_code;
      CLOSE get_agy_loc_id_cur;
   RETURN lv_owner_code;
   END;

-------------------------------------------------------------
   FUNCTION Show_Version
   RETURN VARCHAR2
   IS
   BEGIN
      RETURN(vcp_version);
   END Show_Version;
-------------------------------------------------------------
END;
/
 
