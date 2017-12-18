CREATE TABLE INTERNAL_LOCATION_USAGES --'The agency internal location usages'
(   INTERNAL_LOCATION_USAGE_ID NUMBER(10,0)                      NOT NULL, --'The unique identifier of the agency location usage';
    AGY_LOC_ID                 VARCHAR2(6)                       NOT NULL, --'The agency location of the usage';
    INTERNAL_LOCATION_USAGE    VARCHAR2(12)                      NOT NULL, --'The usage type.  Reference Code(ILOC_USG)';
    EVENT_SUB_TYPE             VARCHAR2(12),                               --'The sub usage type.  Reference Code(Event_Subtyp)';
    CREATE_DATETIME            TIMESTAMP(9) DEFAULT SYSTIMESTAMP NOT NULL, --'The timestamp when the record is created';
    CREATE_USER_ID             VARCHAR2(32) DEFAULT user         NOT NULL, --'The user who creates the record';
    MODIFY_DATETIME            TIMESTAMP(9) DEFAULT SYSTIMESTAMP,          --'The timestamp when the record is modified ';
    MODIFY_USER_ID             VARCHAR2(32),                               --'The user who modifies the record';
    SEAL_FLAG                  VARCHAR2(1)
);

CREATE UNIQUE INDEX INTERNAL_LOCATION_USAGES_UI1 ON INTERNAL_LOCATION_USAGES (AGY_LOC_ID, INTERNAL_LOCATION_USAGE, EVENT_SUB_TYPE);
CREATE UNIQUE INDEX INTERNAL_LOCATION_USAGES_PK ON INTERNAL_LOCATION_USAGES (INTERNAL_LOCATION_USAGE_ID);
ALTER TABLE INTERNAL_LOCATION_USAGES ADD CONSTRAINT INTERNAL_LOCATION_USAGES_PK PRIMARY KEY (INTERNAL_LOCATION_USAGE_ID);
