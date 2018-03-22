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
    INNER JOIN OFFENDER_BOOKINGS OB ON OKW.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID
      AND OB.ROOT_OFFENDER_ID = (SELECT ROOT_OFFENDER_ID FROM OFFENDER_BOOKINGS WHERE OFFENDER_BOOK_ID = :bookingId)
}

GET_ALLOCATIONS_FOR_KEY_WORKER {
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
  WHERE OKW.OFFICER_ID = :staffId
}

GET_ALLOCATION_DETAIL_FOR_KEY_WORKER {
  SELECT
    OKW.OFFENDER_BOOK_ID   BOOKING_ID,
    O.OFFENDER_ID_DISPLAY  OFFENDER_NO,
    OKW.OFFICER_ID         STAFF_ID,
    O.FIRST_NAME,
    O.LAST_NAME,
    OKW.ASSIGNED_TIME      ASSIGNED,
    OKW.AGY_LOC_ID         AGENCY_ID,
    OKW.ALLOC_TYPE         ALLOCATION_TYPE,
    AIL.DESCRIPTION        INTERNAL_LOCATION_DESC
  FROM OFFENDER_KEY_WORKERS OKW
    INNER JOIN OFFENDER_BOOKINGS OB         ON OB.OFFENDER_BOOK_ID = OKW.OFFENDER_BOOK_ID
    INNER JOIN OFFENDERS O                  ON OB.OFFENDER_ID = O.OFFENDER_ID
    LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
  WHERE OKW.OFFICER_ID = :staffId
    AND OB.ACTIVE_FLAG = 'Y'
    AND OKW.ACTIVE_FLAG = 'Y'
}

GET_UNALLOCATED_OFFENDERS {
  SELECT
    OB.OFFENDER_BOOK_ID                            BOOKING_ID,
    O.OFFENDER_ID_DISPLAY                          OFFENDER_NO,
    O.FIRST_NAME,
    CONCAT(O.MIDDLE_NAME,
           CASE
             WHEN O.MIDDLE_NAME_2 IS NOT NULL
             THEN CONCAT(' ', O.MIDDLE_NAME_2)
           ELSE ''
           END)                                    MIDDLE_NAMES,
    O.LAST_NAME,
    O.TITLE,
    O.SUFFIX,
    OB.AGY_LOC_ID                                  AGENCY_LOCATION_ID,
    AL.DESCRIPTION                                 AGENCY_LOCATION_DESC,
    OB.LIVING_UNIT_ID                              INTERNAL_LOCATION_ID,
    AIL.DESCRIPTION                                INTERNAL_LOCATION_DESC,
    OB.ACTIVE_FLAG                                 CURRENTLY_IN_PRISON
  FROM OFFENDERS O
    INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_ID = O.OFFENDER_ID AND OB.BOOKING_SEQ = 1
    INNER JOIN AGENCY_LOCATIONS AL ON AL.AGY_LOC_ID = OB.AGY_LOC_ID
    LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
  WHERE OB.ACTIVE_FLAG = 'Y'
    AND NOT EXISTS (SELECT 1
                    FROM OFFENDER_KEY_WORKERS OKW
                    WHERE OKW.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID AND OKW.ACTIVE_FLAG = 'Y')
    AND AL.AGY_LOC_ID IN (:agencyIds)
}

