INSERT INTO CASELOAD_AGENCY_LOCATIONS (CASELOAD_ID, AGY_LOC_ID)
  VALUES ('BXI', 'BXI'),
         ('BXI', 'OUT'),
         ('BXI', 'TRN'),
         ('LEI', 'LEI'),
         ('LEI', 'OUT'),
         ('LEI', 'TRN'),
         ('WAI', 'WAI'),
         ('WAI', 'OUT'),
         ('WAI', 'TRN'),
         ('ZZGHI', 'WAI'),
         ('ZZGHI', 'OUT'),
         ('ZZGHI', 'TRN');

-- These insertions represent association of multiple agencies to a single location
INSERT INTO CASELOAD_AGENCY_LOCATIONS (CASELOAD_ID, AGY_LOC_ID)
  VALUES ('MUL', 'BXI'),
         ('MUL', 'LEI'),
         ('MUL', 'OUT'),
         ('MUL', 'TRN');
