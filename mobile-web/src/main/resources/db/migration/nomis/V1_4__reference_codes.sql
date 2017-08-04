CREATE TABLE REFERENCE_CODES
(
  DOMAIN                        VARCHAR(12),
  CODE                          VARCHAR(12),
  DESCRIPTION                   VARCHAR(40),
  LIST_SEQ                      DECIMAL(6, 0),
  ACTIVE_FLAG                   VARCHAR(1)  DEFAULT 'Y',
  SYSTEM_DATA_FLAG              VARCHAR(1)  DEFAULT 'Y',
  MODIFY_USER_ID                VARCHAR(32),
  EXPIRED_DATE                  DATE,
  NEW_CODE                      VARCHAR(12),
  PARENT_CODE                   VARCHAR(12),
  PARENT_DOMAIN                 VARCHAR(12),
  CREATE_DATETIME               TIMESTAMP(6)      DEFAULT now(),
  CREATE_USER_ID                VARCHAR(32) DEFAULT user,
  MODIFY_DATETIME               TIMESTAMP(6),
  AUDIT_TIMESTAMP               TIMESTAMP(6),
  AUDIT_USER_ID                 VARCHAR(32),
  AUDIT_MODULE_NAME             VARCHAR(65),
  AUDIT_CLIENT_USER_ID          VARCHAR(64),
  AUDIT_CLIENT_IP_ADDRESS       VARCHAR(39),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR(64),
  AUDIT_ADDITIONAL_INFO         VARCHAR(256)
);
