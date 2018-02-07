
CREATE TABLE CORPORATES
(
  CORPORATE_ID            NUMBER(10)                            NOT NULL,
  CORPORATE_NAME          VARCHAR2(40),
  CASELOAD_ID             VARCHAR2(6),
  CONTACT_PERSON_NAME     VARCHAR2(40),
  CREATED_DATE            DATE                                  NOT NULL,
  UPDATED_DATE            DATE,
  USER_ID                 VARCHAR2(32),
  COMMENT_TEXT            VARCHAR2(240),
  START_DATE              DATE,
  ACCOUNT_TERM_CODE       VARCHAR2(60),
  SHIPPING_TERM_CODE      VARCHAR2(60),
  MINIMUM_PURCHASE_AMOUNT NUMBER(9,2),
  MAXIMUM_PURCHASE_AMOUNT NUMBER(9,2),
  MEMO_TEXT               VARCHAR2(40),
  SUSPENDED_FLAG          VARCHAR2(1)                           NOT NULL,
  SUSPENDED_DATE          DATE,
  FEI_NUMBER              VARCHAR2(40),
  ACTIVE_FLAG             VARCHAR2(1)     DEFAULT 'Y'           NOT NULL,
  EXPIRY_DATE             DATE,
  TAX_NO                  VARCHAR2(12),
  CREATE_DATETIME         TIMESTAMP(9)    default SYSTIMESTAMP  NOT NULL,
  CREATE_USER_ID          VARCHAR2(32)    default user          NOT NULL,
  MODIFY_DATETIME         TIMESTAMP(9)    default SYSTIMESTAMP,
  MODIFY_USER_ID          VARCHAR2(32),
  SEAL_FLAG               VARCHAR2(1),
  CONSTRAINT CORPORATES_PK PRIMARY KEY (CORPORATE_ID)
);

CREATE INDEX CORPORATES_NI1
  on CORPORATES (CASELOAD_ID);

CREATE INDEX CORPORATES_NI2
  on CORPORATES (CORPORATE_NAME);

COMMENT ON TABLE  CORPORATES                     IS 'Corporates. Such as Vendors';
COMMENT ON COLUMN CORPORATES.CORPORATE_ID        IS ' - Column already exists';
COMMENT ON COLUMN CORPORATES.CORPORATE_NAME      IS ' - Column already exists';
COMMENT ON COLUMN CORPORATES.CASELOAD_ID         IS ' - Column already exists';
COMMENT ON COLUMN CORPORATES.CONTACT_PERSON_NAME IS ' - Column already exists';
COMMENT ON COLUMN CORPORATES.CREATED_DATE        IS ' - Column already exists';
COMMENT ON COLUMN CORPORATES.UPDATED_DATE        IS ' - Column already exists';
COMMENT ON COLUMN CORPORATES.USER_ID             IS ' - Column already exists';
COMMENT ON COLUMN CORPORATES.COMMENT_TEXT        IS ' - Column already exists';
COMMENT ON COLUMN CORPORATES.CREATE_DATETIME     IS 'The timestamp when the record is created';
COMMENT ON COLUMN CORPORATES.CREATE_USER_ID      IS 'The user who creates the record';
COMMENT ON COLUMN CORPORATES.MODIFY_DATETIME     IS 'The timestamp when the record is modified ';
COMMENT ON COLUMN CORPORATES.MODIFY_USER_ID      IS 'The user who modifies the record';

