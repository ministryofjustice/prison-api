package uk.gov.justice.hmpps.prison.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PingEndpointTest {
  @Test
  fun ping() {
    assertThat(PingEndpoint().ping()).isEqualTo("pong")
  }
}
