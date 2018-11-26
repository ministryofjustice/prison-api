CREATE TABLE OFFENDER_SENTENCE_TERMS
(
	OFFENDER_BOOK_ID              NUMBER(10)                        NOT NULL,
	SENTENCE_SEQ                  NUMBER(6)                         NOT NULL,
	TERM_SEQ                      NUMBER(6)                         NOT NULL,
	SENTENCE_TERM_CODE            VARCHAR2(12)                      NOT NULL,
	YEARS                         NUMBER(3),
	MONTHS                        NUMBER(3),
	WEEKS                         NUMBER(3),
	DAYS                          NUMBER(6),
	START_DATE                    DATE                              NOT NULL,
	END_DATE                      DATE,
	LIFE_SENTENCE_FLAG            VARCHAR2(1)  DEFAULT 'N',
	MODIFY_DATETIME               TIMESTAMP(9),
	MODIFY_USER_ID                VARCHAR2(32),
	CREATE_DATETIME               TIMESTAMP(9) DEFAULT SYSTIMESTAMP NOT NULL,
	CREATE_USER_ID                VARCHAR2(32) DEFAULT USER         NOT NULL,
	HOURS                         NUMBER(3),
  SEAL_FLAG                     VARCHAR2(1),
	CONSTRAINT OFFENDER_SENTENCE_TERMS_PK
		PRIMARY KEY (OFFENDER_BOOK_ID, SENTENCE_SEQ, TERM_SEQ)
);
