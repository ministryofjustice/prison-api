package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm.PK
import java.util.List

public interface SentenceTermRepository : CrudRepository<SentenceTerm, PK> {
  @EntityGraph(type = EntityGraphType.FETCH, value = "sentence-term-with-offender-sentence")
  fun findByOffenderBookingBookingId(bookingId: Long): List<SentenceTerm>
}
