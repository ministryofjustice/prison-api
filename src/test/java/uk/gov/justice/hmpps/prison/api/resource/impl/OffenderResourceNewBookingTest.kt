package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.CaseNote
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.PrivilegeSummary
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderProfileDetailsBuilder
import uk.gov.justice.hmpps.prison.util.builders.ProfileType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@WithMockUser
class OffenderResourceNewBookingTest : ResourceTest() {
  @Autowired
  private lateinit var dataLoader: DataLoaderRepository

  @Autowired
  private lateinit var externalMovementRepository: ExternalMovementRepository

  @Autowired
  private lateinit var bedAssignmentHistoriesRepository: BedAssignmentHistoriesRepository

  @Nested
  @DisplayName("POST /offenders/{offenderNo}/booking")
  inner class NewBooking {

    @Nested
    @DisplayName("when new booking is rejected")
    inner class Failure {
      @Test
      internal fun `404 when offender not found`() {
        val offenderNo = "Z9999ZZ"

        // Given offender does not exist
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
          .expectStatus().isNotFound

        // when booking is created then the request is rejected
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
          .expectStatus().isNotFound
      }

      @Test
      internal fun `400 when offender is still active, for instance are in another prison `() {
        val offenderNo = createActiveBooking()

        // when booking is created then the request is rejected
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
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Prisoner is currently active")
      }

      @Test
      internal fun `400 when offender is inactive but not OUT, for instance currently being transferred`() {
        val offenderNo = createActiveBooking(prisonId = "MDI").also { transferOut(it) }

        // when booking is created then the request is rejected
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
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Prisoner is not currently OUT")
      }

      @Test
      internal fun `404 when trying to book in from a location that doesn't exist`() {
        val offenderNo = createInactiveBooking()

        // when booking is created then the request is rejected
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
               "fromLocationId": "ZZZ", 
               "movementReasonCode": "24", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("ZZZ is not a valid from location")
      }

      @Test
      internal fun `404 when trying to book in with an imprisonment status that doesn't exist`() {
        val offenderNo = createInactiveBooking()

        // when booking is created then the request is rejected
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
               "imprisonmentStatus": "ZZZ", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("No imprisonment status ZZZ found")
      }

      @Test
      internal fun `404 when trying to book in to a prison that doesn't exist`() {
        val offenderNo = createInactiveBooking()

        // when booking is created then the request is rejected
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
               "prisonId": "ZZZ", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("ZZZ prison not found")
      }

      @Test
      internal fun `404 when trying to book in to a cell that doesn't exist`() {
        val offenderNo = createInactiveBooking()

        // when booking is created then the request is rejected
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
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-BANANAS"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("SYI-BANANAS cell location not found")
      }

      @Test
      internal fun `409 when trying to book in to a cell that is full`() {
        val offenderNo = createInactiveBooking()

        // when booking is created then the request is rejected
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
               "prisonId": "MDI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "MDI-FULL"     
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isEqualTo(409)
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("The cell MDI-FULL does not have any available capacity")
      }

      @Test
      internal fun `400 when trying to book in prisoner in the future (and return a slightly inaccurate message)`() {
        val offenderNo = createInactiveBooking()

        // when booking is created then the request is rejected
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
               "prisonId": "MDI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "imprisonmentStatus": "CUR_ORA",
               "bookingInTime": "${
            LocalDateTime.now().plusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }"
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Transfer cannot be done in the future")
      }

      @Test
      internal fun `400 when trying to book in prisoner before they were released from previous prison`() {
        val offenderNo = createInactiveBooking()

        // when booking is created then the request is rejected
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
               "prisonId": "MDI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "imprisonmentStatus": "CUR_ORA",
               "bookingInTime": "${
            LocalDateTime.now().minusMonths(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }"
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Movement cannot be before the previous active movement")
      }
    }

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
    @DisplayName("when creating a booking it")
    inner class SideEffects {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        offenderNo = createInactiveBooking(iepLevel = "ENH")
      }

      @Test
      internal fun `will by default place prisoner in reception`() {
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
               "prisonId": "MDI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "imprisonmentStatus": "CUR_ORA" 
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk

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
          .jsonPath("assignedLivingUnit.agencyId").isEqualTo("MDI")
          .jsonPath("assignedLivingUnit.description").isEqualTo("RECP")
      }

      @Test
      internal fun `will create a new movement for the new booking`() {

        val bookingId = webTestClient.post()
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
               "prisonId": "MDI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "imprisonmentStatus": "CUR_ORA" 
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .returnResult(InmateDetail::class.java)
          .responseBody.blockFirst()!!.bookingId

        assertThat(getMovements(bookingId))
          .extracting(
            ExternalMovement::getMovementSequence,
            ExternalMovement::getMovementDirection,
            ExternalMovement::isActive
          )
          .containsExactly(
            tuple(1L, MovementDirection.IN, true),
          )
      }

