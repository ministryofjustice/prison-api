package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderProfileDetailsBuilder
import uk.gov.justice.hmpps.prison.util.builders.ProfileType

@WithMockUser
class OffenderResourceNewBookingTest : ResourceTest() {
  @Autowired
  private lateinit var dataLoader: DataLoaderRepository

  @Nested
  @DisplayName("POST /offenders/{offenderNo}/booking")
  inner class NewBooking {
    @Nested
    @DisplayName("when offender has no previous bookings")
    inner class NewOffender {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        OffenderBuilder(
          bookingBuilders = arrayOf()
        ).save(
          webTestClient = webTestClient,
          jwtAuthenticationHelper = jwtAuthenticationHelper,
          dataLoader = dataLoader
        ).also {
          offenderNo = it.offenderNo
        }
      }

      @Test
      internal fun `will create a new booking and mark as youth offender when booked in as a YOUTH`() {
        // Given offender has no existing booking record
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(false)
          .jsonPath("inOutStatus").doesNotExist()
          .jsonPath("status").doesNotExist()
          .jsonPath("bookingId").doesNotExist()
          .jsonPath("bookingNo").doesNotExist()

        // when booking is created
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(true)
          .jsonPath("bookingId").exists()
          .jsonPath("bookingNo").exists()

        // then we have an active booking
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(true)
          .jsonPath("profileInformation[0].type").isEqualTo("YOUTH")
          .jsonPath("profileInformation[0].resultValue").isEqualTo("Yes")
          .jsonPath("inOutStatus").isEqualTo("IN")
          .jsonPath("status").isEqualTo("ACTIVE IN")
          .jsonPath("lastMovementTypeCode").isEqualTo("ADM")
          .jsonPath("lastMovementReasonCode").isEqualTo("24")
          .jsonPath("agencyId").isEqualTo("SYI")
          .jsonPath("assignedLivingUnit.agencyId").isEqualTo("SYI")
          .jsonPath("assignedLivingUnit.description").isEqualTo("A-1-1")
          .jsonPath("imprisonmentStatus").isEqualTo("CUR_ORA")
          .jsonPath("bookingId").exists()
          .jsonPath("bookingNo").exists()
      }

      @Test
      @Disabled("TODO: fix this test")
      internal fun `will create a new booking and mark as NOT a youth offender when booked in as an ADULT`() {
        // Given offender has no existing booking record
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(false)
          .jsonPath("inOutStatus").doesNotExist()
          .jsonPath("status").doesNotExist()
          .jsonPath("bookingId").doesNotExist()
          .jsonPath("bookingNo").doesNotExist()

        // when booking is created
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "false", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()

        // then we have an active booking
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(true)
          .jsonPath("profileInformation[0].type").isEqualTo("YOUTH")
          .jsonPath("profileInformation[0].resultValue").isEqualTo("No")
      }
    }

    @Nested
    @DisplayName("when offender was a YOUTH with previous bookings")
    inner class PreviouslyYouthOffender {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        OffenderBuilder().withBooking(
          OffenderBookingBuilder(
            prisonId = "LEI",
            released = true,
          ).withProfileDetails(OffenderProfileDetailsBuilder("Y", ProfileType.YOUTH))
        ).save(
          webTestClient = webTestClient,
          jwtAuthenticationHelper = jwtAuthenticationHelper,
          dataLoader = dataLoader
        ).also {
          offenderNo = it.offenderNo
        }
      }

      @Test
      internal fun `will create a new booking and mark as youth offender when booked in as a YOUTH`() {
        // Given offender has previous inactive booking record
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(false)
          .jsonPath("inOutStatus").isEqualTo("OUT")

        // when booking is created
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(true)
          .jsonPath("bookingId").exists()
          .jsonPath("bookingNo").exists()

        // then we have an active booking
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(true)
          .jsonPath("profileInformation[0].type").isEqualTo("YOUTH")
          .jsonPath("profileInformation[0].resultValue").isEqualTo("Yes")
          .jsonPath("inOutStatus").isEqualTo("IN")
          .jsonPath("status").isEqualTo("ACTIVE IN")
          .jsonPath("lastMovementTypeCode").isEqualTo("ADM")
          .jsonPath("lastMovementReasonCode").isEqualTo("24")
          .jsonPath("agencyId").isEqualTo("SYI")
          .jsonPath("assignedLivingUnit.agencyId").isEqualTo("SYI")
          .jsonPath("assignedLivingUnit.description").isEqualTo("A-1-1")
          .jsonPath("imprisonmentStatus").isEqualTo("CUR_ORA")
          .jsonPath("bookingId").exists()
          .jsonPath("bookingNo").exists()
      }

      @Test
      @Disabled("This is the defect that needs fixing")
      internal fun `will create a new booking and mark as NOT a youth offender when booked in as an ADULT`() {
        // Given offender has previous inactive booking record
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(false)
          .jsonPath("inOutStatus").isEqualTo("OUT")

        // when booking is created
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "false", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()

        // then we have an active booking
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(true)
          .jsonPath("profileInformation[0].type").isEqualTo("YOUTH")
          .jsonPath("profileInformation[0].resultValue").isEqualTo("No")
      }
    }
    @Nested
    @DisplayName("when offender was not a YOUTH with previous bookings")
    inner class PreviouslyAdultOffender {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        OffenderBuilder().withBooking(
          OffenderBookingBuilder(
            prisonId = "LEI",
            released = true,
          ).withProfileDetails(OffenderProfileDetailsBuilder("N", ProfileType.YOUTH))
        ).save(
          webTestClient = webTestClient,
          jwtAuthenticationHelper = jwtAuthenticationHelper,
          dataLoader = dataLoader
        ).also {
          offenderNo = it.offenderNo
        }
      }

      @Test
      internal fun `will create a new booking and mark as youth offender when booked in as a YOUTH`() {
        // I suspect this scenario must be rare since it implies one of the bookings was incorrect given people can not get younger
        // Given offender has previous inactive booking record
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(false)
          .jsonPath("inOutStatus").isEqualTo("OUT")

        // when booking is created
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "true", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(true)
          .jsonPath("bookingId").exists()
          .jsonPath("bookingNo").exists()

        // then we have an active booking
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(true)
          .jsonPath("profileInformation[0].type").isEqualTo("YOUTH")
          .jsonPath("profileInformation[0].resultValue").isEqualTo("Yes")
          .jsonPath("inOutStatus").isEqualTo("IN")
          .jsonPath("status").isEqualTo("ACTIVE IN")
          .jsonPath("lastMovementTypeCode").isEqualTo("ADM")
          .jsonPath("lastMovementReasonCode").isEqualTo("24")
          .jsonPath("agencyId").isEqualTo("SYI")
          .jsonPath("assignedLivingUnit.agencyId").isEqualTo("SYI")
          .jsonPath("assignedLivingUnit.description").isEqualTo("A-1-1")
          .jsonPath("imprisonmentStatus").isEqualTo("CUR_ORA")
          .jsonPath("bookingId").exists()
          .jsonPath("bookingNo").exists()
      }

      @Test
      internal fun `will create a new booking and mark as NOT a youth offender when booked in as an ADULT`() {
        // Given offender has previous inactive booking record
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(false)
          .jsonPath("inOutStatus").isEqualTo("OUT")

        // when booking is created
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "youthOffender": "false", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()

        // then we have an active booking
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER")
            )
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(true)
          .jsonPath("profileInformation[0].type").isEqualTo("YOUTH")
          .jsonPath("profileInformation[0].resultValue").isEqualTo("No")
      }
    }
  }
}
