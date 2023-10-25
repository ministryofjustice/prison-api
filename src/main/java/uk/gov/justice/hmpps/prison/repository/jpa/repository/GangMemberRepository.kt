package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.GangMember
import uk.gov.justice.hmpps.prison.repository.jpa.model.GangMemberId

@Repository
interface GangMemberRepository : CrudRepository<GangMember, GangMemberId> {
  fun findAllByBookingOffenderNomsId(nomsId: String): List<GangMember>
}
