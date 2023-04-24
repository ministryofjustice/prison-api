package uk.gov.justice.hmpps.prison.repository.jpa.model
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@IdClass(PayProfileId::class)
@Table(name = "AGY_PRISONER_PAY_PROFILES")
data class AgyPrisonerPayProfile(
  @Id
  val agyLocId: String,
  @Id
  val startDate: LocalDate,
  val autoPayFlag: String,
  val endDate: LocalDate?,
  val payFrequency: Int,
  val weeklyAbsenceLimit: Int,
  val minHalfDayRate: BigDecimal,
  val maxHalfDayRate: BigDecimal,
  val maxPieceWorkRate: BigDecimal,
  val maxBonusRate: BigDecimal,
  val backdateDays: Int,
  val defaultPayBandCode: String?,
)

/*
 IdClass for composite key of (agyLocId, startDate)
 */
class PayProfileId : Serializable {
  private val agyLocId: String? = null
  private val startDate: LocalDate? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as PayProfileId
    if (agyLocId != other.agyLocId) return false
    if (startDate != other.startDate) return false
    return true
  }

  override fun hashCode(): Int {
    var result = agyLocId.hashCode()
    result = 31 * result + startDate.hashCode()
    return result
  }
}
