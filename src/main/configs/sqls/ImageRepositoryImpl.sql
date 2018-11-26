
FIND_IMAGE_DETAIL {
    SELECT I.IMAGE_ID,
           I.CAPTURE_DATE,
           I.IMAGE_VIEW_TYPE   IMAGE_VIEW,
           I.ORIENTATION_TYPE  IMAGE_ORIENTATION,
           I.IMAGE_OBJECT_TYPE IMAGE_TYPE,
           I.IMAGE_OBJECT_ID   OBJECT_ID
      FROM IMAGES I
     WHERE I.IMAGE_ID = :imageId
}

FIND_IMAGE_CONTENT {
    SELECT I.IMAGE_THUMBNAIL
      FROM IMAGES I
     WHERE I.IMAGE_ID = :imageId
}

FIND_IMAGE_CONTENT_BY_OFFENDER_NO {
  SELECT
    I.IMAGE_THUMBNAIL
  FROM OFFENDER_BOOKINGS B
    JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
    JOIN IMAGES I ON I.IMAGE_ID =
                              ( SELECT IMAGE_ID FROM
                                  (SELECT IMAGE_ID
                                    FROM IMAGES
                                    WHERE ACTIVE_FLAG = 'Y'
                                    AND IMAGE_OBJECT_TYPE = 'OFF_BKG'
                                    AND IMAGE_OBJECT_ID = B.OFFENDER_BOOK_ID
                                    AND IMAGE_VIEW_TYPE = 'FACE'
                                    AND ORIENTATION_TYPE = 'FRONT'
                                    ORDER BY CREATE_DATETIME DESC)
                                    WHERE ROWNUM <= 1)
  WHERE B.BOOKING_SEQ = 1 AND O.OFFENDER_ID_DISPLAY = :offenderNo
}