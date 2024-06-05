package uk.gov.justice.hmpps.prison.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

interface ConditionalSqlService {
  fun getWaitClause(): String
  fun getLockWaitTime(): Int
}

@Service
@Profile("nomis")
class OracleConditionalSqlService(@Value("\${lock.timeout:10}") private val lockWaitTimeSeconds: Int) : ConditionalSqlService {
  override fun getWaitClause() = " WAIT $lockWaitTimeSeconds"
  override fun getLockWaitTime() = lockWaitTimeSeconds
}

@Service
@Profile("!nomis")
class HsqlConditionalSqlService : ConditionalSqlService {
  override fun getWaitClause() = ""
  override fun getLockWaitTime() = 0
}