GET_AVAILABLE_KEY_WORKERS {
  SELECT SM.LAST_NAME,
         SM.FIRST_NAME,
         SM.STAFF_ID,
         (SELECT COUNT(1) FROM OFFENDER_KEY_WORKERS OKW
          WHERE OKW.ACTIVE_FLAG = 'Y'
            AND OKW.OFFICER_ID = SM.STAFF_ID
            AND OKW.AGY_LOC_ID IN (
              SELECT DISTINCT(SLR.CAL_AGY_LOC_ID)
              FROM STAFF_LOCATION_ROLES SLR
              WHERE SLR.SAC_STAFF_ID = SM.STAFF_ID
                AND SLR.POSITION = 'AO'
                AND SLR.ROLE = 'KW'
                AND TRUNC(SYSDATE) BETWEEN TRUNC(SLR.FROM_DATE) AND TRUNC(COALESCE(SLR.TO_DATE,SYSDATE)))) NUMBER_ALLOCATED
  FROM STAFF_LOCATION_ROLES SLR
    INNER JOIN STAFF_MEMBERS SM ON SM.STAFF_ID = SLR.SAC_STAFF_ID
      AND SM.STATUS = 'ACTIVE'
  WHERE SLR.CAL_AGY_LOC_ID = :agencyId
    AND SLR.POSITION = 'AO'
    AND SLR.ROLE = 'KW'
    AND TRUNC(SYSDATE) BETWEEN TRUNC(SLR.FROM_DATE) AND TRUNC(COALESCE(SLR.TO_DATE,SYSDATE))
    AND SLR.FROM_DATE = (SELECT MAX(SLR2.FROM_DATE)
                         FROM STAFF_LOCATION_ROLES SLR2
                         WHERE SLR2.SAC_STAFF_ID = SLR.SAC_STAFF_ID
                           AND SLR2.CAL_AGY_LOC_ID = SLR.CAL_AGY_LOC_ID
                           AND SLR2.POSITION = SLR.POSITION
                           AND SLR2.ROLE = SLR.ROLE)
}

CHECK_AVAILABLE_KEY_WORKER {
  SELECT DISTINCT(SLR.SAC_STAFF_ID)
  FROM STAFF_LOCATION_ROLES SLR
    INNER JOIN STAFF_MEMBERS SM ON SM.STAFF_ID = SLR.SAC_STAFF_ID
      AND SM.STATUS = 'ACTIVE'
    INNER JOIN OFFENDER_BOOKINGS OB ON OB.AGY_LOC_ID = SLR.CAL_AGY_LOC_ID
      AND OB.OFFENDER_BOOK_ID = :bookingId
      AND OB.ACTIVE_FLAG = 'Y'
  WHERE SLR.SAC_STAFF_ID = :staffId
    AND SLR.POSITION = 'AO'
    AND SLR.ROLE = 'KW'
    AND TRUNC(SYSDATE) BETWEEN TRUNC(SLR.FROM_DATE) AND TRUNC(COALESCE(SLR.TO_DATE,SYSDATE))
}

GET_ALLOCATED_OFFENDERS {
  SELECT
    OB.OFFENDER_BOOK_ID                            BOOKING_ID,
    O.OFFENDER_ID_DISPLAY                          OFFENDER_NO,
    O.FIRST_NAME,
    CONCAT(O.MIDDLE_NAME,
           CASE
             WHEN O.MIDDLE_NAME_2 IS NOT NULL
             THEN CONCAT(' ', O.MIDDLE_NAME_2)
           ELSE ''
           END)                                    MIDDLE_NAMES,
    O.LAST_NAME,
    OKW.OFFICER_ID                                 STAFF_ID,
    OKW.ASSIGNED_TIME                              ASSIGNED,
    OKW.AGY_LOC_ID                                 AGENCY_ID,
    OKW.ALLOC_REASON                               REASON,
    OKW.ALLOC_TYPE                                 ALLOCATION_TYPE,
    AIL.DESCRIPTION                                INTERNAL_LOCATION_DESC
  FROM OFFENDERS O
    INNER JOIN OFFENDER_BOOKINGS OB ON OB.OFFENDER_ID = O.OFFENDER_ID AND OB.BOOKING_SEQ = 1
    INNER JOIN OFFENDER_KEY_WORKERS OKW ON OKW.OFFENDER_BOOK_ID = OB.OFFENDER_BOOK_ID
    INNER JOIN AGENCY_LOCATIONS AL ON AL.AGY_LOC_ID = OB.AGY_LOC_ID
    LEFT JOIN AGENCY_INTERNAL_LOCATIONS AIL ON OB.LIVING_UNIT_ID = AIL.INTERNAL_LOCATION_ID
  WHERE OB.ACTIVE_FLAG = 'Y'
    AND OKW.ACTIVE_FLAG = 'Y'
    AND AL.AGY_LOC_ID IN (:agencyIds)
    AND OKW.ALLOC_TYPE = COALESCE(:allocType, OKW.ALLOC_TYPE)
    AND OKW.ASSIGNED_DATE >= TRUNC(COALESCE(:fromDate, OKW.ASSIGNED_DATE))
    AND TRUNC(OKW.ASSIGNED_DATE) <= COALESCE(:toDate, OKW.ASSIGNED_DATE)
}

