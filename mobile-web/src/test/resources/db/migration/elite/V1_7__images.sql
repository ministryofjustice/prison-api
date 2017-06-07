CREATE TABLE IMAGES
(
  "IMAGE_ID"                      NUMBER(10, 0),
  "CAPTURE_DATE"                  DATE,
  "IMAGE_OBJECT_TYPE"             VARCHAR2(12 CHAR),
  "IMAGE_OBJECT_ID"               NUMBER(10, 0),
  "IMAGE_OBJECT_SEQ"              NUMBER(6, 0),
  "IMAGE_VIEW_TYPE"               VARCHAR2(12 CHAR),
  "IMAGE_THUMBNAIL"               BLOB,
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR)  DEFAULT 'Y',
  "ORIENTATION_TYPE"              VARCHAR2(12 CHAR),
  "SEAL_FLAG"                     VARCHAR2(1 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9)      DEFAULT systimestamp,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR)
);


CREATE TABLE OFFENDER_IMAGES
(
  "OFFENDER_IMAGE_ID"             NUMBER(10, 0),
  "OFFENDER_BOOK_ID"              NUMBER(10, 0),
  "CAPTURE_DATETIME"              DATE,
  "ORIENTATION_TYPE"              VARCHAR2(12 CHAR),
  "FULL_SIZE_IMAGE"               BLOB,
  "THUMBNAIL_IMAGE"               BLOB,
  "IMAGE_OBJECT_TYPE"             VARCHAR2(12 CHAR),
  "IMAGE_VIEW_TYPE"               VARCHAR2(12 CHAR),
  "IMAGE_OBJECT_ID"               NUMBER(10, 0),
  "IMAGE_OBJECT_SEQ"              NUMBER(10, 0),
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR)  DEFAULT 'N',
  "IMAGE_SOURCE_CODE"             VARCHAR2(12 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9)      DEFAULT systimestamp,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "SEAL_FLAG"                     VARCHAR2(1 CHAR)
);


CREATE TABLE TAG_IMAGES
(
  "TAG_IMAGE_ID"                  NUMBER(10, 0),
  "IMAGE_OBJECT_TYPE"             VARCHAR2(12 CHAR),
  "IMAGE_OBJECT_ID"               NUMBER(10, 0),
  "IMAGE_OBJECT_SEQ"              NUMBER(6, 0),
  "CAPTURE_DATETIME"              DATE,
  "SET_NAME"                      VARCHAR2(12 CHAR),
  "IMAGE_VIEW_TYPE"               VARCHAR2(12 CHAR),
  "ORIENTATION_TYPE"              VARCHAR2(12 CHAR),
  "FULL_SIZE_IMAGE"               BLOB,
  "THUMBNAIL_IMAGE"               BLOB,
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR)  DEFAULT 'N',
  "CREATE_DATETIME"               TIMESTAMP(9)      DEFAULT systimestamp,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER,
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "SEAL_FLAG"                     VARCHAR2(1 CHAR)
);
