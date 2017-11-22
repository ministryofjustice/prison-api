-----------------------------------------------------------------------
-- Source tables for Prison Activities (PRISON_ACT) Scheduled Events --
-----------------------------------------------------------------------
CREATE TABLE ADDRESSES
(
	ADDRESS_ID                      BIGSERIAL   PRIMARY KEY       NOT NULL,
	OWNER_CLASS                     VARCHAR(12)                   NOT NULL,
	OWNER_ID                        BIGINT,
	OWNER_SEQ                       INTEGER,
	OWNER_CODE                      VARCHAR(12),
	ADDRESS_TYPE                    VARCHAR(12),
	FLAT                            VARCHAR(30),
	PREMISE                         VARCHAR(50),
	STREET                          VARCHAR(160),
	LOCALITY                        VARCHAR(70),
	CITY_CODE                       VARCHAR(12),
	COUNTY_CODE                     VARCHAR(12),
	POSTAL_CODE                     VARCHAR(12),
	COUNTRY_CODE                    VARCHAR(12),
	VALIDATED_PAF_FLAG              VARCHAR(1)  DEFAULT 'N',
	PRIMARY_FLAG                    VARCHAR(1)                    NOT NULL,
	MAIL_FLAG                       VARCHAR(1)                    NOT NULL,
	CAPACITY                        INTEGER,
	COMMENT_TEXT                    VARCHAR(240),
	CREATE_DATETIME                 TIMESTAMP   DEFAULT now()     NOT NULL,
	CREATE_USER_ID                  VARCHAR(32) DEFAULT USER      NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP,
	MODIFY_USER_ID                  VARCHAR(32),
	NO_FIXED_ADDRESS_FLAG           VARCHAR(1)  DEFAULT 'N',
	SERVICES_FLAG                   VARCHAR(1)  DEFAULT 'N',
	SPECIAL_NEEDS_CODE              VARCHAR(12),
	CONTACT_PERSON_NAME             VARCHAR(40),
	BUSINESS_HOUR                   VARCHAR(60),
	START_DATE                      DATE,
	END_DATE                        DATE,
	CITY_NAME                       VARCHAR(40),
	AUDIT_TIMESTAMP                 TIMESTAMP,
	AUDIT_USER_ID                   VARCHAR(32),
	AUDIT_MODULE_NAME               VARCHAR(65),
	AUDIT_CLIENT_USER_ID            VARCHAR(64),
	AUDIT_CLIENT_IP_ADDRESS         VARCHAR(39),
	AUDIT_CLIENT_WORKSTATION_NAME   VARCHAR(64),
	AUDIT_ADDITIONAL_INFO           VARCHAR(256)
);


