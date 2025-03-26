create table TRANSACTION_TYPES
(
    TXN_TYPE                      VARCHAR2(6 char)                       not null
        constraint TRANSACTION_TYPES_PK
            primary key,
    DESCRIPTION                   VARCHAR2(40 char)                      not null,
    ACTIVE_FLAG                   VARCHAR2(1 char)  default 'Y'          not null,
    TXN_USAGE                     VARCHAR2(12 char)                      not null,
    ALL_CASELOAD_FLAG             VARCHAR2(1 char)  default 'Y'          not null,
    EXPIRY_DATE                   DATE,
    UPDATE_ALLOWED_FLAG           VARCHAR2(1 char)  default 'Y'          not null,
    MANUAL_INVOICE_FLAG           VARCHAR2(1 char)  default 'Y'          not null,
    CREDIT_OBLIGATION_TYPE        VARCHAR2(6 char),
    MODIFY_USER_ID                VARCHAR2(32 char),
    MODIFY_DATE                   DATE                                   not null,
    LIST_SEQ                      NUMBER(6)         default 99,
    GROSS_NET_FLAG                VARCHAR2(1 char)  default 'N',
    CASELOAD_TYPE                 VARCHAR2(12 char),
    CREATE_DATETIME               TIMESTAMP(9)      default systimestamp not null,
    CREATE_USER_ID                VARCHAR2(32 char) default USER         not null,
    MODIFY_DATETIME               TIMESTAMP(9),
    AUDIT_TIMESTAMP               TIMESTAMP(9),
    AUDIT_USER_ID                 VARCHAR2(32 char),
    AUDIT_MODULE_NAME             VARCHAR2(65 char),
    AUDIT_CLIENT_USER_ID          VARCHAR2(64 char),
    AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39 char),
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64 char),
    AUDIT_ADDITIONAL_INFO         VARCHAR2(256 char)
);
comment on table TRANSACTION_TYPES is 'Defines transaction types which feed the majority of data entry screens.';

comment on column TRANSACTION_TYPES.TXN_TYPE is 'Transaction type';

comment on column TRANSACTION_TYPES.DESCRIPTION is 'Transaction description.';

comment on column TRANSACTION_TYPES.ACTIVE_FLAG is 'Flag showing whether code is active or inactive.';

comment on column TRANSACTION_TYPES.TXN_USAGE is 'The usage for the transaction ie. Receipt, Disbursement, Commissary..';

comment on column TRANSACTION_TYPES.ALL_CASELOAD_FLAG is 'Flag indicating whether all caseloads will use transaction type.';

comment on column TRANSACTION_TYPES.EXPIRY_DATE is ' Date of code expiration.';

comment on column TRANSACTION_TYPES.UPDATE_ALLOWED_FLAG is ' Flag on record protection.';

comment on column TRANSACTION_TYPES.MODIFY_USER_ID is 'The user who modifies the record';

comment on column TRANSACTION_TYPES.MODIFY_DATE is 'The data modified date ';

comment on column TRANSACTION_TYPES.LIST_SEQ is ' Sequencing for list of values.';

comment on column TRANSACTION_TYPES.CASELOAD_TYPE is 'The Case Load Type';

comment on column TRANSACTION_TYPES.CREATE_DATETIME is 'The timestamp when the record is created';

comment on column TRANSACTION_TYPES.CREATE_USER_ID is 'The user who creates the record';

comment on column TRANSACTION_TYPES.MODIFY_DATETIME is 'The timestamp when the record is modified ';
