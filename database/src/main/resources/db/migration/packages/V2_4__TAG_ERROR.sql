CREATE OR REPLACE PACKAGE             "TAG_ERROR"
IS
/*
||    Purpose: This package provides the basic functions for sorting.
||
||    MODIFICATION HISTORY (Please put version history IN a REVERSE-chronological ORDER below)
||    --------------------
||    Person                     DATE       Version  Comments
||    ---------              ---------    ---------  ---------------------------------------
||    GJC                  31-MAR-2005       10.1.1  Add extra user parameters with defaults
||    Neil                 09-MAR-2005       10.1.0  Created the Package.
*/


   c_tag_err           CONSTANT PLS_INTEGER := -20999;
   c_programs_err      CONSTANT PLS_INTEGER := -20998;



   PROCEDURE start_log;

   PROCEDURE stop_log;

   PROCEDURE raise_app_error  (p_error_code    PLS_INTEGER,
                               p_error_message VARCHAR2,
				               p_stack_trace   BOOLEAN   DEFAULT FALSE);

   PROCEDURE handle (p_error_code    PLS_INTEGER DEFAULT c_tag_err,
                     p_log           BOOLEAN     DEFAULT FALSE,
                     p_log_message   VARCHAR2    DEFAULT NULL,
		         p_reraise       BOOLEAN     DEFAULT TRUE,
			   p_stack_trace   BOOLEAN     DEFAULT TRUE,
                     p_user_module   VARCHAR2    DEFAULT NULL,
                     p_user_location VARCHAR2    DEFAULT NULL,
                     p_user_message  VARCHAR2    DEFAULT NULL);

   FUNCTION show_version RETURN VARCHAR2;

END;
/


CREATE OR REPLACE PACKAGE BODY "TAG_ERROR"
IS
/*
||    Purpose: This package provides the basic funtions for sorting.
||
||    MODIFICATION HISTORY (Please put version history IN a REVERSE-chronological ORDER below)
||    --------------------
||    Person                     DATE       Version  Comments
||    ---------              ---------    ---------  ---------------------------------------
||    GJC                  31-MAR-2005       10.1.1  Add extra user parameters with defaults
||    Neil                 09-MAR-2005       10.1.0  Created the Package.
*/
-- ====================================================================================
   vcp_version   CONSTANT VARCHAR2 ( 60 ) := '10.1.0 27-APR-2005';
-- ====================================================================================
-------------------------------------------------------------
g_logging     BOOLEAN := FALSE;
g_stack_trace BOOLEAN := TRUE;
c_eol_delim   CONSTANT CHAR(1) := CHR(10);
c_name_delim  CONSTANT CHAR(1) := '"';
c_line_delim  CONSTANT CHAR(4) := 'line';



PROCEDURE log (p_errmsg VARCHAR2,
               p_trace VARCHAR2,
               p_user_module   VARCHAR2    DEFAULT NULL,
               p_user_location VARCHAR2    DEFAULT NULL,
               p_user_message  VARCHAR2    DEFAULT NULL,
			   p_error_code    PLS_INTEGER DEFAULT NULL)
IS

   lv_name_start_loc NUMBER := INSTR(p_trace, c_name_delim, 1, 1);
   lv_name_end_loc   NUMBER := INSTR(p_trace, c_name_delim, 1, 2);
   lv_line_loc       NUMBER := INSTR(p_trace, c_line_delim);
   lv_eol_loc        NUMBER := INSTR(p_trace, c_eol_delim);

   lv_proc_name      tag_error_logs.procedure_name%TYPE;
   lv_location       tag_error_logs.error_location%TYPE;
BEGIN

   lv_proc_name :=
      SUBSTR(p_trace, lv_name_start_loc + 1, lv_name_end_loc - lv_name_start_loc - 1);
   lv_location :=
      SUBSTR(p_trace, lv_line_loc, lv_eol_loc - lv_line_loc);

   tag_exceptions(lv_proc_name, p_errmsg, lv_location, p_user_module,p_user_location,p_user_message,p_error_code);

EXCEPTION
   WHEN OTHERS THEN
      tag_error.handle;
END;


