
INSERT INTO AGENCY_LOCATIONS (AGY_LOC_ID, DESCRIPTION, AGENCY_LOCATION_TYPE, ACTIVE_FLAG)
VALUES ('BXI', 'BRIXTON', 'INST', 'Y'),
       ('BMI', 'BIRMINGHAM', 'INST', 'Y'),
       ('LEI', 'LEEDS', 'INST', 'Y'),
       ('WAI', 'THE WEARE', 'INST', 'Y'),
       ('OUT', 'OUTSIDE', 'INST', 'Y'),
       ('TRN', 'TRANSFER', 'INST', 'Y'),
       ('MUL', 'MUL', 'INST', 'Y'),
       ('ZZGHI', 'GHOST', 'INST', 'N'),
       ('COURT1', 'Court 1', 'CRT', 'Y'),
       ('ABDRCT', 'Court 2', 'CRT', 'Y'),
       ('TRO', 'TROOM', 'INST', 'Y');

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

INSERT INTO AGENCY_INTERNAL_LOCATIONS (INTERNAL_LOCATION_ID, INTERNAL_LOCATION_CODE, INTERNAL_LOCATION_TYPE, AGY_LOC_ID, DESCRIPTION, PARENT_INTERNAL_LOCATION_ID, USER_DESC)
VALUES (-1, 'A', 'WING', 'LEI', 'LEI-A', null, 'Block A'),
       (-2, '1', 'LAND', 'LEI', 'LEI-A-1', -1, 'Landing A/1'),
       (-3, '1', 'CELL', 'LEI', 'LEI-A-1-1', -2, null),
       (-4, '2', 'CELL', 'LEI', 'LEI-A-1-2', -2, null),
       (-5, '3', 'CELL', 'LEI', 'LEI-A-1-3', -2, null),
       (-6, '4', 'CELL', 'LEI', 'LEI-A-1-4', -2, null),
       (-7, '5', 'CELL', 'LEI', 'LEI-A-1-5', -2, null),
       (-8, '6', 'CELL', 'LEI', 'LEI-A-1-6', -2, null),
       (-9, '7', 'CELL', 'LEI', 'LEI-A-1-7', -2, null),
       (-10, '8', 'CELL', 'LEI', 'LEI-A-1-8', -2, null),
       (-11, '9', 'CELL', 'LEI', 'LEI-A-1-9', -2, null),
       (-12, '10', 'CELL', 'LEI', 'LEI-A-1-10', -2, null),
       (-13, 'H', 'WING', 'LEI', 'LEI-H', null, 'Block H'),
       (-14, '1', 'LAND', 'LEI', 'LEI-H-1', -13, 'Landing H/1'),
       (-15, '1', 'CELL', 'LEI', 'LEI-H-1-1', -14, null),
       (-16, '2', 'CELL', 'LEI', 'LEI-H-1-2', -14, null),
       (-17, '3', 'CELL', 'LEI', 'LEI-H-1-3', -14, null),
       (-18, '4', 'CELL', 'LEI', 'LEI-H-1-4', -14, null),
       (-19, '5', 'CELL', 'LEI', 'LEI-H-1-5', -14, null),
       (-20, '6', 'CELL', 'LEI', 'LEI-H-1-6', -14, null),
       (-21, '7', 'CELL', 'LEI', 'LEI-H-1-7', -14, null),
       (-22, '8', 'CELL', 'LEI', 'LEI-H-1-8', -14, null),
       (-23, '9', 'CELL', 'LEI', 'LEI-H-1-9', -14, null),
       (-24, '10', 'CELL', 'LEI', 'LEI-H-1-10', -14, null),
       (-25, 'CHAP', 'AREA', 'LEI', 'LEI-CHAP', null, 'Chapel'),
       (-26, 'CARP', 'WSHP', 'LEI', 'LEI-CARP', null, 'Carpentry Workshop'),
       (-27, 'CRM1', 'CLAS', 'LEI', 'LEI-CRM1', null, 'Classroom 1'),
       (-28, 'VIS', 'VISIT', 'LEI', 'LEI-VIS', null, 'Visiting Room'),
       (-29, 'MED', 'AREA', 'LEI', 'LEI-MED', null, 'Medical Centre'),
       (-30, 'A', 'WING', 'ZZGHI', 'ZZGHI-A', null, 'Block A');
