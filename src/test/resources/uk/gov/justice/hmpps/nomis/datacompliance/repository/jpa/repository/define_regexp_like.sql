-- Mapping the Oracle REGEXP_LIKE function onto the equivalent HSQLDB REGEXP_MATCHES function:
CREATE FUNCTION REGEXP_LIKE(source_string VARCHAR(255), regex_pattern VARCHAR(255), param VARCHAR(255))
RETURNS BOOLEAN
RETURN REGEXP_MATCHES(source_string, regex_pattern);
