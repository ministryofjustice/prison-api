package uk.gov.justice.hmpps.prison.repository

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

interface ConditionalSqlService {
  fun getWaitClause(lockWaitTime: Int): String
}

@Service
@Profile("nomis")
class OracleConditionalSqlService : ConditionalSqlService {
  override fun getWaitClause(lockWaitTime: Int) = " WAIT $lockWaitTime"
}

@Service
@Profile("!nomis")
class H2ConditionalSqlService : ConditionalSqlService {
  override fun getWaitClause(lockWaitTime: Int) = ""
}
