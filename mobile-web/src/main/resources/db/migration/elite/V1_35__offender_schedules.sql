-----------------------------------------------------------------------
-- Source tables for Prison Activities (PRISON_ACT) Scheduled Events --
-----------------------------------------------------------------------
CREATE TABLE ADDRESSES
(
	ADDRESS_ID                      NUMBER(10)                          NOT NULL,
	OWNER_CLASS                     VARCHAR2(12)                        NOT NULL,
	OWNER_ID                        NUMBER(10),
	OWNER_SEQ                       NUMBER(6),
	OWNER_CODE                      VARCHAR2(12),
	ADDRESS_TYPE                    VARCHAR2(12),
	CITY_CODE                       VARCHAR2(12),
	COUNTRY_CODE                    VARCHAR2(12),
	VALIDATED_PAF_FLAG              VARCHAR2(1),
	PRIMARY_FLAG                    VARCHAR2(1)                         NOT NULL,
	MAIL_FLAG                       VARCHAR2(1)                         NOT NULL,
	CAPACITY                        NUMBER(5),
	COMMENT_TEXT                    VARCHAR2(240),
	CREATE_DATETIME                 TIMESTAMP(9)   DEFAULT SYSTIMESTAMP NOT NULL,
	CREATE_USER_ID                  VARCHAR2(32)   DEFAULT USER         NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP(9)   DEFAULT SYSTIMESTAMP,
	MODIFY_USER_ID                  VARCHAR2(32),
	NO_FIXED_ADDRESS_FLAG           VARCHAR2(1),
	SERVICES_FLAG                   VARCHAR2(1),
	SPECIAL_NEEDS_CODE              VARCHAR2(12),
	CONTACT_PERSON_NAME             VARCHAR2(40),
	BUSINESS_HOUR                   VARCHAR2(60),
	START_DATE                      DATE,
	END_DATE                        DATE,
	CITY_NAME                       VARCHAR2(40),
  PROV_STATE_CODE                 VARCHAR2(12),
  STREET                          VARCHAR2(160),
  ZIP_POSTAL_CODE                 VARCHAR2(12),
  SUITE_NUMBER                    VARCHAR2(30),
  STREET_NUMBER                   VARCHAR2(12),
  STREET_DIRECTION                VARCHAR2(12),
  MAIL_CARE_OF                    VARCHAR2(40),
  SEAL_FLAG                       VARCHAR2(1)
);

ALTER TABLE ADDRESSES ADD CONSTRAINT ADDRESSES_PK PRIMARY KEY (ADDRESS_ID);


