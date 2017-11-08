CREATE TABLE OFFENDER_ALERTS
(
  ALERT_DATE                    DATE          DEFAULT now() NOT NULL,
  OFFENDER_BOOK_ID              BIGINT                      NOT NULL,
  ROOT_OFFENDER_ID              BIGINT,
  ALERT_SEQ                     INTEGER                     NOT NULL,
  ALERT_TYPE                    VARCHAR(12)                 NOT NULL,
  ALERT_CODE                    VARCHAR(12)                 NOT NULL,
  AUTHORIZE_PERSON_TEXT         VARCHAR(40),
  CREATE_DATE                   DATE,
  ALERT_STATUS                  VARCHAR(12)                 NOT NULL,
  VERIFIED_FLAG                 VARCHAR(1)    DEFAULT 'N'   NOT NULL,
  EXPIRY_DATE                   DATE,
  COMMENT_TEXT                  VARCHAR(1000),
  CASELOAD_ID                   VARCHAR(6),
  MODIFY_USER_ID                VARCHAR(32),
  MODIFY_DATETIME               TIMESTAMP,
  CASELOAD_TYPE                 VARCHAR(12),
  CREATE_DATETIME               TIMESTAMP     DEFAULT now() NOT NULL,
  CREATE_USER_ID                VARCHAR(32)   DEFAULT USER  NOT NULL,
  AUDIT_TIMESTAMP               TIMESTAMP,
  AUDIT_USER_ID                 VARCHAR(32),
  AUDIT_MODULE_NAME             VARCHAR(65),
  AUDIT_CLIENT_USER_ID          VARCHAR(64),
  AUDIT_CLIENT_IP_ADDRESS       VARCHAR(39),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR(64),
  AUDIT_ADDITIONAL_INFO         VARCHAR(256)
);

ALTER TABLE OFFENDER_ALERTS ADD PRIMARY KEY (OFFENDER_BOOK_ID, ALERT_SEQ);
