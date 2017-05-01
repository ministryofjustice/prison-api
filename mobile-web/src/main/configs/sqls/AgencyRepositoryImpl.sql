
FIND_AGENCY {
    SELECT ROWNUM AS ID,
           AGY_LOC_ID,
           DESCRIPTION,
           AGENCY_LOCATION_TYPE,
           to_char(SYSDATE, 'dd/mm/yyyy') last_update
      FROM AGENCY_LOCATIONS
     WHERE AGY_LOC_ID = :agencyId
           AND ACTIVE_FLAG = 'Y'
           AND AGY_LOC_ID NOT IN ('OUT', 'TRN')

}

FIND_ALL_AGENCIES {
    SELECT ROWNUM AS ID,
           AGY_LOC_ID,
           DESCRIPTION,
           AGENCY_LOCATION_TYPE
      FROM AGENCY_LOCATIONS
     WHERE ACTIVE_FLAG = 'Y'
           AND AGY_LOC_ID NOT IN ('OUT', 'TRN')
  ORDER BY AGY_LOC_ID
}

