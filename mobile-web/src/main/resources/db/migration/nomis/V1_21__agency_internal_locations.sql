CREATE TABLE AGENCY_INTERNAL_LOCATIONS
(
  INTERNAL_LOCATION_ID          BIGSERIAL     PRIMARY KEY   NOT NULL,
  INTERNAL_LOCATION_CODE        VARCHAR(12)                 NOT NULL,
  AGY_LOC_ID                    VARCHAR(6)                  NOT NULL,
  INTERNAL_LOCATION_TYPE        VARCHAR(12)                 NOT NULL,
  DESCRIPTION                   VARCHAR(240)                NOT NULL,
  SECURITY_LEVEL_CODE           VARCHAR(12),
  CAPACITY                      INTEGER,
  CREATE_USER_ID                VARCHAR(32)   DEFAULT USER  NOT NULL,
  PARENT_INTERNAL_LOCATION_ID   BIGINT,
  ACTIVE_FLAG                   VARCHAR(1)    DEFAULT 'Y'   NOT NULL,
  LIST_SEQ                      INTEGER,
  CREATE_DATETIME               TIMESTAMP     DEFAULT now() NOT NULL,
  MODIFY_DATETIME               TIMESTAMP,
  MODIFY_USER_ID                VARCHAR(32),
  CNA_NO                        BIGINT,
  CERTIFIED_FLAG                VARCHAR(1)    DEFAULT 'N'   NOT NULL,
  DEACTIVATE_DATE               DATE,
  REACTIVATE_DATE               DATE,
  DEACTIVATE_REASON_CODE        VARCHAR(12),
  COMMENT_TEXT                  VARCHAR(240),
  USER_DESC                     VARCHAR(40),
  ACA_CAP_RATING                INTEGER,
  UNIT_TYPE                     VARCHAR(12),
  OPERATION_CAPACITY            INTEGER,
  NO_OF_OCCUPANT                BIGINT        DEFAULT 0,
  TRACKING_FLAG                 VARCHAR(1)    DEFAULT 'N'   NOT NULL,
  AUDIT_TIMESTAMP               TIMESTAMP,
  AUDIT_USER_ID                 VARCHAR(32),
  AUDIT_MODULE_NAME             VARCHAR(65),
  AUDIT_CLIENT_USER_ID          VARCHAR(64),
  AUDIT_CLIENT_IP_ADDRESS       VARCHAR(39),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR(64),
  AUDIT_ADDITIONAL_INFO         VARCHAR(256)
);

ALTER TABLE AGENCY_INTERNAL_LOCATIONS ADD UNIQUE (AGY_LOC_ID, DESCRIPTION);
