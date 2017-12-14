CREATE TABLE INT_LOC_USAGE_LOCATIONS --'The internal locations of the agency internal location usage'
(   INTERNAL_LOCATION_USAGE_ID NUMBER(10,0)                      NOT NULL, --'The primary agency internal location usage';
    INTERNAL_LOCATION_ID       NUMBER(10,0)                      NOT NULL, --'The agency internal location ID';
    CAPACITY                   NUMBER(5,0),                                --'The capacity of the internal location in the context of the usage';
    USAGE_LOCATION_TYPE        VARCHAR2(12),                               --'The internal location type : Reference Code(ILOC_TYPE)';
    CREATE_DATETIME            TIMESTAMP(9) DEFAULT SYSTIMESTAMP NOT NULL, --'The timestamp when the record is created';
    CREATE_USER_ID             VARCHAR2(32) DEFAULT user         NOT NULL, --'The user who creates the record';
    MODIFY_DATETIME            TIMESTAMP(9) DEFAULT SYSTIMESTAMP,          --'The timestamp when the record is modified ';
    MODIFY_USER_ID             VARCHAR2(32),                               --'The user who modifies the record';
    LIST_SEQ                   NUMBER(6,0),                                --'The order of the list';
    USAGE_LOCATION_ID          NUMBER(10,0)                      NOT NULL,
    PARENT_USAGE_LOCATION_ID   NUMBER(10,0),
    SEAL_FLAG                  VARCHAR2(1)
) CLUSTER C_INTERNAL_LOCATION_USAGES (INTERNAL_LOCATION_USAGE_ID);

CREATE INDEX INT_LOC_USAGE_LOCATIONS_NI1 ON INT_LOC_USAGE_LOCATIONS (PARENT_USAGE_LOCATION_ID);
CREATE UNIQUE INDEX INT_LOC_USAGE_LOCATIONS_PK ON INT_LOC_USAGE_LOCATIONS (USAGE_LOCATION_ID);
CREATE UNIQUE INDEX INT_LOC_USAGE_LOCATIONS_UK ON INT_LOC_USAGE_LOCATIONS (INTERNAL_LOCATION_USAGE_ID, INTERNAL_LOCATION_ID);
CREATE INDEX INT_LOC_USAGE_LOCATIONS_NI2 ON INT_LOC_USAGE_LOCATIONS (INTERNAL_LOCATION_ID);

ALTER TABLE INT_LOC_USAGE_LOCATIONS ADD CONSTRAINT INT_LOC_USAGE_LOCATIONS_PK PRIMARY KEY (USAGE_LOCATION_ID);
ALTER TABLE INT_LOC_USAGE_LOCATIONS ADD CONSTRAINT INT_LOC_USAGE_LOCATIONS_UK UNIQUE (INTERNAL_LOCATION_USAGE_ID, INTERNAL_LOCATION_ID);
