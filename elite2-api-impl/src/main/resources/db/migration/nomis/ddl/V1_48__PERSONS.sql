CREATE TABLE "PERSONS"
(
  "PERSON_ID"                     NUMBER(10, 0)                     NOT NULL ,
  "LAST_NAME"                     VARCHAR2(35 CHAR)                 NOT NULL ,
  "FIRST_NAME"                    VARCHAR2(35 CHAR)                 NOT NULL ,
  "MIDDLE_NAME"                   VARCHAR2(35 CHAR),
  "BIRTHDATE"                     DATE,
  "OCCUPATION_CODE"               VARCHAR2(12 CHAR),
  "CRIMINAL_HISTORY_TEXT"         VARCHAR2(240 CHAR),
  "NAME_TYPE"                     VARCHAR2(12 CHAR),
  "ALIAS_PERSON_ID"               NUMBER(10, 0),
  "ROOT_PERSON_ID"                NUMBER(10, 0),
  "LANGUAGE_CODE"                 VARCHAR2(12 CHAR),
  "COMPREHEND_ENGLISH_FLAG"       VARCHAR2(1 CHAR) DEFAULT 'N',
  "SEX"                           VARCHAR2(12 CHAR),
  "BIRTH_PLACE"                   VARCHAR2(25 CHAR),
  "EMPLOYER"                      VARCHAR2(60 CHAR),
  "PROFILE_CODE"                  VARCHAR2(12 CHAR),
  "INTERPRETER_REQUIRED"          VARCHAR2(1 CHAR) DEFAULT 'N',
  "PRIMARY_LANGUAGE_CODE"         VARCHAR2(12 CHAR),
  "MEMO_TEXT"                     VARCHAR2(40 CHAR),
  "SUSPENDED_FLAG"                VARCHAR2(1 CHAR) DEFAULT 'N',
  "MARITAL_STATUS"                VARCHAR2(12 CHAR),
  "CITIZENSHIP"                   VARCHAR2(12 CHAR),
  "DECEASED_DATE"                 DATE,
  "CORONER_NUMBER"                VARCHAR2(32 CHAR),
  "ATTENTION"                     VARCHAR2(40 CHAR),
  "CARE_OF"                       VARCHAR2(40 CHAR),
  "SUSPENDED_DATE"                DATE,
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "NAME_SEQUENCE"                 VARCHAR2(12 CHAR),
  "TITLE"                         VARCHAR2(12 CHAR),
  "STAFF_FLAG"                    VARCHAR2(1 CHAR) DEFAULT 'N',
  "REMITTER_FLAG"                 VARCHAR2(1 CHAR) DEFAULT 'N',
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  "KEEP_BIOMETRICS"               VARCHAR2(1 CHAR) DEFAULT 'N',
  CONSTRAINT "PERSONS_PK" PRIMARY KEY ("PERSON_ID") ,
  CONSTRAINT "PERSONS_PERSONS_FK" FOREIGN KEY ("ROOT_PERSON_ID")
  REFERENCES "PERSONS" ("PERSON_ID")
);


COMMENT ON COLUMN "PERSONS"."PERSON_ID" IS 'Primary Key of the Person';

COMMENT ON COLUMN "PERSONS"."LAST_NAME" IS 'Last name of the offender';

COMMENT ON COLUMN "PERSONS"."FIRST_NAME" IS 'Indicates the first name fo the offender.';

COMMENT ON COLUMN "PERSONS"."MIDDLE_NAME" IS 'Indicates middle name of the offender.';

COMMENT ON COLUMN "PERSONS"."BIRTHDATE" IS 'The birthdate';

COMMENT ON COLUMN "PERSONS"."OCCUPATION_CODE" IS 'Reference Code(OCCUPATION)';

COMMENT ON COLUMN "PERSONS"."CRIMINAL_HISTORY_TEXT" IS 'The criminal history';

COMMENT ON COLUMN "PERSONS"."NAME_TYPE" IS 'Reference Code ( NAME_TYPE ) : Name qualifier - Given Name, Maiden Name ..';

COMMENT ON COLUMN "PERSONS"."ALIAS_PERSON_ID" IS 'FK to Persons';

COMMENT ON COLUMN "PERSONS"."ROOT_PERSON_ID" IS 'Point to the same person with different name';

COMMENT ON COLUMN "PERSONS"."LANGUAGE_CODE" IS 'Reference Code (LANG)';

COMMENT ON COLUMN "PERSONS"."COMPREHEND_ENGLISH_FLAG" IS 'If the person can comprehed English';

COMMENT ON COLUMN "PERSONS"."SEX" IS 'Reference Code (SEX)';

COMMENT ON COLUMN "PERSONS"."BIRTH_PLACE" IS 'Place where the offender was born.';

COMMENT ON COLUMN "PERSONS"."EMPLOYER" IS 'The name of the employer';

COMMENT ON COLUMN "PERSONS"."INTERPRETER_REQUIRED" IS 'Interpreter required';

COMMENT ON COLUMN "PERSONS"."PRIMARY_LANGUAGE_CODE" IS 'The primary language of the person';

COMMENT ON COLUMN "PERSONS"."MEMO_TEXT" IS 'General momo text';

COMMENT ON COLUMN "PERSONS"."SUSPENDED_FLAG" IS 'If the person record supsended';

COMMENT ON COLUMN "PERSONS"."MARITAL_STATUS" IS 'Reference Code(MARITAL_STAT)';

COMMENT ON COLUMN "PERSONS"."CITIZENSHIP" IS 'Reference Code(COUNTRY)';

COMMENT ON COLUMN "PERSONS"."DECEASED_DATE" IS 'The deceased date of the record';

COMMENT ON COLUMN "PERSONS"."CORONER_NUMBER" IS 'Coroner reference number';

COMMENT ON COLUMN "PERSONS"."ATTENTION" IS 'The name of the attendtion';

COMMENT ON COLUMN "PERSONS"."CARE_OF" IS 'The name of the care of';

COMMENT ON COLUMN "PERSONS"."SUSPENDED_DATE" IS 'The date of the record suspension';

COMMENT ON COLUMN "PERSONS"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "PERSONS"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "PERSONS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "PERSONS"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON COLUMN "PERSONS"."NAME_SEQUENCE" IS 'The order of names displayed';

COMMENT ON COLUMN "PERSONS"."TITLE" IS 'The title of the person';

COMMENT ON COLUMN "PERSONS"."STAFF_FLAG" IS 'If the person a staff member?';

COMMENT ON COLUMN "PERSONS"."REMITTER_FLAG" IS 'If the person a remitter ?';

COMMENT ON TABLE "PERSONS" IS 'An individual who has or has had either direct or indirect contact with an offender. For example, a family member, an offenders victim, a prisoners lawyer. In certain circumstances, for example prison visits, a Person may be a NOMS employee (ie Staff) or an Offender under community supervision.';


CREATE INDEX "PERSONS_NI1"
  ON "PERSONS" ("LAST_NAME", "FIRST_NAME");

CREATE INDEX "PERSONS_NI4"
  ON "PERSONS" ("ALIAS_PERSON_ID");

CREATE INDEX "PERSONS_NI5"
  ON "PERSONS" ("BIRTHDATE");

CREATE INDEX "PERSONS_NI7"
  ON "PERSONS" ("ROOT_PERSON_ID");

