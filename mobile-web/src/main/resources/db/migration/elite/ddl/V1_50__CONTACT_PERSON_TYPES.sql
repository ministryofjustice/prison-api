CREATE TABLE CONTACT_PERSON_TYPES
(
  CONTACT_TYPE        VARCHAR2(12)                       NOT NULL,
  RELATIONSHIP_TYPE   VARCHAR2(12)                       NOT NULL,
  LIST_SEQ            NUMBER(6),
  ACTIVE_FLAG         VARCHAR2(1)   DEFAULT 'Y'          NOT NULL,
  UPDATE_ALLOWED_FLAG VARCHAR2(1)   DEFAULT 'Y'          NOT NULL,
  MODIFY_USER_ID      VARCHAR2(32),
  EXPIRY_DATE         DATE,
  CREATE_DATETIME     TIMESTAMP(9)  DEFAULT SYSTIMESTAMP  NOT NULL,
  CREATE_USER_ID      VARCHAR2(32)  DEFAULT user          NOT NULL,
  MODIFY_DATETIME     TIMESTAMP(9)  DEFAULT SYSTIMESTAMP,
  SEAL_FLAG           VARCHAR2(1),
  CONSTRAINT CONTACT_PERSON_TYPES_PK
    PRIMARY KEY (CONTACT_TYPE, RELATIONSHIP_TYPE)
);

COMMENT ON TABLE CONTACT_PERSON_TYPES IS '- Retrofitted';

COMMENT ON COLUMN CONTACT_PERSON_TYPES.CONTACT_TYPE        IS 'Refrence Code [CONTACTS]. The contact type with offender ie. Emergency, Professional ..';
COMMENT ON COLUMN CONTACT_PERSON_TYPES.RELATIONSHIP_TYPE   IS 'Refrence Code [ RELATIONSHIP ]. The relationship with offender based upon type.';
COMMENT ON COLUMN CONTACT_PERSON_TYPES.LIST_SEQ            IS 'Listing order of the code (It for controlling the order of listing in LOV).  If the value is "0", then it is taken as the default for this domain';
COMMENT ON COLUMN CONTACT_PERSON_TYPES.ACTIVE_FLAG         IS 'If the code active ?';
COMMENT ON COLUMN CONTACT_PERSON_TYPES.UPDATE_ALLOWED_FLAG IS 'If the code allowed to changed ( It is for controlling the code)';
COMMENT ON COLUMN CONTACT_PERSON_TYPES.MODIFY_USER_ID      IS 'The user who modifies the record';
COMMENT ON COLUMN CONTACT_PERSON_TYPES.EXPIRY_DATE         IS 'The date which the code is no longer used';
COMMENT ON COLUMN CONTACT_PERSON_TYPES.CREATE_DATETIME     IS 'The timestamp when the record is created';
COMMENT ON COLUMN CONTACT_PERSON_TYPES.CREATE_USER_ID      IS 'The user who creates the record';
COMMENT ON COLUMN CONTACT_PERSON_TYPES.MODIFY_DATETIME     IS 'The timestamp when the record is modified ';