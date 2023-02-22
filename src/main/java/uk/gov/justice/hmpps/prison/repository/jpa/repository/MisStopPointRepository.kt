package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface MisStopPointRepository : CrudRepository<MisStopPoint, LocalDateTime> {
  @Query(value = "SELECT min(stopPointDate) FROM MisStopPoint")
  fun findMinStopPointDate(): LocalDateTime?
}

@Entity
@Table(schema = "strmadmin")
data class MisStopPoint(@Id val stopPointDate: LocalDateTime)
