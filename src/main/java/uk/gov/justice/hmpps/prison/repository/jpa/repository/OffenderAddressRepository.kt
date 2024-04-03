package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAddress

@Repository
interface OffenderAddressRepository : CrudRepository<OffenderAddress, Long> {
  @EntityGraph(type = EntityGraphType.FETCH, value = "address")
  fun findByOffenderId(offenderId: Long): List<OffenderAddress>
}