CREATE TABLE COURSE_ACTIVITIES
(
	CRS_ACTY_ID                     BIGSERIAL   PRIMARY KEY       NOT NULL,
	CASELOAD_ID                     VARCHAR(6),
	AGY_LOC_ID                      VARCHAR(6),
	DESCRIPTION                     VARCHAR(40),
	CAPACITY                        SMALLINT    DEFAULT 99,
	ACTIVE_FLAG                     VARCHAR(1)  DEFAULT 'Y'       NOT NULL,
	EXPIRY_DATE                     DATE,
	SCHEDULE_START_DATE             DATE,
	SCHEDULE_END_DATE               DATE,
	CASELOAD_TYPE                   VARCHAR(12),
	SERVICES_ADDRESS_ID             BIGINT,
	PROGRAM_ID                      BIGINT                        NOT NULL,
	PARENT_CRS_ACTY_ID              BIGINT,
	INTERNAL_LOCATION_ID            BIGINT,
	PROVIDER_PARTY_CLASS            VARCHAR(12),
	PROVIDER_PARTY_ID               BIGINT,
	PROVIDER_PARTY_CODE             VARCHAR(6),
	BENEFICIARY_NAME                VARCHAR(80),
	BENEFICIARY_CONTACT             VARCHAR(80),
	LIST_SEQ                        INTEGER,
	PLACEMENT_CORPORATE_ID          BIGINT,
	COMMENT_TEXT                    VARCHAR(240),
	AGENCY_LOCATION_TYPE            VARCHAR(12),
	PROVIDER_TYPE                   VARCHAR(12),
	BENEFICIARY_TYPE                VARCHAR(12),
	PLACEMENT_TEXT                  VARCHAR(240),
	CODE                            VARCHAR(20),
	HOLIDAY_FLAG                    VARCHAR(1)  DEFAULT 'N',
	COURSE_CLASS                    VARCHAR(12) DEFAULT 'COURSE'  NOT NULL,
	COURSE_ACTIVITY_TYPE            VARCHAR(12),
	CREATE_DATETIME                 TIMESTAMP   DEFAULT now()     NOT NULL,
	CREATE_USER_ID                  VARCHAR(32) DEFAULT USER      NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP,
	MODIFY_USER_ID                  VARCHAR(32),
	IEP_LEVEL                       VARCHAR(12),
	AUDIT_TIMESTAMP                 TIMESTAMP,
	AUDIT_USER_ID                   VARCHAR(32),
	AUDIT_MODULE_NAME               VARCHAR(65),
	AUDIT_CLIENT_USER_ID            VARCHAR(64),
	AUDIT_CLIENT_IP_ADDRESS         VARCHAR(39),
	AUDIT_CLIENT_WORKSTATION_NAME   VARCHAR(64),
	AUDIT_ADDITIONAL_INFO           VARCHAR(256),
	NO_OF_SESSIONS                  INTEGER,
	SESSION_LENGTH                  INTEGER,
	MULTI_PHASE_SCHEDULING_FLAG     VARCHAR(12),
	SCHEDULE_NOTES                  VARCHAR(240),
	OUTSIDE_WORK_FLAG               VARCHAR(1)  DEFAULT 'N',
	PAY_PER_SESSION                 VARCHAR(1)  DEFAULT 'H',
	PIECE_WORK_FLAG                 VARCHAR(1)  DEFAULT 'N'
);

--ALTER TABLE COURSE_ACTIVITIES ADD FOREIGN KEY (AGY_LOC_ID) REFERENCES AGENCY_LOCATIONS;
ALTER TABLE COURSE_ACTIVITIES ADD FOREIGN KEY (SERVICES_ADDRESS_ID) REFERENCES ADDRESSES;
--ALTER TABLE COURSE_ACTIVITIES ADD FOREIGN KEY (PROGRAM_ID) REFERENCES PROGRAM_SERVICES;
ALTER TABLE COURSE_ACTIVITIES ADD FOREIGN KEY (PARENT_CRS_ACTY_ID) REFERENCES COURSE_ACTIVITIES;
--ALTER TABLE COURSE_ACTIVITIES ADD FOREIGN KEY (PLACEMENT_CORPORATE_ID) REFERENCES CORPORATES;
ALTER TABLE COURSE_ACTIVITIES ADD CHECK (COURSE_CLASS IN ('COURSE','CRS_MOD','CRS_PH'));


CREATE TABLE COURSE_SCHEDULES
(
	CRS_SCH_ID                      BIGSERIAL   PRIMARY KEY       NOT NULL,
  CRS_ACTY_ID                     BIGINT                        NOT NULL,
	WEEKDAY                         VARCHAR(12),
	SCHEDULE_DATE                   DATE                          NOT NULL,
	START_TIME                      TIMESTAMP                     NOT NULL,
	END_TIME                        TIMESTAMP,
	SESSION_NO                      INTEGER,
	DETAILS                         VARCHAR(40),
	CREATE_DATETIME                 TIMESTAMP   DEFAULT now()     NOT NULL,
	CREATE_USER_ID                  VARCHAR(32) DEFAULT USER      NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP,
	MODIFY_USER_ID                  VARCHAR(32),
	AUDIT_TIMESTAMP                 TIMESTAMP,
	AUDIT_USER_ID                   VARCHAR(32),
	AUDIT_MODULE_NAME               VARCHAR(65),
	AUDIT_CLIENT_USER_ID            VARCHAR(64),
	AUDIT_CLIENT_IP_ADDRESS         VARCHAR(39),
	AUDIT_CLIENT_WORKSTATION_NAME   VARCHAR(64),
	AUDIT_ADDITIONAL_INFO           VARCHAR(256),
	SCHEDULE_STATUS                 VARCHAR(12) DEFAULT 'SCH',
	CATCH_UP_CRS_SCH_ID             BIGINT,
	VIDEO_REFERENCE_ID              VARCHAR(20),
	SLOT_CATEGORY_CODE              VARCHAR(12)
);

