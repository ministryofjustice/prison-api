package net.syscon.elite.repository.jpa.model

import net.syscon.elite.repository.jpa.model.ReferenceCode.Companion.MLTY_BRANCH
import net.syscon.elite.repository.jpa.model.ReferenceCode.Companion.MLTY_DISCP
import net.syscon.elite.repository.jpa.model.ReferenceCode.Companion.MLTY_DSCHRG
import net.syscon.elite.repository.jpa.model.ReferenceCode.Companion.MLTY_RANK
import net.syscon.elite.repository.jpa.model.ReferenceCode.Companion.MLTY_WZONE
import java.io.Serializable
import javax.persistence.*

@Entity(name = "REFERENCE_CODES")
@DiscriminatorColumn(name = "DOMAIN")
@Inheritance
abstract class ReferenceCode(@Id open val domain: String, @Id open val code: String, open val description: String) : Serializable {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ReferenceCode) return false

    if (domain != other.domain) return false
    if (code != other.code) return false
    if (description != other.description) return false

    return true
  }

  override fun hashCode(): Int {
    var result = domain.hashCode()
    result = 31 * result + code.hashCode()
    result = 31 * result + description.hashCode()
    return result
  }

  companion object {
    const val MLTY_BRANCH = "MLTY_BRANCH"
    const val MLTY_WZONE = "MLTY_WZONE"
    const val MLTY_DSCHRG = "MLTY_DSCHRG"
    const val MLTY_DISCP = "MLTY_DISCP"
    const val MLTY_RANK = "MLTY_RANK"
  }
}

@Entity
@DiscriminatorValue(MLTY_BRANCH)
data class MilitaryBranch(override val code: String, override val description: String) : ReferenceCode(MLTY_BRANCH, code, description)

@Entity
@DiscriminatorValue(MLTY_WZONE)
data class WarZone(override val code: String, override val description: String) : ReferenceCode(MLTY_WZONE, code, description)

@Entity
@DiscriminatorValue(MLTY_DSCHRG)
data class MilitaryDischarge(override val code: String, override val description: String) : ReferenceCode(MLTY_DSCHRG, code, description)

@Entity
@DiscriminatorValue(MLTY_RANK)
data class MilitaryRank(override val code: String, override val description: String) : ReferenceCode(MLTY_RANK, code, description)

@Entity
@DiscriminatorValue(MLTY_DISCP)
data class DisciplinaryAction(override val code: String, override val description: String) : ReferenceCode(MLTY_DISCP, code, description)
