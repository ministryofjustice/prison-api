DELETE offences WHERE statute_code = '9235';
DELETE ho_codes WHERE ho_code = '923/99';
UPDATE offences SET description = 'Manslaughter Old' WHERE offence_code = 'M5';

DELETE offences WHERE offence_code in ('COML025', 'COML026', 'STAT001');
DELETE statutes WHERE statute_code IN ('9235', 'COML', 'STAT');
