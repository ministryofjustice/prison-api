CREATE TABLE OFFENCE_RESULT_CODES --The possible result codes of offences
(
  RESULT_CODE                   VARCHAR2(6)                       NOT NULL, -- Result codes used to record offence outcomes at each court appearance
  DESCRIPTION                   VARCHAR2(240)                     NOT NULL, -- Description of the results.
  DISPOSITION_CODE              VARCHAR2(12)                      NOT NULL, -- disposition code .
  CHARGE_STATUS                 VARCHAR2(12)                      NOT NULL, -- Charge status.
  ACTIVE_FLAG                   VARCHAR2(1)  DEFAULT 'N',                   -- Whether result is active or not
  EXPIRY_DATE                   DATE,                                       -- Expiry date
  CREATE_USER_ID                VARCHAR2(40) DEFAULT USER         NOT NULL, -- The user who creates the record
  CREATE_DATETIME               TIMESTAMP(9) DEFAULT systimestamp NOT NULL, -- The timestamp when the record is created
  MODIFY_USER_ID                VARCHAR2(40),                               -- The user who modifies the record
  MODIFY_DATETIME               TIMESTAMP(9),                               -- The timestamp when the record is modified
  CONVICTION_FLAG               VARCHAR2(1)  DEFAULT 'N',
  AUDIT_TIMESTAMP               TIMESTAMP(9),
  AUDIT_USER_ID                 VARCHAR2(32),
  AUDIT_MODULE_NAME             VARCHAR2(65),
  AUDIT_CLIENT_USER_ID          VARCHAR2(64),
  AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64),
  AUDIT_ADDITIONAL_INFO         VARCHAR2(256),
  LIST_SEQ                      NUMBER(6),                                  --The listing order sequence
  CONSTRAINT OFFENCE_RESULT_CODES_PK PRIMARY KEY (RESULT_CODE)
);
