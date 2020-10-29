package uk.gov.justice.hmpps.prison.repository.sql

enum class IdempotentRequestRepositorySql(val sql: String) {
    GET_IDEMPOTENT_REQUEST_CONTROL("""
        SELECT CORRELATION_ID, RESPONSE, RESPONSE_STATUS, CREATE_DATETIME
        FROM API2_OWNER.IDEMPOTENT_REQUEST_CONTROL
        WHERE CORRELATION_ID = :correlationId
    """),

    SET_IDEMPOTENT_REQUEST_CONTROL("""
        INSERT INTO API2_OWNER.IDEMPOTENT_REQUEST_CONTROL (CORRELATION_ID, CREATE_DATETIME)
        VALUES (:correlationId, :createDatetime)
    """),

    UPDATE_IDEMPOTENT_REQUEST_CONTROL_RESPONSE("""
        UPDATE API2_OWNER.IDEMPOTENT_REQUEST_CONTROL
                SET RESPONSE = :response,
        RESPONSE_STATUS = :responseStatus
                WHERE CORRELATION_ID = :correlationId
    """)
}
