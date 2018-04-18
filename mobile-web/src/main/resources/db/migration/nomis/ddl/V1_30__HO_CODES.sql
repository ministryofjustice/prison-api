CREATE TABLE "HO_CODES"
(
  "HO_CODE"                       VARCHAR2(12 CHAR)                 NOT NULL ,
  "DESCRIPTION"                   VARCHAR2(240 CHAR)                NOT NULL ,
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "EXPIRY_DATE"                   DATE,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "HO_CODES_PK" PRIMARY KEY ("HO_CODE")
) ;


COMMENT ON COLUMN "HO_CODES"."HO_CODE" IS 'It is a combination of HO Class/HO Sub Class';

COMMENT ON COLUMN "HO_CODES"."DESCRIPTION" IS 'It is the description of the HO Code';

COMMENT ON COLUMN "HO_CODES"."ACTIVE_FLAG" IS 'Active code if it is ''Y'', Inactive code if it is ''N''';

COMMENT ON COLUMN "HO_CODES"."EXPIRY_DATE" IS 'Expiry Date of the Code';

COMMENT ON COLUMN "HO_CODES"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "HO_CODES"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON COLUMN "HO_CODES"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "HO_CODES"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON TABLE "HO_CODES" IS 'This table will store the HO codes and Description of the HO codes';


