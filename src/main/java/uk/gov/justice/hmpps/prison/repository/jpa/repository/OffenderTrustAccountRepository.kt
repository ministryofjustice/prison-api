package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccountId

interface OffenderTrustAccountRepository : JpaRepository<OffenderTrustAccount, OffenderTrustAccountId>
