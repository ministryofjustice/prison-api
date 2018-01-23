INSERT_KEY_WORKER_ALLOCATION {
    INSERT INTO OFFENDER_KEY_WORKERS (
         OFFENDER_BOOK_ID,
         OFFICER_ID,
         ASSIGNED_DATE,
         ASSIGNED_TIME,
         AGY_LOC_ID,
         ACTIVE_FLAG,
         ALLOC_REASON,
         ALLOC_TYPE,
         USER_ID
	  ) VALUES (
	      :bookingId,
	      :staffId,
	      :assignedDate,
	      :assignedTime,
	      :agencyId,
	      :active,
	      :reason,
	      :type,
	      :userId
	  )
}

DEACTIVATE_KEY_WORKER_ALLOCATION_FOR_OFFENDER_BOOKING {
    UPDATE OFFENDER_KEY_WORKERS SET
      ACTIVE_FLAG = 'N',
      EXPIRY_DATE = :expiryDate,
      DEALLOC_REASON = :deallocationReason
    WHERE OFFENDER_BOOK_ID = :bookingId
}

DEACTIVATE_KEY_WORKER_ALLOCATIONS_FOR_KEY_WORKER {
  UPDATE OFFENDER_KEY_WORKERS SET
    ACTIVE_FLAG = 'N',
    EXPIRY_DATE = :expiryDate,
    DEALLOC_REASON = :deallocationReason
  WHERE OFFICER_ID = :staffId
    AND ACTIVE_FLAG = 'Y'
}

GET_ACTIVE_ALLOCATION_FOR_OFFENDER_BOOKING {
  SELECT OFFENDER_BOOK_ID BOOKING_ID,
         OFFICER_ID STAFF_ID,
         ASSIGNED_TIME ASSIGNED,
         AGY_LOC_ID AGENCY_ID,
         ACTIVE_FLAG ACTIVE,
         ALLOC_REASON REASON,
         ALLOC_TYPE TYPE
  FROM OFFENDER_KEY_WORKERS
  WHERE ACTIVE_FLAG = 'Y'
    AND OFFENDER_BOOK_ID = :bookingId
}

GET_LATEST_ALLOCATION_FOR_OFFENDER_BOOKING {
  SELECT * FROM (
    SELECT OFFENDER_BOOK_ID BOOKING_ID,
           OFFICER_ID STAFF_ID,
           ASSIGNED_TIME ASSIGNED,
           AGY_LOC_ID AGENCY_ID,
           ACTIVE_FLAG ACTIVE,
           ALLOC_REASON REASON,
           DEALLOC_REASON DEALLOCATION_REASON,
           ALLOC_TYPE TYPE,
           EXPIRY_DATE EXPIRY
    FROM OFFENDER_KEY_WORKERS
    WHERE OFFENDER_BOOK_ID = :bookingId
    ORDER BY ASSIGNED_DATE DESC, ASSIGNED_TIME DESC
  )
  WHERE ROWNUM = 1
}

GET_ALLOCATION_HISTORY_FOR_OFFENDER {
  SELECT OKW.OFFENDER_BOOK_ID BOOKING_ID,
         OKW.OFFICER_ID STAFF_ID,
         OKW.ASSIGNED_TIME ASSIGNED,
         OKW.AGY_LOC_ID AGENCY_ID,
         OKW.ACTIVE_FLAG ACTIVE,
         OKW.ALLOC_REASON REASON,
         OKW.DEALLOC_REASON DEALLOCATION_REASON,
         OKW.ALLOC_TYPE TYPE,
         OKW.EXPIRY_DATE EXPIRY
  FROM OFFENDER_KEY_WORKERS OKW
  JOIN OFFENDER_BOOKINGS OB on OKW.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID
  WHERE OB.OFFENDER_ID = :offenderId
}

