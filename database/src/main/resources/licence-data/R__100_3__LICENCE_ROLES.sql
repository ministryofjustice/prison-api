INSERT INTO OMS_ROLES (ROLE_ID, ROLE_NAME, ROLE_SEQ, ROLE_CODE,
                       PARENT_ROLE_CODE, ROLE_TYPE, ROLE_FUNCTION, SYSTEM_DATA_FLAG)
VALUES (ROLE_ID.NEXTVAL, 'Licence Roles', 1, 'LICENCE_ROLE', NULL, 'COMM', 'GENERAL', 'Y');

INSERT INTO OMS_ROLES (ROLE_ID, ROLE_NAME, ROLE_SEQ, ROLE_CODE,
                       PARENT_ROLE_CODE, ROLE_TYPE, ROLE_FUNCTION, SYSTEM_DATA_FLAG)
VALUES (ROLE_ID.NEXTVAL, 'Licence Case Admin', 1, 'LICENCE_CA', 'LICENCE_ROLE', 'COMM', 'GENERAL', 'Y');

INSERT INTO OMS_ROLES (ROLE_ID, ROLE_NAME, ROLE_SEQ, ROLE_CODE,
                       PARENT_ROLE_CODE, ROLE_TYPE, ROLE_FUNCTION, SYSTEM_DATA_FLAG)
VALUES
  (ROLE_ID.NEXTVAL, 'Licence Responsible Officer', 2, 'LICENCE_RO', 'LICENCE_ROLE', 'COMM', 'GENERAL', 'Y');

INSERT INTO OMS_ROLES (ROLE_ID, ROLE_NAME, ROLE_SEQ, ROLE_CODE,
                       PARENT_ROLE_CODE, ROLE_TYPE, ROLE_FUNCTION, SYSTEM_DATA_FLAG)
VALUES (ROLE_ID.NEXTVAL, 'Licence Decision Maker', 3, 'LICENCE_DM', 'LICENCE_ROLE', 'COMM', 'GENERAL', 'Y');


INSERT INTO CONTACT_PERSON_TYPES (CONTACT_TYPE, RELATIONSHIP_TYPE, LIST_SEQ, ACTIVE_FLAG, UPDATE_ALLOWED_FLAG, CONTACT_CLASS)
VALUES ('O', 'COM', 99, 'Y', 'Y', 'OFF');

INSERT INTO REFERENCE_CODES (DOMAIN, CODE, DESCRIPTION, LIST_SEQ, ACTIVE_FLAG, SYSTEM_DATA_FLAG, EXPIRED_DATE, PARENT_DOMAIN, PARENT_CODE)
VALUES ('RELATIONSHIP', 'COM', 'Community Offender Manager', 99, 'Y', 'N', NULL, 'CONTACTS', 'O');

INSERT INTO REFERENCE_CODES (DOMAIN, CODE, DESCRIPTION, LIST_SEQ, ACTIVE_FLAG, SYSTEM_DATA_FLAG)
VALUES ('ID_TYPE', 'EXTERNAL_REL', 'External Relationship', 99, 'N', 'N');
