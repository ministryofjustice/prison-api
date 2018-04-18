CREATE TABLE OFFENCES
(
  OFFENCE_CODE         VARCHAR2(25)                        NOT NULL,    --'Reference Code ( OFFENCE )';
  STATUTE_CODE         VARCHAR2(12)                        NOT NULL,
  DESCRIPTION          VARCHAR2(1000)                      NOT NULL,
  OLD_STATUTE_CODE     VARCHAR2(12),                                    --'Previous Statue Code';
  SEVERITY_RANKING     VARCHAR2(12),                                    --'The severity ranking of Offence';
  DEFAULT_OFFENCE_TYPE VARCHAR2(12),                                    --'The default offence type';
  MAX_SENTENCE_LENGTH  NUMBER(3),                                       --'Maximum Sentence';
  SENTENCE_UNIT_CODE   VARCHAR2(12),                                    --'Reference Code ( PERIOD_TYPE ) : Sentence unit of the max sentence';
  OFFENCE_GROUP        VARCHAR2(12),
  UPDATE_ALLOWED_FLAG  VARCHAR2(1)    DEFAULT 'Y'           NOT NULL,
  REPEALED_DATE        DATE,
  ACTIVE_FLAG          VARCHAR2(1)    DEFAULT 'Y'           NOT NULL,   --' The flag derives whether Offence is active or not';
  LIST_SEQ             NUMBER(6),
  EXPIRY_DATE          DATE,
  CHECK_BOX1           VARCHAR2(1)    DEFAULT 'N',
  CHECK_BOX2           VARCHAR2(1)    DEFAULT 'N',
  CHECK_BOX3           VARCHAR2(1)    DEFAULT 'N',
  OFFENSE_DEGREE       VARCHAR2(12),
  MAX_GOOD_TIME_PERC   NUMBER(3),
  CREATE_DATE          DATE           DEFAULT SYSDATE       NOT NULL,   --'The date when Offence gets Created';
  HO_CODE              VARCHAR2(12),                                    --'This column stores the value of Home Office Class and Home Office sub class';
  SEAL_FLAG            VARCHAR2(1),
  CREATE_DATETIME      TIMESTAMP (9)  DEFAULT systimestamp  NOT NULL,   --'The timestamp when the record is created';
  CREATE_USER_ID       VARCHAR2(32)   DEFAULT USER          NOT NULL,   --'The user who creates the record';
  MODIFY_DATETIME      TIMESTAMP (9),                                   --'The timestamp when the record is modified ';
  MODIFY_USER_ID       VARCHAR2(32),                                    --'The user who modifies the record';
  CONSTRAINT OFFENCES_PK PRIMARY KEY (OFFENCE_CODE, STATUTE_CODE)
);

CREATE INDEX OFN_STT_F2 ON OFFENCES (OLD_STATUTE_CODE);
CREATE INDEX OFN_STT_F1 ON OFFENCES (STATUTE_CODE);
CREATE INDEX OFFEN_HO_CODES_FK ON OFFENCES (HO_CODE);