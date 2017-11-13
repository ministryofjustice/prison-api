CREATE OR REPLACE PACKAGE OMS_MISCELLANEOUS
IS

  --
  -- To return the key of a piece of text.
  --
  FUNCTION return_key(p_text VARCHAR2)
    RETURN VARCHAR2;

END oms_miscellaneous;

/
CREATE OR REPLACE PACKAGE BODY OMS_MISCELLANEOUS IS


  --
  -- To return the key of a piece of text.
  --
  FUNCTION return_key(
    p_text VARCHAR2)
    RETURN VARCHAR2 IS
    n_text VARCHAR2(200);
    l_text VARCHAR2(200);
    BEGIN
      l_text := TRANSLATE(p_text, '?ABCDEFGHIJKLMNOPQRSTUVWXYZ', '?');
      IF l_text IS NULL
      THEN
        RETURN (p_text);
      END IF;
      l_text := 'A' || l_text;
      n_text := TRANSLATE(p_text, l_text, 'A');
      RETURN (n_text);
    END;

  ----------------------------------------------------------------------------------------------
END oms_miscellaneous;
/
