package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.JoinColumnOrFormula
import org.hibernate.annotations.JoinColumnsOrFormulas
import org.hibernate.annotations.JoinFormula
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.io.Serializable
import java.time.LocalDate

@Entity
@Table(name = "STAFF_LOCATION_ROLES")
@EntityOpen
@IdClass(StaffJobRoleId::class)
class StaffJobRole(

  @Id
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "SAC_STAFF_ID", nullable = false)
  val staff: Staff,

  @Id
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "CAL_AGY_LOC_ID", nullable = false)
  val agency: AgencyLocation,

  @Id
  @Column(name = "FROM_DATE", nullable = false)
  val fromDate: LocalDate,

  @Id
  @Column(name = "POSITION", nullable = false)
  val position: String,

  @Id
  @ManyToOne(optional = false)
  @JoinColumnsOrFormulas(
    value = [
      JoinColumnOrFormula(
        formula = JoinFormula(
          value = "'" + StaffRole.DOMAIN + "'",
          referencedColumnName = "domain",
        ),
      ),
      JoinColumnOrFormula(column = JoinColumn(name = "ROLE", referencedColumnName = "code")),
    ],
  )
  val role: StaffRole,

  @Column(name = "TO_DATE")
  val toDate: LocalDate? = null,
) {

  fun isWithinRange(testDate: LocalDate): Boolean {
    return testDate >= fromDate && (toDate == null || testDate < toDate)
  }
}

class StaffJobRoleId(
  val staff: Staff? = null,
  val agency: AgencyLocation,
  val fromDate: LocalDate,
  val position: String,
  val role: StaffRole,
) : Serializable {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as StaffJobRoleId

    if (staff != other.staff) return false
    if (agency != other.agency) return false
    if (fromDate != other.fromDate) return false
    if (position != other.position) return false
    if (role != other.role) return false

    return true
  }

  override fun hashCode(): Int {
    var result = staff.hashCode()
    result = 31 * result + agency.hashCode()
    result = 31 * result + fromDate.hashCode()
    result = 31 * result + position.hashCode()
    result = 31 * result + role.hashCode()
    return result
  }
}
