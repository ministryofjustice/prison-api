CREATE TABLE "IMPRISONMENT_STATUSES"
(
  "IMPRISONMENT_STATUS_ID"        NUMBER(10, 0)                     NOT NULL ENABLE,
  "IMPRISONMENT_STATUS"           VARCHAR2(12 CHAR)                 NOT NULL ENABLE,
  "DESCRIPTION"                   VARCHAR2(40 CHAR)                 NOT NULL ENABLE,
  "BAND_CODE"                     VARCHAR2(12 CHAR)                 NOT NULL ENABLE,
  "RANK_VALUE"                    NUMBER(6, 0)                      NOT NULL ENABLE,
  "IMPRISONMENT_STATUS_SEQ"       NUMBER(6, 0),
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ENABLE,
  "EXPIRY_DATE"                   DATE,
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ENABLE,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ENABLE,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "IMPRISONMENT_STATUSES_PK" PRIMARY KEY ("IMPRISONMENT_STATUS_ID"),
  CONSTRAINT "IMPRISONMENT_STATUSES_UK1" UNIQUE ("IMPRISONMENT_STATUS", "ACTIVE_FLAG", "EXPIRY_DATE"),
  CONSTRAINT "IMPRISONMENT_STATUSES_CK1" CHECK ((active_flag = 'Y' AND expiry_date IS NULL)
                                                OR (active_flag = 'N' AND expiry_date IS NOT NULL)) ENABLE
);


COMMENT ON COLUMN "IMPRISONMENT_STATUSES"."IMPRISONMENT_STATUS_ID" IS 'Primary key generated from sequence imprisonment_status_id';

COMMENT ON COLUMN "IMPRISONMENT_STATUSES"."IMPRISONMENT_STATUS" IS 'Imprisonment Status Code ';

COMMENT ON COLUMN "IMPRISONMENT_STATUSES"."DESCRIPTION" IS 'Description of Imprisonment Status ';

COMMENT ON COLUMN "IMPRISONMENT_STATUSES"."BAND_CODE" IS 'Banding code used to group main imprisonment statuses (reference code domain IMPSBAND)';

COMMENT ON COLUMN "IMPRISONMENT_STATUSES"."RANK_VALUE" IS 'Rank value to differentiate predominant';

COMMENT ON COLUMN "IMPRISONMENT_STATUSES"."IMPRISONMENT_STATUS_SEQ" IS 'User inputted value to determine the display sequence when displayed on an Oracle Form';

COMMENT ON COLUMN "IMPRISONMENT_STATUSES"."ACTIVE_FLAG" IS 'Indicates whether the row is active or not (Y/N)';

COMMENT ON COLUMN "IMPRISONMENT_STATUSES"."EXPIRY_DATE" IS 'Date the row was made inactive';

COMMENT ON TABLE "IMPRISONMENT_STATUSES" IS 'Holds imprisonment statuses and associated banding code and ranking';


