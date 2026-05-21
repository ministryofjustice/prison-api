package uk.gov.justice.hmpps.prison.api.resource.impl

import com.google.gson.Gson
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.exchange
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.resource.impl.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.api.resource.impl.AuthTokenHelper.AuthToken.CREATE_BOOKING_USER
import uk.gov.justice.hmpps.prison.api.resource.impl.AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA
import uk.gov.justice.hmpps.prison.repository.MovementsRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository
import uk.gov.justice.hmpps.prison.util.builders.getCaseNotes
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.function.Function

private const val OFFENDER_NUMBER = "A1234AB"

@ContextConfiguration(classes = [OffenderResourceIntTest.TestClock::class])
class OffenderResourceIntTest : ResourceTest() {
  @Autowired
  lateinit var movementsRepository: MovementsRepository

  @Autowired
  lateinit var offenderCaseNoteRepository: OffenderCaseNoteRepository

  @TestConfiguration
  internal class TestClock {
    @Bean
    fun clock(): Clock = LocalDate.parse("2020-10-01", DateTimeFormatter.ISO_DATE).atStartOfDay()
      .run { Clock.fixed(this.toInstant(ZoneOffset.UTC), ZoneId.systemDefault()) }
  }

  @Nested
  @DisplayName("GET /api/offenders/{offenderNo}/sentences")
  inner class GetBookingSentences {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/offenders/A1234AB/sentences")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/offenders/A1234AB/sentences")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ROLE_GLOBAL_SEARCH override role`() {
      webTestClient.get().uri("/api/offenders/A1234AB/sentences")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri("/api/offenders/A1234AB/sentences")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun testCanRetrieveSentenceDetailsForOffender() {
      val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{nomsId}/sentences",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        OFFENDER_NUMBER,
      )
      assertThatJsonFileAndStatus(response, 200, "sentence.json")
    }
  }

  @Nested
  @DisplayName("GET /api/offenders/{offenderNo}")
  inner class OffenderDetails {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/offenders/$OFFENDER_NUMBER")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.get().uri("/api/offenders/$OFFENDER_NUMBER")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 if has unauthorised ROLE_SYSTEM_USER`() {
      webTestClient.get().uri("/api/offenders/$OFFENDER_NUMBER")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success if has authorised ROLE_GLOBAL_SEARCH`() {
      webTestClient.get().uri("/api/offenders/$OFFENDER_NUMBER")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns success if has authorised ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.get().uri("/api/offenders/$OFFENDER_NUMBER")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 403 if not in user caseload`() {
      webTestClient.get().uri("/api/offenders/$OFFENDER_NUMBER")
        .headers(setAuthorisation("WAI_USER", listOf())).exchange().expectStatus().isForbidden
    }

    @Test
    fun `returns 403 if user has no caseloads`() {
      webTestClient.get().uri("/api/offenders/$OFFENDER_NUMBER")
        .headers(setAuthorisation("RO_USER", listOf())).exchange().expectStatus().isForbidden
    }

    @Test
    fun testGetFullOffenderInformation() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        OFFENDER_NUMBER,
      )
      assertThatJsonFileAndStatus(response, 200, "offender_detail.json")
    }

    @Test
    fun testOffenderWithActiveRecallOffence() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        "A1234AC",
      )
      assertThatJsonFileAndStatus(response, 200, "offender_detail_recall.json")
    }

    @Test
    fun testOffenderWithInActiveRecallOffence() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        "A1234AD",
      )
      assertThatJsonFileAndStatus(response, 200, "offender_detail_no_recall.json")
    }

    @Test
    fun testOffenderInformationWithoutBooking() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        "A1234DD",
      )
      assertThatJsonFileAndStatus(response, 200, "offender_detail_min.json")
    }

    @Test
    fun testOffenderNotFound() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        "B1234DD",
      )
      assertThatStatus(response, 404)
    }

    @Test
    fun testFullOffenderInformation_WithAliases() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        "A1234AI",
      )
      assertThatJsonFileAndStatus(response, 200, "offender_detail_aliases.json")
    }

    @Test
    fun testFullOffenderInformation_WithMergeOffenderCharge() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<InmateDetail>() {},
        "A5577RS",
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
      assertThat((response.body as InmateDetail).offenceHistory).extracting("offenceCode", "mostSerious")
        // The charge for M2 should not be included because it is created by a merge, but M3 should
        .containsExactlyInAnyOrder(
          Tuple.tuple("M3", true),
          Tuple.tuple("M4", true),
        )
    }
  }

  @Test
  fun testViewPrisonTimeline() {
    val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
    val httpEntity = createHttpEntity(token, null)
    val prisonerNo = "A1234AA"
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/prison-timeline",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {},
      prisonerNo,
    )
    assertThatJsonFileAndStatus(response, 200, "prisoner_timeline.json")
  }

  @Test
  fun testCannotReleasePrisonerInTheFuture() {
    val token = authTokenHelper.getToken(CREATE_BOOKING_USER)
    val body = mapOf(
      "movementReasonCode" to "CR",
      "commentText" to "released prisoner today",
      "releaseTime" to LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    )
    val entity = createHttpEntity(token, body)
    val prisonerNo = "A1234AA"
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/release",
      PUT,
      entity,
      object : ParameterizedTypeReference<String>() {},
      prisonerNo,
    )
    assertThat(response.statusCode.value()).isEqualTo(400)
  }

  @Test
  fun testCannotReleasePrisonerBeforeLastMovement() {
    val token = authTokenHelper.getToken(CREATE_BOOKING_USER)
    val body = mapOf(
      "movementReasonCode" to "CR",
      "commentText" to "released prisoner today",
      "releaseTime" to LocalDateTime.of(2019, 10, 17, 17, 29, 0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    )
    val entity = createHttpEntity(token, body)
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/release",
      PUT,
      entity,
      object : ParameterizedTypeReference<String>() {},
      "A1234AA",
    )
    assertThat(response.statusCode.value()).isEqualTo(400)
  }

  @Test
  fun testCanCreateANewPrisoner() {
    val token = authTokenHelper.getToken(CREATE_BOOKING_USER)
    val body = mapOf(
      "pncNumber" to "03/11999M",
      "lastName" to "d'Arras",
      "firstName" to "Mathias",
      "middleName1" to "Hector",
      "middleName2" to "Sausage-Hausen",
      "title" to "MR",
      "croNumber" to "D827492834",
      "dateOfBirth" to LocalDate.of(2000, 10, 17).format(DateTimeFormatter.ISO_LOCAL_DATE),
      "gender" to "M",
      "ethnicity" to "M1",
    )
    val entity = createHttpEntity(token, body)
    val response = testRestTemplate.exchange(
      "/api/offenders",
      POST,
      entity,
      object : ParameterizedTypeReference<String>() {},
    )
    assertThatJsonFileAndStatus(response, 200, "new_prisoner.json")
  }

  @Test
  fun testCanMovePrisonerFromCourtToHospital() {
    val token = authTokenHelper.getToken(CREATE_BOOKING_USER)
    val body = mapOf(
      "lastName" to "TestSurnam",
      "firstName" to "TestFirstnam",
      "dateOfBirth" to LocalDate.of(2001, 10, 19).format(DateTimeFormatter.ISO_LOCAL_DATE),
      "gender" to "M",
      "ethnicity" to "W1",
    )
    val entity = createHttpEntity(token, body)
    val createResponse = testRestTemplate.exchange(
      "/api/offenders",
      POST,
      entity,
      object : ParameterizedTypeReference<String>() {},
    )
    val offenderNo = Gson().fromJson<Map<*, *>>(createResponse.body, MutableMap::class.java)["offenderNo"]!!
    val dischargeRequest = mapOf(
      "hospitalLocationCode" to "ARNOLD",
      "dischargeTime" to LocalDateTime.of(2021, 5, 18, 17, 23, 0).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      "commentText" to "Discharged to Psychiatric hospital",
      "supportingPrisonId" to "LEI",
      "fromLocationId" to "COURT1",
    )
    val dischargeEntity = createHttpEntity(token, dischargeRequest)
    val dischargeResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/discharge-to-hospital",
      PUT,
      dischargeEntity,
      object : ParameterizedTypeReference<String>() {},
      offenderNo,
    )
    assertThatJsonFileAndStatus(dischargeResponse, 200, "discharged_from_court.json")
    assertThat(testDataContext.getCaseNotes(offenderNo.toString()))
      .extracting(
        Function<OffenderCaseNote, Any> { it.typeCode },
        Function<OffenderCaseNote, Any> { it.subTypeCode },
        Function<OffenderCaseNote, Any> { it.agencyLocation.id },
        Function<OffenderCaseNote, Any> { it.caseNoteText },
      )
      .containsExactly(
        Tuple.tuple(
          "TRANSFER",
          "FROMTOL",
          "LEI",
          "Offender admitted to LEEDS for reason: Awaiting Removal to Psychiatric Hospital from Court 1.",
        ),
        Tuple.tuple(
          "PRISON",
          "RELEASE",
          "LEI",
          "Transferred from LEEDS for reason: Moved to psychiatric hospital Arnold Lodge.",
        ),
      )
  }

  @Test
  fun testCanAdjustReleasedPrisonerFromPrisonToHospital() {
    val token = authTokenHelper.getToken(CREATE_BOOKING_USER)
    val body = mapOf(
      "lastName" to "TestSurname",
      "firstName" to "TestFirstname",
      "dateOfBirth" to LocalDate.of(2000, 10, 17).format(DateTimeFormatter.ISO_LOCAL_DATE),
      "gender" to "M",
      "ethnicity" to "M1",
    )
    val entity = createHttpEntity(token, body)
    val createResponse = testRestTemplate.exchange(
      "/api/offenders",
      POST,
      entity,
      object : ParameterizedTypeReference<String>() {},
    )
    val offenderNo = Gson().fromJson<Map<*, *>>(createResponse.body, MutableMap::class.java)["offenderNo"]!!
    val newBookingBody = mapOf(
      "prisonId" to "SYI",
      "fromLocationId" to "COURT1",
      "movementReasonCode" to "24",
      "youthOffender" to "true",
      "imprisonmentStatus" to "CUR_ORA",
      "cellLocation" to "SYI-A-1-1",
    )
    val newBookingEntity = createHttpEntity(token, newBookingBody)
    val newBookingResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/booking",
      POST,
      newBookingEntity,
      object : ParameterizedTypeReference<String>() {},
      offenderNo,
    )
    assertThat(newBookingResponse.statusCode.value()).isEqualTo(200)
    val releaseBody =
      createHttpEntity(token, mapOf("movementReasonCode" to "CR", "commentText" to "released prisoner incorrectly"))
    val releaseResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/release",
      PUT,
      releaseBody,
      object : ParameterizedTypeReference<String>() {},
      offenderNo,
    )
    assertThat(releaseResponse.statusCode.value()).isEqualTo(200)

    // check that no new movement is created
    val latestMovement =
      movementsRepository.getMovementsByOffenders(listOf(offenderNo.toString()), null, true, false)[0]
    assertThat(latestMovement.fromAgency).isEqualTo("SYI")
    assertThat(latestMovement.toAgency).isEqualTo("OUT")
    assertThat(latestMovement.movementType).isEqualTo("REL")
    assertThat(latestMovement.directionCode).isEqualTo("OUT")
    assertThat(latestMovement.movementReason).isEqualTo("Conditional Release (CJA91) -SH Term>1YR")
    val dischargeRequest = mapOf(
      "hospitalLocationCode" to "HAZLWD",
      "dischargeTime" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
      "commentText" to "Discharged to Psychiatric hospital",
    )
    val dischargeEntity = createHttpEntity(token, dischargeRequest)
    val dischargeResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/discharge-to-hospital",
      PUT,
      dischargeEntity,
      object : ParameterizedTypeReference<String>() {},
      offenderNo,
    )
    assertThatJsonFileAndStatus(dischargeResponse, 200, "discharged_from_prison.json")

    // check that no new movement is created
    val noMovement = movementsRepository.getMovementsByOffenders(listOf(offenderNo.toString()), null, true, false)[0]
    assertThat(noMovement.fromAgency).isEqualTo("SYI")
    assertThat(noMovement.toAgency).isEqualTo("HAZLWD")
    assertThat(noMovement.movementType).isEqualTo("REL")
    assertThat(noMovement.directionCode).isEqualTo("OUT")
    assertThat(noMovement.movementReason).isEqualTo("Final Discharge To Hospital-Psychiatric")
    val response = testRestTemplate.exchange(
      "/api/offenders/{offenderNo}",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {},
      offenderNo,
    )
    assertThatOKResponseContainsJson(
      response,
      """
        {
          "locationDescription": "Outside - released from Shrewsbury",
          "latestLocationId": "SYI"
        }
      """.trimIndent(),
    )
    assertThat(testDataContext.getCaseNotes(offenderNo.toString()))
      .extracting(
        Function<OffenderCaseNote, Any> { it.typeCode },
        Function<OffenderCaseNote, Any> { it.subTypeCode },
        Function<OffenderCaseNote, Any> { it.agencyLocation.id },
        Function<OffenderCaseNote, Any> { it.caseNoteText },
      )
      .containsExactly(
        Tuple.tuple(
          "TRANSFER",
          "FROMTOL",
          "SYI",
          "Offender admitted to SHREWSBURY for reason: Recall from Intermittent Custody from Court 1.",
        ),
        Tuple.tuple("PRISON", "RELEASE", "SYI", "Released from SHREWSBURY for reason: Conditional Release."),
        Tuple.tuple(
          "PRISON",
          "RELEASE",
          "SYI",
          "Transferred from SHREWSBURY for reason: Moved to psychiatric hospital Hazelwood House.",
        ),
      )
  }

  @Test
  fun testCanReleasePrisonerFromPrisonToHospitalInNomis() {
    val token = authTokenHelper.getToken(CREATE_BOOKING_USER)
    val body = mapOf(
      "lastName" to "FromNomis",
      "firstName" to "ReleasedToHospital",
      "dateOfBirth" to LocalDate.of(2000, 10, 17).format(DateTimeFormatter.ISO_LOCAL_DATE),
      "gender" to "M",
      "ethnicity" to "M1",
    )
    val entity = createHttpEntity(token, body)
    val createResponse = testRestTemplate.exchange(
      "/api/offenders",
      POST,
      entity,
      object : ParameterizedTypeReference<String>() {},
    )
    val offenderNo = Gson().fromJson<Map<*, *>>(createResponse.body, MutableMap::class.java)["offenderNo"]!!
    val newBookingBody = mapOf(
      "prisonId" to "SYI",
      "fromLocationId" to "COURT1",
      "movementReasonCode" to "24",
      "youthOffender" to "true",
      "imprisonmentStatus" to "CUR_ORA",
      "cellLocation" to "SYI-A-1-1",
    )
    val newBookingEntity = createHttpEntity(token, newBookingBody)
    val newBookingResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/booking",
      POST,
      newBookingEntity,
      object : ParameterizedTypeReference<String>() {},
      offenderNo,
    )
    assertThat(newBookingResponse.statusCode.value()).isEqualTo(200)
    val releaseBody = createHttpEntity(
      token,
      mapOf("movementReasonCode" to "HP", "commentText" to "released prisoner to hospital in NOMIS"),
    )
    val releaseResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/release",
      PUT,
      releaseBody,
      object : ParameterizedTypeReference<String>() {},
      offenderNo,
    )
    assertThat(releaseResponse.statusCode.value()).isEqualTo(200)
    val latestMovement =
      movementsRepository.getMovementsByOffenders(listOf(offenderNo.toString()), null, true, false)[0]
    assertThat(latestMovement.fromAgency).isEqualTo("SYI")
    assertThat(latestMovement.toAgency).isEqualTo("OUT")
    assertThat(latestMovement.movementType).isEqualTo("REL")
    assertThat(latestMovement.movementReason).isEqualTo("Final Discharge To Hospital-Psychiatric")
    assertThat(testDataContext.getCaseNotes(offenderNo.toString()))
      .extracting(
        Function<OffenderCaseNote, Any> { it.typeCode },
        Function<OffenderCaseNote, Any> { it.subTypeCode },
        Function<OffenderCaseNote, Any> { it.agencyLocation.id },
        Function<OffenderCaseNote, Any> { it.caseNoteText },
      )
      .containsExactly(
        Tuple.tuple(
          "TRANSFER",
          "FROMTOL",
          "SYI",
          "Offender admitted to SHREWSBURY for reason: Recall from Intermittent Custody from Court 1.",
        ),
        Tuple.tuple(
          "PRISON",
          "RELEASE",
          "SYI",
          "Released from SHREWSBURY for reason: Discharge To Hospital-Psychiatric.",
        ),
      )
  }

  @Test
  fun testCanReleaseAPrisoner() {
    val token = authTokenHelper.getToken(CREATE_BOOKING_USER)
    val body = mapOf(
      "movementReasonCode" to "CR",
      "commentText" to "released prisoner today",
      "releaseTime" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
    )
    val entity = createHttpEntity(token, body)
    val prisonerNo = "A1181MV"
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/release",
      PUT,
      entity,
      object : ParameterizedTypeReference<String>() {},
      prisonerNo,
    )
    assertThatJsonFileAndStatus(response, 200, "released_prisoner.json")
    assertThat(testDataContext.getCaseNotes(prisonerNo))
      .extracting(
        Function<OffenderCaseNote, Any> { it.typeCode },
        Function<OffenderCaseNote, Any> { it.subTypeCode },
        Function<OffenderCaseNote, Any> { it.agencyLocation.id },
        Function<OffenderCaseNote, Any> { it.caseNoteText },
      )
      .containsExactly(
        Tuple.tuple("PRISON", "RELEASE", "WAI", "Released from THE WEARE for reason: Conditional Release."),
      )
  }

  @Test
  fun testCannotReleasePrisonerAlreadyOut() {
    val token = authTokenHelper.getToken(CREATE_BOOKING_USER)
    val body = mapOf("movementReasonCode" to "CR", "commentText" to "released prisoner today")
    val entity = createHttpEntity(token, body)
    val response = testRestTemplate.exchange<ErrorResponse>(
      "/api/offenders/{nomsId}/release",
      PUT,
      entity,
      "Z0020ZZ",
    )
    val error = response.body!!
    assertThat(response.statusCode.value()).isEqualTo(400)
    assertThat(error.userMessage).contains("Booking -20 is not active")
  }

  @DisplayName("GET /api/offenders/{offenderNo}/addresses")
  @Nested
  inner class GetAddresses {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/offenders/$OFFENDER_NUMBER/addresses")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have override role`() {
      webTestClient.get().uri("/api/offenders/$OFFENDER_NUMBER/addresses")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success if has authorised ROLE_GLOBAL_SEARCH`() {
      webTestClient.get().uri("/api/offenders/$OFFENDER_NUMBER/addresses")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns success if has authorised ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.get().uri("/api/offenders/$OFFENDER_NUMBER/addresses")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `Attempt to get address for offender that is not part of any of logged on staff user's caseloads`() {
      webTestClient.get().uri("/api/offenders/$OFFENDER_NUMBER/addresses")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Unauthorised access to booking with id -2.")
    }

    @Test
    fun `Attempt to get address for offender that does not exist`() {
      webTestClient.get().uri("/api/offenders/A1111ZZ/addresses")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [A1111ZZ] not found.")
    }

    @Test
    fun testCanRetrieveAddresses() {
      val requestEntity = createHttpEntity(authTokenHelper.getToken(AuthToken.PRISON_API_USER), null, mapOf())
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNo}/addresses",
        GET,
        requestEntity,
        object : ParameterizedTypeReference<String>() {},
        OFFENDER_NUMBER,
      )
      assertThatJsonFileAndStatus(response, 200, "offender-address.json")
    }
  }

  @DisplayName("GET /api/offenders/{offenderNo}/adjudications")
  @Nested
  inner class GetAdjudications {
    @Test
    fun testFilterAdjudicationsByFindingCode() {
      val requestEntity = createHttpEntity(authTokenHelper.getToken(AuthToken.PRISON_API_USER), null, mapOf())
      val response = testRestTemplate.exchange(
        "/api/offenders/{offenderNumber}/adjudications?finding={findingCode}",
        GET,
        requestEntity,
        object : ParameterizedTypeReference<String>() {},
        "A1181HH",
        "NOT_PROVEN",
      )
      assertThatJsonFileAndStatus(response, 200, "adjudications_by_finding_code.json")
    }
  }

  @Nested
  @DisplayName("GET /api/offenders/{offenderNo}/damage-obligations")
  inner class GetDamageObligations {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/offenders/A1234AA/damage-obligations")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 as endpoint does not have override role`() {
      webTestClient.get().uri("/api/offenders/A1234AA/damage-obligations")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri("/api/offenders/A1234AA/damage-obligations")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has GLOBAL_SEARCH override role`() {
      webTestClient.get().uri("/api/offenders/A1234AA/damage-obligations")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }
  }
}