CREATE TABLE COURSE_ACTIVITIES
(
	CRS_ACTY_ID                     NUMBER(10)                          NOT NULL,
	CASELOAD_ID                     VARCHAR2(6),
	AGY_LOC_ID                      VARCHAR2(6),
	DESCRIPTION                     VARCHAR2(40),
	CAPACITY                        NUMBER(3)      DEFAULT 99,
	ACTIVE_FLAG                     VARCHAR2(1)    DEFAULT 'Y'          NOT NULL,
	EXPIRY_DATE                     DATE,
	SCHEDULE_START_DATE             DATE,
	SCHEDULE_END_DATE               DATE,
	CASELOAD_TYPE                   VARCHAR2(12),
	SERVICES_ADDRESS_ID             NUMBER(10),
	PROGRAM_ID                      NUMBER(10)                          NOT NULL,
	PARENT_CRS_ACTY_ID              NUMBER(10),
	INTERNAL_LOCATION_ID            NUMBER(10),
	PROVIDER_PARTY_CLASS            VARCHAR2(12),
	PROVIDER_PARTY_ID               NUMBER(10),
	PROVIDER_PARTY_CODE             VARCHAR2(6),
	BENEFICIARY_NAME                VARCHAR2(80),
	BENEFICIARY_CONTACT             VARCHAR2(80),
	LIST_SEQ                        NUMBER(6),
	PLACEMENT_CORPORATE_ID          NUMBER(10),
	COMMENT_TEXT                    VARCHAR2(240),
	AGENCY_LOCATION_TYPE            VARCHAR2(12),
	PROVIDER_TYPE                   VARCHAR2(12),
	BENEFICIARY_TYPE                VARCHAR2(12),
	PLACEMENT_TEXT                  VARCHAR2(240),
	CODE                            VARCHAR2(20),
	HOLIDAY_FLAG                    VARCHAR2(1)    DEFAULT 'N',
	COURSE_CLASS                    VARCHAR2(12)   DEFAULT 'COURSE'     NOT NULL,
	COURSE_ACTIVITY_TYPE            VARCHAR2(12),
	CREATE_DATETIME                 TIMESTAMP(9)   DEFAULT SYSTIMESTAMP NOT NULL,
	CREATE_USER_ID                  VARCHAR2(32)   DEFAULT USER         NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP(9)   DEFAULT SYSTIMESTAMP,
	MODIFY_USER_ID                  VARCHAR2(32),
	IEP_LEVEL                       VARCHAR2(12),
	NO_OF_SESSIONS                  NUMBER(6),
	SESSION_LENGTH                  NUMBER(6),
  MULTI_PHASE_SCHEDULING_FLAG     VARCHAR2(12),
	SCHEDULE_NOTES                  VARCHAR2(240),
	SEAL_FLAG                       VARCHAR2(1),
	ALLOW_DOUBLE_BOOK_FLAG          VARCHAR2(1)
);

ALTER TABLE COURSE_ACTIVITIES ADD CONSTRAINT COURSE_ACTIVITIES_PK PRIMARY KEY (CRS_ACTY_ID);

--ALTER TABLE COURSE_ACTIVITIES ADD FOREIGN KEY (AGY_LOC_ID) REFERENCES AGENCY_LOCATIONS;
--ALTER TABLE COURSE_ACTIVITIES ADD FOREIGN KEY (INTERNAL_LOCATION_ID) REFERENCES AGENCY_INTERNAL_LOCATIONS;
--ALTER TABLE COURSE_ACTIVITIES ADD FOREIGN KEY (SERVICES_ADDRESS_ID) REFERENCES ADDRESSES;
--ALTER TABLE COURSE_ACTIVITIES ADD FOREIGN KEY (PROGRAM_ID) REFERENCES PROGRAM_SERVICES;
--ALTER TABLE COURSE_ACTIVITIES ADD FOREIGN KEY (PARENT_CRS_ACTY_ID) REFERENCES COURSE_ACTIVITIES;
--ALTER TABLE COURSE_ACTIVITIES ADD FOREIGN KEY (PLACEMENT_CORPORATE_ID) REFERENCES CORPORATES;
ALTER TABLE COURSE_ACTIVITIES ADD CHECK (COURSE_CLASS IN ('COURSE','CRS_MOD','CRS_PH', 'MED_SCHED'));


CREATE TABLE COURSE_SCHEDULES
(
	CRS_SCH_ID                      NUMBER(10)                          NOT NULL,
  CRS_ACTY_ID                     NUMBER(10)                          NOT NULL,
	WEEKDAY                         VARCHAR2(12),
	SCHEDULE_DATE                   DATE                                NOT NULL,
	START_TIME                      DATE                                NOT NULL,
	END_TIME                        DATE,
	SESSION_NO                      NUMBER(6),
	DETAILS                         VARCHAR2(40),
	CREATE_DATETIME                 TIMESTAMP(9)   DEFAULT SYSTIMESTAMP NOT NULL,
	CREATE_USER_ID                  VARCHAR2(32)   DEFAULT USER         NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP(9)   DEFAULT SYSTIMESTAMP,
	MODIFY_USER_ID                  VARCHAR2(32),
	SCHEDULE_STATUS                 VARCHAR2(12)   DEFAULT 'SCH',
	CATCH_UP_CRS_SCH_ID             NUMBER(10),
	VIDEO_REFERENCE_ID              VARCHAR2(20),
	SEAL_FLAG                       VARCHAR2(1),
	CANCELLED_FLAG                  VARCHAR2(1)
);

