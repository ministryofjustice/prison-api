package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.Hibernate

@Entity
@Table(name = "SPLASH_SCREEN_FUNCS")
data class SplashScreenFunction(
  @Id
  @Column(name = "FUNCTION_NAME", nullable = false)
  val functionName: String,

  @Column(name = "DESCRIPTION", nullable = false)
  val description: String,

  @OneToMany(mappedBy = "function")
  val splashScreens: MutableList<SplashScreen> = mutableListOf(),
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as SplashScreenFunction
    return functionName == other.functionName
  }

  override fun hashCode(): Int = javaClass.hashCode()

  override fun toString(): String = "SplashScreenFunction(functionName=$functionName)"
}
