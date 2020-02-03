create table DBA_USERS
(
    USERNAME       VARCHAR2(30)  not null,
    ACCOUNT_STATUS VARCHAR2(32)  not null,
    PROFILE        VARCHAR2(30),
    EXPIRY_DATE    DATE,
    primary key (USERNAME)
);
