package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccount

interface OffenderSubAccountRepository : CrudRepository<OffenderSubAccount, OffenderSubAccount.Pk>
