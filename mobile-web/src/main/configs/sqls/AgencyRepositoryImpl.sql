GET_AGENCIES {
  SELECT DISTINCT AGY_LOC_ID,
         DESCRIPTION,
         AGENCY_LOCATION_TYPE
  FROM AGENCY_LOCATIONS
  WHERE ACTIVE_FLAG = 'Y'
    AND AGY_LOC_ID NOT IN ('OUT','TRN')
}

FIND_AGENCIES_BY_USERNAME {
  SELECT DISTINCT A.AGY_LOC_ID,
         A.DESCRIPTION,
         A.AGENCY_LOCATION_TYPE
  FROM AGENCY_LOCATIONS A
    INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON A.AGY_LOC_ID = C.AGY_LOC_ID
  WHERE A.ACTIVE_FLAG = 'Y'
    AND A.AGY_LOC_ID NOT IN ('OUT','TRN')
    AND C.CASELOAD_ID IN
      (SELECT SAC.CASELOAD_ID
       FROM STAFF_ACCESSIBLE_CASELOADS SAC
         INNER JOIN STAFF_MEMBERS SM ON SM.STAFF_ID = SAC.STAFF_ID
           AND SM.PERSONNEL_TYPE = 'STAFF' AND SM.USER_ID = :username)
}

GET_AGENCY {
    SELECT A.AGY_LOC_ID,
           A.DESCRIPTION,
           A.AGENCY_LOCATION_TYPE
    FROM AGENCY_LOCATIONS A
    WHERE A.ACTIVE_FLAG = 'Y'
      AND A.AGY_LOC_ID = :agencyId
}

GET_AVAILABLE_LOCATIONS {
 --- For INSERT_APPOINTMENT and other events
SELECT ail.internal_location_id, -- this is the actual id
       ail.description,
       ilul.usage_location_id, 
       ilul.usage_location_type
FROM int_loc_usage_locations ilul 
  INNER JOIN internal_location_usages ilu ON ilu.internal_location_usage_id = ilul.internal_location_usage_id
  INNER JOIN agency_internal_locations ail ON ail.internal_location_id = ilul.internal_location_id
WHERE ilu.internal_location_usage = :eventType
  AND ilu.agy_loc_id = :agencyId  
  AND ail.active_flag = 'Y'
  AND ail.deactivate_date IS NULL
  AND ail.internal_location_code <> 'RTU'
  AND NOT EXISTS (SELECT 1
                 FROM int_loc_usage_locations
                 WHERE parent_usage_location_id = ilul.usage_location_id)
ORDER BY ail.description
}