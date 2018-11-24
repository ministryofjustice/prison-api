CREATE TABLE "OFFENDER_IDENTIFYING_MARKS"
(
  "OFFENDER_BOOK_ID"              NUMBER(10, 0)                     NOT NULL ,
  "ID_MARK_SEQ"                   NUMBER(6, 0)                      NOT NULL ,
  "BODY_PART_CODE"                VARCHAR2(12 CHAR)                 NOT NULL ,
  "MARK_TYPE"                     VARCHAR2(12 CHAR)                 NOT NULL ,
  "SIDE_CODE"                     VARCHAR2(12 CHAR),
  "PART_ORIENTATION_CODE"         VARCHAR2(12 CHAR),
  "COMMENT_TEXT"                  VARCHAR2(240 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  CONSTRAINT "OFFENDER_IDENTIFIYING_MARKS" PRIMARY KEY ("OFFENDER_BOOK_ID", "ID_MARK_SEQ"),
  CONSTRAINT "OFF_IM_OFF_BKG_F1" FOREIGN KEY ("OFFENDER_BOOK_ID")
  REFERENCES "OFFENDER_BOOKINGS" ("OFFENDER_BOOK_ID")
);


COMMENT ON COLUMN "OFFENDER_IDENTIFYING_MARKS"."OFFENDER_BOOK_ID" IS ' The system generated identifier for an offender booking.';

COMMENT ON COLUMN "OFFENDER_IDENTIFYING_MARKS"."ID_MARK_SEQ" IS ' A system generated secondary primary key column.';

COMMENT ON COLUMN "OFFENDER_IDENTIFYING_MARKS"."BODY_PART_CODE" IS 'Reference Code ( BODY_PART ) : Code for body part with mark ie. Leg, Forearm, Chest etc.';

COMMENT ON COLUMN "OFFENDER_IDENTIFYING_MARKS"."MARK_TYPE" IS 'Reference Code ( MARK_TYPE ) :  Type of mark on offender - ie. Tattoo, Scar etc.';

COMMENT ON COLUMN "OFFENDER_IDENTIFYING_MARKS"."SIDE_CODE" IS 'Reference Code ( SIDE ) : Side of body part containing mark ie. Front, Back, Left, Right etc.';

COMMENT ON COLUMN "OFFENDER_IDENTIFYING_MARKS"."PART_ORIENTATION_CODE" IS 'Reference Code ( PART_ORIENT ) : Orientation of mark on body ie. Lower, Upper etc.';

COMMENT ON COLUMN "OFFENDER_IDENTIFYING_MARKS"."COMMENT_TEXT" IS ' Comment describing identifying mark.';

COMMENT ON COLUMN "OFFENDER_IDENTIFYING_MARKS"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "OFFENDER_IDENTIFYING_MARKS"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "OFFENDER_IDENTIFYING_MARKS"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "OFFENDER_IDENTIFYING_MARKS"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON TABLE "OFFENDER_IDENTIFYING_MARKS" IS 'The Body Identifying Marks of an offender';