ALTER TABLE COURSE_SCHEDULES ADD FOREIGN KEY (CRS_ACTY_ID) REFERENCES COURSE_ACTIVITIES;
ALTER TABLE COURSE_SCHEDULES ADD FOREIGN KEY (CATCH_UP_CRS_SCH_ID) REFERENCES COURSE_SCHEDULES;


CREATE TABLE OFFENDER_PROGRAM_PROFILES
(
	OFF_PRGREF_ID                   BIGSERIAL   PRIMARY KEY     NOT NULL,
	OFFENDER_BOOK_ID                BIGINT                      NOT NULL,
	PROGRAM_ID                      BIGINT                      NOT NULL,
	OFFENDER_START_DATE             DATE,
	OFFENDER_PROGRAM_STATUS         VARCHAR(12) DEFAULT 'PLAN'  NOT NULL,
	CRS_ACTY_ID                     BIGINT,
	REFERRAL_PRIORITY               VARCHAR(12),
	REFERRAL_DATE                   DATE,
	REFERRAL_COMMENT_TEXT           VARCHAR(1000),
	OFFENDER_END_REASON             VARCHAR(12),
	AGREED_TRAVEL_FARE              DECIMAL(11,2),
	AGREED_TRAVEL_HOUR              DECIMAL(6,2),
	OFFENDER_END_COMMENT_TEXT       VARCHAR(240),
	REJECT_DATE                     DATE,
	WAITLIST_DECISION_CODE          VARCHAR(12),
	REFERRAL_STAFF_ID               BIGINT,
	OFFENDER_END_DATE               DATE,
	CREDIT_WORK_HOURS               DECIMAL(8,2),
	CREDIT_OTHER_HOURS              DECIMAL(8,2),
	SUSPENDED_FLAG                  VARCHAR(1)  DEFAULT 'N',
	REJECT_REASON_CODE              VARCHAR(12),
	AGY_LOC_ID                      VARCHAR(6),
	CREATE_DATETIME                 TIMESTAMP   DEFAULT now()   NOT NULL,
	CREATE_USER_ID                  VARCHAR(32) DEFAULT USER    NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP,
	MODIFY_USER_ID                  VARCHAR(32),
	REVIEWED_BY                     VARCHAR(32),
	AUDIT_TIMESTAMP                 TIMESTAMP,
	AUDIT_USER_ID                   VARCHAR(32),
	AUDIT_MODULE_NAME               VARCHAR(65),
	AUDIT_CLIENT_USER_ID            VARCHAR(64),
	AUDIT_CLIENT_IP_ADDRESS         VARCHAR(39),
	AUDIT_CLIENT_WORKSTATION_NAME   VARCHAR(64),
	AUDIT_ADDITIONAL_INFO           VARCHAR(256),
	OFFENDER_SENT_CONDITION_ID      BIGINT,
	SENTENCE_SEQ                    INTEGER,
	HOLIDAY_FLAG                    VARCHAR(1)  DEFAULT 'N',
	START_SESSION_NO                INTEGER,
	PARENT_OFF_PRGREF_ID            BIGINT,
	OFFENDER_PRG_OBLIGATION_ID      BIGINT,
	PROGRAM_OFF_PRGREF_ID           BIGINT,
	PROFILE_CLASS                   VARCHAR(12) DEFAULT 'PRG',
	COMPLETION_DATE                 DATE,
	NEEDED_FLAG                     VARCHAR(1)  DEFAULT 'N',
	COMMENT_TEXT                    VARCHAR(240),
	EARLY_END_REASON                VARCHAR(12)
);

--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (OFFENDER_BOOK_ID) REFERENCES OFFENDER_BOOKINGS;
--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (PROGRAM_ID) REFERENCES PROGRAM_SERVICES;
ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (CRS_ACTY_ID) REFERENCES COURSE_ACTIVITIES;
--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (REFERRAL_STAFF_ID) REFERENCES STAFF_MEMBERS;
--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (OFFENDER_PRG_OBLIGATION_ID) REFERENCES OFFENDER_PRG_OBLIGATIONS;
--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD FOREIGN KEY (PROGRAM_OFF_PRGREF_ID) REFERENCES OFFENDER_PROGRAM_PROFILES;

