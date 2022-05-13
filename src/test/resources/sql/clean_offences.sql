DELETE offences WHERE statute_code = '9235';
DELETE statutes WHERE statute_code = '9235';
DELETE ho_codes WHERE ho_code = '923/99';
UPDATE offences SET description = 'Manslaughter Old' WHERE offence_code = 'M5';
