create table OFFENDER_MEDICAL_TREATMENTS
(
    OFFENDER_MEDICAL_TREATMENT_ID NUMBER(10)                             not null
        constraint OFFENDER_MEDICAL_TREATMENTS_PK
            primary key,
    OFFENDER_HEALTH_PROBLEM_ID    NUMBER(10)                             not null
        constraint OFF_MED_TRT_OFF_HLTH_PROB_FK
            references OFFENDER_HEALTH_PROBLEMS,
    START_DATE                    DATE                                   not null,
    CASELOAD_TYPE                 VARCHAR2(12 char),
    END_DATE                      DATE,
    TREATMENT_CODE                VARCHAR2(12 char)                      not null,
    COMMENT_TEXT                  VARCHAR2(240 char),
    DESCRIPTION                   VARCHAR2(240 char),
    TREATMENT_PROVIDER_CODE       VARCHAR2(12 char),
    CREATE_DATETIME               TIMESTAMP(9)      default systimestamp not null,
    CREATE_USER_ID                VARCHAR2(32 char) default USER         not null,
    MODIFY_DATETIME               TIMESTAMP(9),
    MODIFY_USER_ID                VARCHAR2(32 char),
    AUDIT_TIMESTAMP               TIMESTAMP(9),
    AUDIT_USER_ID                 VARCHAR2(32 char),
    AUDIT_MODULE_NAME             VARCHAR2(65 char),
    AUDIT_CLIENT_USER_ID          VARCHAR2(64 char),
    AUDIT_CLIENT_IP_ADDRESS       VARCHAR2(39 char),
    AUDIT_CLIENT_WORKSTATION_NAME VARCHAR2(64 char),
    AUDIT_ADDITIONAL_INFO         VARCHAR2(256 char),
    AGY_LOC_ID                    VARCHAR2(6 char),
    constraint OFFENDER_MEDICAL_TREATMENTS_UK
        unique (OFFENDER_HEALTH_PROBLEM_ID, START_DATE, TREATMENT_PROVIDER_CODE)
);
--     cluster C_OFFENDER_HEALTH_PROBLEMS ()

comment on table OFFENDER_MEDICAL_TREATMENTS is 'Offender Medical Treatmen Details';

comment on column OFFENDER_MEDICAL_TREATMENTS.OFFENDER_MEDICAL_TREATMENT_ID is 'The offender treatment ID';

comment on column OFFENDER_MEDICAL_TREATMENTS.OFFENDER_HEALTH_PROBLEM_ID is 'The offender health problem ID';

comment on column OFFENDER_MEDICAL_TREATMENTS.START_DATE is 'The start date of the treatment';

comment on column OFFENDER_MEDICAL_TREATMENTS.CASELOAD_TYPE is 'The information source';

comment on column OFFENDER_MEDICAL_TREATMENTS.END_DATE is 'The end date of the treatment';

comment on column OFFENDER_MEDICAL_TREATMENTS.TREATMENT_CODE is 'Reference Code ( MED_TREAT ) : Medical Treatment';

comment on column OFFENDER_MEDICAL_TREATMENTS.COMMENT_TEXT is 'Comment Text';

comment on column OFFENDER_MEDICAL_TREATMENTS.DESCRIPTION is 'The treatment description';

comment on column OFFENDER_MEDICAL_TREATMENTS.TREATMENT_PROVIDER_CODE is 'The treatment provider code (HEALTH_PROV domain)';

comment on column OFFENDER_MEDICAL_TREATMENTS.CREATE_DATETIME is 'The timestamp when the record is created';

comment on column OFFENDER_MEDICAL_TREATMENTS.CREATE_USER_ID is 'The user who creates the record';

comment on column OFFENDER_MEDICAL_TREATMENTS.MODIFY_DATETIME is 'The timestamp when the record is modified';

comment on column OFFENDER_MEDICAL_TREATMENTS.MODIFY_USER_ID is 'The user who modifies the record';
