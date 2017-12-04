CREATE OR REPLACE FUNCTION "ENCRYPTION"(key_string     IN VARCHAR2,
                                        p_input_string IN VARCHAR2)
  RETURN VARCHAR2
IS
  --
  -- Work Variables
  --
  raw_input                   RAW(128) := UTL_RAW.CAST_TO_RAW(p_input_string);
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
    --   dbms_output.put_line('> Raw input                        : ' ||
    --                 UTL_RAW.CAST_TO_VARCHAR2(raw_input));
    BEGIN
      dbms_obfuscation_toolkit.DESEncrypt(input => raw_input,
                                          key => raw_key, encrypted_data => encrypted_raw);
      --               insert into sec (output) values (encrypted_raw);
      --               commit;
      RETURN (encrypted_raw);
    END;
  END;
/
