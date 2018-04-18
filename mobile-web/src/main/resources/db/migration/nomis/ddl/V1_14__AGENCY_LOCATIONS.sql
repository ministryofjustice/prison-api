CREATE TABLE "AGENCY_LOCATIONS"
(
  "AGY_LOC_ID"                    VARCHAR2(6 CHAR)                  NOT NULL ,
  "DESCRIPTION"                   VARCHAR2(40 CHAR)                 NOT NULL ,
  "AGENCY_LOCATION_TYPE"          VARCHAR2(12 CHAR),
  "DISTRICT_CODE"                 VARCHAR2(12 CHAR),
  "UPDATED_ALLOWED_FLAG"          VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "ABBREVIATION"                  VARCHAR2(12 CHAR),
  "DEACTIVATION_DATE"             DATE,
  "CONTACT_NAME"                  VARCHAR2(40 CHAR),
  "PRINT_QUEUE"                   VARCHAR2(240 CHAR),
  "JURISDICTION_CODE"             VARCHAR2(12 CHAR),
  "BAIL_OFFICE_FLAG"              VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "LIST_SEQ"                      NUMBER(6, 0),
  "HOUSING_LEV_1_CODE"            VARCHAR2(12 CHAR),
  "HOUSING_LEV_2_CODE"            VARCHAR2(12 CHAR),
  "HOUSING_LEV_3_CODE"            VARCHAR2(12 CHAR),
  "HOUSING_LEV_4_CODE"            VARCHAR2(12 CHAR),
  "PROPERTY_LEV_1_CODE"           VARCHAR2(12 CHAR),
  "PROPERTY_LEV_2_CODE"           VARCHAR2(12 CHAR),
  "PROPERTY_LEV_3_CODE"           VARCHAR2(12 CHAR),
  "LAST_BOOKING_NO"               NUMBER(10, 0),
  "COMMISSARY_PRIVILEGE"          VARCHAR2(12 CHAR),
  "BUSINESS_HOURS"                VARCHAR2(40 CHAR),
  "ADDRESS_TYPE"                  VARCHAR2(12 CHAR),
  "SERVICE_REQUIRED_FLAG"         VARCHAR2(1 CHAR) DEFAULT NULL,
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT NULL,
  "DISABILITY_ACCESS_CODE"        VARCHAR2(12 CHAR),
  "INTAKE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT NULL,
  "SUB_AREA_CODE"                 VARCHAR2(12 CHAR),
  "AREA_CODE"                     VARCHAR2(12 CHAR),
  "NOMS_REGION_CODE"              VARCHAR2(12 CHAR),
  "GEOGRAPHIC_REGION_CODE"        VARCHAR2(12 CHAR),
  "JUSTICE_AREA_CODE"             VARCHAR2(12 CHAR),
  "CJIT_CODE"                     VARCHAR2(12 CHAR),
  "LONG_DESCRIPTION"              VARCHAR2(3000 CHAR),
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
  CONSTRAINT "AGENCY_LOCATIONS_PK" PRIMARY KEY ("AGY_LOC_ID"),
  CONSTRAINT "AGENCY_LOCATIONS_FK1" FOREIGN KEY ("AREA_CODE")
  REFERENCES "AREAS" ("AREA_CODE")
);

COMMENT ON COLUMN "AGENCY_LOCATIONS"."AGY_LOC_ID" IS ' The location residing within an agency.';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."DESCRIPTION" IS ' Description of the agency location.';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."AGENCY_LOCATION_TYPE" IS 'Refrence Code [ AGY_LOC_TYPE ]';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."DISTRICT_CODE" IS 'Reference Code [ DISTRICT ]';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."UPDATED_ALLOWED_FLAG" IS ' Indicates whether user can change table values.';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."ABBREVIATION" IS ' The abbreviation for the agency location.';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."DEACTIVATION_DATE" IS 'The date on which this location is no longer used.';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."CONTACT_NAME" IS ' Contact person at agency location.';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."PRINT_QUEUE" IS ' - Column already exists';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."JURISDICTION_CODE" IS 'Reference Code [ JURISDICTION ]';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."BAIL_OFFICE_FLAG" IS ' - Column already exists';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."LIST_SEQ" IS ' List seqence number for information entered.';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."HOUSING_LEV_1_CODE" IS 'Reference Code (LIVING_UNIT)';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."HOUSING_LEV_2_CODE" IS 'Reference Code (LIVING_UNIT)';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."HOUSING_LEV_3_CODE" IS 'Reference Code (LIVING_UNIT)';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."HOUSING_LEV_4_CODE" IS 'Reference Code (LIVING_UNIT)';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."PROPERTY_LEV_1_CODE" IS 'Reference Code ( PPTY_STG )';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."PROPERTY_LEV_2_CODE" IS 'Reference Code ( PPTY_STG )';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."PROPERTY_LEV_3_CODE" IS 'Reference Code ( PPTY_STG )';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."LAST_BOOKING_NO" IS 'The last booking number generated for the agency location.';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."ACTIVE_FLAG" IS 'Active data indicator';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."SUB_AREA_CODE" IS 'Reference Code(SUB_area) ';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."AREA_CODE" IS 'Reference Code(AREA) ';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."NOMS_REGION_CODE" IS 'Reference Code(NOMIS_REG) ';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."GEOGRAPHIC_REGION_CODE" IS 'Reference Code(GEO_REG) ';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."JUSTICE_AREA_CODE" IS 'Reference Code(JUSTICE_AREA) ';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."CJIT_CODE" IS 'NOMIS CJIT code ';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."LONG_DESCRIPTION" IS 'Long description ';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "AGENCY_LOCATIONS"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON TABLE "AGENCY_LOCATIONS" IS 'A physical location of an agency involved in the management or administration of offenders on a day to day basis, e.g. a prison, a probation office, a court. NOTE: The following special Agency Location cases exist: - Agy Loc Id = TRN - Represents a Pending Transfer status typically used following an Offender Transfer Out operation and awaiting a Transfer In at the destination establishment; Agy Loc Id = OUT - Represents an Outside status typically associated with offenders that are the subject of an out of jurisdiction transfer or personal property that has been disposed to relatives, for example.';


CREATE INDEX "AGENCY_LOCATIONS_FK1"
  ON "AGENCY_LOCATIONS" ("AREA_CODE");


