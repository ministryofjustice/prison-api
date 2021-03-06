CREATE TABLE AGENCY_INCIDENT_CHARGES (
    AGENCY_INCIDENT_ID NUMBER(10,0),
    CHARGE_SEQ NUMBER(6,0),
    PARTY_SEQ NUMBER(6,0),
    OIC_CHARGE_ID VARCHAR2(13),
    FINDING_CODE VARCHAR2(12),
    GUILTY_EVIDENCE_TEXT VARCHAR2(400),
    REPORT_TEXT VARCHAR2(400),
    EVIDENCE_DISPOSE_TEXT VARCHAR2(400),
    CREATE_DATETIME TIMESTAMP DEFAULT systimestamp ,
    CREATE_USER_ID VARCHAR2(32) DEFAULT USER ,
    MODIFY_DATETIME TIMESTAMP,
    MODIFY_USER_ID VARCHAR2(32),
    AUDIT_TIMESTAMP TIMESTAMP,
    AUDIT_USER_ID VARCHAR2(32),
    AUDIT_MODULE_NAME VARCHAR2(65),
    AUDIT_CLIENT_USER_ID VARCHAR2(64),
    AUDIT_CLIENT_IP_ADDRESS VARCHAR2(39),
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64),
    AUDIT_ADDITIONAL_INFO VARCHAR2(256),
    LIDS_CHARGE_NUMBER NUMBER(6,0),
    CHARGED_OIC_OFFENCE_ID NUMBER(10,0),
    RESULT_OIC_OFFENCE_ID NUMBER(10,0),
    CONSTRAINT AGENCY_INCIDENT_CHARGES_PK PRIMARY KEY (AGENCY_INCIDENT_ID,CHARGE_SEQ),
    CONSTRAINT AGENCY_INCIDENT_CHARGES_UK UNIQUE (OIC_CHARGE_ID),
    CONSTRAINT AGENCY_INCIDENT_CHARGES_FK1 FOREIGN KEY (CHARGED_OIC_OFFENCE_ID) REFERENCES OIC_OFFENCES(OIC_OFFENCE_ID),
    CONSTRAINT AGENCY_INCIDENT_CHARGES_FK2 FOREIGN KEY (RESULT_OIC_OFFENCE_ID) REFERENCES OIC_OFFENCES(OIC_OFFENCE_ID),
    CONSTRAINT AGY_INC_CHG_AGY_INC_PTY_FK FOREIGN KEY (AGENCY_INCIDENT_ID,PARTY_SEQ) REFERENCES AGENCY_INCIDENT_PARTIES(AGENCY_INCIDENT_ID,PARTY_SEQ)
);
CREATE INDEX AGENCY_INCIDENT_CHARGES_FK1 ON AGENCY_INCIDENT_CHARGES (CHARGED_OIC_OFFENCE_ID);
CREATE INDEX AGENCY_INCIDENT_CHARGES_FK2 ON AGENCY_INCIDENT_CHARGES (RESULT_OIC_OFFENCE_ID);
CREATE UNIQUE INDEX AGENCY_INCIDENT_CHARGES_PK ON AGENCY_INCIDENT_CHARGES (AGENCY_INCIDENT_ID,CHARGE_SEQ);
CREATE UNIQUE INDEX AGENCY_INCIDENT_CHARGES_UK ON AGENCY_INCIDENT_CHARGES (OIC_CHARGE_ID);
CREATE INDEX AGY_INC_CHG_AGY_INC_PTY_FK ON AGENCY_INCIDENT_CHARGES (AGENCY_INCIDENT_ID,PARTY_SEQ);
