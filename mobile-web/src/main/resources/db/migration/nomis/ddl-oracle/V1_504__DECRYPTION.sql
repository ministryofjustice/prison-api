CREATE OR REPLACE FUNCTION "DECRYPTION"(key_string   IN VARCHAR2,
                                        input_string IN VARCHAR2)
  RETURN VARCHAR2
IS
  --
  -- Work Variables
  --
  raw_input                   RAW(128) := hextoraw(input_string);
  raw_key                     RAW(128) := UTL_RAW.CAST_TO_RAW(key_string);
  encrypted_raw               RAW(2048);
  encrypted_string            VARCHAR2(2048);
  decrypted_raw               RAW(2048);
  decrypted_string            VARCHAR2(2048);
    error_in_input_buffer_length EXCEPTION;
PRAGMA EXCEPTION_INIT (error_in_input_buffer_length, -28232);
  INPUT_BUFFER_LENGTH_ERR_MSG VARCHAR2(100) :=
  '*** DES INPUT BUFFER NOT A MULTIPLE OF 8 BYTES - IGNORING EXCEPTION ***';
    double_encrypt_not_permitted EXCEPTION;
PRAGMA EXCEPTION_INIT (double_encrypt_not_permitted, -28233);
  DOUBLE_ENCRYPTION_ERR_MSG   VARCHAR2(100) :=
  '*** CANNOT DOUBLE ENCRYPT DATA - IGNORING EXCEPTION ***';
  BEGIN
    --   dbms_output.put_line('> ========= BEGIN TEST RAW DATA =========');
    BEGIN
      dbms_obfuscation_toolkit.DESDecrypt(input => raw_input,
                                          key => raw_key, decrypted_data => decrypted_raw);
      RETURN (UTL_RAW.CAST_TO_VARCHAR2(decrypted_raw));
    END;
  END;
/
