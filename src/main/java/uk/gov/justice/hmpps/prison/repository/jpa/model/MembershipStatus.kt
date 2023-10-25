package uk.gov.justice.hmpps.prison.repository.jpa.model

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import lombok.NoArgsConstructor

@Entity
@DiscriminatorValue(MembershipStatus.DOMAIN)
@NoArgsConstructor
class MembershipStatus(code: String?, description: String?) :
  ReferenceCode(DOMAIN, code, description) {

  companion object {
    const val DOMAIN = "GANG_MBR_STS"
    fun pk(code: String?): Pk {
      return Pk(DOMAIN, code)
    }
  }
}
