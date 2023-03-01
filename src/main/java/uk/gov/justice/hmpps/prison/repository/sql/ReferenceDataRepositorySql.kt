package uk.gov.justice.hmpps.prison.repository.sql

enum class ReferenceDataRepositorySql(val sql: String) {
  FIND_REFERENCE_DOMAIN(
    """
        SELECT DOMAIN,
        DESCRIPTION,
        DOMAIN_STATUS,
        OWNER_CODE,
        APPLN_CODE,
        PARENT_DOMAIN
        FROM REFERENCE_DOMAINS
        WHERE DOMAIN = :domain
    """,
  ),

  FIND_REFERENCE_CODES_BY_DOMAIN(
    """
        SELECT CODE,
        DOMAIN,
        DESCRIPTION,
        PARENT_DOMAIN,
        PARENT_CODE,
        ACTIVE_FLAG,
        LIST_SEQ,
        SYSTEM_DATA_FLAG,
        EXPIRED_DATE
        FROM REFERENCE_CODES
                WHERE DOMAIN = :domain
    """,
  ),

  CREATE_REFERENCE_CODE(
    """
        INSERT INTO REFERENCE_CODES (DOMAIN, CODE, DESCRIPTION, PARENT_CODE, PARENT_DOMAIN, LIST_SEQ, ACTIVE_FLAG, SYSTEM_DATA_FLAG, EXPIRED_DATE)
        VALUES (:domain, :code, :description, :parentCode, :parentDomain, :listSeq, :activeFlag, :systemDataFlag, :expiredDate)
    """,
  ),

  UPDATE_REFERENCE_CODE(
    """
        UPDATE REFERENCE_CODES
                SET DESCRIPTION = :description,
        PARENT_CODE = :parentCode,
        PARENT_DOMAIN = :parentDomain,
        LIST_SEQ = :listSeq,
        ACTIVE_FLAG = :activeFlag,
        SYSTEM_DATA_FLAG = :systemDataFlag,
        EXPIRED_DATE = :expiredDate
                WHERE DOMAIN = :domain AND CODE = :code
    """,
  ),

  FIND_REFERENCE_CODES_BY_DOMAIN_HAVING_SUB_CODES(
    """
        SELECT RC1.CODE,
        RC1.DOMAIN,
        RC1.DESCRIPTION,
        RC1.PARENT_DOMAIN,
        RC1.PARENT_CODE,
        RC1.ACTIVE_FLAG,
        RC1.LIST_SEQ,
        RC1.SYSTEM_DATA_FLAG,
        RC1.EXPIRED_DATE
        FROM REFERENCE_CODES RC1
        WHERE RC1.DOMAIN = :domain
        AND EXISTS (SELECT 1 FROM REFERENCE_CODES RC2 WHERE RC2.PARENT_DOMAIN = RC1.DOMAIN AND RC2.PARENT_CODE = RC1.CODE)
    """,
  ),

  FIND_REFERENCE_CODES_BY_PARENT_DOMAIN_AND_CODE(
    """
        SELECT CODE,
        DOMAIN,
        DESCRIPTION,
        PARENT_DOMAIN,
        PARENT_CODE,
        ACTIVE_FLAG,
        LIST_SEQ,
        SYSTEM_DATA_FLAG,
        EXPIRED_DATE
        FROM REFERENCE_CODES
                WHERE PARENT_DOMAIN = :parentDomain
        AND PARENT_CODE IN (:parentCodes)
    """,
  ),

  FIND_REFERENCE_CODES_BY_DOMAIN_AND_CODE_WITH_CHILDREN(
    """
        SELECT RC.CODE,
        RC.DOMAIN,
        RC.DESCRIPTION,
        RC.PARENT_DOMAIN,
        RC.PARENT_CODE,
        RC.ACTIVE_FLAG,
        RC.LIST_SEQ,
        RC.SYSTEM_DATA_FLAG,
        RC.EXPIRED_DATE,
        RCSUB.CODE SUB_CODE,
        RCSUB.DOMAIN SUB_DOMAIN,
        RCSUB.DESCRIPTION SUB_DESCRIPTION,
        RCSUB.ACTIVE_FLAG SUB_ACTIVE_FLAG,
        RCSUB.LIST_SEQ SUB_LIST_SEQ,
        RCSUB.SYSTEM_DATA_FLAG SUB_SYSTEM_DATA_FLAG,
        RCSUB.EXPIRED_DATE SUB_EXPIRED_DATE
                FROM REFERENCE_CODES RC
        INNER JOIN REFERENCE_CODES RCSUB ON RCSUB.PARENT_CODE = RC.CODE AND RCSUB.PARENT_DOMAIN = RC.DOMAIN
                INNER JOIN REFERENCE_DOMAINS RD ON RCSUB.DOMAIN = RD.DOMAIN AND RC.DOMAIN = RD.PARENT_DOMAIN
                WHERE RC.DOMAIN = :domain
        AND RC.CODE = :code
    """,
  ),

  FIND_REFERENCE_CODE_BY_DOMAIN_AND_CODE(
    """
        SELECT RC.CODE,
        RC.DOMAIN,
        RC.DESCRIPTION,
        RC.PARENT_DOMAIN,
        RC.PARENT_CODE,
        RC.ACTIVE_FLAG,
        RC.LIST_SEQ,
        RC.SYSTEM_DATA_FLAG,
        RC.EXPIRED_DATE
        FROM REFERENCE_CODES RC
        INNER JOIN REFERENCE_DOMAINS RD ON RC.DOMAIN = RD.DOMAIN
                WHERE RC.DOMAIN = :domain
        AND RC.CODE = :code
    """,
  ),

  FIND_REFERENCE_CODE_BY_DOMAIN_AND_DESCRIPTION(
    """
        SELECT RC.CODE,
        RC.DOMAIN,
        RC.DESCRIPTION,
        RC.PARENT_DOMAIN,
        RC.PARENT_CODE,
        RC.ACTIVE_FLAG,
        RC.LIST_SEQ,
        RC.SYSTEM_DATA_FLAG,
        RC.EXPIRED_DATE
        FROM REFERENCE_CODES RC
        INNER JOIN REFERENCE_DOMAINS RD ON RC.DOMAIN = RD.DOMAIN
                WHERE RC.DOMAIN = :domain
        AND UPPER(RC.DESCRIPTION) like :description
    """,
  ),

  GET_AVAILABLE_EVENT_SUBTYPES(
    """
        --- For INSERT_APPOINTMENT
                SELECT internal_schedule_rsn_code AS code,
        description AS description
        FROM internal_schedule_reasons WHERE internal_schedule_type = :eventType AND active_flag = 'Y'
    """,
  ),
}