GET_UNALLOCATED_OFFENDERS {
SELECT
  OB.OFFENDER_BOOK_ID                            booking_id,
  O.OFFENDER_ID_DISPLAY                          offender_no,
  O.FIRST_NAME,
  CONCAT(O.middle_name, CASE WHEN middle_name_2 IS NOT NULL
    THEN concat(' ', O.middle_name_2)
                        ELSE '' END)                MIDDLE_NAMES,
  O.LAST_NAME,
  O.TITLE,
  O.SUFFIX,
  ob.agy_loc_id                                  agency_location_id,
  al.description                                 agency_location_desc,
  OB.LIVING_UNIT_ID                              internal_location_id,
  AIL.DESCRIPTION                                internal_location_desc,
  OB.ACTIVE_FLAG                                 currently_in_prison
FROM OFFENDERS O
  JOIN OFFENDER_BOOKINGS OB
    ON OB.offender_id = o.offender_id
       AND OB.booking_seq = 1
  JOIN agency_locations al
    ON al.agy_loc_id = ob.agy_loc_id
  LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
WHERE
  OB.ACTIVE_FLAG = 'Y'
  AND NOT EXISTS (SELECT 1
                  FROM OFFENDER_KEY_WORKERS OKW
                  WHERE OKW.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OKW.ACTIVE_FLAG = 'Y')

  AND al.agy_loc_id IN (:agencyIds)
}

GET_AVAILABLE_KEYWORKERS {
  SELECT SM.LAST_NAME,
         SM.FIRST_NAME,
         SLR.SAC_STAFF_ID STAFF_ID,
         SLR.HOURS_PER_WEEK CAPACITY
  FROM STAFF_LOCATION_ROLES SLR
    INNER JOIN STAFF_MEMBERS SM ON SM.STAFF_ID = SLR.SAC_STAFF_ID
  WHERE SM.STATUS = 'ACTIVE'
    AND SLR.CAL_AGY_LOC_ID = :agencyId
    AND SLR.POSITION = 'AO'
    AND SLR.ROLE = 'KW'
    AND TRUNC(SYSDATE) BETWEEN TRUNC(SLR.FROM_DATE) AND TRUNC(COALESCE(SLR.TO_DATE,SYSDATE))
}

CHECK_AVAILABLE_KEYWORKER {
  SELECT SLR.SAC_STAFF_ID
  FROM STAFF_LOCATION_ROLES SLR
    INNER JOIN STAFF_MEMBERS SM ON SM.STAFF_ID = SLR.SAC_STAFF_ID
    INNER JOIN OFFENDER_BOOKINGS OB ON OB.AGY_LOC_ID = SLR.CAL_AGY_LOC_ID AND OB.OFFENDER_BOOK_ID = :bookingId
  WHERE SM.STATUS = 'ACTIVE'
    AND SLR.SAC_STAFF_ID = :staffId
    AND OB.ACTIVE_FLAG = 'Y'
    AND SLR.POSITION = 'AO'
    AND SLR.ROLE = 'KW'
    AND TRUNC(SYSDATE) BETWEEN TRUNC(SLR.FROM_DATE) AND TRUNC(COALESCE(SLR.TO_DATE,SYSDATE))
}

GET_ALLOCATED_OFFENDERS {
SELECT
  OB.OFFENDER_BOOK_ID                            booking_id,
  O.OFFENDER_ID_DISPLAY                          offender_no,
  O.FIRST_NAME,
  CONCAT(O.middle_name, CASE WHEN middle_name_2 IS NOT NULL
    THEN concat(' ', O.middle_name_2)
                        ELSE '' END)                MIDDLE_NAMES,
  O.LAST_NAME,
  OKW.OFFICER_ID                                 STAFF_ID,
  OKW.ASSIGNED_TIME                              ASSIGNED,
  OKW.AGY_LOC_ID                                 AGENCY_ID,
  OKW.ALLOC_REASON                               REASON,
  OKW.ALLOC_TYPE                                 allocationType,
  AIL.DESCRIPTION                                internal_location_desc
FROM OFFENDERS O
  JOIN OFFENDER_BOOKINGS OB
    ON OB.offender_id = o.offender_id
       AND OB.booking_seq = 1
  JOIN OFFENDER_KEY_WORKERS OKW
    ON OKW.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID
  JOIN agency_locations al
    ON al.agy_loc_id = ob.agy_loc_id
  LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
WHERE
  OB.ACTIVE_FLAG = 'Y'
  AND OKW.ACTIVE_FLAG = 'Y'
  AND al.agy_loc_id IN (:agencyIds)
  AND (:allocType is NULL OR OKW.ALLOC_TYPE = :allocType)
  AND (:fromDate is not NULL OR TRUNC(OKW.ASSIGNED_TIME) <= TRUNC(COALESCE(:toDate,SYSDATE)))
  AND (:fromDate is NULL OR TRUNC(OKW.ASSIGNED_TIME) BETWEEN  TRUNC(:fromDate)  AND TRUNC(COALESCE(:toDate,SYSDATE)))
}