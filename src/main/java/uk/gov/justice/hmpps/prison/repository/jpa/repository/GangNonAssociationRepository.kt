package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.GangNonAssociation
import uk.gov.justice.hmpps.prison.repository.jpa.model.GangNonAssociationId

@Repository
interface GangNonAssociationRepository : CrudRepository<GangNonAssociation, GangNonAssociationId>
