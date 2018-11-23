
CREATE TABLE "OFFENDER_IMAGES"
(
  "OFFENDER_IMAGE_ID"             NUMBER(10, 0)                     NOT NULL ,
  "OFFENDER_BOOK_ID"              NUMBER(10, 0)                     NOT NULL ,
  "CAPTURE_DATETIME"              DATE                              NOT NULL ,
  "ORIENTATION_TYPE"              VARCHAR2(12 CHAR)                 NOT NULL ,
  "FULL_SIZE_IMAGE"               BLOB,
  "THUMBNAIL_IMAGE"               BLOB,
  "IMAGE_OBJECT_TYPE"             VARCHAR2(12 CHAR),
  "IMAGE_VIEW_TYPE"               VARCHAR2(12 CHAR),
  "IMAGE_OBJECT_ID"               NUMBER(10, 0),
  "IMAGE_OBJECT_SEQ"              NUMBER(10, 0),
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'N'      NOT NULL ,
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
  "IMAGE_SOURCE_CODE"             VARCHAR2(12 CHAR)                 NOT NULL ,
  CONSTRAINT "OFFENDER_IMAGES_PK" PRIMARY KEY ("OFFENDER_IMAGE_ID"),
  CONSTRAINT "OFF_IMG_OFF_BKG_F1" FOREIGN KEY ("OFFENDER_BOOK_ID")
  REFERENCES "OFFENDER_BOOKINGS" ("OFFENDER_BOOK_ID") ON DELETE CASCADE
);


COMMENT ON COLUMN "OFFENDER_IMAGES"."OFFENDER_IMAGE_ID" IS 'Unique sequence identifying each image - Oracle Sequence - (PK)';

COMMENT ON COLUMN "OFFENDER_IMAGES"."OFFENDER_BOOK_ID" IS 'Unique Sequence identifying the Offender''s Booking record.';

COMMENT ON COLUMN "OFFENDER_IMAGES"."CAPTURE_DATETIME" IS 'The timestamp when the image is taken';

COMMENT ON COLUMN "OFFENDER_IMAGES"."ORIENTATION_TYPE" IS 'The orientation type ';

COMMENT ON COLUMN "OFFENDER_IMAGES"."FULL_SIZE_IMAGE" IS 'The full size image (MUGSHOT) of the offender stored as jpeg format.';

COMMENT ON COLUMN "OFFENDER_IMAGES"."THUMBNAIL_IMAGE" IS 'The thumbnail size image (MUGSHOT) of the offender stored as jpeg format.';

COMMENT ON COLUMN "OFFENDER_IMAGES"."IMAGE_OBJECT_ID" IS 'The ID of the image object.';

COMMENT ON COLUMN "OFFENDER_IMAGES"."IMAGE_OBJECT_SEQ" IS 'The seq ID of the image object.';

COMMENT ON COLUMN "OFFENDER_IMAGES"."ACTIVE_FLAG" IS 'Flag to identify if the image record is currently active or not.';

COMMENT ON COLUMN "OFFENDER_IMAGES"."CREATE_DATETIME" IS 'The timestamp when the record is created';

COMMENT ON COLUMN "OFFENDER_IMAGES"."CREATE_USER_ID" IS 'The user who creates the record';

COMMENT ON COLUMN "OFFENDER_IMAGES"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';

COMMENT ON COLUMN "OFFENDER_IMAGES"."MODIFY_USER_ID" IS 'The user who modifies the record';

COMMENT ON TABLE "OFFENDER_IMAGES"  IS 'The images of an offender';


CREATE INDEX "OFFENDER_IMAGES_NI1" ON "OFFENDER_IMAGES" ("OFFENDER_BOOK_ID");



