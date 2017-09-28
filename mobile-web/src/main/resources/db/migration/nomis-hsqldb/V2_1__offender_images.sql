CREATE TABLE OFFENDER_IMAGES
(
  OFFENDER_IMAGE_ID             BIGSERIAL     PRIMARY KEY   NOT NULL,
  OFFENDER_BOOK_ID              BIGINT                      NOT NULL,
  CAPTURE_DATETIME              TIMESTAMP                   NOT NULL,
  ORIENTATION_TYPE              VARCHAR(12)                 NOT NULL,
  FULL_SIZE_IMAGE               BLOB,
  THUMBNAIL_IMAGE               BLOB,
  IMAGE_OBJECT_TYPE             VARCHAR(12),
  IMAGE_VIEW_TYPE               VARCHAR(12),
  IMAGE_OBJECT_ID               BIGINT,
  IMAGE_OBJECT_SEQ              BIGINT,
  ACTIVE_FLAG                   VARCHAR(1)    DEFAULT 'N'   NOT NULL,
  IMAGE_SOURCE_CODE             VARCHAR(12)                 NOT NULL,
  CREATE_DATETIME               TIMESTAMP     DEFAULT now() NOT NULL,
  CREATE_USER_ID                VARCHAR(32)   DEFAULT USER  NOT NULL,
  MODIFY_DATETIME               TIMESTAMP,
  MODIFY_USER_ID                VARCHAR(32)
);
