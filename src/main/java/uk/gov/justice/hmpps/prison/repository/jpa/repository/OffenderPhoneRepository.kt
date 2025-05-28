package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPhone
import java.util.Optional

interface OffenderPhoneRepository : CrudRepository<OffenderPhone, Long> {
  @Query("Select offPhone from OffenderPhone offPhone where offPhone.offender.rootOffender.nomsId = :nomsId and offPhone.phoneId = :phoneId")
  fun findByRootNomsIdAndPhoneId(nomsId: String, phoneId: Long?): Optional<OffenderPhone>
}
