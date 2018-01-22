CREATE OR REPLACE PACKAGE web_user_pkg IS
  TYPE caseloadType IS TABLE OF VARCHAR2(50) INDEX BY BINARY_INTEGER;
  TYPE roleType IS TABLE OF VARCHAR2(50) INDEX BY BINARY_INTEGER;

  PROCEDURE create_elite2_web_user (
    username IN STAFF_USER_ACCOUNTS.USERNAME%TYPE,
    password IN VARCHAR2,
    firstname IN STAFF_MEMBERS.FIRST_NAME%TYPE,
    lastname IN STAFF_MEMBERS.LAST_NAME%TYPE,
    emailAddress IN INTERNET_ADDRESSES.INTERNET_ADDRESS%TYPE,
    caseloadIds IN caseloadType,
    roles IN roleType);

END web_user_pkg;


CREATE OR REPLACE PACKAGE BODY web_user_pkg IS

  PROCEDURE create_elite2_web_user (
    username IN STAFF_USER_ACCOUNTS.USERNAME%TYPE,
    password IN VARCHAR2,
    firstname IN STAFF_MEMBERS.FIRST_NAME%TYPE,
    lastname IN STAFF_MEMBERS.LAST_NAME%TYPE,
    emailAddress IN INTERNET_ADDRESSES.INTERNET_ADDRESS%TYPE,
    caseloadIds IN caseloadType,
    roles IN roleType)
  IS
    staffId STAFF_USER_ACCOUNTS.STAFF_ID%TYPE;
    internetAddressId INTERNET_ADDRESSES.INTERNET_ADDRESS_ID%TYPE;
    roleId OMS_ROLES.ROLE_ID%TYPE;
    usernameCaps VARCHAR2(50);
    any_rows_found number;
    userexist integer;
    numRoles integer;
    standardRoles roleType;
    BEGIN
      usernameCaps := UPPER(username);

      select count(*) into userexist from dba_users where username = usernameCaps;
      if (userexist = 0) then
        EXECUTE IMMEDIATE 'CREATE USER ' || usernameCaps || ' IDENTIFIED BY ' || password;
      end if;

      EXECUTE IMMEDIATE 'ALTER USER '|| usernameCaps || ' GRANT CONNECT THROUGH API_PROXY_USER';
      EXECUTE IMMEDIATE 'GRANT CREATE SESSION TO '|| usernameCaps;
      EXECUTE IMMEDIATE 'GRANT TAG_USER TO '|| usernameCaps;
      EXECUTE IMMEDIATE 'ALTER USER '|| usernameCaps || ' DEFAULT ROLE TAG_USER';
      EXECUTE IMMEDIATE 'ALTER USER '|| usernameCaps || ' PROFILE TAG_GENERAL';
      EXECUTE IMMEDIATE 'GRANT CREATE SESSION TO '|| usernameCaps;
      EXECUTE IMMEDIATE 'GRANT TAG_USER TO '|| usernameCaps;
      EXECUTE IMMEDIATE 'GRANT TAG_RO TO '|| usernameCaps;
      EXECUTE IMMEDIATE 'GRANT CONNECT TO '|| usernameCaps ;
      EXECUTE IMMEDIATE 'ALTER USER '|| usernameCaps || ' DEFAULT ROLE TAG_RO';



      select count(*)into any_rows_found from STAFF_USER_ACCOUNTS WHERE USERNAME = usernameCaps;
      if any_rows_found = 0 then

        SELECT STAFF_ID.NEXTVAL INTO staffId FROM DUAL;
        INSERT INTO STAFF_MEMBERS (STAFF_ID, LAST_NAME, FIRST_NAME, BIRTHDATE,
                                   UPDATE_ALLOWED_FLAG, SUSPENDED_FLAG, AS_OF_DATE,
                                   ROLE, SEX_CODE, STATUS, SUSPENSION_DATE, SUSPENSION_REASON, FORCE_PASSWORD_CHANGE_FLAG, LAST_PASSWORD_CHANGE_DATE, LICENSE_CODE, LICENSE_EXPIRY_DATE, CREATE_DATETIME,
                                   CREATE_USER_ID, MODIFY_DATETIME, MODIFY_USER_ID, TITLE, NAME_SEQUENCE, QUEUE_CLUSTER_ID, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME, AUDIT_CLIENT_USER_ID,
                                   AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME, AUDIT_ADDITIONAL_INFO, FIRST_LOGON_FLAG, SIGNIFICANT_DATE, SIGNIFICANT_NAME, NATIONAL_INSURANCE_NUMBER)
        VALUES (staffId, UPPER(lastname), UPPER(firstname),
                         TO_DATE('1970-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'Y', 'N',
                         sysdate, NULL, 'M', 'ACTIVE', NULL, 'CA', 'N', NULL, NULL, NULL,
                                                             sysdate, 'SYSCON_ADM', sysdate, 'OMS_OWNER', NULL, NULL, 2,
                                                                                                                sysdate, 'OMS_OWNER', 'JDBC Thin Client', 'mick', '10.200.3.14', 'unknown', NULL, 'N', NULL, NULL, NULL);

        SELECT INTERNET_ADDRESS_ID.NEXTVAL INTO internetAddressId FROM DUAL;
        INSERT INTO INTERNET_ADDRESSES (INTERNET_ADDRESS_ID, OWNER_ID, OWNER_CLASS, INTERNET_ADDRESS_CLASS, INTERNET_ADDRESS)
        VALUES (internetAddressId, staffId, 'STF', 'EMAIL', emailAddress);

        INSERT INTO STAFF_USER_ACCOUNTS (USERNAME, STAFF_ID, STAFF_USER_TYPE, ID_SOURCE, WORKING_CASELOAD_ID, CREATE_DATETIME, CREATE_USER_ID, MODIFY_DATETIME, MODIFY_USER_ID, AUDIT_TIMESTAMP,
                                         AUDIT_USER_ID, AUDIT_MODULE_NAME, AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME, AUDIT_ADDITIONAL_INFO)
        VALUES (usernameCaps, staffId, 'GENERAL', 'USER', caseloadIds(1), sysdate, 'SYSCON_ADM', sysdate, usernameCaps, sysdate, usernameCaps,
                'frmweb@weblg01.syscon.ca (TNS V1-V3)', 'skadubur', '10.200.2.11', 'SVAGGA-E4310', NULL);



      END IF;

      standardRoles(1) := 202;
      standardRoles(2) := 962;
      standardRoles(3) := 100;

      FOR i IN 1..caseloadIds.count LOOP

        select count(*) into any_rows_found from USER_ACCESSIBLE_CASELOADS WHERE USERNAME = usernameCaps AND CASELOAD_ID = caseloadIds(i);
        if any_rows_found = 0 then
          INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE, CREATE_DATETIME, CREATE_USER_ID, MODIFY_DATETIME, MODIFY_USER_ID, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                                                 AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME)
          VALUES (caseloadIds(i), usernameCaps, sysdate, sysdate, 'SYSCON_ADM', NULL, NULL, sysdate, 'SYSCON_ADM', 'OUUUSERS', 'JHickinbotham', '10.200.1.42', 'Sheffield');
        END IF;

        FOR j IN 1..standardRoles.count LOOP
          select ROLE_ID into roleId from OMS_ROLES WHERE ROLE_CODE = standardRoles(j);

          select count(*) into any_rows_found from USER_CASELOAD_ROLES WHERE ROLE_ID = roleId AND USERNAME = usernameCaps AND CASELOAD_ID = caseloadIds(i);
          if any_rows_found = 0 then
            INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID, CREATE_DATETIME, CREATE_USER_ID,
                                             MODIFY_DATETIME, MODIFY_USER_ID, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                                             AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME )
            VALUES (roleId, usernameCaps, caseloadIds(i), sysdate, 'SYSCON_ADM', NULL, NULL,  sysdate, 'SYSCON_ADM', 'OUUUSERS', 'TRichardson', '10.200.3.3', 'trevlt');
          END IF;
        END LOOP;

        FOR j IN 1..roles.count LOOP
          select ROLE_ID into roleId from OMS_ROLES WHERE ROLE_CODE = roles(j);

          select count(*) into any_rows_found from USER_CASELOAD_ROLES WHERE ROLE_ID = roleId AND USERNAME = usernameCaps AND CASELOAD_ID = caseloadIds(i);
          if any_rows_found = 0 then
            INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, USERNAME, CASELOAD_ID, CREATE_DATETIME, CREATE_USER_ID,
                                             MODIFY_DATETIME, MODIFY_USER_ID, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                                             AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME )
            VALUES (roleId, usernameCaps, caseloadIds(i), sysdate, 'SYSCON_ADM', NULL, NULL,  sysdate, 'SYSCON_ADM', 'OUUUSERS', 'TRichardson', '10.200.3.3', 'trevlt');
          END IF;
        END LOOP;
      END LOOP;

    END create_elite2_web_user;
END web_user_pkg;
/
