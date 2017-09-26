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
	SEAL_FLAG                       VARCHAR2(1)
);

CREATE TABLE OFFENDER_IEP_LEVELS
(
	OFFENDER_BOOK_ID                NUMBER(10)                        NOT NULL,
	IEP_LEVEL_SEQ                   NUMBER(10)                        NOT NULL,
	IEP_DATE                        DATE                              NOT NULL,
	IEP_TIME                        DATE,
	AGY_LOC_ID                      VARCHAR2(6)                       NOT NULL,
	IEP_LEVEL                       VARCHAR2(12)                      NOT NULL,
	COMMENT_TEXT                    VARCHAR2(240),
	USER_ID                         VARCHAR2(40),
	CREATE_DATETIME                 TIMESTAMP(9) DEFAULT SYSTIMESTAMP NOT NULL,
	CREATE_USER_ID                  VARCHAR2(32) DEFAULT USER         NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP(9) DEFAULT SYSTIMESTAMP,
	MODIFY_USER_ID                  VARCHAR2(32),
	SEAL_FLAG                       VARCHAR2(1)
);