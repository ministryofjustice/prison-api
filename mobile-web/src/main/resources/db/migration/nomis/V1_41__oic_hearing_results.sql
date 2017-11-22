CREATE TABLE OIC_HEARING_RESULTS (
  --'The outcome of an adjudication hearing in respect of a specific charge. NOTE1 : there cannot be more than one result per charge. If a result is quashed on appeal then the status of the result is changed to quashed. NOTE2 : it follows from Note1 above that (a) attributes Plea Finding Code & Finding Code & OIC Hearing Id belong logically within the Agency Incident Charge entity (ie. they are dependent on the Charge - not on both Charge & Hearing). In other words, this entity is logically redundant. Hence, it is represented here with a 1:1 relationship with Agency incident Charge. NOTE3 : as per the comment on parent entity Agency Incident Charge, the FK inherited from that parent is a natural key rather than the physical primary key of the parent';
    OIC_HEARING_ID BIGINT NOT NULL,                        --System generated primary key for hearing.';
    RESULT_SEQ INTEGER NOT NULL,                           --Sequential number for hearing results.';
    AGENCY_INCIDENT_ID BIGINT NOT NULL,                    --System generated seqential log number for the incident.';
    CHARGE_SEQ INTEGER NOT NULL,                           --Sequential number for charge.';
    PLEA_FINDING_CODE VARCHAR(12) NOT NULL,                --Reference Code ( FINDING ). The offenders plea on this charge.';
    FINDING_CODE VARCHAR(12) NOT NULL,                     --Reference Code ( FINDING )';
    CREATE_DATETIME TIMESTAMP DEFAULT now() NOT NULL,      --The timestamp when the record is created';
    CREATE_USER_ID VARCHAR(32) DEFAULT USER NOT NULL,      --The user who creates the record';
    MODIFY_DATETIME TIMESTAMP,                             --The timestamp when the record is modified ';
    MODIFY_USER_ID VARCHAR(32),                            --The user who modifies the record';
    AUDIT_TIMESTAMP TIMESTAMP, 
    AUDIT_USER_ID VARCHAR(32), 
    AUDIT_MODULE_NAME VARCHAR(65), 
    AUDIT_CLIENT_USER_ID VARCHAR(64), 
    AUDIT_CLIENT_IP_ADDRESS VARCHAR(39), 
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR(64), 
    AUDIT_ADDITIONAL_INFO VARCHAR(256), 
    OIC_OFFENCE_ID BIGINT NOT NULL,
     CONSTRAINT OIC_HEARING_RESULTS_PK PRIMARY KEY (OIC_HEARING_ID, RESULT_SEQ),
     CONSTRAINT OIC_HEARING_RESULTS_UK UNIQUE (OIC_HEARING_ID, AGENCY_INCIDENT_ID, CHARGE_SEQ)
);

CREATE INDEX OIC_HR_OIC_AGY_INC_CHG_FK ON OIC_HEARING_RESULTS (AGENCY_INCIDENT_ID, CHARGE_SEQ)    ;
CREATE INDEX OIC_HEARING_RESULTS_FK1 ON OIC_HEARING_RESULTS (OIC_OFFENCE_ID)  ;
