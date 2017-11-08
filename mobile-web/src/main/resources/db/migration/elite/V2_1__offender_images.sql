-- Although this table exists in Elite core schema, it is not used - see IMAGES table.
CREATE TABLE OFFENDER_IMAGES
(
  "OFFENDER_IMAGE_ID"             NUMBER(10, 0) NOT NULL,
  "OFFENDER_BOOK_ID"              NUMBER(10, 0) NOT NULL,
  "CAPTURE_DATETIME"              DATE NOT NULL,
  "ORIENTATION_TYPE"              VARCHAR2(12 CHAR) NOT NULL,
  "FULL_SIZE_IMAGE"               BLOB,
  "THUMBNAIL_IMAGE"               BLOB,
  "IMAGE_OBJECT_TYPE"             VARCHAR2(12 CHAR),
  "IMAGE_VIEW_TYPE"               VARCHAR2(12 CHAR),
  "IMAGE_OBJECT_ID"               NUMBER(10, 0),
  "IMAGE_OBJECT_SEQ"              NUMBER(10, 0),
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'N' NOT NULL,
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER NOT NULL,
  "MODIFY_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp,
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "SEAL_FLAG"                     VARCHAR2(1 CHAR)
);
