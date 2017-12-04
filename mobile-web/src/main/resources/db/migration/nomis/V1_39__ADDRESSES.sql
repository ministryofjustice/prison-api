CREATE TABLE "ADDRESSES"
(
  "ADDRESS_ID"                    NUMBER(10, 0)                     NOT NULL ,
  "OWNER_CLASS"                   VARCHAR2(12 CHAR)                 NOT NULL ,
  "OWNER_ID"                      NUMBER(10, 0),
  "OWNER_SEQ"                     NUMBER(6, 0),
  "OWNER_CODE"                    VARCHAR2(12 CHAR),
  "ADDRESS_TYPE"                  VARCHAR2(12 CHAR),
  "FLAT"                          VARCHAR2(30 CHAR),
  "PREMISE"                       VARCHAR2(50 CHAR),
  "STREET"                        VARCHAR2(160 CHAR),
  "LOCALITY"                      VARCHAR2(70 CHAR),
  "CITY_CODE"                     VARCHAR2(12 CHAR),
  "COUNTY_CODE"                   VARCHAR2(12 CHAR),
  "POSTAL_CODE"                   VARCHAR2(12 CHAR),
  "COUNTRY_CODE"                  VARCHAR2(12 CHAR),
  "VALIDATED_PAF_FLAG"            VARCHAR2(1 CHAR) DEFAULT 'N',
  "PRIMARY_FLAG"                  VARCHAR2(1 CHAR)                  NOT NULL ,
  "MAIL_FLAG"                     VARCHAR2(1 CHAR)                  NOT NULL ,
  "CAPACITY"                      NUMBER(5, 0),
  "COMMENT_TEXT"                  VARCHAR2(240 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "NO_FIXED_ADDRESS_FLAG"         VARCHAR2(1 CHAR) DEFAULT 'N',
  "SERVICES_FLAG"                 VARCHAR2(1 CHAR) DEFAULT 'N',
  "SPECIAL_NEEDS_CODE"            VARCHAR2(12 CHAR),
  "CONTACT_PERSON_NAME"           VARCHAR2(40 CHAR),
  "BUSINESS_HOUR"                 VARCHAR2(60 CHAR),
  "START_DATE"                    DATE,
  "END_DATE"                      DATE,
  "CITY_NAME"                     VARCHAR2(40 CHAR),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "ADDRESSES_PK" PRIMARY KEY ("ADDRESS_ID")
);

COMMENT ON COLUMN "ADDRESSES"."ADDRESS_ID" IS 'PK of an Address (Oracle Sequenc ADDRESS_ID)';
COMMENT ON COLUMN "ADDRESSES"."OWNER_CLASS" IS 'Ref Domain (OWNER_CLASS)';
COMMENT ON COLUMN "ADDRESSES"."OWNER_ID" IS 'PK ID of the Object';
COMMENT ON COLUMN "ADDRESSES"."OWNER_SEQ" IS 'PK Seq of the Object';
COMMENT ON COLUMN "ADDRESSES"."OWNER_CODE" IS 'PK code of the Object';
COMMENT ON COLUMN "ADDRESSES"."ADDRESS_TYPE" IS 'Reference Code (ADDR_TYPE)';
COMMENT ON COLUMN "ADDRESSES"."FLAT" IS 'The Partment/Flat No';
COMMENT ON COLUMN "ADDRESSES"."PREMISE" IS 'The Premise Name';
COMMENT ON COLUMN "ADDRESSES"."STREET" IS 'The Street';
COMMENT ON COLUMN "ADDRESSES"."LOCALITY" IS 'The locality';
COMMENT ON COLUMN "ADDRESSES"."CITY_CODE" IS 'Ref Domain (CITY)';
COMMENT ON COLUMN "ADDRESSES"."COUNTY_CODE" IS 'Ref Domain (COUNTY)';
COMMENT ON COLUMN "ADDRESSES"."POSTAL_CODE" IS 'The Postal Code';
COMMENT ON COLUMN "ADDRESSES"."COUNTRY_CODE" IS 'Ref Domain (COUNTRY) The country Code';
COMMENT ON COLUMN "ADDRESSES"."VALIDATED_PAF_FLAG" IS '? verified with PAF file';
COMMENT ON COLUMN "ADDRESSES"."PRIMARY_FLAG" IS '? Is this the primary address';
COMMENT ON COLUMN "ADDRESSES"."MAIL_FLAG" IS '? Is this the mailing address';
COMMENT ON COLUMN "ADDRESSES"."CAPACITY" IS 'Capacity of the address';
COMMENT ON COLUMN "ADDRESSES"."COMMENT_TEXT" IS 'The comment text of the address';
COMMENT ON COLUMN "ADDRESSES"."CREATE_DATETIME" IS 'The timestamp when the record is created';
COMMENT ON COLUMN "ADDRESSES"."CREATE_USER_ID" IS 'The user who creates the record';
COMMENT ON COLUMN "ADDRESSES"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';
COMMENT ON COLUMN "ADDRESSES"."MODIFY_USER_ID" IS 'The user who modifies the record';
COMMENT ON COLUMN "ADDRESSES"."NO_FIXED_ADDRESS_FLAG" IS '? if the address';
COMMENT ON COLUMN "ADDRESSES"."SERVICES_FLAG" IS '? if the address a services place';
COMMENT ON COLUMN "ADDRESSES"."SPECIAL_NEEDS_CODE" IS 'reference code(SPECIAL_NEEDS)';
COMMENT ON COLUMN "ADDRESSES"."CONTACT_PERSON_NAME" IS 'The name of the contact person';
COMMENT ON COLUMN "ADDRESSES"."BUSINESS_HOUR" IS 'The business hour. eg. 9:00 - 17:00';
COMMENT ON COLUMN "ADDRESSES"."START_DATE" IS 'The effective start date';
COMMENT ON COLUMN "ADDRESSES"."END_DATE" IS 'The end date';
COMMENT ON COLUMN "ADDRESSES"."CITY_NAME" IS 'The city name';
COMMENT ON TABLE "ADDRESSES" IS 'A Postal Address used by an individual, an agency location or an organisation. In the case of addressable entities (eg an Agency Location) which are not an offender an Address is used for a specific purpose, eg as a home address. NOTE: There cannot be more than one live primary and/or correspondence address for an addressable entity.';

CREATE INDEX "ADDRESSES_NI1" ON "ADDRESSES" ("OWNER_ID");
CREATE INDEX "ADDRESSES_NI2" ON "ADDRESSES" ("OWNER_CODE");
CREATE INDEX "ADDRESSES_NI3" ON "ADDRESSES" ("POSTAL_CODE");
CREATE INDEX "ADDRESSES_NI4" ON "ADDRESSES" ("CITY_CODE");
CREATE INDEX "ADDRESSES_NI5" ON "ADDRESSES" ("COUNTY_CODE");


