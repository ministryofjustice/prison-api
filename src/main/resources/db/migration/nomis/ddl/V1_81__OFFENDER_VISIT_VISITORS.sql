CREATE TABLE OFFENDER_VISIT_VISITORS
(   OFFENDER_VISIT_ID             NUMBER(10)                               NOT NULL,  --'The visit ID';
    PERSON_ID                     NUMBER(10),                                         --'The ID of the person';
    GROUP_LEADER_FLAG             VARCHAR2(1 CHAR)    DEFAULT 'N'          NOT NULL,  --'?If the person the group leader';
    OFFENDER_VISIT_VISITOR_ID     NUMBER(10)                               NOT NULL,  --'The visit Visitor ID';
    ASSISTED_VISIT_FLAG           VARCHAR2(1 CHAR)    DEFAULT 'N'          NOT NULL,  --'?If assisted visited applied';
    COMMENT_TEXT                  VARCHAR2(240 CHAR),                                 --'The comment of the visitor';
    CREATE_DATETIME               TIMESTAMP(9)        DEFAULT systimestamp NOT NULL,  --'The timestamp when the record is created';
    CREATE_USER_ID                VARCHAR2(32 CHAR)   DEFAULT USER         NOT NULL,  --'The user who creates the record';
    MODIFY_DATETIME               TIMESTAMP(9),                                       --'The timestamp when the record is modified';
    MODIFY_USER_ID                VARCHAR2(32 CHAR),                                  --'The user who modifies the record';
    AUDIT_TIMESTAMP               TIMESTAMP(9),
    AUDIT_USER_ID                 VARCHAR2(32 CHAR),
    AUDIT_MODULE_NAME             VARCHAR2(65 CHAR),
    AUDIT_CLIENT_USER_ID          VARCHAR2(64 CHAR),
    AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39 CHAR),
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64 CHAR),
    AUDIT_ADDITIONAL_INFO         VARCHAR2(256 CHAR),
    EVENT_OUTCOME                 VARCHAR2(12 CHAR),                                  --'The event outcome. Reference Code(OUTCOMES)';
    OUTCOME_REASON_CODE           VARCHAR2(12 CHAR),                                  --'The reason of the outcome';
    OFFENDER_BOOK_ID              NUMBER(10),                                         --'The offender Book ID';
    EVENT_ID                      NUMBER(10),                                         --'The Offender event ID';
    EVENT_STATUS                  VARCHAR2(12 CHAR)   DEFAULT 'SCH'                   --'The status of the event. Reference Code(EVENT_STS)';
);

CREATE        INDEX OFFENDER_VISIT_VISITORS_NI1 ON OFFENDER_VISIT_VISITORS (OFFENDER_BOOK_ID);
CREATE        INDEX OFFENDER_VISIT_VISITORS_UI2 ON OFFENDER_VISIT_VISITORS (OFFENDER_VISIT_ID);
CREATE UNIQUE INDEX OFFNDER_VISIT_VISITORS_UI1 ON OFFENDER_VISIT_VISITORS (EVENT_ID);
CREATE        INDEX OFF_VIS_VISR_PER_FK ON OFFENDER_VISIT_VISITORS (PERSON_ID);

ALTER TABLE OFFENDER_VISIT_VISITORS ADD CONSTRAINT OFFENDER_VISIT_VISITORS_C1 CHECK (EVENT_OUTCOME IN ('ATT', 'ABS', 'CANC'));
ALTER TABLE OFFENDER_VISIT_VISITORS ADD CONSTRAINT OFFENDER_VISIT_VISITORS_PK PRIMARY KEY (OFFENDER_VISIT_VISITOR_ID);
ALTER TABLE OFFENDER_VISIT_VISITORS ADD CONSTRAINT OFFENDER_VISIT_VISITORS_FK9 FOREIGN KEY (OFFENDER_BOOK_ID) REFERENCES OFFENDER_BOOKINGS (OFFENDER_BOOK_ID);
ALTER TABLE OFFENDER_VISIT_VISITORS ADD CONSTRAINT OFF_VIS_VISR_OFF_VIS_FK FOREIGN KEY (OFFENDER_VISIT_ID) REFERENCES OFFENDER_VISITS (OFFENDER_VISIT_ID);
ALTER TABLE OFFENDER_VISIT_VISITORS ADD CONSTRAINT OFF_VIS_VISR_PER_FK FOREIGN KEY (PERSON_ID) REFERENCES PERSONS (PERSON_ID);
