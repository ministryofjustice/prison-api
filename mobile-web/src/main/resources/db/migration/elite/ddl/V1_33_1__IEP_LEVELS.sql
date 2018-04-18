create table IEP_LEVELS
(
  IEP_LEVEL                       VARCHAR2(12)                      NOT NULL,
  AGY_LOC_ID                      VARCHAR2(6)                       NOT NULL,
  ACTIVE_FLAG                     VARCHAR2(1)                       NOT NULL,
  EXPIRY_DATE                     DATE,
  USER_ID                         VARCHAR2(40),
  DEFAULT_FLAG                    VARCHAR2(1)                       NOT NULL,
  CREATE_DATETIME                 TIMESTAMP(9) DEFAULT SYSTIMESTAMP NOT NULL,
  CREATE_USER_ID                  VARCHAR2(32) DEFAULT USER         NOT NULL,
  MODIFY_DATETIME                 TIMESTAMP(9) DEFAULT SYSTIMESTAMP,
  MODIFY_USER_ID                  VARCHAR2(32),
  REMAND_TRANSFER_LIMIT           NUMBER(12,2),
  REMAND_SPEND_LIMIT              NUMBER(12,2),
  CONVICTED_TRANSFER_LIMIT        NUMBER(12,2),
  CONVICTED_SPEND_LIMIT           NUMBER(12,2),
  RECORD_USER_ID                  VARCHAR2(30) DEFAULT USER,
  SEAL_FLAG                       VARCHAR2(1),
  CONSTRAINT IEP_LEVELS_PK PRIMARY KEY (IEP_LEVEL, AGY_LOC_ID),
  CONSTRAINT IEP_LEVELS_AGY_LOC_FK FOREIGN KEY (AGY_LOC_ID) REFERENCES AGENCY_LOCATIONS(AGY_LOC_ID)
);
