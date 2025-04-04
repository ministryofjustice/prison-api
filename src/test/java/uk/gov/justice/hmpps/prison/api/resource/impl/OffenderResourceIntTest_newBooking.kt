@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.hmpps.prison.api.model.CaseNote
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.dsl.JAN
import uk.gov.justice.hmpps.prison.dsl.NomisDataBuilder
import uk.gov.justice.hmpps.prison.dsl.TimePoints.midday
import uk.gov.justice.hmpps.prison.dsl.TimePoints.midnight
import uk.gov.justice.hmpps.prison.dsl.at
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.enteringandleaving.TrustAccountService
import uk.gov.justice.hmpps.prison.util.builders.getBedAssignments
import uk.gov.justice.hmpps.prison.util.builders.getCaseNotes
import uk.gov.justice.hmpps.prison.util.builders.getMovements
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val REMAND_REASON = "N"
private const val UNLAWFULLY_AT_LARGE = "UAL"
private const val ESCAPED = "ESCP"

class OffenderResourceIntTest_newBooking : ResourceTest() {

  @MockitoBean
  private lateinit var trustAccountService: TrustAccountService

  @Autowired
  private lateinit var builder: NomisDataBuilder

  @Autowired
  private lateinit var offenderBookingRepository: OffenderBookingRepository

