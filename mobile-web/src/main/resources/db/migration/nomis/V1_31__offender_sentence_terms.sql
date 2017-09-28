CREATE TABLE OFFENDER_SENTENCE_TERMS
(
	OFFENDER_BOOK_ID              BIGINT                      NOT NULL,
	SENTENCE_SEQ                  INTEGER                     NOT NULL,
	TERM_SEQ                      INTEGER                     NOT NULL,
	SENTENCE_TERM_CODE            VARCHAR(12)                 NOT NULL,
	YEARS                         SMALLINT,
	MONTHS                        SMALLINT,
	WEEKS                         SMALLINT,
	DAYS                          INTEGER,
	START_DATE                    DATE                        NOT NULL,
	END_DATE                      DATE,
	LIFE_SENTENCE_FLAG            VARCHAR(1)    DEFAULT 'N',
	MODIFY_DATETIME               TIMESTAMP,
	MODIFY_USER_ID                VARCHAR(32),
	CREATE_DATETIME               TIMESTAMP     DEFAULT now() NOT NULL,
	CREATE_USER_ID                VARCHAR(32)   DEFAULT USER  NOT NULL,
	HOURS                         SMALLINT,
  AUDIT_TIMESTAMP               TIMESTAMP,
  AUDIT_USER_ID                 VARCHAR(32),
  AUDIT_MODULE_NAME             VARCHAR(65),
  AUDIT_CLIENT_USER_ID          VARCHAR(64),
  AUDIT_CLIENT_IP_ADDRESS       VARCHAR(39),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR(64),
  AUDIT_ADDITIONAL_INFO         VARCHAR(256)
);

ALTER TABLE OFFENDER_SENTENCE_TERMS ADD PRIMARY KEY (OFFENDER_BOOK_ID, SENTENCE_SEQ, TERM_SEQ);
