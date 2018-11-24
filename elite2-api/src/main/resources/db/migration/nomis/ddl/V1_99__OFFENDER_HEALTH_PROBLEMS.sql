CREATE TABLE OFFENDER_HEALTH_PROBLEMS (
  OFFENDER_HEALTH_PROBLEM_ID    NUMBER(10)                        NOT NULL constraint OFFENDER_HEALTH_PROBLEMS_PK primary key,
  OFFENDER_BOOK_ID              NUMBER(10)                        NOT NULL,
  PROBLEM_TYPE                  VARCHAR2(12)                      NOT NULL, -- Reference Code ( HEALTH ) [PHYSICAL | MENTA | DRUG ABUSED ]
  PROBLEM_CODE                  VARCHAR2(12)                      NOT NULL, -- Reference Code ( HEALTH PBLM )
  START_DATE                    DATE,
  END_DATE                      DATE,
  CASELOAD_TYPE                 VARCHAR2(12)                      NOT NULL, -- The Case Load Type
  DESCRIPTION                   VARCHAR2(240),
  PROBLEM_STATUS                VARCHAR2(12),                               -- Reference Code ( STATUS ) : [ Active | Inactive ]
  CREATE_DATETIME               TIMESTAMP(9) DEFAULT SYSTIMESTAMP NOT NULL, -- The timestamp when the record is created
  CREATE_USER_ID                VARCHAR2(32) DEFAULT USER         NOT NULL, -- The user who creates the record
  MODIFY_DATETIME               TIMESTAMP(9),                               -- The timestamp when the record is modified
  MODIFY_USER_ID                VARCHAR2(32),                               -- The user who modifies the record
  AUDIT_TIMESTAMP               TIMESTAMP(9),
  AUDIT_USER_ID                 VARCHAR2(32),
  AUDIT_MODULE_NAME             VARCHAR2(65),
  AUDIT_CLIENT_USER_ID          VARCHAR2(64),
  AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64),
  AUDIT_ADDITIONAL_INFO         VARCHAR2(256)
);

CREATE INDEX OFF_HLTH_PBLM_OFF_BKG_FK ON OFFENDER_HEALTH_PROBLEMS (OFFENDER_BOOK_ID ASC);
ALTER TABLE OFFENDER_HEALTH_PROBLEMS ADD CONSTRAINT OFF_HLTH_PBLM_OFF_BKG_FK FOREIGN KEY(OFFENDER_BOOK_ID)REFERENCES OFFENDER_BOOKINGS(OFFENDER_BOOK_ID);