PROCEDURE start_log
IS
BEGIN
   g_logging := TRUE;
EXCEPTION
   WHEN OTHERS THEN
      tag_error.handle;
END;


PROCEDURE stop_log
IS
BEGIN
   g_logging := FALSE;
EXCEPTION
   WHEN OTHERS THEN
      tag_error.handle;
END;


PROCEDURE raise_app_error  (p_error_code    PLS_INTEGER,
                            p_error_message VARCHAR2,
				            p_stack_trace   BOOLEAN   DEFAULT FALSE)
IS

BEGIN
   RAISE_APPLICATION_ERROR (p_error_code, p_error_message);
EXCEPTION
   WHEN OTHERS THEN
      IF NOT p_stack_trace THEN
         handle (p_error_code  => p_error_code,
		         p_stack_trace => p_stack_trace);
	  END IF;
	  RAISE;
END;

PROCEDURE handle (p_error_code    PLS_INTEGER DEFAULT c_tag_err,
                  p_log           BOOLEAN     DEFAULT FALSE,
                  p_log_message   VARCHAR2    DEFAULT NULL,
		      p_reraise       BOOLEAN     DEFAULT TRUE,
			p_stack_trace   BOOLEAN     DEFAULT TRUE,
                  p_user_module   VARCHAR2    DEFAULT NULL,
                  p_user_location VARCHAR2    DEFAULT NULL,
                  p_user_message  VARCHAR2    DEFAULT NULL)
IS

   lv_raise_code PLS_INTEGER;
   lv_errm_pos   PLS_INTEGER := 1;
   lv_trace_pos  PLS_INTEGER := 1;
   lv_delim      CHAR(1);
   lv_trace_msg  VARCHAR2(1000);
   
BEGIN

   

   IF SQLCODE <= -20000 AND SQLCODE >= -20999
   THEN

 	  lv_errm_pos  := INSTR(SQLERRM, 'ORA' || SQLCODE) + 11;
	  lv_trace_pos:= INSTR(DBMS_UTILITY.FORMAT_ERROR_BACKTRACE, 'ORA-06512', 2);

      IF lv_trace_pos < 1
      THEN
	     lv_trace_pos := 1;
	     g_stack_trace := p_stack_trace;
	  END IF;

	  IF SQLCODE = c_tag_err THEN
	     lv_raise_code := p_error_code;
	  ELSE
	     lv_raise_code := SQLCODE;
	  END IF;
 
   ELSE      
      
	  g_stack_trace := p_stack_trace;
      lv_raise_code := p_error_code;
   
   END IF;

   IF g_stack_trace
   THEN
      lv_trace_msg := SUBSTR(DBMS_UTILITY.FORMAT_ERROR_BACKTRACE, lv_trace_pos);
      lv_delim     := c_eol_delim;
   ELSE
      lv_trace_msg := NULL;
	lv_delim     := NULL;
   END IF;

   -- Moved gjc, as was in exception
   
   IF p_log OR g_logging
   THEN
      tag_error.log(SQLERRM, lv_trace_msg, p_user_module, p_user_location, p_user_message, p_error_code);
   END IF;   
   
   -- gjc set to -20000 as -21000 caused message corruption
   
   IF NOT (lv_raise_code <= -20000 AND lv_raise_code >= -20999 )
   THEN
      lv_raise_code := -20000;
   END IF;
   
   BEGIN
      RAISE_APPLICATION_ERROR (
         lv_raise_code,
	        SUBSTR(SQLERRM, lv_errm_pos) || lv_delim || lv_trace_msg
	     );
   EXCEPTION
      WHEN OTHERS 
	  THEN
         IF p_reraise
         THEN
		    RAISE;
         END IF;
	  END;
END;
-------------------------------------------------------------
  FUNCTION show_version
      RETURN VARCHAR2
   IS
   BEGIN
      RETURN vcp_version;
   EXCEPTION
      WHEN OTHERS
      THEN
         RETURN ('OmsErr-0001: SHOW_VERSION(). '|| SQLERRM);
   END show_version;
-------------------------------------------------------------
-- Enter further code below as specified in the Package spec.
END;
/

