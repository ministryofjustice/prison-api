-- Mapping the Oracle REGEXP_SUBSTR function onto the equivalent HSQLDB REGEXP_SUBSTRING function:
CREATE FUNCTION REGEXP_SUBSTR (source_string VARCHAR(255), pattern VARCHAR(255))
RETURNS VARCHAR(255)
RETURN REGEXP_SUBSTRING(source_string, pattern);