-- Inclusion of DECODE function within UNIQUE index declaration not supported in HSQLDB.
--ALTER TABLE OFFENDER_PROGRAM_PROFILES ADD UNIQUE (OFFENDER_BOOK_ID, CRS_ACTY_ID, DECODE(OFFENDER_PROGRAM_STATUS,'ALLOC','ALLOC',TO_CHAR(OFF_PRGREF_ID)));


CREATE TABLE OFFENDER_COURSE_ATTENDANCES
(
	EVENT_ID                        BIGSERIAL   PRIMARY KEY       NOT NULL,
	OFFENDER_BOOK_ID                BIGINT                        NOT NULL,
	EVENT_DATE                      DATE                          NOT NULL,
	START_TIME                      DATE,
	END_TIME                        DATE,
	EVENT_SUB_TYPE                  VARCHAR(12)                   NOT NULL,
	EVENT_STATUS                    VARCHAR(12) DEFAULT 'SCH'     NOT NULL,
	COMMENT_TEXT                    VARCHAR(4000),
	HIDDEN_COMMENT_TEXT             VARCHAR(240),
	TO_INTERNAL_LOCATION_ID         BIGINT,
	CRS_SCH_ID                      BIGINT,
	OUTCOME_REASON_CODE             VARCHAR(12),
	PIECE_WORK                      DECIMAL(11,2),
	ENGAGEMENT_CODE                 VARCHAR(12),
	UNDERSTANDING_CODE              VARCHAR(12),
	DETAILS                         VARCHAR(40),
	CREDITED_HOURS                  DECIMAL(6,2),
	AGREED_TRAVEL_HOUR              DECIMAL(6,2),
	SUPERVISOR_NAME                 VARCHAR(30),
	BEHAVIOUR_CODE                  VARCHAR(12),
	ACTION_CODE                     VARCHAR(12),
	SICK_NOTE_RECEIVED_DATE         DATE,
	SICK_NOTE_EXPIRY_DATE           DATE,
	OFF_PRGREF_ID                   BIGINT,
	IN_TIME                         DATE,
	OUT_TIME                        DATE,
	PERFORMANCE_CODE                VARCHAR(12),
	REFERENCE_ID                    BIGINT,
	TO_ADDRESS_OWNER_CLASS          VARCHAR(12),
	TO_ADDRESS_ID                   BIGINT,
	EVENT_OUTCOME                   VARCHAR(12),
	OFF_CRS_SCH_REF_ID              BIGINT,
	SUPERVISOR_STAFF_ID             BIGINT,
	CRS_APPT_ID                     BIGINT,
	OFFENDER_COURSE_APPT_RULE_ID    BIGINT,
	CRS_ACTY_ID                     BIGINT,
	EVENT_TYPE                      VARCHAR(12)                   NOT NULL,
	CREATE_DATETIME                 TIMESTAMP   DEFAULT now()     NOT NULL,
	CREATE_USER_ID                  VARCHAR(32) DEFAULT USER      NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP,
	MODIFY_USER_ID                  VARCHAR(32),
	AGY_LOC_ID                      VARCHAR(6),
	EVENT_CLASS                     VARCHAR(12)                   NOT NULL,
	AUDIT_TIMESTAMP                 TIMESTAMP,
	AUDIT_USER_ID                   VARCHAR(32),
	AUDIT_MODULE_NAME               VARCHAR(65),
	AUDIT_CLIENT_USER_ID            VARCHAR(64),
	AUDIT_CLIENT_IP_ADDRESS         VARCHAR(39),
	AUDIT_CLIENT_WORKSTATION_NAME   VARCHAR(64),
	AUDIT_ADDITIONAL_INFO           VARCHAR(256),
	UNEXCUSED_ABSENCE_FLAG          VARCHAR(1)  DEFAULT 'N',
	TO_AGY_LOC_ID                   VARCHAR(6),
	SESSION_NO                      INTEGER,
	OFFENDER_PRG_OBLIGATION_ID      BIGINT,
	PROGRAM_ID                      BIGINT,
	BONUS_PAY                       DECIMAL(11,3),
	TXN_ID                          BIGINT,
	TXN_ENTRY_SEQ                   INTEGER,
	PAY_FLAG                        VARCHAR(1)  DEFAULT 'N',
	AUTHORISED_ABSENCE_FLAG         VARCHAR(1)  DEFAULT 'N'
);

--ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD FOREIGN KEY (OFFENDER_BOOK_ID) REFERENCES OFFENDER_BOOKINGS;
--ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD FOREIGN KEY (OFF_PRGREF_ID) REFERENCES OFFENDER_PROGRAM_PROFILES;
ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD FOREIGN KEY (CRS_ACTY_ID) REFERENCES COURSE_ACTIVITIES;
--ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD FOREIGN KEY (OFFENDER_PRG_OBLIGATION_ID) REFERENCES OFFENDER_PRG_OBLIGATIONS;
--ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD FOREIGN KEY (PROGRAM_ID) REFERENCES PROGRAM_SERVICES;
--ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD FOREIGN KEY (TXN_ID, TXN_ENTRY_SEQ) REFERENCES OFFENDER_TRANSACTIONS;

ALTER TABLE OFFENDER_COURSE_ATTENDANCES ADD CHECK (EVENT_CLASS IN ('INT_MOV','COMM','EXT_MOV'));


CREATE TABLE OFFENDER_EXCLUDE_ACTS_SCHDS
(
	OFFENDER_EXCLUDE_ACT_SCHD_ID    BIGSERIAL   PRIMARY KEY       NOT NULL,
	OFFENDER_BOOK_ID                BIGINT                        NOT NULL,
	OFF_PRGREF_ID                   BIGINT                        NOT NULL,
	SLOT_CATEGORY_CODE              VARCHAR(12),
	EXCLUDE_DAY                     VARCHAR(12)                   NOT NULL,
	CRS_ACTY_ID                     BIGINT                        NOT NULL,
	CREATE_DATETIME                 TIMESTAMP   DEFAULT now()     NOT NULL,
	CREATE_USER_ID                  VARCHAR(32) DEFAULT USER      NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP,
	MODIFY_USER_ID                  VARCHAR(32),
	AUDIT_TIMESTAMP                 TIMESTAMP,
	AUDIT_USER_ID                   VARCHAR(32),
	AUDIT_MODULE_NAME               VARCHAR(65),
	AUDIT_CLIENT_USER_ID            VARCHAR(64),
	AUDIT_CLIENT_IP_ADDRESS         VARCHAR(39),
	AUDIT_CLIENT_WORKSTATION_NAME   VARCHAR(64),
	AUDIT_ADDITIONAL_INFO           VARCHAR(256)
);

--ALTER TABLE OFFENDER_EXCLUDE_ACTS_SCHDS ADD FOREIGN KEY (OFFENDER_BOOK_ID) REFERENCES OFFENDER_BOOKINGS;
ALTER TABLE OFFENDER_EXCLUDE_ACTS_SCHDS ADD FOREIGN KEY (OFF_PRGREF_ID) REFERENCES OFFENDER_PROGRAM_PROFILES;
ALTER TABLE OFFENDER_EXCLUDE_ACTS_SCHDS ADD FOREIGN KEY (CRS_ACTY_ID) REFERENCES COURSE_ACTIVITIES;

ALTER TABLE OFFENDER_EXCLUDE_ACTS_SCHDS ADD UNIQUE (OFF_PRGREF_ID, EXCLUDE_DAY, SLOT_CATEGORY_CODE);


