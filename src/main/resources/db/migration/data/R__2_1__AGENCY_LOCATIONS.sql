INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG, AREA_CODE, NOMS_REGION_CODE,
                              GEOGRAPHIC_REGION_CODE)
VALUES ('BXI', 'BRIXTON', 'INST', 'Y', 'LONDON', 'LON', 'LONDON');
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG, AREA_CODE, NOMS_REGION_CODE,
                              GEOGRAPHIC_REGION_CODE)
VALUES ('BMI', 'BIRMINGHAM', 'INST', 'Y', 'WMID', 'WMIDS', 'WMID');
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, LONG_DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG, AREA_CODE,
                              NOMS_REGION_CODE, GEOGRAPHIC_REGION_CODE)
VALUES ('LEI', 'LEEDS', 'HMP LEEDS', 'INST', 'Y', 'YORK', 'YOHUM', 'YORK');
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG, AREA_CODE, NOMS_REGION_CODE,
                              GEOGRAPHIC_REGION_CODE)
VALUES ('WAI', 'THE WEARE', 'INST', 'Y', 'WMID', 'WMIDS', 'WMID');
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG, DEACTIVATION_DATE)
VALUES ('OUT', 'OUTSIDE', 'INST', 'N', TO_DATE('2017-07-27', 'YYYY-MM-DD'));
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('TRN', 'TRANSFER', 'INST', 'Y');
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('MUL', 'MUL', 'INST', 'Y');
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('ZZGHI', 'GHOST', 'INST', 'N');
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG, AREA_CODE,
                              NOMS_REGION_CODE, GEOGRAPHIC_REGION_CODE)
VALUES ('RSI', 'RISLEY', 'INST', 'Y', 'NWEST', 'NW', 'NWEST');

INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG, JURISDICTION_CODE)
VALUES ('COURT1', 'Court 1', 'CRT', 'Y', 'MC');
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG, JURISDICTION_CODE)
VALUES ('ABDRCT', 'court 2', 'CRT', 'Y', 'YC');

INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('TRO', 'TROOM', 'INST', 'Y');
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, LONG_DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG, AREA_CODE, NOMS_REGION_CODE, GEOGRAPHIC_REGION_CODE)
VALUES ('MDI', 'MOORLAND', 'Moorland (HMP & YOI)', 'INST', 'Y', 'YORK', 'YOHUM', 'YORK');
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('SYI', 'SHREWSBURY', 'INST', 'Y');
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('RNI', 'RANBY (HMP)', 'INST', 'Y');

INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('HAZLWD', 'Hazelwood House', 'HSHOSP', 'Y');
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('ARNOLD', 'Arnold Lodge', 'HSHOSP', 'Y');
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('NHS014', 'Nottinghamshire MAPPP Unit', 'COMM', 'Y');

INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('*ALL*', 'Dummy entry for service switching', 'INST', 'N');
