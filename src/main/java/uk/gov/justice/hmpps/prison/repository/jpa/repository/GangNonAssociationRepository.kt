package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.GangNonAssociation
import uk.gov.justice.hmpps.prison.repository.jpa.model.GangNonAssociationId

@Repository
interface GangNonAssociationRepository : CrudRepository<GangNonAssociation, GangNonAssociationId> {

  fun findAllByPrimaryGangCodeOrSecondaryGangCode(primaryGangCode: String, secondaryGangCode: String): List<GangNonAssociation>

  /**
   * Returns all gang non-associations where the given gang code is either primary OR secondary in the non-association
   */
  fun findAllByGangCode(gangCode: String): List<GangNonAssociation> =
    findAllByPrimaryGangCodeOrSecondaryGangCode(gangCode, gangCode)
}
