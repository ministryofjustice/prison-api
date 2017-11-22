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