CREATE TABLE "SYSTEM_PROFILES"
(
  "PROFILE_TYPE"                  VARCHAR2(12 CHAR)                 NOT NULL ,
  "PROFILE_CODE"                  VARCHAR2(12 CHAR)                 NOT NULL ,
  "DESCRIPTION"                   VARCHAR2(80 CHAR),
  "PROFILE_VALUE"                 VARCHAR2(40 CHAR),
  "PROFILE_VALUE_2"               VARCHAR2(12 CHAR),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "OLD_TABLE_NAME"                VARCHAR2(50 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT user    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "SYSTEM_PROFILES_PK" PRIMARY KEY ("PROFILE_TYPE", "PROFILE_CODE")
);


COMMENT ON COLUMN "SYSTEM_PROFILES"."PROFILE_TYPE" IS 'Reference Code ( PROFILE_TYPE ) : Profile Type Such as (Label, Default Code Value ..)';

COMMENT ON COLUMN "SYSTEM_PROFILES"."PROFILE_CODE" IS 'Profile Code Value of the Profile Type(Such as Label for Prov, Default Currency Code)';

COMMENT ON COLUMN "SYSTEM_PROFILES"."DESCRIPTION" IS 'Description of the Profile Code';

COMMENT ON COLUMN "SYSTEM_PROFILES"."PROFILE_VALUE" IS 'Profile Code Value(Such as Default Currency Code = USD)';

COMMENT ON COLUMN "SYSTEM_PROFILES"."PROFILE_VALUE_2" IS 'The second value of the profile code (If there is one)';

COMMENT ON COLUMN "SYSTEM_PROFILES"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON COLUMN "SYSTEM_PROFILES"."OLD_TABLE_NAME" IS 'Old Table Name Used in Previous OMS Version';

COMMENT ON COLUMN "SYSTEM_PROFILES"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "SYSTEM_PROFILES"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "SYSTEM_PROFILES"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON TABLE "SYSTEM_PROFILES" IS 'System Profiles Such as Details Currency, Display Format, Installed Applications';

