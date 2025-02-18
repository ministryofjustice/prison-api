package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.io.Serializable

@Embeddable
data class OffenderPhysicalAttributeId(
  @ManyToOne(optional = false, fetch = LAZY)
  @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
  val offenderBooking: OffenderBooking,

  @Column(name = "ATTRIBUTE_SEQ", nullable = false)
  val sequence: Long,
) : Serializable

@Entity
@Table(name = "OFFENDER_PHYSICAL_ATTRIBUTES")
@EntityOpen
internal class OffenderPhysicalAttributes(
  @EmbeddedId
  val id: OffenderPhysicalAttributeId,

  @Column(name = "HEIGHT_FT")
  var heightFeet: Int? = null,

  @Column(name = "HEIGHT_IN")
  var heightInches: Int? = null,

  @Column(name = "HEIGHT_CM")
  var heightCentimetres: Int? = null,

  @Column(name = "WEIGHT_LBS")
  var weightPounds: Int? = null,

  @Column(name = "WEIGHT_KG")
  var weightKgs: Int? = null,
) : AuditableEntity()
