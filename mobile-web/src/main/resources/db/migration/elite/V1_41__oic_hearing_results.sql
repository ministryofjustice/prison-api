CREATE TABLE OIC_HEARING_RESULTS (
--'The results recorded during the OIC hearing.'
    OIC_HEARING_ID NUMBER(10) NOT NULL,                          --System generated primary key for hearing.';
    RESULT_SEQ NUMBER(6) NOT NULL,                               --Sequential number for hearing results.';
    AGENCY_INCIDENT_ID NUMBER(10) NOT NULL,                      --System generated seqential log number for the incident.';
    CHARGE_SEQ NUMBER(6) NOT NULL,                               --Sequential number for charge.';
    PLEA_FINDING_CODE VARCHAR2(12) NOT NULL,                     --Reference Code ( FINDING ). The offenders plea on this charge.';
    FINDING_CODE VARCHAR2(12) NOT NULL,                          --Reference Code ( FINDING )';
    CREATE_DATETIME TIMESTAMP (9) DEFAULT SYSTIMESTAMP NOT NULL, --The timestamp when the record is created';
    CREATE_USER_ID VARCHAR2(32) DEFAULT user NOT NULL,           --The user who creates the record';
    MODIFY_DATETIME TIMESTAMP (9) DEFAULT SYSTIMESTAMP,          --The timestamp when the record is modified ';
    MODIFY_USER_ID VARCHAR2(32),                                 --The user who modifies the record';
    OIC_OFFENCE_ID NUMBER(10) NOT NULL, 
    SEAL_FLAG VARCHAR2(1),
     CONSTRAINT OIC_HEARING_RESULTS_PK PRIMARY KEY (OIC_HEARING_ID, RESULT_SEQ),
     CONSTRAINT OIC_HEARING_RESULTS_UK UNIQUE (OIC_HEARING_ID, AGENCY_INCIDENT_ID, CHARGE_SEQ)
);

CREATE INDEX OIC_HEARING_RESULTS_NI1 ON OIC_HEARING_RESULTS (AGENCY_INCIDENT_ID, CHARGE_SEQ);
CREATE INDEX OIC_HEARING_RESULTS_NI2 ON OIC_HEARING_RESULTS (OIC_OFFENCE_ID);
