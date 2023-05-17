package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.Hibernate

@Entity
@Table(name = "EXTERNAL_SERVICES")
data class ExternalServiceEntity(
  @Id
  @Column(nullable = false)
  val serviceName: String,

  @Column
  val description: String,

  @OneToMany(mappedBy = "id.externalServiceEntity", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
  val serviceAgencySwitches: MutableList<ServiceAgencySwitch> = mutableListOf(),
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as ExternalServiceEntity
    return serviceName == other.serviceName
  }

  override fun hashCode(): Int {
    return this.javaClass.hashCode()
  }
}
