package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime

interface RefreshMetadataRepository : CrudRepository<RefreshMetadata, LocalDateTime>

@Entity
@Table(schema = "api_proxy_user")
data class RefreshMetadata(
  @Id val lastRefreshDate: LocalDateTime,
  val sourceBackupDate: LocalDateTime,
)
