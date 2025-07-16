package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.hibernate.Hibernate
import uk.gov.justice.hmpps.prison.api.model.BlockAccessType

@Entity
@Table(name = "SPLASH_SCREENS")
data class SplashScreen(
  @SequenceGenerator(name = "SPLASH_ID", sequenceName = "SPLASH_ID", allocationSize = 1)
  @GeneratedValue(generator = "SPLASH_ID")
  @Id
  @Column(name = "SPLASH_ID", nullable = false)
  val splashId: Long? = null,

  @Column(name = "MODULE_NAME", nullable = false, unique = true)
  val moduleName: String,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "FUNCTION_NAME")
  val function: SplashScreenFunction? = null,

  @Column(name = "WARNING_TEXT")
  val warningText: String? = null,

  @Column(name = "BLOCKED_TEXT")
  val blockedText: String? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "BLOCK_ACCESS_CODE", nullable = false)
  val blockAccessType: BlockAccessType = BlockAccessType.NO,

  @OneToMany(mappedBy = "splashScreen", cascade = [jakarta.persistence.CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
  val conditions: MutableList<SplashCondition> = mutableListOf(),
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as SplashScreen
    return moduleName == other.moduleName
  }

  fun addCondition(conditionType: String, conditionValue: String, blockAccess: Boolean) {
    conditions.add(SplashCondition(splashScreen = this, conditionType = conditionType, conditionValue = conditionValue, blockAccess = blockAccess))
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = "SplashScreen(splashId=$splashId, moduleName=$moduleName)"
}
