CREATE USER DEMO_NOMIS_OWNER IDENTIFIED BY DEMO_NOMIS_OWNER TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users;
GRANT CONNECT TO DEMO_NOMIS_OWNER;
GRANT RESOURCE TO DEMO_NOMIS_OWNER;
ALTER USER DEMO_NOMIS_OWNER QUOTA 20G ON USERS;
GRANT UNLIMITED TABLESPACE TO DEMO_NOMIS_OWNER;
grant select on v$database to DEMO_NOMIS_OWNER;

CREATE ROLE TAG_USER IDENTIFIED BY password;
GRANT CREATE SESSION TO TAG_USER;
GRANT CONNECT TO TAG_USER;

CREATE USER API_PROXY_USER IDENTIFIED BY api_proxy_user TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users;
GRANT CONNECT TO API_PROXY_USER;
GRANT CREATE SESSION TO API_PROXY_USER;

CREATE USER OFFICER1 IDENTIFIED BY password TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users;
CREATE USER OFFICER2 IDENTIFIED BY password TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users;
CREATE USER OFFICER3 IDENTIFIED BY password TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users;
CREATE USER OFFICER4 IDENTIFIED BY password TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users;
CREATE USER OFFICER5 IDENTIFIED BY password TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users;
CREATE USER ADMINSTAFF IDENTIFIED BY password TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users;

CREATE USER ITAG_USER IDENTIFIED BY password TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users;
CREATE USER ELITE2_API_USER IDENTIFIED BY password TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users;
CREATE USER HPA_USER IDENTIFIED BY password TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users;
CREATE USER API_TEST_USER IDENTIFIED BY password TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users;

GRANT CREATE SESSION TO OFFICER1;
GRANT CONNECT TO OFFICER1;

GRANT CREATE SESSION TO OFFICER2;
GRANT CONNECT TO OFFICER2;

GRANT CREATE SESSION TO OFFICER3;
GRANT CONNECT TO OFFICER3;

GRANT CREATE SESSION TO OFFICER4;
GRANT CONNECT TO OFFICER4;

GRANT CREATE SESSION TO OFFICER5;
GRANT CONNECT TO OFFICER5;

GRANT CREATE SESSION TO ADMINSTAFF;
GRANT CONNECT TO ADMINSTAFF;

GRANT CREATE SESSION TO ITAG_USER;
GRANT CONNECT TO ITAG_USER;

GRANT CREATE SESSION TO ELITE2_API_USER;
GRANT CONNECT TO ELITE2_API_USER;

GRANT CREATE SESSION TO HPA_USER;
GRANT CONNECT TO HPA_USER;

GRANT CREATE SESSION TO API_TEST_USER;
GRANT CONNECT TO API_TEST_USER;

ALTER USER OFFICER1 GRANT CONNECT THROUGH API_PROXY_USER;
ALTER USER OFFICER2 GRANT CONNECT THROUGH API_PROXY_USER;
ALTER USER OFFICER3 GRANT CONNECT THROUGH API_PROXY_USER;
ALTER USER OFFICER4 GRANT CONNECT THROUGH API_PROXY_USER;
ALTER USER OFFICER5 GRANT CONNECT THROUGH API_PROXY_USER;
ALTER USER ADMINSTAFF GRANT CONNECT THROUGH API_PROXY_USER;

ALTER USER ITAG_USER GRANT CONNECT THROUGH API_PROXY_USER;
ALTER USER ELITE2_API_USER GRANT CONNECT THROUGH API_PROXY_USER;
ALTER USER HPA_USER GRANT CONNECT THROUGH API_PROXY_USER;
ALTER USER API_TEST_USER GRANT CONNECT THROUGH API_PROXY_USER;

GRANT TAG_USER TO OFFICER1;
GRANT TAG_USER TO OFFICER2;
GRANT TAG_USER TO OFFICER3;
GRANT TAG_USER TO OFFICER4;
GRANT TAG_USER TO OFFICER5;
GRANT TAG_USER TO ADMINSTAFF;

GRANT TAG_USER TO ITAG_USER;
GRANT TAG_USER TO ELITE2_API_USER;
GRANT TAG_USER TO HPA_USER;
GRANT TAG_USER TO API_TEST_USER;
