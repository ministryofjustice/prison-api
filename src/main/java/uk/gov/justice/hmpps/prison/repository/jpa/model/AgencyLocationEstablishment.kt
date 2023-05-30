package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.JoinColumnOrFormula
import org.hibernate.annotations.JoinColumnsOrFormulas
import org.hibernate.annotations.JoinFormula
import java.io.Serializable

@Embeddable
data class AgencyLocationEstablishmentId(
  @JoinColumn(name = "AGY_LOC_ID", nullable = false)
  val agencyLocationId: String,

  @JoinColumn(name = "ESTABLISHMENT_TYPE", nullable = false)
  val establishmentTypeCode: String,
) : Serializable

@Entity
@Table(name = "AGY_LOC_ESTABLISHMENTS")
data class AgencyLocationEstablishment(
  @EmbeddedId
  val id: AgencyLocationEstablishmentId,

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "AGY_LOC_ID", updatable = false, insertable = false)
  val agencyLocation: AgencyLocation,

  @ManyToOne
  @JoinColumnsOrFormulas(
    value = [
      JoinColumnOrFormula(
        formula = JoinFormula(
          value = "'" + AgencyEstablishmentType.ESTABLISHMENT_TYPE + "'",
          referencedColumnName = "domain",
        ),
      ), JoinColumnOrFormula(column = JoinColumn(name = "ESTAB_TYPE", referencedColumnName = "code", nullable = true, updatable = false, insertable = false)),
    ],
  )
  val establishmentType: AgencyEstablishmentType,
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as AgencyLocationEstablishment
    return id.agencyLocationId == other.id.agencyLocationId &&
      id.establishmentTypeCode == other.id.establishmentTypeCode
  }

  override fun hashCode(): Int {
    return this.javaClass.hashCode()
  }
}