-------------------------------------------------------
-- Source tables for Visits (VISIT) Scheduled Events --
-------------------------------------------------------
CREATE TABLE OFFENDER_VISITS
(
	OFFENDER_VISIT_ID               BIGSERIAL   PRIMARY KEY       NOT NULL,
	OFFENDER_BOOK_ID                BIGINT                        NOT NULL,
	COMMENT_TEXT                    VARCHAR(240),
	OVERRIDE_BAN_STAFF_ID           BIGINT,
	SEARCH_TYPE                     VARCHAR(12),
	RAISED_INCIDENT_TYPE            VARCHAR(12),
	RAISED_INCIDENT_NUMBER          BIGINT,
	VISITOR_CONCERN_TEXT            VARCHAR(240),
	VISIT_DATE                      DATE                          NOT NULL,
	START_TIME                      TIMESTAMP                     NOT NULL,
	END_TIME                        TIMESTAMP                     NOT NULL,
	EVENT_OUTCOME                   VARCHAR(12),
	VISIT_TYPE                      VARCHAR(12)                   NOT NULL,
	VISIT_STATUS                    VARCHAR(12)                   NOT NULL,
	OUTCOME_REASON_CODE             VARCHAR(12),
	VISIT_INTERNAL_LOCATION_ID      BIGINT,
	AGENCY_VISIT_SLOT_ID            BIGINT,
	AGY_LOC_ID                      VARCHAR(6)                    NOT NULL,
	CREATE_DATETIME                 TIMESTAMP   DEFAULT now()     NOT NULL,
	CREATE_USER_ID                  VARCHAR(32) DEFAULT USER      NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP,
	MODIFY_USER_ID                  VARCHAR(32),
	RECORD_USER_ID                  VARCHAR(30) DEFAULT USER      NOT NULL,
	AUDIT_TIMESTAMP                 TIMESTAMP,
	AUDIT_USER_ID                   VARCHAR(32),
	AUDIT_MODULE_NAME               VARCHAR(65),
	AUDIT_CLIENT_USER_ID            VARCHAR(64),
	AUDIT_CLIENT_IP_ADDRESS         VARCHAR(39),
	AUDIT_CLIENT_WORKSTATION_NAME   VARCHAR(64),
	AUDIT_ADDITIONAL_INFO           VARCHAR(256),
	OFFENDER_VISIT_ORDER_ID         BIGINT,
	CLIENT_UNIQUE_REF               VARCHAR(100)
);

--ALTER TABLE OFFENDER_VISITS ADD FOREIGN KEY (OFFENDER_BOOK_ID) REFERENCES OFFENDER_BOOKINGS;
ALTER TABLE OFFENDER_VISITS ADD UNIQUE (CLIENT_UNIQUE_REF);


