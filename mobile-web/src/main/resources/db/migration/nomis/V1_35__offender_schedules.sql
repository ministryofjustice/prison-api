CREATE TABLE OFFENDER_IND_SCHEDULES
(
	EVENT_ID                        BIGSERIAL   PRIMARY KEY   NOT NULL,
	OFFENDER_BOOK_ID                BIGINT                    NOT NULL,
	EVENT_DATE                      DATE,
	START_TIME                      DATE,
	END_TIME                        DATE,
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

ALTER TABLE OFFENDER_IND_SCHEDULES ADD CHECK (EVENT_CLASS IN ('EXT_MOV','INT_MOV','COMM'));


-- CREATE VIEW V_OFFENDER_ALL_SCHEDULES_2
--   AS SELECT
--     EVENT_ID,
--     OFFENDER_BOOK_ID,
--     AGY_LOC_ID,
--     EVENT_DATE,
--     START_TIME,
--     END_TIME,
--     EVENT_CLASS,
--     EVENT_TYPE,
--     EVENT_SUB_TYPE,
--     EVENT_STATUS,
--     EVENT_OUTCOME,
--     CONFIRM_FLAG,
--     OUTCOME_REASON_CODE,
--     COMMENT_TEXT,
--     REFERENCE_ID,
--     APPLICATION_DATE,
--     APPLICATION_TIME,
--     RETURN_DATE,
--     RETURN_TIME,
--     TO_AGY_LOC_ID,
--     ESCORT_CODE,
--     DIRECTION_CODE,
--     TO_INTERNAL_LOCATION_ID,
--     FROM_CITY_CODE,
--     TO_CITY_CODE,
--     CREDITED_HOURS,
--     PIECE_WORK,
--     ENGAGEMENT_CODE,
--     UNDERSTANDING_CODE,
--     DETAILS,
--     UNPAID_WORK_BEHAVIOUR,
--     UNPAID_WORK_ACTION,
--     SICK_NOTE_RECEIVED_DATE,
--     SICK_NOTE_EXPIRY_DATE,
--     UNEXCUSED_ABSENCE_FLAG,
--     IN_TIME,
--     OUT_TIME,
--     TRANSPORT_CODE,
--     PERFORMANCE_CODE,
--     OJ_LOCATION_CODE,
--     TO_COUNTRY_CODE,
--     AGREED_TRAVEL_HOUR,
--     CHECK_BOX_1,
--     CHECK_BOX_2,
--     HIDDEN_COMMENT_TEXT,
--     IN_CHARGE_STAFF_ID,
--     OFF_PRGREF_ID,
--     CONTACT_PERSON_NAME,
--     TO_ADDRESS_OWNER_CLASS,
--     TO_ADDRESS_ID,
--     TO_CORPORATE_ID,
--     UNPAID_WORK_SUPERVISOR,
--     TA_ID,
--     'SCH',
--     0,
--     OFFENDER_PRG_OBLIGATION_ID,
--     SENTENCE_SEQ,
--     CREATE_DATETIME,
--     CREATE_USER_ID,
--     TO_NUMBER(NULL),
--     TO_NUMBER(NULL),
--     TO_NUMBER(NULL),
--     NULL COURSE_CODE,
--     NULL COURSE_DESCRIPTION
--   FROM OFFENDER_IND_SCHEDULES
--   WHERE EVENT_STATUS <> 'DEL';


-- CREATE FUNCTION event_type_desc(p_event_class VARCHAR(12), p_event_type VARCHAR(12))
--   RETURNS VARCHAR(40)
--   READS SQL DATA
--   BEGIN ATOMIC
--     DECLARE lv_desc VARCHAR(40) DEFAULT 'Not Specified';
--     DECLARE lv_domain VARCHAR(12);
--     IF p_event_type IS NOT NULL
--     THEN
--       CASE p_event_class
--         WHEN 'EXT_MOV'
--         THEN
--           SET lv_domain = 'MOVE_TYPE';
--         WHEN 'INT_MOV'
--         THEN
--           SET lv_domain = 'INT_SCH_TYPE';
--         WHEN 'COMM'
--         THEN
--           SET lv_domain = 'EVENTS';
--         ELSE
--           SET lv_domain = 'EVENTS';
--       END CASE;
--
--       SELECT COALESCE(DESCRIPTION, 'Not Specified')
--         INTO lv_desc
--         FROM REFERENCE_CODES
--        WHERE CODE = p_event_type
--          AND DOMAIN = lv_domain;
--     END IF;
--
--     RETURN lv_desc;
--   END;
--
-- CREATE FUNCTION event_sub_type_desc(p_event_class VARCHAR(12), p_event_type VARCHAR(12), p_event_sub_type VARCHAR(12))
--   RETURNS VARCHAR(40)
--   BEGIN ATOMIC
--     RETURN 'Not Specified';
--   END;
--
-- CREATE FUNCTION getdesccode(p_code_type VARCHAR(12), p_code_value VARCHAR(12))
--   RETURNS VARCHAR(40)
--   BEGIN ATOMIC
--     RETURN 'Not Specified';
--   END;
--
-- CREATE FUNCTION level_code(p_description VARCHAR(240), p_level INTEGER)
--   RETURNS VARCHAR(40)
--   BEGIN ATOMIC
--     RETURN 'X';
--   END;
--
-- CREATE FUNCTION get_corporate_name(p_code_type VARCHAR(6))
--   RETURNS VARCHAR(40)
--   BEGIN ATOMIC
--     RETURN 'Not Specified';
--   END;
--
-- CREATE FUNCTION active_flag(p_location_id BIGINT)
--   RETURNS VARCHAR(1)
--   BEGIN ATOMIC
--     RETURN 'N';
--   END;
--
-- CREATE FUNCTION operation_flag(p_location_id BIGINT)
--   RETURNS VARCHAR(1)
--   BEGIN ATOMIC
--     RETURN 'N';
--   END;
--
--
-- CREATE VIEW LIVING_UNITS
--   AS SELECT
--     INTERNAL_LOCATION_ID LIVING_UNIT_ID,
--     AGY_LOC_ID,
--     INTERNAL_LOCATION_TYPE LIVING_UNIT_TYPE,
--     INTERNAL_LOCATION_CODE LIVING_UNIT_CODE,
--     DESCRIPTION,
--     substr(level_code(DESCRIPTION,1),1,40),
--     substr(level_code(DESCRIPTION,2),1,40),
--     substr(level_code(DESCRIPTION,3),1,40),
--     substr(level_code(DESCRIPTION,4),1,40),
--     USER_DESC,
--     ACA_CAP_RATING,
--     SECURITY_LEVEL_CODE,
--     LIST_SEQ,
--     PARENT_INTERNAL_LOCATION_ID PARENT_LIVING_UNIT_ID,
--     UNIT_TYPE,
--     ACTIVE_FLAG,
--     substr(active_flag(INTERNAL_LOCATION_ID),1,1),
--     CNA_NO,
--     CAPACITY,
--     OPERATION_CAPACITY,
--     CERTIFIED_FLAG,
--     DEACTIVATE_DATE,
--     REACTIVATE_DATE,
--     DEACTIVATE_REASON_CODE,
--     COMMENT_TEXT,
--     (SELECT DECODE(COUNT(*), 0, 'Y', 'N')
--      FROM AGENCY_INTERNAL_LOCATIONS AIL2
--      WHERE AIL2.PARENT_INTERNAL_LOCATION_ID = AIL.INTERNAL_LOCATION_ID
--      AND AIL2.UNIT_TYPE IS NOT NULL),
--     substr(operation_flag(INTERNAL_LOCATION_ID),1,1),
--     NO_OF_OCCUPANT
--   FROM AGENCY_INTERNAL_LOCATIONS AIL
--   WHERE UNIT_TYPE IS NOT NULL;
--
--
-- CREATE VIEW V_OFFENDER_ALL_SCHEDULES
--   AS SELECT
--     SCH.EVENT_ID,
--     SCH.OFFENDER_BOOK_ID,
--     BKG.IN_OUT_STATUS,
--     BKG.BOOKING_NO,
--     BKG.ACTIVE_FLAG,
--     BKG.OFFENDER_ID,
--     OFF.OFFENDER_ID_DISPLAY,
--     OFF.LAST_NAME,
--     OFF.FIRST_NAME,
--     SCH.EVENT_DATE,
--     CASE
-- 	    WHEN SCH.EVENT_TYPE IN ('UW','DRR')
--    		THEN
-- 		    SCH.IN_TIME
-- 		  ELSE
-- 	  	  SCH.START_TIME
--     END START_TIME,
--     CASE
-- 	    WHEN SCH.EVENT_TYPE IN ('UW','DRR')
--    		THEN
-- 	 	    SCH.OUT_TIME
-- 		  ELSE
-- 		    SCH.END_TIME
--     END END_TIME,
--     SCH.EVENT_CLASS,
--     SCH.EVENT_TYPE,
--     SUBSTR(event_type_desc(SCH.EVENT_CLASS, SCH.EVENT_TYPE), 1, 40),
--     SCH.EVENT_SUB_TYPE,
--     SUBSTR(event_sub_type_desc(SCH.EVENT_CLASS, SCH.EVENT_TYPE, SCH.EVENT_SUB_TYPE), 1, 40),
--     DECODE(EVENT_STATUS, 'SCH', 'Y', 'N'),
--     EVENT_STATUS,
--     SUBSTR(getdesccode('EVENT_STS', SCH.EVENT_STATUS), 1, 40),
--     EVENT_OUTCOME,
--     SUBSTR(getdesccode('OUTCOMES', SCH.EVENT_OUTCOME), 1, 40),
--     SCH.OUTCOME_REASON_CODE,
--     SCH.REFERENCE_ID,
--     SCH.APPLICATION_DATE,
--     SCH.APPLICATION_TIME,
--     SCH.RETURN_DATE,
--     SCH.RETURN_TIME,
--     SCH.COMMENT_TEXT,
--     SCH.DETAILS,
--     SCH.AGY_LOC_ID,
--     AGY.DESCRIPTION,
--     BKG.LIVING_UNIT_ID,
--     LU.DESCRIPTION,
--     LU.LEVEL_1_CODE,
--     LU.LEVEL_2_CODE,
--     LU.LEVEL_3_CODE,
--     LU.LEVEL_4_CODE,
--     BKG.AGENCY_IML_ID,
--     AIL2.DESCRIPTION,
--     SUBSTR(level_code(AIL2.DESCRIPTION, 1), 1, 40),
--     SUBSTR(level_code(AIL2.DESCRIPTION, 2), 1, 40),
--     SUBSTR(level_code(AIL2.DESCRIPTION, 3), 1, 40),
--     SCH.TO_AGY_LOC_ID,
-- 	  CASE
-- 		  WHEN SCH.EVENT_TYPE IN ('DRR','UW') AND SCH.RECORD_SOURCE != 'SCH'
-- 		  THEN
-- 		    get_corporate_name(SCH.TO_AGY_LOC_ID)
-- 		  ELSE
-- 		    AGY2.DESCRIPTION
--     END,
--     nvl(sch.to_agy_loc_id, oj_location_code),
--     NVL(AGY2.DESCRIPTION, SUBSTR(getdesccode('OJ_LOCATION', OJ_LOCATION_CODE), 1, 40)),
--     SCH.ESCORT_CODE,
--     SUBSTR(getdesccode('ESCORT', SCH.ESCORT_CODE), 1, 40),
--     SCH.DIRECTION_CODE,
--     SCH.FROM_CITY_CODE,
--     CASE
--       WHEN SCH.FROM_CITY_CODE IS NOT NULL
--       THEN
--         SUBSTR(getdesccode('CITY', SCH.FROM_CITY_CODE), 1, 40)
--       ELSE
-- 	      ' '
-- 	  END,
--     SCH.TO_CITY_CODE,
--     CASE
--       WHEN SCH.TO_CITY_CODE IS NOT NULL
--       THEN
--         SUBSTR(getdesccode('CITY', SCH.TO_CITY_CODE), 1, 40)
--       ELSE
--         ' '
--     END,
--     SCH.TO_INTERNAL_LOCATION_ID,
--     AIL.DESCRIPTION,
--     SUBSTR(level_code(AIL.DESCRIPTION, 1), 1, 40),
--     SUBSTR(level_code(AIL.DESCRIPTION, 2), 1, 40),
--     SUBSTR(level_code(AIL.DESCRIPTION, 3), 1, 40),
--     AIL.USER_DESC,
--     SCH.CREDITED_HOURS,
--     SCH.ENGAGEMENT_CODE,
--     SCH.UNDERSTANDING_CODE,
--     SCH.PIECE_WORK,
--     SCH.IN_TIME,
--     SCH.OUT_TIME,
--     SCH.PERFORMANCE_CODE,
--     SCH.TRANSPORT_CODE,
--     SCH.OJ_LOCATION_CODE,
--     CASE
-- 	    WHEN OJ_LOCATION_CODE IS NOT NULL
--       THEN
--         SUBSTR(getdesccode('OJ_LOCATION', OJ_LOCATION_CODE), 1, 40)
--       ELSE
--         ' '
-- 	  END,
--     SCH.TO_COUNTRY_CODE,
--     CASE
--       WHEN TO_COUNTRY_CODE IS NOT NULL
--       THEN
--         SUBSTR(getdesccode('COUNTRY', TO_COUNTRY_CODE), 1, 40)
--       ELSE
--         ' '
--     END,
--     SCH.SICK_NOTE_EXPIRY_DATE,
--     SCH.SICK_NOTE_RECEIVED_DATE,
--     SCH.UNEXCUSED_ABSENCE_FLAG,
--     SCH.UNPAID_WORK_ACTION,
--     SCH.UNPAID_WORK_BEHAVIOUR,
--     SCH.AGREED_TRAVEL_HOUR,
--     SCH.CHECK_BOX_1,
--     SCH.CHECK_BOX_2,
--     SCH.HIDDEN_COMMENT_TEXT,
--     SCH.IN_CHARGE_STAFF_ID,
--     SUBSTR(DECODE(IN_CHARGE_STAFF_ID, NULL, ' ', (STF.LAST_NAME || ', ' || STF.FIRST_NAME)), 1, 40),
--     SCH.OFF_PRGREF_ID,
--     SCH.CONTACT_PERSON_NAME,
--     SCH.TO_ADDRESS_OWNER_CLASS,
--     SCH.TO_ADDRESS_ID,
--     SCH.UNPAID_WORK_SUPERVISOR,
--     SCH.TA_ID,
--     SCH.RECORD_SOURCE,
--     SCH.CHECK_SUM,
--     SCH.CREATE_DATETIME,
--     SCH.CREATE_USER_ID,
--     SCH.PROGRAM_ID,
--     SCH.CRS_ACTY_ID,
--     SCH.SESSION_NO,
--     SCH.OFFENDER_PRG_OBLIGATION_ID,
-- 	  SCH.COURSE_CODE,
-- 		SCH.COURSE_DESCRIPTION
--   FROM V_OFFENDER_ALL_SCHEDULES_2 SCH
--     INNER JOIN OFFENDER_BOOKINGS BKG ON SCH.OFFENDER_BOOK_ID = BKG.OFFENDER_BOOK_ID
--     INNER JOIN OFFENDERS OFF ON BKG.OFFENDER_ID = OFF.OFFENDER_ID
--     LEFT JOIN AGENCY_LOCATIONS AGY ON SCH.AGY_LOC_ID = AGY.AGY_LOC_ID
--     LEFT JOIN AGENCY_LOCATIONS AGY2 ON SCH.TO_AGY_LOC_ID = AGY.AGY_LOC_ID
--     LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON SCH.TO_INTERNAL_LOCATION_ID = AIL.INTERNAL_LOCATION_ID
--     LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL2 ON BKG.AGENCY_IML_ID = AIL2.INTERNAL_LOCATION_ID
--     LEFT JOIN STAFF_MEMBERS STF ON SCH.IN_CHARGE_STAFF_ID = STF.STAFF_ID
--     LEFT JOIN LIVING_UNITS LU ON BKG.LIVING_UNIT_ID = LU.LIVING_UNIT_ID;
--
--      agency_locations agy,
--      offender_bookings bkg,
--      offenders OFF,
--      agency_internal_locations ail,
--      agency_locations agy2,
--      staff_members stf,
--      living_units lu,
--      agency_internal_locations ail2
--  WHERE sch.agy_loc_id = agy.agy_loc_id(+)
--    AND sch.to_agy_loc_id = agy2.agy_loc_id(+)
--    AND sch.to_internal_location_id = ail.internal_location_id(+)
--    AND bkg.agency_iml_id = ail2.internal_location_id(+)
--    AND sch.offender_book_id = bkg.offender_book_id
--    AND bkg.offender_id = OFF.offender_id
--    AND sch.in_charge_staff_id = stf.staff_id(+)
--    AND bkg.living_unit_id = lu.living_unit_id(+);
