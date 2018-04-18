CREATE TABLE "SENTENCE_CALC_TYPES"
(
  "SENTENCE_CALC_TYPE"            VARCHAR2(12 CHAR)                 NOT NULL ,
  "DESCRIPTION"                   VARCHAR2(240 CHAR)                NOT NULL ,
  "EXPIRY_DATE"                   DATE,
  "ACTIVE_FLAG"                   VARCHAR2(1 CHAR) DEFAULT 'Y'      NOT NULL ,
  "LIST_SEQ"                      NUMBER(6, 0) DEFAULT 99,
  "SENTENCE_CATEGORY"             VARCHAR2(12 CHAR)                 NOT NULL ,
  "SENTENCE_TYPE"                 VARCHAR2(12 CHAR)                 NOT NULL ,
  "PROGRAM_METHOD"                VARCHAR2(12 CHAR),
  "HEADER_SEQ"                    NUMBER,
  "HEADER_LABEL"                  VARCHAR2(6 CHAR),
  "MODIFY_DATETIME"               TIMESTAMP(9),
  "MODIFY_USER_ID"                VARCHAR2(32 CHAR),
  "CREATE_DATETIME"               TIMESTAMP(9) DEFAULT systimestamp NOT NULL ,
  "CREATE_USER_ID"                VARCHAR2(32 CHAR) DEFAULT USER    NOT NULL ,
  "AUDIT_TIMESTAMP"               TIMESTAMP(9),
  "AUDIT_USER_ID"                 VARCHAR2(32 CHAR),
  "AUDIT_MODULE_NAME"             VARCHAR2(65 CHAR),
  "AUDIT_CLIENT_USER_ID"          VARCHAR2(64 CHAR),
  "AUDIT_CLIENT_IP_ADDRESS"       VARCHAR2(39 CHAR),
  "AUDIT_CLIENT_WORKSTATION_NAME" VARCHAR2(64 CHAR),
  "AUDIT_ADDITIONAL_INFO"         VARCHAR2(256 CHAR),
  "FUNCTION_TYPE"                 VARCHAR2(12 CHAR),
  "REORDER_SENTENCE_SEQ"          NUMBER(6, 0),
  CONSTRAINT "SENTENCE_CALC_TYPES_PK" PRIMARY KEY ("SENTENCE_CATEGORY", "SENTENCE_CALC_TYPE"),
  CHECK ( active_flag IN ('Y', 'N')  ) ,
  CHECK ( active_flag IN ('Y', 'N')  ) ,
  CHECK ( active_flag IN ('Y', 'N')  ) ,
  CHECK ( active_flag IN ('Y', 'N')  ) ,
  CONSTRAINT "SENTENCE_CALC_TYPE_UK" UNIQUE ("HEADER_SEQ")
);


COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."SENTENCE_CALC_TYPE" IS 'The sentence Calculation Type';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."DESCRIPTION" IS 'The description of the sentence calculation';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."EXPIRY_DATE" IS 'The date which the code is no longer used';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."ACTIVE_FLAG" IS '?If the sentence calculation type valid';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."LIST_SEQ" IS 'The order of the listing';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."SENTENCE_CATEGORY" IS 'The sentence category.  Reference Code(CATEGORY)';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."SENTENCE_TYPE" IS 'The Sentence Type';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."PROGRAM_METHOD" IS 'The program method';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."HEADER_SEQ" IS 'sequence of header in form';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."HEADER_LABEL" IS 'The label of header in form';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."MODIFY_DATETIME" IS 'The timestamp when the record is modified ';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."MODIFY_USER_ID" IS 'The user who modifies the record';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."CREATE_DATETIME" IS 'The timestamp when the record is created';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."CREATE_USER_ID" IS 'The user who creates the record';
COMMENT ON COLUMN "SENTENCE_CALC_TYPES"."REORDER_SENTENCE_SEQ" IS 'Sequence for sentence reordering.';
COMMENT ON TABLE "SENTENCE_CALC_TYPES" IS 'A set of variables used by an algorithm which calculates the sentence of a particular type (eg. Custody Plus).';


CREATE INDEX "SENTENCE_CALC_TYPES_NI1"
  ON "SENTENCE_CALC_TYPES" ("SENTENCE_CALC_TYPE");


