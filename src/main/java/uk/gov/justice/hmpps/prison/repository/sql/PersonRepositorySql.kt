package uk.gov.justice.hmpps.prison.repository.sql

enum class PersonRepositorySql(val sql: String) {
  GET_PERSON_IDENTIFIERS(
    """
        select pi.IDENTIFIER_TYPE identifier_Type,
        pi.IDENTIFIER identifier_Value
        from PERSON_IDENTIFIERS pi
        where pi.PERSON_ID = :personId
        and pi.ID_SEQ = (
        select max(pi1.ID_SEQ)
        from PERSON_IDENTIFIERS pi1
        where pi1.PERSON_ID = :personId
        AND pi1.IDENTIFIER_TYPE = pi.IDENTIFIER_TYPE)
    """,
  ),
}