GET_KEY_WORKER_DETAILS {
  SELECT SM.LAST_NAME,
         SM.FIRST_NAME,
         SM.STAFF_ID,
         (SELECT COUNT(1) FROM OFFENDER_KEY_WORKERS OKW
          WHERE OKW.ACTIVE_FLAG = 'Y'
            AND OKW.OFFICER_ID = SM.STAFF_ID
            AND OKW.AGY_LOC_ID IN (
              SELECT DISTINCT(SLR.CAL_AGY_LOC_ID)
              FROM STAFF_LOCATION_ROLES SLR
              WHERE SLR.SAC_STAFF_ID = SM.STAFF_ID
                AND SLR.POSITION = 'AO'
                AND SLR.ROLE = 'KW'
                AND TRUNC(SLR.FROM_DATE) <= SYSDATE
                AND TRUNC(SYSDATE) BETWEEN TRUNC(SLR.FROM_DATE) AND TRUNC(COALESCE(SLR.TO_DATE,SYSDATE)))) NUMBER_ALLOCATED
  FROM STAFF_MEMBERS SM
  WHERE SM.STAFF_ID = :staffId
    AND SM.STATUS = 'ACTIVE'
}

GET_KEY_WORKER_DETAILS_FOR_OFFENDER {
  SELECT
    SM.STAFF_ID,
    SM.LAST_NAME,
    SM.FIRST_NAME,
    (SELECT IA.INTERNET_ADDRESS
     FROM INTERNET_ADDRESSES IA
     WHERE IA.OWNER_ID = SM.STAFF_ID
           AND IA.OWNER_CLASS = 'STF'
           AND IA.INTERNET_ADDRESS_CLASS = 'EMAIL') EMAIL
 FROM OFFENDER_KEY_WORKERS OKW JOIN STAFF_MEMBERS SM ON SM.STAFF_ID = OKW.OFFICER_ID
 WHERE OKW.OFFENDER_BOOK_ID = :bookingId
       AND SM.STATUS = 'ACTIVE'
       AND OKW.ACTIVE_FLAG = 'Y'
       AND (OKW.EXPIRY_DATE is null OR OKW.EXPIRY_DATE >= :currentDate)
}

CHECK_KEY_WORKER_EXISTS {
  SELECT SM.STAFF_ID
  FROM STAFF_MEMBERS SM
  WHERE SM.STAFF_ID = :staffId
    AND SM.STATUS = 'ACTIVE'
}

GET_ALLOCATION_HISTORY_BY_AGENCY {
  SELECT
    O.OFFENDER_ID_DISPLAY OFFENDER_NO,
    OKW.OFFICER_ID        STAFF_ID,
    OKW.AGY_LOC_ID        AGENCY_ID,
    OKW.ASSIGNED_TIME     ASSIGNED,
    OKW.EXPIRY_DATE       EXPIRED,
    OKW.USER_ID           USER_ID,
    OKW.ACTIVE_FLAG       ACTIVE,
    OKW.CREATE_DATETIME   CREATED,
    OKW.CREATE_USER_ID    CREATED_BY,
    OKW.MODIFY_DATETIME   MODIFIED,
    OKW.MODIFY_USER_ID    MODIFIED_BY
  FROM OFFENDER_KEY_WORKERS OKW
    INNER JOIN OFFENDER_BOOKINGS OB         ON OB.OFFENDER_BOOK_ID = OKW.OFFENDER_BOOK_ID
    INNER JOIN OFFENDERS O                  ON OB.OFFENDER_ID = O.OFFENDER_ID
  WHERE OKW.AGY_LOC_ID = :agencyId
}
