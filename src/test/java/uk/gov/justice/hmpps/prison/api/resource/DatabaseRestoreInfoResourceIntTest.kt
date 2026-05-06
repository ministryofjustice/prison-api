package uk.gov.justice.hmpps.prison.api.resource

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.jdbc.datasource.lookup.DataSourceLookupFailureException
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest
import uk.gov.justice.hmpps.prison.repository.jpa.repository.MisStopPointRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.RefreshMetadata
import uk.gov.justice.hmpps.prison.repository.jpa.repository.RefreshMetadataRepository
import uk.gov.justice.hmpps.prison.service.BackupRestoreDetails
import java.time.LocalDateTime

class DatabaseRestoreInfoResourceIntTest : ResourceTest() {
  @MockitoSpyBean
  private lateinit var misStopPointRepository: MisStopPointRepository

  @MockitoSpyBean
  private lateinit var refreshMetadataRepository: RefreshMetadataRepository

  @Nested
  @DisplayName("GET /api/restore-info")
  inner class RestoreInfo {
    @Test
    fun `restore info`() {
      val stopPointDate: LocalDateTime = LocalDateTime.parse("2022-02-03T12:22:23")
      doReturn(stopPointDate).whenever(misStopPointRepository).findMinStopPointDate()

      webTestClient.get()
        .uri("/api/restore-info")
        .exchange()
        .expectStatus().isOk
        .expectBody<String>().isEqualTo("\"2022-02-02\"")
    }

    @Test
    fun `restore info no data`() {
      doReturn(null).whenever(misStopPointRepository).findMinStopPointDate()

      webTestClient.get()
        .uri("/api/restore-info")
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `restore info not found`() {
      webTestClient.get()
        .uri("/api/restore-info")
        .exchange()
        .expectStatus().isNotFound
    }
  }

  @Nested
  @DisplayName("GET /api/restore-details")
  inner class RestoreDetails {
    @BeforeEach
    internal fun setUp() {
      refreshMetadataRepository.deleteAll()
    }

    @Test
    fun `restore details`() {
      refreshMetadataRepository.save(
        RefreshMetadata(
          lastRefreshDate = LocalDateTime.parse("2026-03-02T12:13:14"),
          sourceBackupDate = LocalDateTime.parse("2026-03-02T05:06:07"),
        ),
      )
      webTestClient.get()
        .uri("/api/restore-details")
        .exchange()
        .expectStatus().isOk
        .expectBody<BackupRestoreDetails>().isEqualTo(
          BackupRestoreDetails(
            backup = LocalDateTime.parse("2026-03-02T05:06:07"),
            restore = LocalDateTime.parse("2026-03-02T12:13:14"),
          ),
        )
    }

    @Test
    fun `get the latest of multiple rows`() {
      refreshMetadataRepository.save(
        RefreshMetadata(
          lastRefreshDate = LocalDateTime.parse("2026-03-01T05:00:00"),
          sourceBackupDate = LocalDateTime.parse("2026-03-01T04:00:00"),
        ),
      )
      refreshMetadataRepository.save(
        RefreshMetadata(
          lastRefreshDate = LocalDateTime.parse("2026-03-05T00:00:00"),
          sourceBackupDate = LocalDateTime.parse("2026-03-04T00:00:00"),
        ),
      )
      webTestClient.get()
        .uri("/api/restore-details")
        .exchange()
        .expectStatus().isOk
        .expectBody<BackupRestoreDetails>().isEqualTo(
          BackupRestoreDetails(
            backup = LocalDateTime.parse("2026-03-04T00:00:00"),
            restore = LocalDateTime.parse("2026-03-05T00:00:00"),
          ),
        )
    }

    @Test
    fun `restore details no data`() {
      whenever(refreshMetadataRepository.findAll()).thenReturn(emptyList())

      webTestClient.get()
        .uri("/api/restore-details")
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `restore details not found when failure`() {
      whenever(refreshMetadataRepository.findAll()).thenThrow(DataSourceLookupFailureException("test"))

      webTestClient.get()
        .uri("/api/restore-details")
        .exchange()
        .expectStatus().isNotFound
    }
  }
}
