CREATE TABLE "AREAS"
(
  "AREA_CLASS"                    VARCHAR2(12 CHAR)                 NOT NULL ,
  "AREA_CODE"                     VARCHAR2(12 CHAR)                 NOT NULL ,
  "DESCRIPTION"                   VARCHAR2(40 CHAR)                 NOT NULL ,
  "PARENT_AREA_CODE"              VARCHAR2(12 CHAR),
  "LIST_SEQ"                      NUMBER(6, 0),
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "EXPIRY_DATE"                   DATE,
  "AREA_TYPE"                     VARCHAR2(12 CHAR),
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
  CONSTRAINT "AREAS_PK" PRIMARY KEY ("AREA_CODE"),
  CONSTRAINT "AREAS_AREAS_FK" FOREIGN KEY ("PARENT_AREA_CODE")
  REFERENCES "AREAS" ("AREA_CODE")
);

COMMENT ON COLUMN "AREAS"."AREA_CLASS" IS 'The area class (REGION;AREA;SUB_AREA). Reference_Code(AREA_CLASS)';

COMMENT ON COLUMN "AREAS"."AREA_CODE" IS 'The area code';

COMMENT ON COLUMN "AREAS"."DESCRIPTION" IS 'Description';

COMMENT ON COLUMN "AREAS"."PARENT_AREA_CODE" IS 'The parent of the area code';

COMMENT ON COLUMN "AREAS"."LIST_SEQ" IS 'The listing order';

COMMENT ON COLUMN "AREAS"."ACTIVE_FLAG" IS '?if the record actively used';

COMMENT ON COLUMN "AREAS"."EXPIRY_DATE" IS 'expiry date of the ara';

COMMENT ON COLUMN "AREAS"."AREA_TYPE" IS 'The area usage based on the agency location Type.  Reference_Code(AREA_TYPE)';

COMMENT ON COLUMN "AREAS"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "AREAS"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "AREAS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "AREAS"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON TABLE "AREAS" IS 'The NOMIS areas, including regions and areas';


CREATE INDEX "AREAS_NI1"
  ON "AREAS" ("PARENT_AREA_CODE");


