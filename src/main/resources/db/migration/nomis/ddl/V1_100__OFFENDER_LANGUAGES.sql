-- Offender Language and numeracy skills
CREATE TABLE OFFENDER_LANGUAGES
(
  OFFENDER_BOOK_ID              NUMBER(10)                        NOT NULL, -- FK to Offender Bookings.
  LANGUAGE_TYPE                 VARCHAR2(12)                      NOT NULL, -- Reference Domain (LANG_TYPE) PRIMary or SECondary
  LANGUAGE_CODE                 VARCHAR2(12)                      NOT NULL, -- Reference Domain (LANG) A code for a language
  READ_SKILL                    VARCHAR2(12),                               -- Reference Domain (LANG_SKILLS).
  SPEAK_SKILL                   VARCHAR2(12),                               -- Reference Domain (LANG_SKILLS).
  WRITE_SKILL                   VARCHAR2(12),                               -- Reference Domain (LANG_SKILLS).
  COMMENT_TEXT                  VARCHAR2(240),                              -- General comment on offender language skill
  MODIFY_DATETIME               TIMESTAMP(9),                               -- The timestamp when the record is modified
  MODIFY_USER_ID                VARCHAR2(32),                               -- The user who modifies the record
  CREATE_DATETIME               TIMESTAMP(9) DEFAULT SYSTIMESTAMP NOT NULL, -- The timestamp when the record is created
  CREATE_USER_ID                VARCHAR2(32) DEFAULT USER         NOT NULL, -- The user who creates the record
  NUMERACY_SKILL                VARCHAR2(12),                               -- Reference Domain (LANG_SKILLS)
  PREFERED_WRITE_FLAG           VARCHAR2(1)  DEFAULT 'N'          NOT NULL, -- Y/N flag if it is the preferred written language.
  PREFERED_SPEAK_FLAG           VARCHAR2(1)  DEFAULT 'N'          NOT NULL, -- Y/N flag if it is the preferred spoken language.
  INTERPRETER_REQUESTED_FLAG    VARCHAR2(1)  DEFAULT 'N'          NOT NULL, -- Y/N flag if an interpreter is required.
  AUDIT_TIMESTAMP               TIMESTAMP(9),
  AUDIT_USER_ID                 VARCHAR2(32),
  AUDIT_MODULE_NAME             VARCHAR2(65),
  AUDIT_CLIENT_USER_ID          VARCHAR2(64),
  AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64),
  AUDIT_ADDITIONAL_INFO         VARCHAR2(256),
  CONSTRAINT OFFENDER_LANGUAGES_PK PRIMARY KEY (OFFENDER_BOOK_ID, LANGUAGE_CODE, LANGUAGE_TYPE)
);

ALTER TABLE OFFENDER_LANGUAGES ADD CONSTRAINT OFF_LANG_OFF_BKG_F1 FOREIGN KEY(OFFENDER_BOOK_ID) REFERENCES OFFENDER_BOOKINGS(OFFENDER_BOOK_ID);
