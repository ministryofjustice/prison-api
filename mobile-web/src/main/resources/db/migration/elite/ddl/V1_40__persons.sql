CREATE TABLE PERSONS
-- This entity stores all kinds of persons, including
--1. Person
--2. Offender : person with active bookings
--3. Staff       :  person with system user account
(   PERSON_ID NUMBER(10) PRIMARY KEY,                            -- 'Primary Key of the Person';
    LAST_NAME VARCHAR2(35) NOT NULL,                             -- 'Last name of the offender';
    FIRST_NAME VARCHAR2(35) NOT NULL,                            -- 'Indicates the first name fo the offender.';
    MIDDLE_NAME VARCHAR2(35),                                    -- 'Indicates middle name of the offender.';
    BIRTHDATE DATE,                                              -- 'The birthdate';
    OCCUPATION_CODE VARCHAR2(12),                                -- 'Reference Code(OCCUPATION)';
    CRIMINAL_HISTORY_TEXT VARCHAR2(240),                         -- 'The criminal history';
    NAME_TYPE VARCHAR2(12),                                      -- 'Reference Code ( NAME_TYPE ) : Name qualifier - Given Name, Maiden Name ..';
    ALIAS_PERSON_ID NUMBER(10),                                  -- 'FK to Persons';
    ROOT_PERSON_ID NUMBER(10),                                   -- 'Point to the same person with different name';
    LANGUAGE_CODE VARCHAR2(12),                                  -- 'Reference Code (LANG)';
    COMPREHEND_ENGLISH_FLAG VARCHAR2(1),                         -- 'If the person can comprehed English';
    SEX VARCHAR2(12),                                            -- 'Reference Code (SEX)';
    BIRTH_PLACE VARCHAR2(25),                                    -- 'Place where the offender was born.';
    EMPLOYER VARCHAR2(60),                                       -- 'The name of the employer';
    PROFILE_CODE VARCHAR2(12),
    INTERPRETER_REQUIRED VARCHAR2(1),                            -- 'Interpreter required';
    PRIMARY_LANGUAGE_CODE VARCHAR2(12),                          -- 'The primary language of the person';
    MEMO_TEXT VARCHAR2(40),                                      -- 'General momo text';
    SUSPENDED_FLAG VARCHAR2(1),                                  -- 'If the person record suspended';
    MARITAL_STATUS VARCHAR2(12),                                 -- 'Reference Code(MARITAL_STAT)';
    CITIZENSHIP VARCHAR2(12),                                    -- 'Reference Code(COUNTRY)';
    DECEASED_DATE DATE,                                          -- 'The deceased date of the record';
    CORONER_NUMBER VARCHAR2(32),                                 -- 'Coroner reference number';
    ATTENTION VARCHAR2(40),                                      -- 'The name of the attendtion';
    CARE_OF VARCHAR2(40),                                        -- 'The name of the care of';
    SUSPENDED_DATE DATE,                                         -- 'The date of the record suspension';
    CREATE_DATETIME TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,     -- 'The timestamp when the record is created';
    CREATE_USER_ID VARCHAR2(32) DEFAULT user NOT NULL,           -- 'The user who creates the record';
    MODIFY_DATETIME TIMESTAMP DEFAULT SYSTIMESTAMP,              -- 'The timestamp when the record is modified ';
    MODIFY_USER_ID VARCHAR2(32),                                 -- 'The user who modifies the record';
    STAFF_FLAG VARCHAR2(1) DEFAULT 'N',                          -- 'If the person a staff member?';
    REMITTER_FLAG VARCHAR2(1), 
    LAST_NAME_SOUNDEX VARCHAR2(6), 
    FIRST_NAME_KEY VARCHAR2(35), 
    MIDDLE_NAME_KEY VARCHAR2(35), 
    LAST_NAME_KEY VARCHAR2(35), 
    SEAL_FLAG VARCHAR2(1)
);

CREATE INDEX PERSONS_NI1 ON PERSONS (LAST_NAME, FIRST_NAME)  ;
CREATE INDEX PERSONS_NI4 ON PERSONS (ALIAS_PERSON_ID) ;
CREATE INDEX PERSONS_NI5 ON PERSONS (BIRTHDATE)  ;
CREATE INDEX PERSONS_NI7 ON PERSONS (ROOT_PERSON_ID) ;
--CREATE INDEX PERSONS_NI6 ON PERSONS (SOUNDEX(LAST_NAME)) ;
--CREATE INDEX PERSONS_NI2 ON PERSONS (UPPER(LAST_NAME), UPPER(FIRST_NAME)) ;
