CREATE TABLE "CONTACT_PERSON_TYPES"
(
  "CONTACT_TYPE"                  VARCHAR2(12 CHAR)                 NOT NULL ,
  "RELATIONSHIP_TYPE"             VARCHAR2(12 CHAR)                 NOT NULL ,
  "LIST_SEQ"                      NUMBER(6, 0),
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "UPDATE_ALLOWED_FLAG"           VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "EXPIRY_DATE"                   DATE,
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "CONTACT_CLASS"                 VARCHAR2(12 CHAR),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "CONTACT_PERSON_TYPES_PK" PRIMARY KEY ("CONTACT_TYPE", "RELATIONSHIP_TYPE")
) ;


COMMENT ON COLUMN "CONTACT_PERSON_TYPES"."CONTACT_TYPE" IS 'Refrence Code [CONTACTS]. The contact type with offender ie. Emergency, Professional ..';

COMMENT ON COLUMN "CONTACT_PERSON_TYPES"."RELATIONSHIP_TYPE" IS 'Refrence Code [ RELATIONSHIP ]. The relationship with offender based upon type.';

COMMENT ON COLUMN "CONTACT_PERSON_TYPES"."LIST_SEQ" IS 'Listing order of the code (It for controlling the order of listing in LOV).  If the value is "0", then it is taken as the default for this domain';

COMMENT ON COLUMN "CONTACT_PERSON_TYPES"."ACTIVE_FLAG" IS 'If the code active ?';

COMMENT ON COLUMN "CONTACT_PERSON_TYPES"."UPDATE_ALLOWED_FLAG" IS 'If the code allowed to changed ( It is for controlling the code)';

COMMENT ON COLUMN "CONTACT_PERSON_TYPES"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON COLUMN "CONTACT_PERSON_TYPES"."EXPIRY_DATE" IS 'The date which the code is no longer used';

COMMENT ON COLUMN "CONTACT_PERSON_TYPES"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "CONTACT_PERSON_TYPES"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "CONTACT_PERSON_TYPES"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "CONTACT_PERSON_TYPES"."CONTACT_CLASS" IS 'Ref Code(CONT_CLS)';

COMMENT ON TABLE "CONTACT_PERSON_TYPES" IS '- Retrofitted';