  @Autowired
  private lateinit var offenderRepository: OffenderRepository

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
              listOf("ROLE_SYSTEM_USER"),
            ),
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
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
        lateinit var offenderNo: String
        builder.build {
          offenderNo = offender {
            booking(prisonId = "MDI") {
              transferOut(prisonId = "SYI")
            }
          }.offenderNo
        }

        // when booking is created then the request is rejected
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("ZZZ is not a valid from location")
      }

      @Test
      internal fun `404 (possibly incorrectly) when trying to book in from the OUT location (even though this the default when no supplied)`() {
        val offenderNo = createInactiveBooking()

        // when booking is created then the request is rejected
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE"),
            ),
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            """
            {
               "prisonId": "SYI", 
               "fromLocationId": "OUT", 
               "movementReasonCode": "24", 
               "imprisonmentStatus": "CUR_ORA", 
               "cellLocation": "SYI-A-1-1"     
            }
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isNotFound
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("OUT is not a valid from location")
      }

      @Test
      internal fun `404 when trying to book in with an imprisonment status that doesn't exist`() {
        val offenderNo = createInactiveBooking()

        // when booking is created then the request is rejected
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isEqualTo(409)
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("The cell MDI-FULL does not have any available capacity")
      }

      @Test
      internal fun `400 when trying to book in prisoner in the future`() {
        val offenderNo = createInactiveBooking()

        // when booking is created then the request is rejected
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Movement cannot be done in the future")
      }

      @Test
      internal fun `400 when trying to book in prisoner before they were released from previous prison`() {
        val offenderNo = createInactiveBooking()

        // when booking is created then the request is rejected
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isBadRequest
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("Movement cannot be before the previous active movement")
      }

      @Test
      fun `409 when trying create a new booking for someone who has absconded`() {
        lateinit var offenderNo: String
        builder.build {
          offenderNo = offender(lastName = "DUBOIS") {
            booking(
              prisonId = "MDI",
              bookingInTime = 10 JAN 2024 at midday,
              movementReasonCode = REMAND_REASON,
            ) {
              sendToCourt(
                releaseTime = 11 JAN 2024 at midnight,
              )
              returnFromCourt(
                prisonId = "MDI",
                returnTime = 11 JAN 2024 at midday,
              )
              release(
                releaseTime = 20 JAN 2024 at midnight,
                movementReasonCode = UNLAWFULLY_AT_LARGE,
              )
            }
          }.offenderNo
        }

        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE"),
            ),
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            // language=JSON
            """
            {
               "prisonId": "MDI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "RECA", 
               "imprisonmentStatus": "CUR_ORA"
            }
            """,
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isEqualTo(HttpStatus.CONFLICT)
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("A new booking cannot be created for someone who has escaped")
      }

      @Test
      fun `409 when trying create a new booking for someone who has escaped`() {
        lateinit var offenderNo: String
        builder.build {
          offenderNo = offender(lastName = "DUBOIS") {
            booking(
              prisonId = "MDI",
              bookingInTime = 10 JAN 2024 at midday,
              movementReasonCode = REMAND_REASON,
            ) {
              release(
                releaseTime = 11 JAN 2024 at midday,
              )
              recall(
                prisonId = "MDI",
                recallTime = 13 JAN 2024 at midday,
              )
              release(
                releaseTime = 20 JAN 2024 at midnight,
                movementReasonCode = ESCAPED,
              )
            }
          }.offenderNo
        }

        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE"),
            ),
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .bodyValue(
            // language=JSON
            """
            {
               "prisonId": "MDI", 
               "fromLocationId": "COURT1", 
               "movementReasonCode": "RECA", 
               "imprisonmentStatus": "CUR_ORA"
            }
            """,
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isEqualTo(HttpStatus.CONFLICT)
          .expectBody()
          .jsonPath("userMessage")
          .isEqualTo("A new booking cannot be created for someone who has escaped")
      }
    }

    @Nested
    @DisplayName("when offender has no previous bookings")
    inner class NewOffender {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        builder.build {
          offenderNo = offender().offenderNo
        }
      }

      @Test
      internal fun `will create a new booking and mark as youth offender when booked in as a YOUTH`() {
        // Given offender has no existing booking record
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER"),
            ),
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
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
              listOf("ROLE_SYSTEM_USER"),
            ),
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
        // Given offender has no existing booking record
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER"),
            ),
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
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
              listOf("ROLE_SYSTEM_USER"),
            ),
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
        builder.build {
          offenderNo = offender {
            booking(prisonId = "LEI", youthOffender = true) {
              release()
            }
          }.offenderNo
        }
      }

      @Test
      internal fun `will create a new booking and mark as youth offender when booked in as a YOUTH`() {
        // Given offender has previous inactive booking record
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_VIEW_PRISONER_DATA"),
            ),
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
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
              listOf("ROLE_SYSTEM_USER"),
            ),
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
              listOf("ROLE_VIEW_PRISONER_DATA"),
            ),
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
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
              listOf("ROLE_SYSTEM_USER"),
            ),
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
        builder.build {
          offenderNo = offender {
            booking(prisonId = "LEI", youthOffender = false) {
              release()
            }
          }.offenderNo
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
              listOf("ROLE_VIEW_PRISONER_DATA"),
            ),
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
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
              listOf("ROLE_SYSTEM_USER"),
            ),
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
              listOf("ROLE_VIEW_PRISONER_DATA"),
            ),
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk

        // then we have an active booking
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER"),
            ),
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
    @DisplayName("when offender has an alias (multiple offender records)")
    inner class OffenderWithAlias {
      private lateinit var offenderNo: String
      private var aliasId: Long = 0
      private var bookingId: Long = 0

      @BeforeEach
      internal fun setUp() {
        builder.build {
          offenderNo = offender(firstName = "Root", lastName = "Offender") {
            aliasId = alias(firstName = "JACK", lastName = "TRENT").offenderId
            bookingId = booking(prisonId = "LEI", youthOffender = false) {
              release()
            }.bookingId
          }.offenderNo
        }
      }

      @AfterEach
      internal fun tearDown() {
        builder.deletePrisoner(offenderNo)
      }

      @Test
      internal fun `will create a new booking`() {
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_VIEW_PRISONER_DATA"),
            ),
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(false)
          .jsonPath("inOutStatus").isEqualTo("OUT")
          // check name is the name of the root offender
          .jsonPath("firstName").isEqualTo("ROOT")
          .jsonPath("lastName").isEqualTo("OFFENDER")

        // when booking is created
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk

        // then we have an active booking
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER"),
            ),
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(true)
          .jsonPath("profileInformation[0].type").isEqualTo("YOUTH")
          .jsonPath("profileInformation[0].resultValue").isEqualTo("No")
          // with name of offender still set to the root offender
          .jsonPath("firstName").isEqualTo("ROOT")
          .jsonPath("lastName").isEqualTo("OFFENDER")
      }

      @Test
      internal fun `will create a new booking keeping working name`() {
        // need to reassign the booking to the offender alias
        val booking = offenderBookingRepository.findById(bookingId).orElseThrow()
        booking.offender = offenderRepository.findById(aliasId).orElseThrow()
        offenderBookingRepository.save(booking)

        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_VIEW_PRISONER_DATA"),
            ),
          )
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(false)
          .jsonPath("inOutStatus").isEqualTo("OUT")
          // check name should now be the alias as the latest booking is against the alias
          .jsonPath("firstName").isEqualTo("JACK")
          .jsonPath("lastName").isEqualTo("TRENT")

        // when booking is created
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk

        // then we have an active booking
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("activeFlag").isEqualTo(true)
          .jsonPath("profileInformation[0].type").isEqualTo("YOUTH")
          .jsonPath("profileInformation[0].resultValue").isEqualTo("No")
          // with prisoner name still the alias
          .jsonPath("firstName").isEqualTo("JACK")
          .jsonPath("lastName").isEqualTo("TRENT")
      }
    }

    @Nested
    @DisplayName("when creating a booking it")
    inner class SideEffects {
      private lateinit var offenderNo: String

      @BeforeEach
      internal fun setUp() {
        offenderNo = createInactiveBooking()
      }

      @Test
      internal fun `will by default place prisoner in reception`() {
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk

        // then we have an active booking
        webTestClient.get()
          .uri("/api/offenders/{offenderNo}", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_SYSTEM_USER"),
            ),
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .returnResult(InmateDetail::class.java)
          .responseBody.blockFirst()!!.bookingId

        assertThat(testDataContext.getMovements(bookingId))
          .extracting(
            ExternalMovement::getMovementSequence,
            ExternalMovement::getMovementDirection,
            ExternalMovement::isActive,
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
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .returnResult(InmateDetail::class.java)
          .responseBody.blockFirst()!!.bookingId

        assertThat(testDataContext.getBedAssignments(bookingId))
          .extracting(
            BedAssignmentHistory::getAssignmentReason,
            BedAssignmentHistory::getAssignmentDate,
            BedAssignmentHistory::getAssignmentEndDate,
          )
          .containsExactly(
            tuple(
              "ADM",
              bookingInTime.toLocalDate(),
              null,
            ),
          )
      }

      @Test
      internal fun `will create admission case note`() {
        webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk

        assertThat(testDataContext.getCaseNotes(offenderNo))
          .extracting(CaseNote::getType, CaseNote::getSubType, CaseNote::getText)
          .contains(
            tuple(
              "TRANSFER",
              "FROMTOL",
              "Offender admitted to MOORLAND for reason: Recall From Intermittent Custody from Court 1.",
            ),
          )
      }

      @Test
      internal fun `will create trust accounts`() {
        val inmate = webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .returnResult(InmateDetail::class.java)
          .responseBody.blockFirst()!!

        // since this calls a NOMIS store procedure the best we can do
        // is check the service was called with the correct parameters
        verify(trustAccountService).createTrustAccount(
          check {
            assertThat(it.bookingId).isEqualTo(inmate.bookingId)
            assertThat(it.rootOffender.id).isEqualTo(inmate.rootOffenderId)
          },
          check {
            assertThat(it.id).isEqualTo("COURT1")
          },
          check {
            assertThat(it.toAgency.id).isEqualTo("MDI")
            assertThat(it.movementReason.code).isEqualTo("24")
          },
        )
      }

      @Test
      internal fun `will assign a unique book number`() {
        val inmate = webTestClient.post()
          .uri("/api/offenders/{offenderNo}/booking", offenderNo)
          .headers(
            setAuthorisation(
              listOf("ROLE_BOOKING_CREATE"),
            ),
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
            """.trimIndent(),
          )
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .returnResult(InmateDetail::class.java)
          .responseBody.blockFirst()!!

        assertThat(inmate.bookingNo).matches("^\\d{5}[A-Z]\$")
      }
    }
  }

  fun createActiveBooking(prisonId: String = "MDI"): String {
    lateinit var offenderNo: String

    builder.build {
      offenderNo = offender {
        booking(prisonId = prisonId)
      }.offenderNo
    }

    return offenderNo
  }

  fun createInactiveBooking(): String {
    lateinit var offenderNo: String

    builder.build {
      offenderNo = offender {
        booking(prisonId = "MDI") {
          release()
        }
      }.offenderNo
    }

    return offenderNo
  }
}
