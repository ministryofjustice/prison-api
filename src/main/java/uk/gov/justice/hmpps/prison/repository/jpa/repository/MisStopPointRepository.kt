package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

interface MisStopPointRepository : CrudRepository<MisStopPoint, LocalDateTime> {
  @Query(value = "SELECT min(stopPointDate) FROM MisStopPoint")
  fun findMinStopPointDate(): LocalDateTime?
}

@Entity
@Table(schema = "strmadmin")
data class MisStopPoint(@Id val stopPointDate: LocalDateTime)
