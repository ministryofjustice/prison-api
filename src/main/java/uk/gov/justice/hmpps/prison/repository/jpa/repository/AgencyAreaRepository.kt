package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyArea
import uk.gov.justice.hmpps.prison.repository.jpa.model.Area
import uk.gov.justice.hmpps.prison.repository.jpa.model.Region

@Repository
interface AgencyAreaRepository :
  JpaRepository<AgencyArea, String>,
  JpaSpecificationExecutor<AgencyArea>

@Repository
interface AreaRepository :
  JpaRepository<Area, String>,
  JpaSpecificationExecutor<Area> {
  @EntityGraph(value = "area-entity-graph", type = EntityGraph.EntityGraphType.LOAD)
  override fun findAll(spec: Specification<Area>?): List<Area>

  @EntityGraph(value = "area-entity-graph", type = EntityGraph.EntityGraphType.LOAD)
  override fun findAll(): List<Area>
}

@Repository
interface RegionRepository :
  JpaRepository<Region, String>,
  JpaSpecificationExecutor<Region>
