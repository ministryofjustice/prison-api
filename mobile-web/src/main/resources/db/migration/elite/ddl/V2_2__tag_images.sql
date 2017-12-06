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
