FIND_LOCATIONS_BY_AGENCY_ID {
	 -- Internal Agency Locations (PILOT VERSION)
	 --  This clause is for the pilot only
	-- AND A.INTERNAL_LOCATION_TYPE != 'BLOCK' --  This clause is for the pilot only
	 SELECT A.INTERNAL_LOCATION_ID,
	  		  A.AGY_LOC_ID,
	  		  A.INTERNAL_LOCATION_TYPE,
          COALESCE(A.USER_DESC, A.DESCRIPTION) AS DESCRIPTION,
	  		  A.PARENT_INTERNAL_LOCATION_ID,
	  		  A.NO_OF_OCCUPANT
	   FROM	AGENCY_INTERNAL_LOCATIONS A JOIN CASELOAD_AGENCY_LOCATIONS C
			 ON A.AGY_LOC_ID = C.AGY_LOC_ID
	  WHERE	A.ACTIVE_FLAG = 'Y'
			    AND A.AGY_LOC_ID = :agencyId
          AND C.CASELOAD_ID = :caseLoadId
}

FIND_ALL_LOCATIONS {
  -- Internal Agency Locations (PILOT VERSION)
  SELECT A.INTERNAL_LOCATION_ID,
         A.INTERNAL_LOCATION_TYPE,
         COALESCE(A.USER_DESC, A.DESCRIPTION) AS DESCRIPTION,
         A.PARENT_INTERNAL_LOCATION_ID,
         A.NO_OF_OCCUPANT,
         A.AGY_LOC_ID
  FROM AGENCY_INTERNAL_LOCATIONS A
    INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON A.AGY_LOC_ID = C.AGY_LOC_ID
    INNER JOIN STAFF_MEMBERS SM ON C.CASELOAD_ID = SM.ASSIGNED_CASELOAD_ID
  WHERE	A.ACTIVE_FLAG = 'Y' AND SM.PERSONNEL_TYPE = 'STAFF' AND SM.USER_ID = :username
}

FIND_LOCATION {
  -- Internal Agency Location (PILOT VERSION)
  SELECT A.INTERNAL_LOCATION_ID,
         A.INTERNAL_LOCATION_TYPE,
         COALESCE(A.USER_DESC, A.DESCRIPTION) AS DESCRIPTION,
         A.PARENT_INTERNAL_LOCATION_ID,
         A.NO_OF_OCCUPANT,
         A.AGY_LOC_ID
  FROM AGENCY_INTERNAL_LOCATIONS A
    INNER JOIN CASELOAD_AGENCY_LOCATIONS C ON A.AGY_LOC_ID = C.AGY_LOC_ID
    INNER JOIN STAFF_MEMBERS SM ON C.CASELOAD_ID = SM.ASSIGNED_CASELOAD_ID
  WHERE A.INTERNAL_LOCATION_ID = :locationId AND A.ACTIVE_FLAG = 'Y' AND SM.USER_ID = :username
}

FIND_LOCATIONS_BY_AGENCY {
    SELECT A.INTERNAL_LOCATION_ID,
           A.AGY_LOC_ID,
           A.INTERNAL_LOCATION_TYPE,
           COALESCE(A.USER_DESC, A.DESCRIPTION) AS DESCRIPTION,
           A.PARENT_INTERNAL_LOCATION_ID,
           A.NO_OF_OCCUPANT
    FROM AGENCY_INTERNAL_LOCATIONS A
    WHERE A.ACTIVE_FLAG = 'Y'
    AND A.AGY_LOC_ID = :agencyId
}

FIND_LOCATIONS_BY_AGENCY_AND_TYPE {
  SELECT A.INTERNAL_LOCATION_ID,
    A.AGY_LOC_ID,
    A.INTERNAL_LOCATION_TYPE,
    A.DESCRIPTION as LOCATION_PREFIX,
    COALESCE(A.USER_DESC, A.DESCRIPTION) AS DESCRIPTION,
    A.PARENT_INTERNAL_LOCATION_ID,
    A.NO_OF_OCCUPANT,
    LIST_SEQ
  FROM AGENCY_INTERNAL_LOCATIONS A
  WHERE A.ACTIVE_FLAG = 'Y'
        AND A.AGY_LOC_ID = :agencyId
        AND A.INTERNAL_LOCATION_TYPE = :locationType
  order by A.DESCRIPTION
}

GET_CELLS {
  SELECT DESCRIPTION 
  FROM AGENCY_INTERNAL_LOCATIONS
  WHERE INTERNAL_LOCATION_TYPE = 'CELL'
    AND AGY_LOC_ID = :agencyId
}
