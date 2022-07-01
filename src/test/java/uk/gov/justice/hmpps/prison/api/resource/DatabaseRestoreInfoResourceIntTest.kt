package uk.gov.justice.hmpps.prison.api.resource

import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.SpyBean
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest
import uk.gov.justice.hmpps.prison.repository.jpa.repository.MisStopPointRepository
import java.time.LocalDateTime

class DatabaseRestoreInfoResourceIntTest : ResourceTest() {
  @SpyBean
  private lateinit var misStopPointRepository: MisStopPointRepository

  @Test
  fun `restore info`() {
    val stopPointDate: LocalDateTime = LocalDateTime.parse("2022-02-03T12:22:23")
    doReturn(stopPointDate).whenever(misStopPointRepository).findMinStopPointDate()

    webTestClient.get()
      .uri("/api/restore-info")
      .exchange()
      .expectStatus().isOk
      .expectBody(String::class.java).isEqualTo("\"2022-02-02\"")
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