ALTER TABLE COURSE_SCHEDULES ADD CONSTRAINT COURSE_SCHEDULES_PK PRIMARY KEY (CRS_SCH_ID);

--ALTER TABLE COURSE_SCHEDULES ADD FOREIGN KEY (CRS_ACTY_ID) REFERENCES COURSE_ACTIVITIES;
--ALTER TABLE COURSE_SCHEDULES ADD FOREIGN KEY (CATCH_UP_CRS_SCH_ID) REFERENCES COURSE_SCHEDULES;


CREATE TABLE OFFENDER_PROGRAM_PROFILES
(
	OFF_PRGREF_ID                   NUMBER(10)                          NOT NULL,
	OFFENDER_BOOK_ID                NUMBER(10)                          NOT NULL,
	PROGRAM_ID                      NUMBER(10)                          NOT NULL,
	OFFENDER_START_DATE             DATE,
	OFFENDER_PROGRAM_STATUS         VARCHAR2(12)   DEFAULT 'PLAN'       NOT NULL,
	CRS_ACTY_ID                     NUMBER(10),
	REFERRAL_PRIORITY               VARCHAR2(12),
	REFERRAL_DATE                   DATE,
	REFERRAL_COMMENT_TEXT           VARCHAR2(1000),
	OFFENDER_END_REASON             VARCHAR2(12),
	AGREED_TRAVEL_FARE              NUMBER(11,2),
	AGREED_TRAVEL_HOUR              NUMBER(6,2),
	OFFENDER_END_COMMENT_TEXT       VARCHAR2(240),
	REJECT_DATE                     DATE,
	WAITLIST_DECISION_CODE          VARCHAR2(12),
	REFERRAL_STAFF_ID               NUMBER(10),
	OFFENDER_END_DATE               DATE,
	CREDIT_WORK_HOURS               NUMBER(8,2),
	CREDIT_OTHER_HOURS              NUMBER(8,2),
	SUSPENDED_FLAG                  VARCHAR2(1)    DEFAULT 'N',
	REJECT_REASON_CODE              VARCHAR2(12),
	AGY_LOC_ID                      VARCHAR2(6),
	CREATE_DATETIME                 TIMESTAMP(9)   DEFAULT SYSTIMESTAMP NOT NULL,
	CREATE_USER_ID                  VARCHAR2(32)   DEFAULT USER         NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP(9)   DEFAULT SYSTIMESTAMP,
	MODIFY_USER_ID                  VARCHAR2(32),
	REVIEWED_BY                     VARCHAR2(32),
	OFFENDER_SENT_CONDITION_ID      NUMBER(10),
	SENTENCE_SEQ                    NUMBER(6),
	HOLIDAY_FLAG                    VARCHAR2(1)    DEFAULT 'N',
	START_SESSION_NO                NUMBER(6),
	PARENT_OFF_PRGREF_ID            NUMBER(10),
	OFFENDER_PRG_OBLIGATION_ID      NUMBER(10),
	PROGRAM_OFF_PRGREF_ID           NUMBER(10),
	PROFILE_CLASS                   VARCHAR2(12)   DEFAULT 'PRG',
	COMPLETION_DATE                 DATE,
	NEEDED_FLAG                     VARCHAR2(1)    DEFAULT 'Y',
	COMMENT_TEXT                    VARCHAR2(240),
	EARLY_END_REASON                VARCHAR2(12),
	OFFENDER_ID                     NUMBER(10),
	MEDICAL_RECORD_SEQ              NUMBER(6),
	PARAMETER_1                     VARCHAR2(12),
	SEAL_FLAG                       VARCHAR2(1)
);

ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD CONSTRAINT OFFENDER_PROGRAM_PROFILES_PK PRIMARY KEY (OFF_PRGREF_ID);

