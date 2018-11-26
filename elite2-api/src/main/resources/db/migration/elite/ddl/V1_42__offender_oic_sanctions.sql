CREATE TABLE OFFENDER_OIC_SANCTIONS
(
  OFFENDER_BOOK_ID              NUMBER(10)                          NOT NULL, --Unique identifer for an offender booking.';
  SANCTION_SEQ                  NUMBER(6)                           NOT NULL, --Sequence number on sanction forming part of primary key.';
  OIC_SANCTION_CODE             VARCHAR2(12),                                 --Reference Code ( OIC_SANCT )';
  COMPENSATION_AMOUNT           NUMBER(11,2),                                 --Penalty involving compensation amount.';
  SANCTION_MONTHS               NUMBER(3),                                    --Penalty months imposed against sentences.';
  SANCTION_DAYS                 NUMBER(3),                                    --The number of penalty days imposed against sentences.';
  COMMENT_TEXT                  VARCHAR2(240),                                --Pop-up edit window allowing penalty comments.';
  EFFECTIVE_DATE                DATE                                NOT NULL, --Effective date for penalty.';
  APPEALING_DATE                DATE,                                         --Date of appeal.';
  CONSECUTIVE_OFFENDER_BOOK_ID  NUMBER(10),                                   --FK to OIC sanction';
  CONSECUTIVE_SANCTION_SEQ      NUMBER(6),                                    --Specification of specific penalty that this may be consecutive to.';
  OIC_HEARING_ID                NUMBER(10),                                   --FK to OIC snaction';
  STATUS                        VARCHAR2(12),                                 --Referece Code (OIC_SANCT_STS)';
  OFFENDER_ADJUST_ID            NUMBER(10),                                   --FK Offender OIC Appeal Penalty';
  RESULT_SEQ                    NUMBER(6),                                    --Sequential number for hearing results';
  CREATE_DATETIME               TIMESTAMP (9) DEFAULT SYSTIMESTAMP  NOT NULL, --The timestamp when the record is created';
  CREATE_USER_ID                VARCHAR2(32)  DEFAULT USER          NOT NULL, --The user who creates the record';
  MODIFY_DATETIME               TIMESTAMP (9),                                --The timestamp when the record is modified ';
  MODIFY_USER_ID                VARCHAR2(32),                                 --The user who modifies the record';
  STATUS_DATE                   DATE,                                         --The date when the status changed';
  OIC_INCIDENT_ID               NUMBER(10),
  LIDS_SANCTION_NUMBER          NUMBER(6),
  SEAL_FLAG                     VARCHAR2(1),

  CONSTRAINT OFFENDER_OIC_SANCTIONS_PK PRIMARY KEY (OFFENDER_BOOK_ID, SANCTION_SEQ),

  CONSTRAINT OFF_OS_OFF_BKG_F1 FOREIGN KEY (OFFENDER_BOOK_ID)                                       REFERENCES OFFENDER_BOOKINGS,
  CONSTRAINT OFF_OS_OFF_OS_F1  FOREIGN KEY (CONSECUTIVE_OFFENDER_BOOK_ID, CONSECUTIVE_SANCTION_SEQ) REFERENCES OFFENDER_OIC_SANCTIONS,
  CONSTRAINT OIC_OS_OIC_HR_FK1 FOREIGN KEY (OIC_HEARING_ID, RESULT_SEQ)                             REFERENCES OIC_HEARING_RESULTS
);