-----------------------------------------------------------
-- Source tables for Appointments (APP) Scheduled Events --
-----------------------------------------------------------
CREATE TABLE OFFENDER_IND_SCHEDULES
(
	EVENT_ID                        BIGSERIAL   PRIMARY KEY   NOT NULL,
	OFFENDER_BOOK_ID                BIGINT                    NOT NULL,
	EVENT_DATE                      DATE,
	START_TIME                      TIMESTAMP,
	END_TIME                        TIMESTAMP,
	EVENT_CLASS                     VARCHAR(12)               NOT NULL,
	EVENT_TYPE                      VARCHAR(12)               NOT NULL,
	EVENT_SUB_TYPE                  VARCHAR(12)               NOT NULL,
	EVENT_STATUS                    VARCHAR(12)               NOT NULL,
	COMMENT_TEXT                    VARCHAR(4000),
	HIDDEN_COMMENT_TEXT             VARCHAR(240),
	APPLICATION_DATE                DATE,
	PARENT_EVENT_ID                 BIGINT,
	AGY_LOC_ID                      VARCHAR(6),
	TO_AGY_LOC_ID                   VARCHAR(6),
	TO_INTERNAL_LOCATION_ID         BIGINT,
	FROM_CITY                       VARCHAR(20),
	TO_CITY                         VARCHAR(20),
	CRS_SCH_ID                      BIGINT,
	ORDER_ID                        BIGINT,
	SENTENCE_SEQ                    BIGINT,
	OUTCOME_REASON_CODE             VARCHAR(12),
	JUDGE_NAME                      VARCHAR(60),
	CHECK_BOX_1                     VARCHAR(1)  DEFAULT 'N',
	CHECK_BOX_2                     VARCHAR(1)  DEFAULT 'N',
	IN_CHARGE_STAFF_ID              BIGINT,
	CREDITED_HOURS                  INTEGER,
	REPORT_IN_DATE                  DATE,
	PIECE_WORK                      DECIMAL(11,2),
	ENGAGEMENT_CODE                 VARCHAR(12),
	UNDERSTANDING_CODE              VARCHAR(12),
	DETAILS                         VARCHAR(40),
	CREDITED_WORK_HOUR              DECIMAL(6,2),
	AGREED_TRAVEL_HOUR              DECIMAL(6,2),
	UNPAID_WORK_SUPERVISOR          VARCHAR(30),
	UNPAID_WORK_BEHAVIOUR           VARCHAR(12),
	UNPAID_WORK_ACTION              VARCHAR(12),
	SICK_NOTE_RECEIVED_DATE         DATE,
	SICK_NOTE_EXPIRY_DATE           DATE,
	COURT_EVENT_RESULT              VARCHAR(12),
	UNEXCUSED_ABSENCE_FLAG          VARCHAR(1)  DEFAULT 'N',
	CREATE_USER_ID                  VARCHAR(32) DEFAULT USER  NOT NULL,
	MODIFY_USER_ID                  VARCHAR(32),
	CREATE_DATETIME                 TIMESTAMP   DEFAULT now() NOT NULL,
	MODIFY_DATETIME                 TIMESTAMP,
	ESCORT_CODE                     VARCHAR(12),
	CONFIRM_FLAG                    VARCHAR(1)  DEFAULT 'N',
	DIRECTION_CODE                  VARCHAR(12),
	TO_CITY_CODE                    VARCHAR(12),
	FROM_CITY_CODE                  VARCHAR(12),
	OFF_PRGREF_ID                   BIGINT,
	IN_TIME                         DATE,
	OUT_TIME                        DATE,
	PERFORMANCE_CODE                VARCHAR(12),
	TEMP_ABS_SCH_ID                 BIGINT,
	REFERENCE_ID                    BIGINT,
	TRANSPORT_CODE                  VARCHAR(12),
	APPLICATION_TIME                DATE,
	TO_COUNTRY_CODE                 VARCHAR(12),
	OJ_LOCATION_CODE                VARCHAR(12),
	CONTACT_PERSON_NAME             VARCHAR(40),
	TO_ADDRESS_OWNER_CLASS          VARCHAR(12),
	TO_ADDRESS_ID                   BIGINT,
	RETURN_DATE                     DATE,
	RETURN_TIME                     DATE,
	TO_CORPORATE_ID                 BIGINT,
	TA_ID                           BIGINT,
	EVENT_OUTCOME                   VARCHAR(12),
	AUDIT_TIMESTAMP                 TIMESTAMP,
	AUDIT_USER_ID                   VARCHAR(32),
	AUDIT_MODULE_NAME               VARCHAR(65),
	AUDIT_CLIENT_USER_ID            VARCHAR(64),
	AUDIT_CLIENT_IP_ADDRESS         VARCHAR(39),
	AUDIT_CLIENT_WORKSTATION_NAME   VARCHAR(64),
	AUDIT_ADDITIONAL_INFO           VARCHAR(256),
	OFFENDER_PRG_OBLIGATION_ID      BIGINT,
	OFFENDER_MOVEMENT_APP_ID        BIGINT
);

--ALTER TABLE OFFENDER_IND_SCHEDULES ADD FOREIGN KEY (OFFENDER_BOOK_ID) REFERENCES OFFENDER_BOOKINGS;
--ALTER TABLE OFFENDER_IND_SCHEDULES ADD FOREIGN KEY (AGY_LOC_ID) REFERENCES AGENCY_LOCATIONS;
--ALTER TABLE OFFENDER_IND_SCHEDULES ADD FOREIGN KEY (TO_AGY_LOC_ID) REFERENCES AGENCY_LOCATIONS;
--ALTER TABLE OFFENDER_IND_SCHEDULES ADD FOREIGN KEY (IN_CHARGE_STAFF_ID) REFERENCES STAFF_MEMBERS;
--ALTER TABLE OFFENDER_IND_SCHEDULES ADD FOREIGN KEY (OFFENDER_PRG_OBLIGATION_ID) REFERENCES OFFENDER_PRG_OBLIGATIONS;
--ALTER TABLE OFFENDER_IND_SCHEDULES ADD FOREIGN KEY (OFFENDER_MOVEMENT_APP_ID) REFERENCES OFFENDER_MOVEMENT_APPS;
ALTER TABLE OFFENDER_IND_SCHEDULES ADD CHECK (EVENT_CLASS IN ('EXT_MOV','INT_MOV','COMM'));