--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (OFFENDER_BOOK_ID) REFERENCES OFFENDER_BOOKINGS;
--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (PROGRAM_ID) REFERENCES PROGRAM_SERVICES;
--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (CRS_ACTY_ID) REFERENCES COURSE_ACTIVITIES;
--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (REFERRAL_STAFF_ID) REFERENCES STAFF_MEMBERS;
--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (OFFENDER_SENT_CONDITION_ID) REFERENCES OFFENDER_SENT_CONDITIONS;
--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (OFFENDER_PRG_OBLIGATION_ID) REFERENCES OFFENDER_PRG_OBLIGATIONS;
--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (PROGRAM_OFF_PRGREF_ID) REFERENCES OFFENDER_PROGRAM_PROFILES;
--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (OFFENDER_ID, MEDICAL_RECORD_SEQ) REFERENCES OFFENDER_MEDICAL_RECORDS;

-- Inclusion of DECODE function within UNIQUE index declaration not supported in HSQLDB.
--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD UNIQUE (OFFENDER_BOOK_ID, CRS_ACTY_ID, DECODE(OFFENDER_PROGRAM_STATUS,'ALLOC','ALLOC',TO_CHAR(OFF_PRGREF_ID)));


CREATE TABLE OFFENDER_COURSE_ATTENDANCES
(
	EVENT_ID                        NUMBER(10)                          NOT NULL,
	OFFENDER_BOOK_ID                NUMBER(10)                          NOT NULL,
	EVENT_DATE                      DATE                                NOT NULL,
	START_TIME                      DATE,
	END_TIME                        DATE,
	EVENT_SUB_TYPE                  VARCHAR2(12)                        NOT NULL,
	EVENT_STATUS                    VARCHAR2(12)   DEFAULT 'SCH'        NOT NULL,
	COMMENT_TEXT                    VARCHAR2(4000),
	HIDDEN_COMMENT_TEXT             VARCHAR2(240),
	TO_INTERNAL_LOCATION_ID         NUMBER(10),
	CRS_SCH_ID                      NUMBER(10),
	OUTCOME_REASON_CODE             VARCHAR2(12),
	PIECE_WORK                      NUMBER(11,2),
	ENGAGEMENT_CODE                 VARCHAR2(12),
	UNDERSTANDING_CODE              VARCHAR2(12),
	DETAILS                         VARCHAR2(40),
	CREDITED_HOURS                  NUMBER(6,2),
	AGREED_TRAVEL_HOUR              NUMBER(6,2),
	SUPERVISOR_NAME                 VARCHAR2(30),
	BEHAVIOUR_CODE                  VARCHAR2(12),
	ACTION_CODE                     VARCHAR2(12),
	SICK_NOTE_RECEIVED_DATE         DATE,
	SICK_NOTE_EXPIRY_DATE           DATE,
	OFF_PRGREF_ID                   NUMBER(10),
	IN_TIME                         DATE,
	OUT_TIME                        DATE,
	PERFORMANCE_CODE                VARCHAR2(12),
	REFERENCE_ID                    NUMBER(20),
	TO_ADDRESS_OWNER_CLASS          VARCHAR2(12),
	TO_ADDRESS_ID                   NUMBER(10),
	EVENT_OUTCOME                   VARCHAR2(12),
	OFF_CRS_SCH_REF_ID              NUMBER(20),
	SUPERVISOR_STAFF_ID             NUMBER(10),
	CRS_APPT_ID                     NUMBER(20),
	OFFENDER_COURSE_APPT_RULE_ID    NUMBER(10),
	CRS_ACTY_ID                     NUMBER(10),
	EVENT_TYPE                      VARCHAR2(12)                        NOT NULL,
	AGY_LOC_ID                      VARCHAR2(6),
	EVENT_CLASS                     VARCHAR2(12)                        NOT NULL,
	UNEXCUSED_ABSENCE_FLAG          VARCHAR2(1),
	TO_AGY_LOC_ID                   VARCHAR2(6),
	SESSION_NO                      NUMBER(6),
	OFFENDER_PRG_OBLIGATION_ID      NUMBER(10),
	PROGRAM_ID                      NUMBER(10),
	BONUS_PAY                       NUMBER(11,3),
	TXN_ID                          NUMBER(10),
	TXN_ENTRY_SEQ                   NUMBER(6),
	PAY_FLAG                        VARCHAR2(1)    DEFAULT 'N',
	AUTHORISED_ABSENCE_FLAG         VARCHAR2(1)    DEFAULT 'N',
	SEAL_FLAG                       VARCHAR2(1),
	DIRECTION_CODE                  VARCHAR2(12),
	CREATE_DATETIME                 TIMESTAMP(9)   DEFAULT SYSTIMESTAMP NOT NULL,
	CREATE_USER_ID                  VARCHAR2(32)   DEFAULT USER      NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP(9),
	MODIFY_USER_ID                  VARCHAR2(32)
);

ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD CONSTRAINT OFFENDER_COURSE_ATTENDANCES_PK PRIMARY KEY (EVENT_ID);

--ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD FOREIGN KEY (OFFENDER_BOOK_ID) REFERENCES OFFENDER_BOOKINGS;
--ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD FOREIGN KEY (OFF_PRGREF_ID) REFERENCES OFFENDER_PROGRAM_PROFILES;
--ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD FOREIGN KEY (CRS_ACTY_ID) REFERENCES COURSE_ACTIVITIES;
--ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD FOREIGN KEY (OFFENDER_PRG_OBLIGATION_ID) REFERENCES OFFENDER_PRG_OBLIGATIONS;
--ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD FOREIGN KEY (PROGRAM_ID) REFERENCES PROGRAM_SERVICES;
--ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD FOREIGN KEY (TXN_ID, TXN_ENTRY_SEQ) REFERENCES OFFENDER_TRANSACTIONS;

ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD CHECK (EVENT_CLASS IN ('INT_MOV','COMM','EXT_MOV'));

-----------------------------------------------------------
-- Source tables for Appointments (APP) Scheduled Events --
-----------------------------------------------------------
CREATE TABLE OFFENDER_IND_SCHEDULES
(
	EVENT_ID                        NUMBER(10)                          NOT NULL,
	OFFENDER_BOOK_ID                NUMBER(10)                          NOT NULL,
	EVENT_DATE                      DATE,
	START_TIME                      DATE,
	END_TIME                        DATE,
	EVENT_CLASS                     VARCHAR2(12)                        NOT NULL,
	EVENT_TYPE                      VARCHAR2(12)                        NOT NULL,
	EVENT_SUB_TYPE                  VARCHAR2(12)                        NOT NULL,
	EVENT_STATUS                    VARCHAR2(12)                        NOT NULL,
	COMMENT_TEXT                    VARCHAR2(3600),
	HIDDEN_COMMENT_TEXT             VARCHAR2(240),
	APPLICATION_DATE                DATE,
	PARENT_EVENT_ID                 NUMBER(10),
	AGY_LOC_ID                      VARCHAR2(12),
	TO_AGY_LOC_ID                   VARCHAR2(6),
	TO_INTERNAL_LOCATION_ID         NUMBER(10),
	FROM_CITY                       VARCHAR2(20),
	TO_CITY                         VARCHAR2(20),
	CRS_SCH_ID                      NUMBER(10),
	ORDER_ID                        NUMBER(10),
	SENTENCE_SEQ                    NUMBER(10),
	OUTCOME_REASON_CODE             VARCHAR2(12),
	JUDGE_NAME                      VARCHAR2(60),
	CHECK_BOX_1                     VARCHAR2(1)    DEFAULT 'N',
	CHECK_BOX_2                     VARCHAR2(1)    DEFAULT 'N',
	IN_CHARGE_STAFF_ID              NUMBER(6),
	CREDITED_HOURS                  NUMBER(6),
	REPORT_IN_DATE                  DATE,
	PIECE_WORK                      NUMBER(11,2),
	ENGAGEMENT_CODE                 VARCHAR2(12),
	UNDERSTANDING_CODE              VARCHAR2(12),
	DETAILS                         VARCHAR2(40),
	CREDITED_WORK_HOUR              NUMBER(6,2),
	AGREED_TRAVEL_HOUR              NUMBER(6,2),
	UNPAID_WORK_SUPERVISOR          VARCHAR2(30),
	UNPAID_WORK_BEHAVIOUR           VARCHAR2(12),
	UNPAID_WORK_ACTION              VARCHAR2(12),
	SICK_NOTE_RECEIVED_DATE         DATE,
	SICK_NOTE_EXPIRY_DATE           DATE,
	COURT_EVENT_RESULT              VARCHAR2(12),
	UNEXCUSED_ABSENCE_FLAG          VARCHAR2(1),
	CREATE_USER_ID                  VARCHAR2(32)                        NOT NULL,
	MODIFY_USER_ID                  VARCHAR2(32),
	CREATE_DATETIME                 TIMESTAMP(9)   DEFAULT SYSTIMESTAMP NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP(9)   DEFAULT SYSTIMESTAMP,
	ESCORT_CODE                     VARCHAR2(12),
	CONFIRM_FLAG                    VARCHAR2(1),
	DIRECTION_CODE                  VARCHAR2(12),
	TO_CITY_CODE                    VARCHAR2(12),
	FROM_CITY_CODE                  VARCHAR2(12),
	OFF_PRGREF_ID                   NUMBER(10),
	IN_TIME                         DATE,
	OUT_TIME                        DATE,
	PERFORMANCE_CODE                VARCHAR2(12),
	TEMP_ABS_SCH_ID                 NUMBER(10),
	REFERENCE_ID                    NUMBER(10),
	TRANSPORT_CODE                  VARCHAR2(12),
	APPLICATION_TIME                DATE,
	CONTACT_PERSON_NAME             VARCHAR2(40),
	TO_ADDRESS_OWNER_CLASS          VARCHAR2(12),
	TO_ADDRESS_ID                   NUMBER(10),
	RETURN_DATE                     DATE,
	RETURN_TIME                     DATE,
	TO_CORPORATE_ID                 NUMBER(10),
	TA_ID                           NUMBER(10),
	EVENT_OUTCOME                   VARCHAR2(12),
	OFFENDER_PRG_OBLIGATION_ID      NUMBER(10),
	PROV_STATE_CODE                 VARCHAR2(12),
	SEAL_FLAG                       VARCHAR2(1),
	SCHEDULED_TRIP_ID               NUMBER(10)
);

