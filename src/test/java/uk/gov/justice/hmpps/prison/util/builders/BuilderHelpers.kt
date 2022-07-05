package uk.gov.justice.hmpps.prison.util.builders

import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper

internal fun randomName(): String {
  // return random name between 3 and 10 characters long
  return (1..(3 + (Math.random() * 7).toInt())).map {
    ('a' + (Math.random() * 26).toInt())
  }.joinToString("")
}

data class BuilderContext(
  val webTestClient: WebTestClient,
  val jwtAuthenticationHelper: JwtAuthenticationHelper,
  val dataLoader: DataLoaderRepository
)