-- CREATE OR REPLACE VIEW V_OIISCHED_ALL_SCHEDULES
-- (OFFENDER_ID, OFFENDER_ID_DISPLAY, OFFENDER_LAST_NAME, OFFENDER_FIRST_NAME, OFFENDER_BOOK_ID,
--  EVENT_CLASS, EVENT_STATUS, EVENT_TYPE, EVENT_SUB_TYPE, EVENT_DATE,
--  START_TIME, EVENT_TYPE_DESC, EVENT_SUB_TYPE_DESC, TO_INTERNAL_LOCATION_DESC, AGY_LOC_ID,
--  TO_AGY_LOC_DESC, TO_LOC_DESC, TO_AGY_LOC_ID, TO_ADDRESS_ID, TO_CITY_CODE,
--  SOURCE)
-- AS
-- SELECT
--        off.offender_id,
--        off.offender_id_display,
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
-- 	            ind.agy_loc_id,
-- 	            ind.event_class,
-- 	            ind.event_status,
-- 	            ind.event_type,
-- 	            ind.event_sub_type,
-- 	            ind.event_date,
-- 	            ind.start_time,
-- 	            ind.to_address_id,
-- 	            ind.to_city_code,
-- 	            ind.to_agy_loc_id,
--                ind.to_internal_location_id,
--                'APP' source
--              FROM offender_ind_schedules ind
--             WHERE event_status = 'SCH'
--             UNION ALL
--            SELECT ce.offender_book_id,
-- 	               NULL,
--                   'EXT_MOV',
--                   'SCH',
-- 	               'CRT',
-- 	               ce.court_event_type,
--                   ce.event_date,
-- 	               ce.start_time,
-- 	               NULL,
-- 	               NULL,
--                   ce.agy_loc_id,
--                   NULL,
--                   'CRT'
--              FROM court_events ce
--             WHERE NVL(ce.event_status,'SCH') = 'SCH'
--             UNION ALL
--            SELECT ord.offender_book_id,
-- 	               NULL,
-- 	               'EXT_MOV',
-- 	               'SCH',
-- 	               ord.movement_type,
-- 	               ord.movement_reason_code,
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
--                   opp.agy_loc_id,
-- 	               'INT_MOV',
-- 	               'SCH',
--                   'PRISON_ACT',
-- 	               ca.course_activity_type,
-- 	               cs.schedule_date,
-- 	               cs.start_time,
-- 	               ca.services_address_id,
-- 	               NULL,
--                   ca.agy_loc_id,
--                   ca.internal_location_id,
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
--               AND ca.active_flag = 'Y'
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
--                   oca.agy_loc_id,
-- 	               'INT_MOV',
-- 	               'SCH',
-- 	               oca.event_type,
-- 	               oca.event_sub_type,
-- 	               oca.event_date,
-- 	               oca.start_time,
-- 	               oca.to_address_id,
-- 	               NULL,
-- 	               oca.to_agy_loc_id,
--                   oca.to_internal_location_id,
--                   'PA'
--              FROM offender_course_attendances oca
--             WHERE oca.event_status = 'SCH'
--             UNION ALL
--            SELECT ov.offender_book_id,
-- 	               ov.agy_loc_id,
-- 	               'INT_MOV',
-- 	               'SCH',
-- 	               'VISIT',
-- 	               'VISIT',
-- 	               ov.visit_date,
-- 	               ov.end_time,
-- 	               null,
-- 	               null,
-- 	               ov.agy_loc_id,
--                   visit_internal_location_id,
--                   'VIS'
--              FROM offender_visits ov
--             WHERE visit_status = 'SCH'
--             UNION ALL
--            SELECT aip.offender_book_id,
--                   ai.agy_loc_id,
--                   'INT_MOV',
--                   'SCH',
--                   'OIC',
--                   'OIC',
--                   oh.hearing_date,
--                   null,
--                   null,
--                   null,
--                   ai.agy_loc_id,
--                   oh.internal_location_id,
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
--        AND bkg.active_flag  = 'Y'
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