ALTER TABLE OFFENDER_IND_SCHEDULES ADD CONSTRAINT OFFENDER_IND_SCHEDULES_PK PRIMARY KEY (EVENT_ID);

--ALTER TABLE OFFENDER_IND_SCHEDULES ADD CHECK (EVENT_CLASS IN ('EXT_MOV','INT_MOV','COMM'));




-- CREATE OR REPLACE VIEW V_OIISCHED_ALL_SCHEDULES
-- (OFFENDER_ID, OFFENDER_ID_DISPLAY, OFFENDER_LAST_NAME, OFFENDER_FIRST_NAME, OFFENDER_BOOK_ID,
--  EVENT_CLASS, EVENT_STATUS, EVENT_TYPE, EVENT_SUB_TYPE, EVENT_DATE,
--  START_TIME, EVENT_TYPE_DESC, EVENT_SUB_TYPE_DESC, TO_INTERNAL_LOCATION_DESC, AGY_LOC_ID,
--  TO_AGY_LOC_DESC, TO_LOC_DESC, TO_AGY_LOC_ID, TO_ADDRESS_ID, TO_CITY_CODE,
--  SOURCE)
-- AS
-- SELECT
--        off.offender_id,
--        off.offenderIdDisplay,
--        off.last_name offender_last_name,
--        off.first_name offender_first_name,
--        sch.offender_book_id,
--        sch.event_class,
--        sch.event_status,
--        sch.event_type,
--        sch.event_sub_type,
--        sch.event_date,
--        sch.start_time,
--        rd1.description event_type_desc,
--        rd2.description event_sub_type_desc,
--        SUBSTR(COALESCE(ail.description,
--                        agy.description,
--                        tag_reports.get_full_address_details (sch.to_address_id),
--                        tag_reference_codes.getdesccode ('CITY',sch.to_city_code)),1,40) to_internal_location_desc,
--        bkg.agy_loc_id agy_loc_id,
--        agy.description to_agy_loc_desc,
--        agy.description to_loc_desc,
-- 	    sch.to_agy_loc_id,
--        sch.to_address_id,
--        sch.to_city_code,
--        sch.source
--   FROM (SELECT ind.offender_book_id,
-- 	            ind.agy_loc_id,                     <-- not used
-- 	            ind.event_class,
-- 	            ind.event_status,
-- 	            ind.event_type,
-- 	            ind.event_sub_type,
-- 	            ind.event_date,
-- 	            ind.start_time,
-- 	            ind.to_address_id,
-- 	            ind.to_city_code,
-- 	            ind.to_agy_loc_id,
--                ind.to_internal_location_id,      <-- not used
--                'APP' source
--              FROM offender_ind_schedules ind
--             WHERE event_status = 'SCH'
--             UNION ALL
--            SELECT ce.offender_book_id,
-- 	               NULL,                            <-- not used
--                   'EXT_MOV',
--                   'SCH',
-- 	               'CRT',
-- 	               ce.court_event_type,
--                   ce.event_date,
-- 	               ce.start_time,
-- 	               NULL,
-- 	               NULL,
--                   ce.agy_loc_id,
--                   NULL,                          <-- not used
--                   'CRT'
--              FROM court_events ce
--             WHERE NVL(ce.event_status,'SCH') = 'SCH'
--             UNION ALL
--            SELECT ord.offender_book_id,
-- 	               NULL,                            <-- not used
-- 	               'EXT_MOV',
-- 	               'SCH',
-- 	               ord.movementType,
-- 	               ord.movementReasonCode,
-- 	               ord.release_date,
-- 	               NULL,
-- 	               NULL,
-- 	               NULL,
-- 	               NULL,
--                   NULL,
--                   'REL'
--              FROM offender_release_details ord
--             WHERE ord.event_status = 'SCH'
--             UNION ALL
--            SELECT opp.offender_book_id,
--                   opp.agy_loc_id,                <-- not used
-- 	               'INT_MOV',
-- 	               'SCH',
--                   'PRISON_ACT',
-- 	               ca.course_activity_type,
-- 	               cs.schedule_date,
-- 	               cs.start_time,
-- 	               ca.services_address_id,
-- 	               NULL,
--                   ca.agy_loc_id,
--                   ca.internal_location_id,       <-- not used
--                   'PA'
--              FROM offender_program_profiles opp
--              JOIN course_activities ca
--                ON ca.crs_acty_id = opp.crs_acty_id
--              JOIN course_schedules cs
--                ON opp.crs_acty_id = cs.crs_acty_id
--                   AND opp.offender_start_date <= cs.schedule_date
--                   AND COALESCE(opp.offender_end_date,
--                                ca.schedule_end_date,
--                                cs.schedule_date) >= cs.schedule_date
--             WHERE opp.offender_program_status = 'ALLOC'
--               AND NVL(opp.suspended_flag,'N') = 'N'
--               AND ca.activeFlag = 'Y'
--               AND ca.course_activity_type IS NOT NULL
--               AND cs.catch_up_crs_sch_id IS NULL
--               AND NOT EXISTS ( SELECT 'x'
--                                  FROM offender_course_attendances oca
--                                 WHERE oca.offender_book_id = opp.offender_book_id
--                                   AND oca.event_date = cs.schedule_date
--                                   AND oca.crs_sch_id = cs.crs_sch_id)
--               AND (TO_CHAR(cs.schedule_date, 'DY'), cs.slot_category_code) NOT IN
-- 	               (SELECT oe.exclude_day, NVL(oe.slot_category_code, cs.slot_category_code)
--                      FROM offender_exclude_acts_schds oe
--                     WHERE oe.off_prgref_id = opp.off_prgref_id)
--             UNION ALL
--            SELECT oca.offender_book_id,
--                   oca.agy_loc_id,                <-- not used
-- 	               'INT_MOV',
-- 	               'SCH',
-- 	               oca.event_type,
-- 	               oca.event_sub_type,
-- 	               oca.event_date,
-- 	               oca.start_time,
-- 	               oca.to_address_id,
-- 	               NULL,
-- 	               oca.to_agy_loc_id,
--                   oca.to_internal_location_id,   <-- not used
--                   'PA'
--              FROM offender_course_attendances oca
--             WHERE oca.event_status = 'SCH'
--             UNION ALL
--            SELECT ov.offender_book_id,
-- 	               ov.agy_loc_id,                   <-- not used
-- 	               'INT_MOV',
-- 	               'SCH',
-- 	               'VISIT',
-- 	               'VISIT',
-- 	               ov.visit_date,
-- 	               ov.end_time,
-- 	               null,
-- 	               null,
-- 	               ov.agy_loc_id,
--                   visit_internal_location_id,    <-- not used
--                   'VIS'
--              FROM offender_visits ov
--             WHERE visit_status = 'SCH'
--             UNION ALL
--            SELECT aip.offender_book_id,
--                   ai.agy_loc_id,                 <-- not used
--                   'INT_MOV',
--                   'SCH',
--                   'OIC',
--                   'OIC',
--                   oh.hearing_date,
--                   null,
--                   null,
--                   null,
--                   ai.agy_loc_id,
--                   oh.internal_location_id,       <-- not used
--                   'OIC'
--              FROM agency_incident_parties aip
--              JOIN oic_hearings oh
--                ON oh.oic_incident_id = aip.oic_incident_id
--              JOIN agency_incidents ai
--                ON ai.agency_incident_id = aip.agency_incident_id
--             WHERE oh.hearing_date IS NOT NULL
--               AND aip.offender_book_id IS NOT NULL
--               AND oh.event_status = 'SCH') sch
--   JOIN offender_bookings bkg
--     ON sch.offender_book_id = bkg.offender_book_id
--        AND bkg.activeFlag  = 'Y'
--   JOIN offenders off
--     ON bkg.offender_id = off.offender_id
--   LEFT JOIN agency_internal_locations   ail
--     ON sch.to_internal_location_id = ail.internal_location_id
--   LEFT JOIN agency_locations agy
--     ON sch.to_agy_loc_id = agy.agy_loc_id
--   LEFT JOIN reference_codes rd1
--     ON rd1.code = sch.event_type
--        AND rd1.domain = DECODE (sch.event_class, 'EXT_MOV', 'MOVE_TYPE', 'INT_SCH_TYPE')
--   LEFT JOIN reference_codes rd2
--     ON rd2.code = sch.event_sub_type
--        AND rd2.domain = DECODE (sch.event_class, 'EXT_MOV', 'MOVE_RSN', 'INT_SCH_RSN');
