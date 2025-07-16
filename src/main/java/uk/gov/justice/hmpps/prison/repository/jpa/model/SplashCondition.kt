package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.Hibernate
import org.hibernate.type.YesNoConverter

@Entity
@Table(
  name = "SPLASH_CONDITIONS",
  uniqueConstraints = [
    UniqueConstraint(
      name = "SPLASH_CONDITIONS_UK1",
      columnNames = ["SPLASH_ID", "CONDITION_TYPE", "CONDITION_VALUE"],
    ),
  ],
)
data class SplashCondition(
  @SequenceGenerator(name = "SPLASH_CONDITION_ID", sequenceName = "SPLASH_CONDITION_ID", allocationSize = 1)
  @GeneratedValue(generator = "SPLASH_CONDITION_ID")
  @Id
  @Column(name = "SPLASH_CONDITION_ID", nullable = false)
  val splashConditionId: Long? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "SPLASH_ID", nullable = false)
  val splashScreen: SplashScreen,

  @Column(name = "CONDITION_TYPE", nullable = false)
  val conditionType: String,

  @Column(name = "CONDITION_VALUE", nullable = false)
  val conditionValue: String,

  @Convert(converter = YesNoConverter::class)
  @Column(name = "BLOCK_ACCESS_YORN", nullable = false)
  val blockAccess: Boolean = false,

) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as SplashCondition
    return splashConditionId == other.splashConditionId
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = "SplashCondition(splashConditionId=$splashConditionId, conditionType=$conditionType, conditionValue=$conditionValue)"
}