      @Test
      internal fun `will create a bed history for the new booking`() {
        val bookingInTime = LocalDateTime.now()

        val bookingId = webTestClient.post()
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
               "prisonId": "MDI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "imprisonmentStatus": "CUR_ORA" ,
               "bookingInTime": "${bookingInTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .returnResult(InmateDetail::class.java)
          .responseBody.blockFirst()!!.bookingId

        assertThat(getBedAssignments(bookingId))
          .extracting(
            BedAssignmentHistory::getAssignmentReason,
            BedAssignmentHistory::getAssignmentDate,
            BedAssignmentHistory::getAssignmentEndDate
          )
          .containsExactly(
            tuple(
              "ADM",
              bookingInTime.toLocalDate(),
              null
            ),
          )
      }

      @Test
      internal fun `will reset IEP level back to default for prison`() {
        assertThat(getCurrentIEP(offenderNo))
          .extracting(PrivilegeSummary::getIepLevel)
          .isEqualTo("Enhanced")

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
               "prisonId": "MDI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "imprisonmentStatus": "CUR_ORA" 
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk

        assertThat(getCurrentIEP(offenderNo))
          .extracting(PrivilegeSummary::getIepLevel)
          .isEqualTo("Entry")
      }

      @Test
      internal fun `will create admission case note`() {
        val bookingId = webTestClient.post()
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
               "prisonId": "MDI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "24", 
               "imprisonmentStatus": "CUR_ORA"
            }
            """.trimIndent()
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .returnResult(InmateDetail::class.java)
          .responseBody.blockFirst()!!.bookingId

        assertThat(getCaseNotes(bookingId))
          .extracting(CaseNote::getType, CaseNote::getSubType, CaseNote::getText)
          .contains(
            tuple(
              "TRANSFER",
              "FROMTOL",
              "Offender admitted to MOORLAND for reason: Recall From Intermittent Custody from Court 1."
            )
          )
      }
    }
  }

  fun createActiveBooking(prisonId: String = "MDI"): String = OffenderBuilder().withBooking(
    OffenderBookingBuilder(
      prisonId = prisonId,
    )
  ).save(
    webTestClient = webTestClient,
    jwtAuthenticationHelper = jwtAuthenticationHelper,
    dataLoader = dataLoader
  ).offenderNo

  fun createInactiveBooking(iepLevel: String = "ENH"): String = OffenderBuilder().withBooking(
    OffenderBookingBuilder(
      prisonId = "MDI",
      released = true
    ).withIEPLevel(iepLevel)
  ).save(
    webTestClient = webTestClient,
    jwtAuthenticationHelper = jwtAuthenticationHelper,
    dataLoader = dataLoader
  ).offenderNo

  private fun getMovements(bookingId: Long) = externalMovementRepository.findAllByOffenderBooking_BookingId(bookingId)
  private fun getBedAssignments(bookingId: Long) =
    bedAssignmentHistoriesRepository.findAllByBedAssignmentHistoryPKOffenderBookingId(bookingId)

  private fun getCurrentIEP(offenderNo: String) = webTestClient.get()
    .uri("/api/offenders/{offenderNo}/iepSummary", offenderNo)
    .headers(
      setAuthorisation(
        listOf("ROLE_SYSTEM_USER")
      )
    )
    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk
    .returnResult<PrivilegeSummary>().responseBody.blockFirst()!!

  private fun getCaseNotes(bookingId: Long) = webTestClient.get()
    .uri("/api/bookings/{bookingId}/caseNotes?size=999", bookingId)
    .headers(
      setAuthorisation(
        listOf("ROLE_SYSTEM_USER")
      )
    )
    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk
    .returnResult<RestResponsePage<CaseNote>>().responseBody.blockFirst()!!.content

  private fun transferOut(offenderNo: String, toLocation: String = "LEI") {
    webTestClient.put()
      .uri("/api/offenders/{nomsId}/transfer-out", offenderNo)
      .headers(setAuthorisation(listOf("ROLE_TRANSFER_PRISONER")))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(
          """
          {
            "transferReasonCode":"NOTR",
            "commentText":"transferred prisoner today",
            "toLocation":"$toLocation",
            "movementTime": "${LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
            
          }
          """.trimIndent()
        )
      )
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("inOutStatus").isEqualTo("TRN")
      .jsonPath("status").isEqualTo("INACTIVE TRN")
      .jsonPath("lastMovementTypeCode").isEqualTo("TRN")
      .jsonPath("lastMovementReasonCode").isEqualTo("NOTR")
      .jsonPath("assignedLivingUnit.agencyId").isEqualTo("TRN")
      .jsonPath("assignedLivingUnit.description").doesNotExist()
  }
}
