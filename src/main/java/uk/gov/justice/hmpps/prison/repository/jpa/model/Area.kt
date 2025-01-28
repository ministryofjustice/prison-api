package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.NamedAttributeNode
import jakarta.persistence.NamedEntityGraph
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.JoinColumnOrFormula
import org.hibernate.annotations.JoinColumnsOrFormulas
import org.hibernate.annotations.JoinFormula
import org.hibernate.type.YesNoConverter
import uk.gov.justice.hmpps.prison.api.model.RefCodeAndDescription

@Entity
@Table(name = "AREAS")
@DiscriminatorColumn(name = "AREA_CLASS")
@Inheritance
abstract class AgencyArea(

  @Id
  @Column(name = "AREA_CODE", nullable = false)
  open val code: String,

  @Column(name = "DESCRIPTION", nullable = false)
  open val description: String,

  @Column(name = "ACTIVE_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  open val active: Boolean = true,
) {

  fun toDto() = RefCodeAndDescription(code, description)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as AgencyArea
    return code == other.code
  }

  override fun hashCode(): Int = javaClass.hashCode()
  override fun toString(): String = "${javaClass.simpleName} (code='$code', description='$description')"
}

@Entity
@DiscriminatorValue(Area.TYPE)
@NamedEntityGraph(
  name = "area-entity-graph",
  attributeNodes = [
    NamedAttributeNode("region"),
    NamedAttributeNode("areaType"),
  ],
)
open class Area(
  @JoinColumn(name = "PARENT_AREA_CODE")
  @ManyToOne(optional = false, fetch = LAZY)
  open val region: Region,

  code: String,
  description: String,
  active: Boolean = true,
  @ManyToOne
  @JoinColumnsOrFormulas(
    value = [
      JoinColumnOrFormula(
        formula = JoinFormula(
          value = "'" + AreaType.AREA_TYPE + "'",
          referencedColumnName = "domain",
        ),
      ), JoinColumnOrFormula(column = JoinColumn(name = "AREA_TYPE", referencedColumnName = "code")),
    ],
  )
  open val areaType: AreaType? = null,
) : AgencyArea(
  code = code,
  description = description,
  active = active,
) {
  companion object {
    const val TYPE = "AREA"
  }
}

@Entity
@DiscriminatorValue(Region.TYPE)
class Region(
  code: String,
  description: String,
  active: Boolean = true,
) : AgencyArea(
  code = code,
  description = description,
  active = active,
) {
  companion object {
    const val TYPE = "REGION"
  }
}

@Entity
@DiscriminatorValue(SubArea.TYPE)
class SubArea(
  code: String,
  description: String,
  active: Boolean = true,
  areaType: AreaType,
  region: Region,
) : Area(
  code = code,
  description = description,
  active = active,
  region = region,
  areaType = areaType,
) {
  companion object {
    const val TYPE = "SUB_AREA"
  }
}
