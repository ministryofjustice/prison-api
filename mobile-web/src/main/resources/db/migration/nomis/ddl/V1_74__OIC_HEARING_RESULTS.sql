CREATE TABLE "OIC_HEARING_RESULTS"
(
  "OIC_HEARING_ID"                NUMBER(10, 0)                     NOT NULL ,
  "RESULT_SEQ"                    NUMBER(6, 0)                      NOT NULL ,
  "AGENCY_INCIDENT_ID"            NUMBER(10, 0)                     NOT NULL ,
  "CHARGE_SEQ"                    NUMBER(6, 0)                      NOT NULL ,
  "PLEA_FINDING_CODE"             VARCHAR2(12 CHAR)                 NOT NULL ,
  "FINDING_CODE"                  VARCHAR2(12 CHAR)                 NOT NULL ,
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
  "OIC_OFFENCE_ID"                NUMBER(10, 0)                     NOT NULL ,
  CONSTRAINT "OIC_HEARING_RESULTS_PK" PRIMARY KEY ("OIC_HEARING_ID", "RESULT_SEQ"),
  CONSTRAINT "OIC_HEARING_RESULTS_UK" UNIQUE ("OIC_HEARING_ID", "AGENCY_INCIDENT_ID", "CHARGE_SEQ")
--  CONSTRAINT "OIC_HEARING_RESULTS_FK1" FOREIGN KEY ("OIC_OFFENCE_ID") REFERENCES "OIC_OFFENCES" ("OIC_OFFENCE_ID")   ,
--  CONSTRAINT "OIC_HR_OIC_HEAR_F1" FOREIGN KEY ("OIC_HEARING_ID") REFERENCES "OIC_HEARINGS" ("OIC_HEARING_ID") ,
--  CONSTRAINT "OIC_HR_OIC_AGY_INC_CHG_FK" FOREIGN KEY ("AGENCY_INCIDENT_ID", "CHARGE_SEQ") REFERENCES "AGENCY_INCIDENT_CHARGES" ("AGENCY_INCIDENT_ID", "CHARGE_SEQ")
);


COMMENT ON COLUMN "OIC_HEARING_RESULTS"."OIC_HEARING_ID" IS 'System generated primary key for hearing.';

COMMENT ON COLUMN "OIC_HEARING_RESULTS"."RESULT_SEQ" IS 'Sequential number for hearing results.';

COMMENT ON COLUMN "OIC_HEARING_RESULTS"."AGENCY_INCIDENT_ID" IS 'System generated seqential log number for the incident.';

COMMENT ON COLUMN "OIC_HEARING_RESULTS"."CHARGE_SEQ" IS 'Sequential number for charge.';

COMMENT ON COLUMN "OIC_HEARING_RESULTS"."PLEA_FINDING_CODE" IS 'Reference Code ( FINDING ). The offender"s plea on this charge.';

COMMENT ON COLUMN "OIC_HEARING_RESULTS"."FINDING_CODE" IS 'Reference Code ( FINDING )';

COMMENT ON COLUMN "OIC_HEARING_RESULTS"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "OIC_HEARING_RESULTS"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "OIC_HEARING_RESULTS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "OIC_HEARING_RESULTS"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON TABLE "OIC_HEARING_RESULTS"  IS 'The outcome of an adjudication hearing in respect of a specific charge. NOTE1 : there cannot be more than one result per charge. If a result is quashed on appeal then the status of the result is changed to quashed. NOTE2 : it follows from Note1 above that (a) attributes Plea Finding Code & Finding Code & OIC Hearing Id belong logically within the Agency Incident Charge entity (ie. they are dependent on the Charge - not on both Charge & Hearing). In other words, this entity is logically redundant. Hence, it is represented here with a 1:1 relationship with Agency incident Charge. NOTE3 : as per the comment on parent entity Agency Incident Charge, the FK inherited from that parent is a natural key rather than the physical primary key of the parent';


CREATE INDEX "OIC_HEARING_RESULTS_FK1" ON "OIC_HEARING_RESULTS" ("OIC_OFFENCE_ID");
CREATE INDEX "OIC_HR_OIC_AGY_INC_CHG_FK" ON "OIC_HEARING_RESULTS" ("AGENCY_INCIDENT_ID", "CHARGE_SEQ");