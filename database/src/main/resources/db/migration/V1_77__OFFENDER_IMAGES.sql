CREATE TABLE OFFENDER_IMAGES
(
  OFFENDER_IMAGE_ID             NUMBER(10, 0)               NOT NULL ENABLE,
  "OFFENDER_BOOK_ID"              NUMBER(10, 0)                     NOT NULL,
  CAPTURE_DATETIME              TIMESTAMP                   NOT NULL,
  ORIENTATION_TYPE              VARCHAR(12)                 NOT NULL,
  FULL_SIZE_IMAGE               BLOB,
  THUMBNAIL_IMAGE               BLOB,
  IMAGE_OBJECT_TYPE             VARCHAR(12),
  IMAGE_VIEW_TYPE               VARCHAR(12),
  "IMAGE_OBJECT_ID"               NUMBER(10, 0)                     NOT NULL ENABLE,
  "IMAGE_OBJECT_SEQ"              NUMBER(6, 0),
  ACTIVE_FLAG                   VARCHAR(1)    DEFAULT 'N'   NOT NULL,
  IMAGE_SOURCE_CODE             VARCHAR(12)                 NOT NULL,
  CREATE_DATETIME               TIMESTAMP     DEFAULT sysdate NOT NULL,
  CREATE_USER_ID                VARCHAR(32)   DEFAULT USER  NOT NULL,
  MODIFY_DATETIME               TIMESTAMP,
  MODIFY_USER_ID                VARCHAR(32)
);