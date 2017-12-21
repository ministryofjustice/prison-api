CREATE TABLE OFFENDER_VISIT_VISITORS 
(   OFFENDER_VISIT_ID         NUMBER(10)                              NOT NULL, --'The visit ID';
    PERSON_ID                 NUMBER(10),                                       --'The ID of the person';
    GROUP_LEADER_FLAG         VARCHAR2(1 BYTE)   DEFAULT 'N',                   --'?If the person the group leader';
    OFFENDER_VISIT_VISITOR_ID NUMBER(10)                              NOT NULL, --'The visit Visitor ID';
    ASSISTED_VISIT_FLAG       VARCHAR2(1 BYTE)   DEFAULT 'N'          NOT NULL, --'?If assisted visited applied';
    COMMENT_TEXT              VARCHAR2(240 BYTE),                               --'The comment of the visitor';
    CREATE_DATETIME           TIMESTAMP(9)       DEFAULT SYSTIMESTAMP NOT NULL, --'The timestamp when the record is created';
    CREATE_USER_ID            VARCHAR2(32 BYTE)                       NOT NULL, --'The user who creates the record';
    MODIFY_DATETIME           TIMESTAMP(9)       DEFAULT SYSTIMESTAMP,          --'The timestamp when the record is modified ';
    MODIFY_USER_ID            VARCHAR2(32 BYTE),                                --'The user who modifies the record';
    EVENT_OUTCOME             VARCHAR2(12 BYTE),                                --'The event outcome. Reference Code(OUTCOMES)';
    OUTCOME_REASON_CODE       VARCHAR2(12 BYTE),                                --'The reason of the outcome';
    OFFENDER_BOOK_ID          NUMBER(10),                                       --'The offender Book ID';
    EVENT_ID                  NUMBER(10),                                       --'The Offender event ID';
    EVENT_STATUS              VARCHAR2(12 BYTE)  DEFAULT 'SCH',                 --'The status of the event. Reference Code(EVENT_STS)';
    SEAL_FLAG                 VARCHAR2(1 BYTE)
);

CREATE UNIQUE INDEX OFFENDER_VISIT_VISITORS_PK ON OFFENDER_VISIT_VISITORS (OFFENDER_VISIT_VISITOR_ID);
CREATE        INDEX OFFENDER_VISIT_VISITORS_NI1 ON OFFENDER_VISIT_VISITORS (OFFENDER_BOOK_ID);
CREATE UNIQUE INDEX OFFNDER_VISIT_VISITORS_UI1 ON OFFENDER_VISIT_VISITORS (EVENT_ID);
CREATE        INDEX OFFENDER_VISIT_VISITORS_NI3 ON OFFENDER_VISIT_VISITORS (PERSON_ID);
--CREATE UNIQUE INDEX OFFENDER_VISIT_VISITORS_UI2 ON OFFENDER_VISIT_VISITORS (OFFENDER_VISIT_ID, NVL(PERSON_ID,OFFENDER_BOOK_ID), DECODE(TO_CHAR(PERSON_ID),NULL,'OFF','PER'));

ALTER TABLE OFFENDER_VISIT_VISITORS ADD CONSTRAINT OFFENDER_VISIT_VISITORS_C1 CHECK (EVENT_OUTCOME IN ('ATT', 'ABS', 'CANC'));
ALTER TABLE OFFENDER_VISIT_VISITORS ADD CONSTRAINT OFFENDER_VISIT_VISITORS_PK PRIMARY KEY (OFFENDER_VISIT_VISITOR_ID);
