LIST_CUSTODY_STATUSES {
  SELECT
    O.OFFENDER_ID_DISPLAY,
    OB.agy_loc_id,
    OB.booking_status,
    OB.active_flag,
    (SELECT OEM.direction_code
         FROM offender_external_movements OEM
         WHERE OEM.offender_book_id = OB.offender_book_id
             AND OEM.movement_seq = (SELECT MAX(OEM1.movement_seq)
                                        FROM offender_external_movements OEM1
                                        WHERE OEM.offender_book_id = OEM1.offender_book_id
                                            AND OEM1.MOVEMENT_DATE <= :onDate)) AS DIRECTION_CODE,
    (SELECT OEM.movement_type
         FROM offender_external_movements OEM
         WHERE OEM.offender_book_id = OB.offender_book_id
             AND OEM.movement_seq = (SELECT MAX(OEM1.movement_seq)
                                        FROM offender_external_movements OEM1
                                        WHERE OEM.offender_book_id = OEM1.offender_book_id
                                            AND OEM1.MOVEMENT_DATE <= :onDate)) AS MOVEMENT_TYPE,
    (SELECT OEM.movement_reason_code
         FROM offender_external_movements OEM
         WHERE OEM.offender_book_id = OB.offender_book_id
             AND OEM.movement_seq = (SELECT MAX(OEM1.movement_seq)
                                        FROM offender_external_movements OEM1
                                        WHERE OEM.offender_book_id = OEM1.offender_book_id
                                            AND OEM1.MOVEMENT_DATE <= :onDate)) AS MOVEMENT_REASON_CODE
  FROM OFFENDERS O
    JOIN OFFENDER_BOOKINGS OB
        ON OB.offender_id = O.offender_id
            AND OB.booking_seq = 1
  WHERE OB.agy_loc_id <> 'ZZGHI'
}

GET_CUSTODY_STATUS {
  SELECT
    O.OFFENDER_ID_DISPLAY,
    OB.agy_loc_id,
    OB.booking_status,
    OB.active_flag,
    (SELECT OEM.direction_code
         FROM offender_external_movements OEM
         WHERE OEM.offender_book_id = OB.offender_book_id
             AND OEM.movement_seq = (SELECT MAX(OEM1.movement_seq)
                                        FROM offender_external_movements OEM1
                                        WHERE OEM.offender_book_id = OEM1.offender_book_id
                                            AND OEM1.MOVEMENT_DATE <= :onDate)) AS DIRECTION_CODE,
    (SELECT OEM.movement_type
         FROM offender_external_movements OEM
         WHERE OEM.offender_book_id = OB.offender_book_id
             AND OEM.movement_seq = (SELECT MAX(OEM1.movement_seq)
                                        FROM offender_external_movements OEM1
                                        WHERE OEM.offender_book_id = OEM1.offender_book_id
                                            AND OEM1.MOVEMENT_DATE <= :onDate)) AS MOVEMENT_TYPE,
    (SELECT OEM.movement_reason_code
         FROM offender_external_movements OEM
         WHERE OEM.offender_book_id = OB.offender_book_id
             AND OEM.movement_seq = (SELECT MAX(OEM1.movement_seq)
                                        FROM offender_external_movements OEM1
                                        WHERE OEM.offender_book_id = OEM1.offender_book_id
                                            AND OEM1.MOVEMENT_DATE <= :onDate)) AS MOVEMENT_REASON_CODE
  FROM OFFENDERS O
    JOIN OFFENDER_BOOKINGS OB
        ON OB.offender_id = O.offender_id
            AND OB.booking_seq = 1
  WHERE OB.agy_loc_id <> 'ZZGHI'
    AND O.OFFENDER_ID_DISPLAY = :offenderNo
}

GET_RECENT_MOVEMENTS
{
 SELECT OFFENDERS.OFFENDER_ID_DISPLAY AS OFFENDER_NO,
        OEM.CREATE_DATETIME           AS CREATE_DATE_TIME
  FROM OFFENDER_EXTERNAL_MOVEMENTS OEM
    INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID AND OB.BOOKING_SEQ = 1
    INNER JOIN OFFENDERS ON OFFENDERS.OFFENDER_ID = OB.OFFENDER_ID
  WHERE OEM.MOVEMENT_DATE > :fromDateTime - INTERVAL '30' DAY -- MOVEMENT_DATE has index: rough filter
    --AND TO_DATE(TO_CHAR(oem.movement_date,'DD/MM/YYYY')|| ' '|| TO_CHAR(oem.movement_time,'HH24:MI'), 'DD/MM/YYYY HH24:MI') <= SYSDATE -- ignore future movements?
    AND OEM.CREATE_DATETIME >= :fromDateTime
    AND OEM.MOVEMENT_TYPE IN ('TRN','REL','ADM')
    AND OEM.MOVEMENT_SEQ = (SELECT MAX(OEM2.MOVEMENT_SEQ) FROM OFFENDER_EXTERNAL_MOVEMENTS OEM2
                                        Where OEM2.OFFENDER_BOOK_ID = OEM.OFFENDER_BOOK_ID);
}