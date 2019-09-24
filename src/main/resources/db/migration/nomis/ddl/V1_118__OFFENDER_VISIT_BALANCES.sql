create table OFFENDER_VISIT_BALANCES
   (
       OFFENDER_BOOK_ID NUMBER(10) not null
           constraint OFFENDER_VISIT_BALANCES_PK
               primary key
           constraint OFFENDER_VISIT_BALANCES_FK9
               references OFFENDER_BOOKINGS,
       REMAINING_VO NUMBER(5),
       REMAINING_PVO NUMBER(5),
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
       VISIT_ALLOWANCE_INDICATOR VARCHAR2(1 char) default 'N'
   );


comment on table OFFENDER_VISIT_BALANCES is 'The counter of remaing visit order ';

comment on column OFFENDER_VISIT_BALANCES.OFFENDER_BOOK_ID is 'The Offender Book ID';

comment on column OFFENDER_VISIT_BALANCES.REMAINING_VO is 'Number of remaing visit order';

comment on column OFFENDER_VISIT_BALANCES.REMAINING_PVO is 'Number of remaing privileged visit order';

comment on column OFFENDER_VISIT_BALANCES.CREATE_DATETIME is 'The timestamp when the record is created';

comment on column OFFENDER_VISIT_BALANCES.CREATE_USER_ID is 'The user who creates the record';

comment on column OFFENDER_VISIT_BALANCES.MODIFY_DATETIME is 'The timestamp when the record is modified ';

comment on column OFFENDER_VISIT_BALANCES.MODIFY_USER_ID is 'The user who modifies the record';
