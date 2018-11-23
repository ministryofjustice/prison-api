
CREATE TABLE "PHONES"
(
  "PHONE_ID"                      NUMBER(10, 0)                     NOT NULL ,
  "OWNER_CLASS"                   VARCHAR2(12 CHAR)                 NOT NULL ,
  "OWNER_ID"                      NUMBER(10, 0),
  "OWNER_SEQ"                     NUMBER(6, 0),
  "OWNER_CODE"                    VARCHAR2(12 CHAR),
  "PHONE_TYPE"                    VARCHAR2(12 CHAR)                 NOT NULL ,
  "PHONE_NO"                      VARCHAR2(40 CHAR)                 NOT NULL ,
  "EXT_NO"                        VARCHAR2(7 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "PHONES_PK" PRIMARY KEY ("PHONE_ID")
);


COMMENT ON COLUMN "PHONES"."PHONE_ID" IS 'PK of the phone number';

COMMENT ON COLUMN "PHONES"."OWNER_CLASS" IS 'Ref Domain (owner_Class): ADDR-Address ; PER-Person ; OFF-Offender_Booking';

COMMENT ON COLUMN "PHONES"."OWNER_ID" IS 'FK to the PK of the phone owner';

COMMENT ON COLUMN "PHONES"."OWNER_SEQ" IS 'The sequence number of Owner class ';

COMMENT ON COLUMN "PHONES"."OWNER_CODE" IS 'The code of the owner when owner has varchar PK';

COMMENT ON COLUMN "PHONES"."PHONE_TYPE" IS 'Ref Domain (PHONE_TYPE)';

COMMENT ON COLUMN "PHONES"."PHONE_NO" IS 'The phone number';

COMMENT ON COLUMN "PHONES"."EXT_NO" IS 'The extension no of the phone';

COMMENT ON COLUMN "PHONES"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "PHONES"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "PHONES"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "PHONES"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON TABLE "PHONES"  IS 'Telephone Number details categorised by Phone Type (e.g. Home).';

CREATE INDEX "PHONES_NI1" ON "PHONES" ("OWNER_ID");
CREATE INDEX "PHONES_NI2" ON "PHONES" ("OWNER_CODE");
CREATE INDEX "PHONES_NI3" ON "PHONES" ("PHONE_NO");


