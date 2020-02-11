
  CREATE TABLE "CURFEW_ADDRESSES"
   (    "CURFEW_ADDRESS_ID" NUMBER(10,0) NOT NULL,
    "OFFENDER_BOOK_ID" NUMBER(10,0),
    "ADDRESS_ID" NUMBER(10,0) NOT NULL,
    "ACTIVE_FLAG" VARCHAR2(1 CHAR) DEFAULT NULL,
    "ELECTRICITY_FLAG" VARCHAR2(1 CHAR) DEFAULT NULL,
    "PHONE_FLAG" VARCHAR2(1 CHAR) DEFAULT NULL,
    "PHONE_NO" VARCHAR2(40 CHAR),
    "EXT_NO" VARCHAR2(7 CHAR),
    "ELECTRICITY_CONFIRM_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "PHONE_CONFIRM_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "PHONE_NO_CONFIRM_FLAG" VARCHAR2(1 CHAR) DEFAULT 'N',
    "OFFENDER_CURFEW_ID" NUMBER(10,0) NOT NULL,
    "CREATE_DATETIME" TIMESTAMP (9) DEFAULT systimestamp NOT NULL,
    "CREATE_USER_ID" VARCHAR2(32 CHAR) DEFAULT USER NOT NULL,
    "MODIFY_DATETIME" TIMESTAMP (9),
    "MODIFY_USER_ID" VARCHAR2(32 CHAR),
    "AUDIT_TIMESTAMP" TIMESTAMP (9),
    "AUDIT_USER_ID" VARCHAR2(32 CHAR),
    "AUDIT_MODULE_NAME" VARCHAR2(65 CHAR),
    "AUDIT_CLIENT_USER_ID" VARCHAR2(64 CHAR),
    "AUDIT_CLIENT_IP_ADDRESS" VARCHAR2(39 CHAR),
    "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
    "AUDIT_ADDITIONAL_INFO" VARCHAR2(256 CHAR),
     CONSTRAINT "CURFEW_ADDRESSES_PK" PRIMARY KEY ("CURFEW_ADDRESS_ID"),
  );

  CREATE INDEX "CURFEW_ADDRESSES_NI2" ON "CURFEW_ADDRESSES" ("ADDRESS_ID");


  CREATE INDEX "CURFEW_ADDRESSES_FK9" ON "CURFEW_ADDRESSES" ("OFFENDER_BOOK_ID");


  CREATE INDEX "CURFEW_ADDRESSES_NI1" ON "CURFEW_ADDRESSES" ("OFFENDER_CURFEW_ID");


  CREATE UNIQUE INDEX "CURFEW_ADDRESSES_PK" ON "CURFEW_ADDRESSES" ("CURFEW_ADDRESS_ID");
