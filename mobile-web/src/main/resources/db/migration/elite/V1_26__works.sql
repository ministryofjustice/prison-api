CREATE TABLE WORKS
(
  WORK_ID                       NUMBER(10)                          NOT NULL,
  WORKFLOW_TYPE                 VARCHAR2(12)                        NOT NULL,
  WORK_TYPE                     VARCHAR2(12)                        NOT NULL,
  WORK_SUB_TYPE                 VARCHAR2(12)                        NOT NULL,
  MANUAL_CLOSE_FLAG             VARCHAR2(1)   DEFAULT 'N'           NOT NULL,
  MODULE_NAME                   VARCHAR2(20),
  ACTIVE_FLAG                   VARCHAR2(1)   DEFAULT 'Y'           NOT NULL,
  EXPIRY_DATE                   DATE,
  CASELOAD_TYPE                 VARCHAR2(12),
  MANUAL_SELECT_FLAG            VARCHAR2(1),
  CREATE_DATETIME               TIMESTAMP(9)  DEFAULT SYSTIMESTAMP  NOT NULL,
  CREATE_USER_ID                VARCHAR2(32)  DEFAULT USER          NOT NULL,
  MODIFY_DATETIME               TIMESTAMP(9),
  MODIFY_USER_ID                VARCHAR2(32),
  SEAL_FLAG                     VARCHAR2(1),
  EMAIL_SUBJECT                 VARCHAR2(240),
  EMAIL_BODY                    VARCHAR2(4000)
);

ALTER TABLE WORKS ADD PRIMARY KEY (WORK_ID);
ALTER TABLE WORKS ADD UNIQUE (WORKFLOW_TYPE, WORK_TYPE, WORK_SUB_TYPE, CASELOAD_TYPE);
