CREATE TABLE COURSE_SCHEDULES
(
  CRS_SCH_ID                      NUMBER(10)                          NOT NULL,
  CRS_ACTY_ID                     NUMBER(10)                          NOT NULL,
  WEEKDAY                         VARCHAR2(12),
  SCHEDULE_DATE                   DATE                                NOT NULL,
  START_TIME                      DATE                                NOT NULL,
  END_TIME                        DATE,
  SESSION_NO                      NUMBER(6),
  DETAILS                         VARCHAR2(40),
  CREATE_DATETIME                 TIMESTAMP(9)   DEFAULT SYSTIMESTAMP NOT NULL,
  CREATE_USER_ID                  VARCHAR2(32)   DEFAULT USER         NOT NULL,
  MODIFY_DATETIME                 TIMESTAMP(9)   DEFAULT SYSTIMESTAMP,
  MODIFY_USER_ID                  VARCHAR2(32),
  SCHEDULE_STATUS                 VARCHAR2(12)   DEFAULT 'SCH',
  CATCH_UP_CRS_SCH_ID             NUMBER(10),
  VIDEO_REFERENCE_ID              VARCHAR2(20),
  SEAL_FLAG                       VARCHAR2(1),
  CANCELLED_FLAG                  VARCHAR2(1)
);


ALTER TABLE COURSE_SCHEDULES ADD CONSTRAINT COURSE_SCHEDULES_PK PRIMARY KEY (CRS_SCH_ID);

ALTER TABLE COURSE_SCHEDULES ADD FOREIGN KEY (CRS_ACTY_ID)         REFERENCES COURSE_ACTIVITIES;
ALTER TABLE COURSE_SCHEDULES ADD FOREIGN KEY (CATCH_UP_CRS_SCH_ID) REFERENCES COURSE_SCHEDULES;

