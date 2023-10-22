package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.*
import org.hibernate.Hibernate
import org.hibernate.type.YesNoConverter
import uk.gov.justice.hmpps.prison.repository.jpa.helper.EntityOpen
import java.time.LocalDate

@Entity
@Table(name = "GANGS")
@EntityOpen
class Gang(

  @Id
  @Column(name = "GANG_CODE", nullable = false)
  val code: String,

  @Column(name = "GANG_NAME")
  val name: String,

  @Column(name = "LIST_SEQ")
  val sequence: Int = 99,

  @Column(name = "ACTIVE_FLAG", nullable = false)
  @Convert(converter = YesNoConverter::class)
  var active: Boolean = true,

  @Column(name = "EXPIRY_DATE")
  var expiryDate: LocalDate? = null,

  @JoinColumn(name = "PARENT_GANG_CODE")
  @ManyToOne(optional = true, fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  val parent: Gang? = null,

  ) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false

    other as Gang

    return code == other.code
  }

  override fun hashCode(): Int {
    return code.hashCode()
  }
}
