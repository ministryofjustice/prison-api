-----------------------------------------------------------------------
-- Source tables for Prison Activities (PRISON_ACT) Scheduled Events --
-----------------------------------------------------------------------

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
