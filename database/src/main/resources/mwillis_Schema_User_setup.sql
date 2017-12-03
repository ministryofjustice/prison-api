CREATE USER MICK_NOMIS_OWNER IDENTIFIED BY MICK_NOMIS_OWNER TEMPORARY TABLESPACE temp DEFAULT TABLESPACE users;
GRANT CONNECT TO MICK_NOMIS_OWNER;
GRANT RESOURCE TO MICK_NOMIS_OWNER;
ALTER USER MICK_NOMIS_OWNER QUOTA 20G ON USERS;
GRANT UNLIMITED TABLESPACE TO MICK_NOMIS_OWNER;
grant select on v$database to MICK_NOMIS_OWNER;

CREATE ROLE MICK_TAG_USER IDENTIFIED BY password;
GRANT CREATE SESSION TO MICK_TAG_USER;
GRANT CONNECT TO MICK_TAG_USER;

GRANT MICK_TAG_USER TO OFFICER1;
GRANT MICK_TAG_USER TO OFFICER2;
GRANT MICK_TAG_USER TO OFFICER3;
GRANT MICK_TAG_USER TO OFFICER4;
GRANT MICK_TAG_USER TO OFFICER5;
GRANT MICK_TAG_USER TO ADMINSTAFF;



