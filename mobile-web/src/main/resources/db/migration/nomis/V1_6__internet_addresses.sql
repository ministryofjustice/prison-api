CREATE TABLE INTERNET_ADDRESSES
(
  INTERNET_ADDRESS_ID           BIGSERIAL     PRIMARY KEY     NOT NULL,
  OWNER_CLASS                   VARCHAR(12)                   NOT NULL,
  OWNER_ID                      BIGINT,
  OWNER_SEQ                     INTEGER,
  OWNER_CODE                    VARCHAR(12),
  INTERNET_ADDRESS_CLASS        VARCHAR(12)   DEFAULT 'EMAIL' NOT NULL,
  INTERNET_ADDRESS              VARCHAR(240)                  NOT NULL,
  CREATE_DATETIME               TIMESTAMP     DEFAULT now()   NOT NULL,
  CREATE_USER_ID                VARCHAR(32)   DEFAULT USER    NOT NULL,
  MODIFY_DATETIME               TIMESTAMP,
  MODIFY_USER_ID                VARCHAR(32),
  AUDIT_TIMESTAMP               TIMESTAMP,
  AUDIT_USER_ID                 VARCHAR(32),
  AUDIT_MODULE_NAME             VARCHAR(65),
  AUDIT_CLIENT_USER_ID          VARCHAR(64),
  AUDIT_CLIENT_IP_ADDRESS       VARCHAR(39),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR(64),
  AUDIT_ADDITIONAL_INFO         VARCHAR(256)
);
