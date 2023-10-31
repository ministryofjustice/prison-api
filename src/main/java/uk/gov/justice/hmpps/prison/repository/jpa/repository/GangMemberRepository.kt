package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.GangMember
import uk.gov.justice.hmpps.prison.repository.jpa.model.GangMemberId

@Repository
interface GangMemberRepository : JpaRepository<GangMember, GangMemberId> {
  fun findAllByBookingOffenderNomsIdAndGangActiveIsTrue(nomsId: String): List<GangMember>
}
