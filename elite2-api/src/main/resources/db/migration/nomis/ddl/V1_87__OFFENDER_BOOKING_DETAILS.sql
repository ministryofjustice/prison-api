create table OFFENDER_BOOKING_DETAILS
(
  OFFENDER_BOOK_ID NUMBER(10) not null constraint OFFENDER_BOOKING_DETAILS_PK primary key
    constraint OFF_BKG_DTL_OFF_BKG_FK
    references OFFENDER_BOOKINGS,
  CELL_SHARING_ALERT_FLAG VARCHAR2(1 char) default 'Y' not null,
  CREATE_DATETIME TIMESTAMP(9) default systimestamp not null,
  CREATE_USER_ID VARCHAR2(32 char) default USER not null,
  MODIFY_DATETIME TIMESTAMP(9),
  MODIFY_USER_ID VARCHAR2(32 char),
  AUDIT_TIMESTAMP TIMESTAMP(9),
  AUDIT_USER_ID VARCHAR2(32 char),
  AUDIT_MODULE_NAME VARCHAR2(65 char),
  AUDIT_CLIENT_USER_ID VARCHAR2(64 char),
  AUDIT_CLIENT_IP_ADDRESS VARCHAR2(39 char),
  AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64 char),
  AUDIT_ADDITIONAL_INFO VARCHAR2(256 char),
  SPECIAL_NEEDS_TEXT VARCHAR2(1000 char),
  AVAILABILITY_TEXT VARCHAR2(1000 char)
);

comment on column OFFENDER_BOOKING_DETAILS.OFFENDER_BOOK_ID is 'The Offender Book ID';

comment on column OFFENDER_BOOKING_DETAILS.CELL_SHARING_ALERT_FLAG is '?if cell sharing alerted';

comment on column OFFENDER_BOOKING_DETAILS.CREATE_DATETIME is 'The timestamp when the record is created';

comment on column OFFENDER_BOOKING_DETAILS.CREATE_USER_ID is 'The user who creates the record';

comment on column OFFENDER_BOOKING_DETAILS.MODIFY_DATETIME is 'The timestamp when the record is modified ';

comment on column OFFENDER_BOOKING_DETAILS.MODIFY_USER_ID is 'The user who modifies the record';

comment on column OFFENDER_BOOKING_DETAILS.SPECIAL_NEEDS_TEXT is 'Special need details';

comment on column OFFENDER_BOOKING_DETAILS.AVAILABILITY_TEXT is 'Availability details';
