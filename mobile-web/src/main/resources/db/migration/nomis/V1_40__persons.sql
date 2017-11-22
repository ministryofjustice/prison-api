CREATE TABLE PERSONS
-- 'An individual who has or has had either direct or indirect contact with an offender. For example, a family member, an offenders victim, a prisoners lawyer. In certain circumstances, for example prison visits, a Person may be a NOMS employee (ie Staff) or an Offender under community supervision.';
(   PERSON_ID BIGINT PRIMARY KEY,                                --'Primary Key of the Person';
    LAST_NAME VARCHAR(35) NOT NULL,                              --'Last name of the offender';
    FIRST_NAME VARCHAR(35) NOT NULL,                             --'Indicates the first name fo the offender.';
    MIDDLE_NAME VARCHAR(35),                                     --'Indicates middle name of the offender.';
    BIRTHDATE DATE,                                              --'The birthdate';
    OCCUPATION_CODE VARCHAR(12),                                 --'Reference Code(OCCUPATION)';
    CRIMINAL_HISTORY_TEXT VARCHAR(240),                          --'The criminal history';
    NAME_TYPE VARCHAR(12),                                       --'Reference Code ( NAME_TYPE ) : Name qualifier - Given Name, Maiden Name ..';
    ALIAS_PERSON_ID BIGINT,                                      --'FK to Persons';
    ROOT_PERSON_ID BIGINT,                                       --'Point to the same person with different name';
    LANGUAGE_CODE VARCHAR(12),                                   --'Reference Code (LANG)';
    COMPREHEND_ENGLISH_FLAG VARCHAR(1) DEFAULT 'N',              --'If the person can comprehed English';
    SEX VARCHAR(12),                                             --'Reference Code (SEX)';
    BIRTH_PLACE VARCHAR(25),                                     --'Place where the offender was born.';
    EMPLOYER VARCHAR(60),                                        --'The name of the employer';
    PROFILE_CODE VARCHAR(12),
    INTERPRETER_REQUIRED VARCHAR(1) DEFAULT 'N',                 --'Interpreter required';
    PRIMARY_LANGUAGE_CODE VARCHAR(12),                           --'The primary language of the person';
    MEMO_TEXT VARCHAR(40),                                       --'General momo text';
    SUSPENDED_FLAG VARCHAR(1) DEFAULT 'N',                       --'If the person record suspended';
    MARITAL_STATUS VARCHAR(12),                                  --'Reference Code(MARITAL_STAT)';
    CITIZENSHIP VARCHAR(12),                                     --'Reference Code(COUNTRY)';
    DECEASED_DATE DATE,                                          --'The deceased date of the record';
    CORONER_NUMBER VARCHAR(32),                                  --'Coroner reference number';
    ATTENTION VARCHAR(40),                                       --'The name of the attendtion';
    CARE_OF VARCHAR(40),                                         --'The name of the care of';
    SUSPENDED_DATE DATE,                                         --'The date of the record suspension';
    CREATE_DATETIME TIMESTAMP NOT NULL DEFAULT now(),            --'The timestamp when the record is created';
    CREATE_USER_ID VARCHAR(32) NOT NULL DEFAULT USER,            --'The user who creates the record';
    MODIFY_DATETIME TIMESTAMP,                                   --'The timestamp when the record is modified ';
    MODIFY_USER_ID VARCHAR(32),                                  --'The user who modifies the record';
    NAME_SEQUENCE VARCHAR(12),                                   --'The order of names displayed';
    TITLE VARCHAR(12),                                           --'The title of the person';
    STAFF_FLAG VARCHAR(1) DEFAULT 'N',                           --'If the person a staff member?';
    REMITTER_FLAG VARCHAR(1) DEFAULT 'N',                        --'If the person a remitter ?';
    AUDIT_TIMESTAMP TIMESTAMP,
    AUDIT_USER_ID VARCHAR(32), 
    AUDIT_MODULE_NAME VARCHAR(65), 
    AUDIT_CLIENT_USER_ID VARCHAR(64), 
    AUDIT_CLIENT_IP_ADDRESS VARCHAR(39), 
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR(64), 
    AUDIT_ADDITIONAL_INFO VARCHAR(256), 
    KEEP_BIOMETRICS VARCHAR(1) DEFAULT 'N'
);

CREATE INDEX PERSONS_NI5 ON PERSONS (BIRTHDATE) ;
CREATE INDEX PERSONS_NI4 ON PERSONS (ALIAS_PERSON_ID) ;
CREATE INDEX PERSONS_NI1 ON PERSONS (LAST_NAME, FIRST_NAME) ;
CREATE INDEX PERSONS_NI7 ON PERSONS (ROOT_PERSON_ID)  ;
--CREATE INDEX PERSONS_NI6 ON PERSONS (SOUNDEX(LAST_NAME)) ;
--CREATE INDEX PERSONS_NI2 ON PERSONS (UPPER(LAST_NAME), UPPER(FIRST_NAME))  ;
