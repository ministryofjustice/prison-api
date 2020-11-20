package uk.gov.justice.hmpps.prison.repository.sql

enum class ImageRepositorySql(val sql: String) {
  FIND_IMAGE_DETAIL(
    """
        SELECT I.OFFENDER_IMAGE_ID  IMAGE_ID,
        I.CAPTURE_DATETIME   CAPTURE_DATE,
        I.IMAGE_VIEW_TYPE    IMAGE_VIEW,
        I.ORIENTATION_TYPE   IMAGE_ORIENTATION,
        I.IMAGE_OBJECT_TYPE  IMAGE_TYPE,
        I.IMAGE_OBJECT_ID    OBJECT_ID
        FROM OFFENDER_IMAGES I
        WHERE I.OFFENDER_IMAGE_ID = :imageId
    """
  ),

  FIND_IMAGE_CONTENT(
    """
        SELECT I.%s
        FROM OFFENDER_IMAGES I
        WHERE I.OFFENDER_IMAGE_ID = :imageId
    """
  ),

  FIND_IMAGE_CONTENT_BY_OFFENDER_NO(
    """
        SELECT
        I.%s
                FROM OFFENDER_BOOKINGS B
        JOIN OFFENDERS O ON B.OFFENDER_ID = O.OFFENDER_ID
        JOIN OFFENDER_IMAGES I ON I.OFFENDER_IMAGE_ID =
                (SELECT MAX(OI.OFFENDER_IMAGE_ID)
                        FROM OFFENDER_IMAGES OI
                        WHERE OI.ACTIVE_FLAG = 'Y'
        AND IMAGE_OBJECT_TYPE = 'OFF_BKG'
        AND OI.OFFENDER_BOOK_ID = B.OFFENDER_BOOK_ID
                AND OI.IMAGE_VIEW_TYPE = 'FACE'
        AND OI.ORIENTATION_TYPE = 'FRONT')
        WHERE B.BOOKING_SEQ = 1 AND O.OFFENDER_ID_DISPLAY = :offenderNo
    """
  )
}
