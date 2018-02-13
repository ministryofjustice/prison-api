CREATE TABLE HO_CODES
(
  HO_CODE          VARCHAR2(12)                        NOT NULL,
  DESCRIPTION      VARCHAR2(240)                       NOT NULL,
  ACTIVE_FLAG      VARCHAR2(1)   default 'Y'           NOT NULL,
  EXPIRY_DATE      DATE,
  CREATE_USER_ID   VARCHAR2(32)  default user          NOT NULL,
  CREATE_DATE      DATE default  SYSDATE               NOT NULL,
  MODIFY_USER_ID   VARCHAR2(32),
  MODIFY_DATETIME  TIMESTAMP(9)  default SYSTIMESTAMP,
  CREATE_DATETIME  TIMESTAMP(9)  default SYSTIMESTAMP  NOT NULL,
  SEAL_FLAG        VARCHAR2(1),
  CONSTRAINT HO_CODES_PK
    PRIMARY KEY (HO_CODE)
);

COMMENT ON TABLE HO_CODES is 'This table will store the HO codes and Description of the HO codes';

COMMENT ON COLUMN HO_CODES.HO_CODE         IS 'It is a combination of HO Class/HO Sub Class';
COMMENT ON COLUMN HO_CODES.DESCRIPTION     IS 'It is the description of the HO Code';
COMMENT ON COLUMN HO_CODES.ACTIVE_FLAG     IS 'Active code if it is ''Y'', Inactive code if it is ''N''';
COMMENT ON COLUMN HO_CODES.EXPIRY_DATE     IS 'Expiry Date of the Code';
COMMENT ON COLUMN HO_CODES.CREATE_USER_ID  IS 'The user who creates the record';
COMMENT ON COLUMN HO_CODES.CREATE_DATE     IS 'When the record was created';
COMMENT ON COLUMN HO_CODES.MODIFY_USER_ID  IS 'The user who modifies the record';
COMMENT ON COLUMN HO_CODES.MODIFY_DATETIME IS 'The timestamp when the record is modified ';
COMMENT ON COLUMN HO_CODES.CREATE_DATETIME IS 'The timestamp when the record is created';

