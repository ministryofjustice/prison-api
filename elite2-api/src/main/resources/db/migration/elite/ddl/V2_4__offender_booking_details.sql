create table OFFENDER_BOOKING_DETAILS
(
  OFFENDER_BOOK_ID NUMBER(10) not null constraint OFFENDER_BOOKING_DETAILS_PK primary key
    constraint OFF_BKG_DTL_OFF_BKG_FK references OFFENDER_BOOKINGS,
  CELL_SHARING_ALERT_FLAG VARCHAR2(1) default 'Y' not null,
  CREATE_DATETIME TIMESTAMP(9) default SYSTIMESTAMP not null,
  CREATE_USER_ID VARCHAR2(32) default USER not null,
  MODIFY_DATETIME TIMESTAMP(9) default SYSTIMESTAMP,
  MODIFY_USER_ID VARCHAR2(32),
  SPECIAL_NEEDS_TEXT VARCHAR2(1000),
  AVAILABILITY_TEXT VARCHAR2(1000),
  SEAL_FLAG VARCHAR2(1)
);

comment on column OFFENDER_BOOKING_DETAILS.OFFENDER_BOOK_ID is 'The Offender Book ID';

comment on column OFFENDER_BOOKING_DETAILS.CELL_SHARING_ALERT_FLAG is '?if cell sharing alerted';

comment on column OFFENDER_BOOKING_DETAILS.CREATE_DATETIME is 'The timestamp when the record is created';

comment on column OFFENDER_BOOKING_DETAILS.CREATE_USER_ID is 'The user who creates the record';

comment on column OFFENDER_BOOKING_DETAILS.MODIFY_DATETIME is 'The timestamp when the record is modified ';

comment on column OFFENDER_BOOKING_DETAILS.MODIFY_USER_ID is 'The user who modifies the record';

comment on column OFFENDER_BOOKING_DETAILS.SPECIAL_NEEDS_TEXT is 'Special need details';

comment on column OFFENDER_BOOKING_DETAILS.AVAILABILITY_TEXT is 'Availability details';
