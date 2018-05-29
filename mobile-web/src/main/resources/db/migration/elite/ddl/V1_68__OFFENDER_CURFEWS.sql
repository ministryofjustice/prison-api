create table OFFENDER_CURFEWS
(
  OFFENDER_BOOK_ID             NUMBER(10)                        not null constraint OFF_CURFEW_INFO_OFF_BOOKING_FK references OFFENDER_BOOKINGS,
  OFFENDER_CURFEW_ID           NUMBER(10)                        not null constraint OFFENDER_CURFEWS_PK primary key,
  ELIGIBILITY_DATE             DATE                              not null,
  ASSESSMENT_DATE              DATE,
  ARD_CRD_DATE                 DATE,
  APPROVAL_STATUS              VARCHAR2(12),
  REFUSED_REASON               VARCHAR2(12),
  DECISION_DATE                DATE,
  REVIEW_FLAG                  VARCHAR2(1),
  MON_START_TIME               DATE,
  TUE_START_TIME               DATE,
  WED_START_TIME               DATE,
  THU_START_TIME               DATE,
  FRI_START_TIME               DATE,
  SAT_START_TIME               DATE,
  SUN_START_TIME               DATE,
  MON_END_TIME                 DATE,
  TUE_END_TIME                 DATE,
  WED_END_TIME                 DATE,
  THU_END_TIME                 DATE,
  FRI_END_TIME                 DATE,
  SAT_END_TIME                 DATE,
  SUN_END_TIME                 DATE,
  COMMENT_TEXT                 VARCHAR2(240),
  OFFENDING_BEHAVIOUR_FLAG     VARCHAR2(1),
  FAMILY_LINKS_FLAG            VARCHAR2(1),
  SUCCESSFUL_ROTL_FLAG         VARCHAR2(1),
  RISK_BEHAVIOUR_ATTITUDE_FLAG VARCHAR2(1),
  VIOLENT_OFFENDING_RISK       VARCHAR2(12),
  REIMPRISONMENT_RISK          VARCHAR2(12),
  SEX_OFFENDING_RISK           VARCHAR2(12),
  CONTRACTOR_CORP_ID           NUMBER(10),
  REVIEW_REASON                VARCHAR2(12),
  CREATE_DATETIME              TIMESTAMP(9) default SYSTIMESTAMP not null,
  CREATE_USER_ID               VARCHAR2(32)                      not null,
  MODIFY_DATETIME              TIMESTAMP(9) default SYSTIMESTAMP,
  MODIFY_USER_ID               VARCHAR2(32),
  SEAL_FLAG                    VARCHAR2(1)
);

create index OFFENDER_CURFEWS_NI2 on OFFENDER_CURFEWS (OFFENDER_BOOK_ID);

comment on table OFFENDER_CURFEWS
is 'The offender curfew details';

comment on column OFFENDER_CURFEWS.OFFENDER_BOOK_ID
is 'A unique ID for each offender booking.';

comment on column OFFENDER_CURFEWS.OFFENDER_CURFEW_ID
is 'A sequence to determine the running number of curfew record per offender.';

comment on column OFFENDER_CURFEWS.ELIGIBILITY_DATE
is 'Derived based on Sentence Calculation.';

comment on column OFFENDER_CURFEWS.ASSESSMENT_DATE
is 'Calculated 10 weeks before Eligibility Date.';

comment on column OFFENDER_CURFEWS.ARD_CRD_DATE
is 'A date populated based on entry of legal information in TAG.';

comment on column OFFENDER_CURFEWS.APPROVAL_STATUS
is 'Approval Status of the Curfew record.';

comment on column OFFENDER_CURFEWS.REFUSED_REASON
is 'Reason if refused. Reference Codes(HDC_REJ_RSN)';

comment on column OFFENDER_CURFEWS.DECISION_DATE
is 'Date on which Decision is made.';

comment on column OFFENDER_CURFEWS.REVIEW_FLAG
is 'review flag.';

comment on column OFFENDER_CURFEWS.MON_START_TIME
is 'The Monday start time';

comment on column OFFENDER_CURFEWS.TUE_START_TIME
is 'The Tuesday start time';

comment on column OFFENDER_CURFEWS.WED_START_TIME
is 'The Wednesday start time';

comment on column OFFENDER_CURFEWS.THU_START_TIME
is 'The Thursday start time';

comment on column OFFENDER_CURFEWS.FRI_START_TIME
is 'The Friday start time';

comment on column OFFENDER_CURFEWS.SAT_START_TIME
is 'The Saturaday start time';

comment on column OFFENDER_CURFEWS.SUN_START_TIME
is 'The Sunday start time';

comment on column OFFENDER_CURFEWS.MON_END_TIME
is 'The Monday end time';

comment on column OFFENDER_CURFEWS.TUE_END_TIME
is 'The Tuesday end time';

comment on column OFFENDER_CURFEWS.WED_END_TIME
is 'The Wednesday end time';

comment on column OFFENDER_CURFEWS.THU_END_TIME
is 'The Thurday end time';

comment on column OFFENDER_CURFEWS.FRI_END_TIME
is 'The Friday end time';

comment on column OFFENDER_CURFEWS.SAT_END_TIME
is 'The Saturday end time';

comment on column OFFENDER_CURFEWS.SUN_END_TIME
is 'The Sunday end time';

comment on column OFFENDER_CURFEWS.COMMENT_TEXT
is 'The comment text';

comment on column OFFENDER_CURFEWS.OFFENDING_BEHAVIOUR_FLAG
is 'Flag to indicate for offending behaviour.';

comment on column OFFENDER_CURFEWS.FAMILY_LINKS_FLAG
is 'Flag to indicate for Family links.';

comment on column OFFENDER_CURFEWS.SUCCESSFUL_ROTL_FLAG
is 'Flag to indicate for Successful release on Temporary License.';

comment on column OFFENDER_CURFEWS.RISK_BEHAVIOUR_ATTITUDE_FLAG
is 'Flag to indicate for Risk behaviour attitude.';

comment on column OFFENDER_CURFEWS.VIOLENT_OFFENDING_RISK
is 'Flag to indicate for Violent offending risk?.';

comment on column OFFENDER_CURFEWS.REIMPRISONMENT_RISK
is 'Flag to indicate for re-imprisonment risk?.';

comment on column OFFENDER_CURFEWS.SEX_OFFENDING_RISK
is 'Flag to indicate for Sex offending risk?.';

comment on column OFFENDER_CURFEWS.CONTRACTOR_CORP_ID
is 'The contractor corporate';

comment on column OFFENDER_CURFEWS.REVIEW_REASON
is 'Reason for review.  Reference Codes(HDC_RVW_RSN)';

comment on column OFFENDER_CURFEWS.CREATE_DATETIME
is 'The timestamp when the record is created';

comment on column OFFENDER_CURFEWS.CREATE_USER_ID
is 'The user who creates the record';

comment on column OFFENDER_CURFEWS.MODIFY_DATETIME
is 'The timestamp when the record is modified';

comment on column OFFENDER_CURFEWS.MODIFY_USER_ID
is 'The user who modifies the record';


