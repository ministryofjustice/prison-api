CREATE OR REPLACE PROCEDURE insert_images (
  captureDateTime IN OFFENDER_IMAGES.CAPTURE_DATETIME%TYPE,
  createDateTime IN OFFENDER_IMAGES.CREATE_DATETIME%TYPE,
  imageId IN OFFENDER_IMAGES.OFFENDER_IMAGE_ID%TYPE,
  offenderBookId IN OFFENDER_IMAGES.OFFENDER_BOOK_ID%TYPE,
  imageData IN VARCHAR2)
IS
  v_b blob;
  rawImageData raw(32767);
  begin
    DBMS_LOB.createtemporary(v_b, FALSE);
    rawImageData := hextoraw(imageData);
    INSERT INTO OFFENDER_IMAGES (CAPTURE_DATETIME, ORIENTATION_TYPE, IMAGE_VIEW_TYPE,  IMAGE_OBJECT_TYPE,
                                 ACTIVE_FLAG, CREATE_DATETIME,
                                 IMAGE_SOURCE_CODE, OFFENDER_IMAGE_ID, OFFENDER_BOOK_ID, THUMBNAIL_IMAGE)
    VALUES (captureDateTime,'FRONT', 'FACE', 'OFF_BKG', 'Y',
            createDateTime,
            'GEN', imageId,offenderBookId, empty_blob())
    returning THUMBNAIL_IMAGE into v_b;
    dbms_lob.open(v_b, dbms_lob.lob_readwrite);
    dbms_lob.writeappend(v_b, UTL_RAW.LENGTH (rawImageData) ,rawImageData);
    dbms_lob.close(LOB_LOC=>v_b);
  end;