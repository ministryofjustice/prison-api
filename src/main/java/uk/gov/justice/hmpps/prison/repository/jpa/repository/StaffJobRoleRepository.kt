package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffJobRole
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffJobRoleId

@Repository
interface StaffJobRoleRepository : JpaRepository<StaffJobRole, StaffJobRoleId> {
  fun findAllByAgencyIdAndStaffStaffId(agencyId: String, staffId: Long): List<StaffJobRole>
  fun findAllByAgencyIdAndStaffStaffIdAndRoleCode(agencyId: String, staffId: Long, roleCode: String): List<StaffJobRole>
}
