CREATE TABLE MV_OFFENDER_MATCH_DETAILS (
  OFFENDER_ID_DISPLAY         VARCHAR2(10),
  OFFENDER_ID                 NUMBER(10),
  LAST_NAME_KEY               VARCHAR2(35),
  FIRST_NAME_KEY              VARCHAR2(35),
  BIRTH_DATE                  DATE,
  BIRTH_PLACE                 VARCHAR2(25),
  AVG_HEIGHT                  NUMBER(6, 0),
  FLAT                        VARCHAR2(30),
  PREMISE                     VARCHAR2(50),
  STREET                      VARCHAR2(160),
  LOCALITY                    VARCHAR2(70),
  CITY_CODE                   VARCHAR2(12),
  COUNTY_CODE                 VARCHAR2(12),
  POSTAL_CODE                 VARCHAR2(12),
  ADDRESS_USAGE               VARCHAR2(12)
);

-- The reference offender:
INSERT INTO MV_OFFENDER_MATCH_DETAILS VALUES ('A1234AA', 001, 'Smith', 'John',  to_date('1990-01-02', 'YYYY-MM-DD'), 'YORKSHIRE',  190, 'Flat 1', 'The Flats', 'Flat Street', 'Flatland', 'Sheffield', 'S YORKSHIRE', 'S1 0OO', 'HOME');

-- Matches:
INSERT INTO MV_OFFENDER_MATCH_DETAILS VALUES ('B1234BB', 002, 'Smith', 'John',  to_date('1990-01-02', 'YYYY-MM-DD'), 'YORKSHIRE',  190, 'Flat 1', 'The Flats', 'Flat Street', 'Flatland', 'Sheffield', 'S YORKSHIRE', 'S1 0OO', 'HOME');
INSERT INTO MV_OFFENDER_MATCH_DETAILS VALUES ('C1234CC', 003, 'Smith', 'Alias', to_date('1999-01-01', 'YYYY-MM-DD'), 'YORKSHIRE',  191, 'Flat 1', 'The Flats', 'Flat Street', 'Flatland', 'Sheffield', 'S YORKSHIRE', 'S1 0OO', 'HOME');
INSERT INTO MV_OFFENDER_MATCH_DETAILS VALUES ('D1234DD', 004, 'Alias', 'John',  to_date('1990-01-02', 'YYYY-MM-DD'), 'LANCASHIRE', 188, NULL, 'Other', 'Flat Street', NULL, 'Sheffield', 'S YORKSHIRE', 'S1 0OO', 'HOME');

-- Matches when taking into account data across all aliases,
-- even though none would individually be counted as a match
-- (see the 'Not a match' section for proof)
INSERT INTO MV_OFFENDER_MATCH_DETAILS VALUES ('E1234EE', 005, 'Someother', 'Alias',  to_date('1990-01-02', 'YYYY-MM-DD'), 'YORKSHIRE',  0,   NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'HOME');
INSERT INTO MV_OFFENDER_MATCH_DETAILS VALUES ('E1234EE', 006, 'Smith',     'Alias',  to_date('2005-06-07', 'YYYY-MM-DD'), 'LANCASHIRE', 0,   NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'HOME');
INSERT INTO MV_OFFENDER_MATCH_DETAILS VALUES ('E1234EE', 007, 'Alias',     'John',   to_date('2006-07-08', 'YYYY-MM-DD'), 'LANCASHIRE', 190, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'HOME');
INSERT INTO MV_OFFENDER_MATCH_DETAILS VALUES ('E1234EE', 008, 'Alias',     'Alias',  to_date('2007-08-09', 'YYYY-MM-DD'), 'LANCASHIRE', 200, 'Flat 1', 'The Flats', 'Flat Street', 'Flatland', 'Sheffield', 'S YORKSHIRE', 'S1 0OO', 'HOME');

-- Not a match:
INSERT INTO MV_OFFENDER_MATCH_DETAILS VALUES ('V1234VV', 005, 'Someother', 'Alias',  to_date('1990-01-02', 'YYYY-MM-DD'), 'YORKSHIRE',  0,   NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'HOME');
INSERT INTO MV_OFFENDER_MATCH_DETAILS VALUES ('W1234WW', 006, 'Smith',     'Alias',  to_date('2005-06-07', 'YYYY-MM-DD'), 'LANCASHIRE', 0,   NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'HOME');
INSERT INTO MV_OFFENDER_MATCH_DETAILS VALUES ('X1234XX', 007, 'Alias',     'John',   to_date('2006-07-08', 'YYYY-MM-DD'), 'LANCASHIRE', 190, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'HOME');
INSERT INTO MV_OFFENDER_MATCH_DETAILS VALUES ('Y1234YY', 008, 'Alias',     'Alias',  to_date('2007-08-09', 'YYYY-MM-DD'), 'LANCASHIRE', 200, 'Flat 1', 'The Flats', 'Flat Street', 'Flatland', 'Sheffield', 'S YORKSHIRE', 'S1 0OO', 'HOME');

-- Not a match (entirely different):
INSERT INTO MV_OFFENDER_MATCH_DETAILS VALUES ('Z1234ZZ', 009, 'Bloggs',    'Joe',    to_date('1935-12-13', 'YYYY-MM-DD'), 'LANCASHIRE', 150, 'Apartment 999', 'Apartment Complex', 'Apartment Place', 'Apartmentville', 'Manchester', 'MANCHESTER', 'M2 3AB', 'HOME');
