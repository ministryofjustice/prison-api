CREATE TABLE OFFENDER_OIC_SANCTIONS (
  --'A punishment imposed upon an offender after having been found guilty of an offence defined in Prison Rule 51 or YOI Rule 55. The touchy-feely New-Labour term for
  -- punishment is now award..... A punishment can be made to run consecutively from other punishments imposed as a result of charges arising from unrelated incidents
  -- or from the same incident. NOTE : the primary key structure is not logical. It contains the Offender Booking Id of the guilty offender which, in fact, is inherited
  -- via parent entities.';
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
  SEAL_FLAG                     VARCHAR2(1)
);

ALTER TABLE OFFENDER_OIC_SANCTIONS ADD PRIMARY KEY (OFFENDER_BOOK_ID, SANCTION_SEQ);
--ALTER TABLE OFFENDER_OIC_SANCTIONS ADD FOREIGN KEY (OFFENDER_BOOK_ID) REFERENCES OFFENDER_BOOKINGS (OFFENDER_BOOK_ID);
--ALTER TABLE OFFENDER_OIC_SANCTIONS ADD FOREIGN KEY (CONSECUTIVE_OFFENDER_BOOK_ID, CONSECUTIVE_SANCTION_SEQ) REFERENCES OFFENDER_OIC_SANCTIONS;
ALTER TABLE OFFENDER_OIC_SANCTIONS ADD FOREIGN KEY (OIC_HEARING_ID, RESULT_SEQ) REFERENCES OIC_HEARING_RESULTS (OIC_HEARING_ID, RESULT_SEQ);
