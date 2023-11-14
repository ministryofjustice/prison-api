package uk.gov.justice.hmpps.prison.api.resource.impl

import com.google.gson.Gson
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.hmpps.prison.api.model.CaseNote
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.IncidentCase
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.CREATE_BOOKING_USER
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.GLOBAL_SEARCH
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA
import uk.gov.justice.hmpps.prison.repository.MovementsRepository
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.List
import java.util.function.Function

@ContextConfiguration(classes = [OffenderResourceIntTest.TestClock::class])
class OffenderResourceIntTest : ResourceTest() {
  @Autowired
  lateinit var movementsRepository: MovementsRepository

  @TestConfiguration
  internal class TestClock {
    private val timeIs_2020_10_01T000000 = LocalDate.parse("2020-10-01", DateTimeFormatter.ISO_DATE).atStartOfDay()

    @Bean
    fun clock(): Clock {
      return Clock.fixed(timeIs_2020_10_01T000000.toInstant(ZoneOffset.UTC), ZoneId.systemDefault())
    }
  }

  private val OFFENDER_NUMBER = "A1234AB"

  @Test
  fun testCanRetrieveSentenceDetailsForOffender() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/sentences",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
      OFFENDER_NUMBER,
    )
    assertThatJsonFileAndStatus(response, 200, "sentence.json")
  }

  @Test
  fun testCanRetrieveSentenceDetailsForOffenderWithSystemUser() {
    val token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/sentences",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
      OFFENDER_NUMBER,
    )
    assertThatJsonFileAndStatus(response, 200, "sentence.json")
  }

  @Test
  fun testCanRetrieveAlertsForOffenderWithViewDataRole() {
    val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/alerts/v2",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
      OFFENDER_NUMBER,
    )
    assertThatJsonFileAndStatus(response, 200, "alerts.json")
  }

  @Test
  fun testCanRetrieveCaseNotesForOffender() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/case-notes/v2",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
      OFFENDER_NUMBER,
    )
    assertThatJsonFileAndStatus(response, 200, "casenotes.json")
  }

  @Test
  fun testViewCaseNotesRoleCanRetrieveCaseNotesForOffender() {
    val token = authTokenHelper.getToken(AuthToken.VIEW_CASE_NOTES)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/case-notes/v2",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
      OFFENDER_NUMBER,
    )
    assertThatJsonFileAndStatus(response, 200, "casenotes.json")
  }

  @Test
  fun testCannotRetrieveCaseNotesForOffenderWithViewPrisonerData() {
    val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/case-notes/v2",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
      OFFENDER_NUMBER,
    )
    assertThat(response.statusCode.value()).isEqualTo(403)
  }

  @Nested
  @DisplayName("GET api/offenders/{offenderNo}")
  inner class OffenderDetails {

    @Test
    fun testGetFullOffenderInformation() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{nomsId}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        OFFENDER_NUMBER,
      )
      assertThatJsonFileAndStatus(response, 200, "offender_detail.json")
    }

    @Test
    fun compareV1AndV1_1VersionsOfGetOffender() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntityV1 = createHttpEntity(token, null, mapOf("version" to "1.0"))
      val responseV1 = testRestTemplate.exchange(
        "/api/offenders/{nomsId}",
        GET,
        httpEntityV1,
        object : ParameterizedTypeReference<String?>() {},
        OFFENDER_NUMBER,
      )
      assertThatJsonFileAndStatus(responseV1, 200, "offender_detail_v1.1.json")
      val httpEntityV1_1 = createHttpEntity(token, null, mapOf("version" to "1.1_beta"))
      val responseV1_1 = testRestTemplate.exchange(
        "/api/offenders/{nomsId}",
        GET,
        httpEntityV1_1,
        object : ParameterizedTypeReference<String>() {},
        OFFENDER_NUMBER,
      )
      assertThatJsonFileAndStatus(responseV1_1, 200, "offender_detail_v1.1.json")
    }

    @Test
    fun testOffenderWithActiveRecallOffence() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{nomsId}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        "A1234AC",
      )
      assertThatJsonFileAndStatus(response, 200, "offender_detail_recall.json")
    }

    @Test
    fun testOffenderWithInActiveRecallOffence() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{nomsId}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        "A1234AD",
      )
      assertThatJsonFileAndStatus(response, 200, "offender_detail_no_recall.json")
    }

    @Test
    fun testOffenderInformationWithoutBooking() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{nomsId}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        "A1234DD",
      )
      assertThatJsonFileAndStatus(response, 200, "offender_detail_min.json")
    }

    @Test
    fun testOffenderNotFound() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{nomsId}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        "B1234DD",
      )
      assertThatStatus(response, 404)
    }

    @Test
    fun testFullOffenderInformation_WithAliases() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/offenders/{nomsId}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        "A1234AI",
      )
      assertThatJsonFileAndStatus(response, 200, "offender_detail_aliases.json")
    }
  }

  @Nested
  @DisplayName("GET api/offenders/{offenderNo}/incidents")
  inner class OffenderIncidents {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/offenders/A1234AA/incidents?incidentType=ASSAULT&participationRoles=FIGHT")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.get().uri("/api/offenders/A1234AA/incidents?incidentType=ASSAULT&participationRoles=FIGHT")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 as ROLE_BANANAS is not override role`() {
      webTestClient.get().uri("/api/offenders/A1234AA/incidents?incidentType=ASSAULT&participationRoles=FIGHT")
        .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success if has authorised ROLE_SYSTEM_USER`() {
      webTestClient.get().uri("/api/offenders/A1234AA/incidents?incidentType=ASSAULT&participationRoles=FIGHT")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns success if has authorised ROLE_VIEW_INCIDENTS`() {
      webTestClient.get().uri("/api/offenders/A1234AA/incidents?incidentType=ASSAULT&participationRoles=FIGHT")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_INCIDENTS")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 403 if not client authorisation`() {
      webTestClient.get().uri("/api/offenders/A1234AA/incidents?incidentType=ASSAULT&participationRoles=FIGHT")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Test
  fun testGetIncidents() {
    val token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE)
    val response = testRestTemplate.exchange(
      "/api/incidents/-1",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<IncidentCase>() {},
    )
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    val result = response.body
    assertThat(result).extracting("incidentCaseId", "incidentTitle", "incidentType")
      .containsExactlyInAnyOrder(-1L, "Big Fight", "ASSAULT")
    assertThat(result.responses).hasSize(19)
    assertThat(result.parties).hasSize(6)
  }

  @Test
  fun testGetIncidentsNoParties() {
    val token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE)
    val response = testRestTemplate.exchange(
      "/api/incidents/-4",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<IncidentCase?>() {},
    )
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(response.body).extracting("incidentCaseId", "incidentTitle")
      .containsExactlyInAnyOrder(-4L, "Medium sized fight")
  }

  @Test
  fun testGetIncidentsNoRoles() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val response = testRestTemplate.exchange(
      "/api/incidents/-4",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<IncidentCase?>() {},
    )
    assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
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
      object : ParameterizedTypeReference<String?>() {},
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
      HttpMethod.PUT,
      entity,
      object : ParameterizedTypeReference<String?>() {},
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
      HttpMethod.PUT,
      entity,
      object : ParameterizedTypeReference<String?>() {},
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
      HttpMethod.POST,
      entity,
      object : ParameterizedTypeReference<String?>() {},
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
      HttpMethod.POST,
      entity,
      object : ParameterizedTypeReference<String?>() {},
    )
    val offenderNo = Gson().fromJson<Map<*, *>>(createResponse.body, MutableMap::class.java)["offenderNo"]
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
      HttpMethod.PUT,
      dischargeEntity,
      object : ParameterizedTypeReference<String?>() {},
      offenderNo,
    )
    assertThatJsonFileAndStatus(dischargeResponse, 200, "discharged_from_court.json")
    val caseNotes: ResponseEntity<RestResponsePage<CaseNote>> = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/case-notes/v2?sort=id,asc",
      GET,
      createEmptyHttpEntity(GLOBAL_SEARCH),
      object : ParameterizedTypeReference<RestResponsePage<CaseNote>>() {},
      offenderNo,
    )
    assertThat(caseNotes.body.content)
      .extracting(Function<CaseNote, Any> { obj: CaseNote -> obj.type }, Function<CaseNote, Any> { obj: CaseNote -> obj.subType }, Function<CaseNote, Any> { obj: CaseNote -> obj.agencyId }, Function<CaseNote, Any> { obj: CaseNote -> obj.text })
      .containsExactly(
        Tuple.tuple("TRANSFER", "FROMTOL", "LEI", "Offender admitted to LEEDS for reason: Awaiting Removal to Psychiatric Hospital from Court 1."),
        Tuple.tuple("PRISON", "RELEASE", "LEI", "Transferred from LEEDS for reason: Moved to psychiatric hospital Arnold Lodge."),
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
      HttpMethod.POST,
      entity,
      object : ParameterizedTypeReference<String?>() {},
    )
    val offenderNo = Gson().fromJson<Map<*, *>>(createResponse.body, MutableMap::class.java)["offenderNo"]
    val newBookingBody = mapOf("prisonId" to "SYI", "fromLocationId" to "COURT1", "movementReasonCode" to "24", "youthOffender" to "true", "imprisonmentStatus" to "CUR_ORA", "cellLocation" to "SYI-A-1-1")
    val newBookingEntity = createHttpEntity(token, newBookingBody)
    val newBookingResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/booking",
      HttpMethod.POST,
      newBookingEntity,
      object : ParameterizedTypeReference<String?>() {},
      offenderNo,
    )
    assertThat(newBookingResponse.statusCode.value()).isEqualTo(200)
    val bookingId = BigDecimal(Gson().fromJson<Map<*, *>>(newBookingResponse.body, MutableMap::class.java)["bookingId"].toString()).toBigInteger().toLong()
    val releaseBody = createHttpEntity(token, mapOf("movementReasonCode" to "CR", "commentText" to "released prisoner incorrectly"))
    val releaseResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/release",
      HttpMethod.PUT,
      releaseBody,
      object : ParameterizedTypeReference<String?>() {},
      offenderNo,
    )
    assertThat(releaseResponse.statusCode.value()).isEqualTo(200)

    // check that no new movement is created
    val latestMovement = movementsRepository.getMovementsByOffenders(List.of(offenderNo.toString()), null, true, false)[0]
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
      HttpMethod.PUT,
      dischargeEntity,
      object : ParameterizedTypeReference<String?>() {},
      offenderNo,
    )
    assertThatJsonFileAndStatus(dischargeResponse, 200, "discharged_from_prison.json")

    // check that no new movement is created
    val noMovement = movementsRepository.getMovementsByOffenders(List.of(offenderNo.toString()), null, true, false)[0]
    assertThat(noMovement.fromAgency).isEqualTo("SYI")
    assertThat(noMovement.toAgency).isEqualTo("HAZLWD")
    assertThat(noMovement.movementType).isEqualTo("REL")
    assertThat(noMovement.directionCode).isEqualTo("OUT")
    assertThat(noMovement.movementReason).isEqualTo("Final Discharge To Hospital-Psychiatric")
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}",
      GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String?>() {},
      offenderNo,
    )
    assertThatOKResponseContainsJson(
      response,
      """
              {
                  "locationDescription": "Outside - released from SHREWSBURY",
                  "latestLocationId": "SYI"
              }
            
      """.trimIndent(),
    )
    val caseNotes: ResponseEntity<RestResponsePage<CaseNote>> = testRestTemplate.exchange<RestResponsePage<CaseNote>>(
      "/api/offenders/{nomsId}/case-notes/v2?sort=id,asc",
      GET,
      createEmptyHttpEntity(GLOBAL_SEARCH),
      object : ParameterizedTypeReference<RestResponsePage<CaseNote>>() {},
      offenderNo,
    )

    // TODO Possibly a bug - shows that case notes do not reflect the adjusted movement to hospital
    assertThat(caseNotes.body.content)
      .extracting(Function<CaseNote, Any> { obj: CaseNote -> obj.type }, Function<CaseNote, Any> { obj: CaseNote -> obj.subType }, Function<CaseNote, Any> { obj: CaseNote -> obj.agencyId }, Function<CaseNote, Any> { obj: CaseNote -> obj.text })
      .containsExactly(
        Tuple.tuple("TRANSFER", "FROMTOL", "SYI", "Offender admitted to SHREWSBURY for reason: Recall From Intermittent Custody from Court 1."),
        Tuple.tuple("PRISON", "RELEASE", "SYI", "Released from SHREWSBURY for reason: Conditional Release (CJA91) -SH Term>1YR."),
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
      HttpMethod.POST,
      entity,
      object : ParameterizedTypeReference<String?>() {},
    )
    val offenderNo = Gson().fromJson<Map<*, *>>(createResponse.body, MutableMap::class.java)["offenderNo"]
    val newBookingBody = mapOf("prisonId" to "SYI", "fromLocationId" to "COURT1", "movementReasonCode" to "24", "youthOffender" to "true", "imprisonmentStatus" to "CUR_ORA", "cellLocation" to "SYI-A-1-1")
    val newBookingEntity = createHttpEntity(token, newBookingBody)
    val newBookingResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/booking",
      HttpMethod.POST,
      newBookingEntity,
      object : ParameterizedTypeReference<String?>() {},
      offenderNo,
    )
    assertThat(newBookingResponse.statusCode.value()).isEqualTo(200)
    val bookingId = BigDecimal(Gson().fromJson<Map<*, *>>(newBookingResponse.body, MutableMap::class.java)["bookingId"].toString()).toBigInteger().toLong()
    val releaseBody = createHttpEntity(token, mapOf("movementReasonCode" to "HP", "commentText" to "released prisoner to hospital in NOMIS"))
    val releaseResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/release",
      HttpMethod.PUT,
      releaseBody,
      object : ParameterizedTypeReference<String?>() {},
      offenderNo,
    )
    assertThat(releaseResponse.statusCode.value()).isEqualTo(200)
    val latestMovement = movementsRepository.getMovementsByOffenders(List.of(offenderNo.toString()), null, true, false)[0]
    assertThat(latestMovement.fromAgency).isEqualTo("SYI")
    assertThat(latestMovement.toAgency).isEqualTo("OUT")
    assertThat(latestMovement.movementType).isEqualTo("REL")
    assertThat(latestMovement.movementReason).isEqualTo("Final Discharge To Hospital-Psychiatric")
    val caseNotes: ResponseEntity<RestResponsePage<CaseNote>> = testRestTemplate.exchange<RestResponsePage<CaseNote>>(
      "/api/offenders/{nomsId}/case-notes/v2?sort=id,asc",
      GET,
      createEmptyHttpEntity(GLOBAL_SEARCH),
      object : ParameterizedTypeReference<RestResponsePage<CaseNote>>() {},
      offenderNo,
    )
    assertThat(caseNotes.body.content)
      .extracting(Function<CaseNote, Any> { obj: CaseNote -> obj.type }, Function<CaseNote, Any> { obj: CaseNote -> obj.subType }, Function<CaseNote, Any> { obj: CaseNote -> obj.agencyId }, Function<CaseNote, Any> { obj: CaseNote -> obj.text })
      .containsExactly(
        Tuple.tuple("TRANSFER", "FROMTOL", "SYI", "Offender admitted to SHREWSBURY for reason: Recall From Intermittent Custody from Court 1."),
        Tuple.tuple("PRISON", "RELEASE", "SYI", "Released from SHREWSBURY for reason: Final Discharge To Hospital-Psychiatric."),
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
      HttpMethod.PUT,
      entity,
      object : ParameterizedTypeReference<String?>() {},
      prisonerNo,
    )
    assertThatJsonFileAndStatus(response, 200, "released_prisoner.json")
    val caseNotes: ResponseEntity<RestResponsePage<CaseNote>> = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/case-notes/v2?sort=id,asc",
      GET,
      createEmptyHttpEntity(GLOBAL_SEARCH),
      object : ParameterizedTypeReference<RestResponsePage<CaseNote>>() {},
      prisonerNo,
    )
    assertThat(caseNotes.body.content)
      .extracting(Function<CaseNote, Any> { obj: CaseNote -> obj.type }, Function<CaseNote, Any> { obj: CaseNote -> obj.subType }, Function<CaseNote, Any> { obj: CaseNote -> obj.agencyId }, Function<CaseNote, Any> { obj: CaseNote -> obj.text })
      .containsExactly(
        Tuple.tuple("PRISON", "RELEASE", "WAI", "Released from THE WEARE for reason: Conditional Release (CJA91) -SH Term>1YR."),
      )
  }

  @Test
  fun testCannotReleasePrisonerAlreadyOut() {
    val token = authTokenHelper.getToken(CREATE_BOOKING_USER)
    val body = mapOf("movementReasonCode" to "CR", "commentText" to "released prisoner today")
    val entity = createHttpEntity(token, body)
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/release",
      HttpMethod.PUT,
      entity,
      ErrorResponse::class.java,
      "Z0020ZZ",
    )
    val error = response.body
    assertThat(response.statusCode.value()).isEqualTo(400)
    assertThat(error.userMessage).contains("Booking -20 is not active")
  }

  @Test
  fun testCanRetrieveAddresses() {
    val requestEntity = createHttpEntity(authTokenHelper.getToken(AuthToken.PRISON_API_USER), null, mapOf())
    val response = testRestTemplate.exchange(
      "/api/offenders/{offenderNumber}/addresses",
      GET,
      requestEntity,
      object : ParameterizedTypeReference<String?>() {},
      OFFENDER_NUMBER,
    )
    assertThatJsonFileAndStatus(response, 200, "offender-address.json")
  }

  @Test
  fun testCanGenerateNextNomisIdSequence() {
    val token = authTokenHelper.getToken(CREATE_BOOKING_USER)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/offenders/next-sequence",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
    )
    assertThatStatus(response, 200)
  }

  @Test
  fun testFilterAdjudicationsByFindingCode() {
    val requestEntity = createHttpEntity(authTokenHelper.getToken(AuthToken.PRISON_API_USER), null, mapOf())
    val response = testRestTemplate.exchange(
      "/api/offenders/{offenderNumber}/adjudications?finding={findingCode}",
      GET,
      requestEntity,
      object : ParameterizedTypeReference<String?>() {},
      "A1181HH",
      "NOT_PROVEN",
    )
    assertThatJsonFileAndStatus(response, 200, "adjudications_by_finding_code.json")
  }

  @Test
  fun testInvalidMovedCellSubType() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val newCaseNote = mapOf(
      "type" to "MOVED_CELL",
      "subType" to "BEH1",
      "text" to "This is a test comment",
    )
    val httpEntity = createHttpEntity(token, newCaseNote)
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/case-notes",
      HttpMethod.POST,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
      OFFENDER_NUMBER,
    )
    assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
  }

  @Test
  fun testCreateMovedCellCaseNote() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val newCaseNote = mapOf(
      "type" to "MOVED_CELL",
      "subType" to "BEH",
      "text" to "This is a test comment",
    )
    val httpEntity = createHttpEntity(token, newCaseNote)
    val response = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/case-notes",
      HttpMethod.POST,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
      "A9876RS",
    )
    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
  }
}
