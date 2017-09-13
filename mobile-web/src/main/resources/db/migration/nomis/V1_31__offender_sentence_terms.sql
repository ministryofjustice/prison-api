CREATE TABLE OFFENDER_SENTENCE_TERMS
(
	OFFENDER_BOOK_ID              DECIMAL(10)                NOT NULL,
	SENTENCE_SEQ                  DECIMAL(6)                 NOT NULL,
	TERM_SEQ                      DECIMAL(6)                 NOT NULL,
	SENTENCE_TERM_CODE            VARCHAR(12)                NOT NULL,
	YEARS                         DECIMAL(3),
	MONTHS                        DECIMAL(3),
	WEEKS                         DECIMAL(3),
	DAYS                          DECIMAL(6),
	START_DATE                    DATE                       NOT NULL,
	END_DATE                      DATE,
	LIFE_SENTENCE_FLAG            VARCHAR(1)   DEFAULT 'N',
	MODIFY_DATETIME               TIMESTAMP(9),
	MODIFY_USER_ID                VARCHAR(32),
	CREATE_DATETIME               TIMESTAMP(9) DEFAULT now() NOT NULL,
	CREATE_USER_ID                VARCHAR(32)  DEFAULT USER  NOT NULL,
	HOURS                         DECIMAL(3),
  AUDIT_TIMESTAMP               TIMESTAMP(9),
  AUDIT_USER_ID                 VARCHAR(32),
  AUDIT_MODULE_NAME             VARCHAR(65),
  AUDIT_CLIENT_USER_ID          VARCHAR(64),
  AUDIT_CLIENT_IP_ADDRESS       VARCHAR(39),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR(64),
  AUDIT_ADDITIONAL_INFO         VARCHAR(256),
	CONSTRAINT OFFENDER_SENTENCE_TERMS_PK
		PRIMARY KEY (OFFENDER_BOOK_ID, SENTENCE_SEQ, TERM_SEQ)
);
