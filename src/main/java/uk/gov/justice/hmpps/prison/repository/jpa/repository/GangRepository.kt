package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gang

@Repository
interface GangRepository : JpaRepository<Gang, String>
