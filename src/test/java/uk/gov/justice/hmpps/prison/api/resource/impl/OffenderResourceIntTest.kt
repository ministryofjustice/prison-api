package uk.gov.justice.hmpps.prison.api.resource.impl

import com.google.gson.Gson
import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Pageable.unpaged
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.hmpps.prison.api.model.CaseNote
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.IncidentCase
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.CREATE_BOOKING_USER
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.GLOBAL_SEARCH
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA
import uk.gov.justice.hmpps.prison.repository.MovementsRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CaseNoteFilter.builder
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository
import uk.gov.justice.hmpps.prison.util.DateTimeConverter
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.function.Function

@ContextConfiguration(classes = [OffenderResourceIntTest.TestClock::class])
class OffenderResourceIntTest : ResourceTest() {
  @Autowired
  lateinit var movementsRepository: MovementsRepository

  @Autowired
  lateinit var offenderCaseNoteRepository: OffenderCaseNoteRepository

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
  @DisplayName("GET /api/offenders/{offenderNo}")
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
  @DisplayName("GET /api/offenders/{offenderNo}/incidents")
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
    assertThat(response.statusCode).isEqualTo(OK)
    val result = response.body!!
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
    assertThat(response.statusCode).isEqualTo(OK)
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
    assertThat(response.statusCode).isEqualTo(FORBIDDEN)
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
      PUT,
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
      PUT,
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
      POST,
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
      POST,
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
      PUT,
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
    assertThat(caseNotes.body!!.content)
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
      POST,
      entity,
      object : ParameterizedTypeReference<String?>() {},
    )
    val offenderNo = Gson().fromJson<Map<*, *>>(createResponse.body, MutableMap::class.java)["offenderNo"]
    val newBookingBody = mapOf("prisonId" to "SYI", "fromLocationId" to "COURT1", "movementReasonCode" to "24", "youthOffender" to "true", "imprisonmentStatus" to "CUR_ORA", "cellLocation" to "SYI-A-1-1")
    val newBookingEntity = createHttpEntity(token, newBookingBody)
    val newBookingResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/booking",
      POST,
      newBookingEntity,
      object : ParameterizedTypeReference<String?>() {},
      offenderNo,
    )
    assertThat(newBookingResponse.statusCode.value()).isEqualTo(200)
    val releaseBody = createHttpEntity(token, mapOf("movementReasonCode" to "CR", "commentText" to "released prisoner incorrectly"))
    val releaseResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/release",
      PUT,
      releaseBody,
      object : ParameterizedTypeReference<String?>() {},
      offenderNo,
    )
    assertThat(releaseResponse.statusCode.value()).isEqualTo(200)

    // check that no new movement is created
    val latestMovement = movementsRepository.getMovementsByOffenders(listOf(offenderNo.toString()), null, true, false)[0]
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
      object : ParameterizedTypeReference<String?>() {},
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

    assertThat(caseNotes.body!!.content)
      .extracting(Function<CaseNote, Any> { obj: CaseNote -> obj.type }, Function<CaseNote, Any> { obj: CaseNote -> obj.subType }, Function<CaseNote, Any> { obj: CaseNote -> obj.agencyId }, Function<CaseNote, Any> { obj: CaseNote -> obj.text })
      .containsExactly(
        Tuple.tuple("TRANSFER", "FROMTOL", "SYI", "Offender admitted to SHREWSBURY for reason: Recall From Intermittent Custody from Court 1."),
        Tuple.tuple("PRISON", "RELEASE", "SYI", "Released from SHREWSBURY for reason: Conditional Release (CJA91) -SH Term>1YR."),
        Tuple.tuple("PRISON", "RELEASE", "SYI", "Transferred from SHREWSBURY for reason: Moved to psychiatric hospital Hazelwood House."),
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
      object : ParameterizedTypeReference<String?>() {},
    )
    val offenderNo = Gson().fromJson<Map<*, *>>(createResponse.body, MutableMap::class.java)["offenderNo"]
    val newBookingBody = mapOf("prisonId" to "SYI", "fromLocationId" to "COURT1", "movementReasonCode" to "24", "youthOffender" to "true", "imprisonmentStatus" to "CUR_ORA", "cellLocation" to "SYI-A-1-1")
    val newBookingEntity = createHttpEntity(token, newBookingBody)
    val newBookingResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/booking",
      POST,
      newBookingEntity,
      object : ParameterizedTypeReference<String?>() {},
      offenderNo,
    )
    assertThat(newBookingResponse.statusCode.value()).isEqualTo(200)
    val releaseBody = createHttpEntity(token, mapOf("movementReasonCode" to "HP", "commentText" to "released prisoner to hospital in NOMIS"))
    val releaseResponse = testRestTemplate.exchange(
      "/api/offenders/{nomsId}/release",
      PUT,
      releaseBody,
      object : ParameterizedTypeReference<String?>() {},
      offenderNo,
    )
    assertThat(releaseResponse.statusCode.value()).isEqualTo(200)
    val latestMovement = movementsRepository.getMovementsByOffenders(listOf(offenderNo.toString()), null, true, false)[0]
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
    assertThat(caseNotes.body!!.content)
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
      PUT,
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
    assertThat(caseNotes.body!!.content)
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
      PUT,
      entity,
      ErrorResponse::class.java,
      "Z0020ZZ",
    )
    val error = response.body!!
    assertThat(response.statusCode.value()).isEqualTo(400)
    assertThat(error.userMessage).contains("Booking -20 is not active")
  }

  @DisplayName("/api/offenders/{offenderNo}/addresses")
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
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -2 not found.")

      verify(telemetryClient).trackEvent(eq("UserUnauthorisedBookingAccess"), any(), isNull())
    }

    @Test
    fun `Attempt to get address for offender that does not exist`() {
      webTestClient.get().uri("/api/offenders/A1111ZZ/addresses")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
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
        object : ParameterizedTypeReference<String?>() {},
        OFFENDER_NUMBER,
      )
      assertThatJsonFileAndStatus(response, 200, "offender-address.json")
    }
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

  @Nested
  @DisplayName("PUT /api/offenders/{offenderNo}/case-notes/{caseNoteId}")
  inner class UpdateCaseNote {
    private val caseNoteUpdate =
      """ 
        {
          "type": "CHAP",
          "subType": "FAMMAR",
          "text" : "Hello this is a case note"
        }
      """

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.put().uri("/api/offenders/A1234AP/case-notes/34")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(caseNoteUpdate)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 if no user`() {
      webTestClient.put().uri("/api/offenders/A1234AP/case-notes/34")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(caseNoteUpdate)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Attempt to update case note for offender that is not part of any of logged on staff user's caseloads`() {
      webTestClient.put().uri("/api/offenders/A1234AP/case-notes/34")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(caseNoteUpdate)
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [A1234AP] not found.")
    }

    @Test
    fun `Attempt to update case note for offender that does not exist`() {
      webTestClient.put().uri("/api/offenders/A1111ZZ/case-notes/34")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(caseNoteUpdate)
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `validation error when update a case note with blank data`() {
      val caseNoteId = createCaseNote()

      webTestClient.put().uri("/api/offenders/A1176RS/case-notes/$caseNoteId")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .bodyValue(
          """ 
            {
              "type": "CHAP",
              "subType": "FAMMAR",
               "text" : " "
             }
            """,
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("updateCaseNote.newCaseNoteText: Case Note text is blank")

      removeCaseNoteCreated(caseNoteId)
    }

    @Test
    fun `Validation error when update a case note with which is too long`() {
      val caseNoteId = createCaseNote()

      val caseNoteText = StringUtils.repeat("a", 3950) // total text will be over 4000

      webTestClient.put().uri("/api/offenders/A1176RS/case-notes/$caseNoteId")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .bodyValue(
          """
            {
              "type": "CHAP",
              "subType": "FAMMAR",
              "text" : "$caseNoteText"
             }
           """,
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Length should not exceed 3880 characters")

      removeCaseNoteCreated(caseNoteId)
    }

    @Test
    fun `Validation error when update a case note when there is no space left`() {
      val caseNoteText = StringUtils.repeat("a", 3900)
      val caseNoteId = createCaseNote(text = caseNoteText)

      webTestClient.put().uri("/api/offenders/A1176RS/case-notes/$caseNoteId")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .bodyValue(
          """ 
            {
            "type": "CHAP",
                "subType": "FAMMAR",
                "text" : "$caseNoteText"
              }
              """,
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Amendments can no longer be made due to the maximum character limit being reached")

      removeCaseNoteCreated(caseNoteId)
    }

    @Test
    fun `A staff user can amend a case note they created`() {
      val caseNoteId = createCaseNote()
      val caseNoteText = StringUtils.repeat("z", 100)

      val resp = webTestClient.put().uri("/api/offenders/A1176RS/case-notes/$caseNoteId")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .bodyValue(
          """ {
                "type": "CHAP",
                "subType": "FAMMAR",
                "text" : "$caseNoteText"
              }
              """,
        )
        .exchange()
        .expectStatus().isOk

      val cn = resp.returnResult(CaseNote::class.java).responseBody.blockFirst()!!
      assertThat(cn.text).contains("Hello this is a new case note")
      assertThat(cn.text).contains(caseNoteText)

      removeCaseNoteCreated(caseNoteId)
    }

    @Test
    fun `A staff user cannot amend a case note that they did not create`() {
      val caseNoteText = StringUtils.repeat("a", 100)

      webTestClient.put().uri("/api/offenders/A1234AA/case-notes/-1")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .bodyValue(
          """ 
            {
              "type": "CHAP",
              "subType": "FAMMAR",
              "text" : "$caseNoteText"
            }
          """,
        )
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("User not authorised to amend case note.")
    }

    private fun createCaseNote(type: String = "CHAP", subType: String = "FAMMAR", text: String = "Hello this is a new case note", occurrenceDateTime: String? = null): Long {
      val newCaseNote = NewCaseNote()
      newCaseNote.type = type
      newCaseNote.subType = subType
      newCaseNote.text = text
      if (StringUtils.isNotBlank(occurrenceDateTime)) {
        newCaseNote.occurrenceDateTime = DateTimeConverter.fromISO8601DateTimeToLocalDateTime(occurrenceDateTime, ZoneOffset.UTC)
      }

      val resp = webTestClient.post().uri("/api/offenders/A1176RS/case-notes")
        .headers(setAuthorisation("ITAG_USER", listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(newCaseNote)
        .exchange()
        .expectStatus().isOk

      val cn = resp.returnResult(CaseNote::class.java).responseBody.blockFirst()!!
      return cn.caseNoteId
    }
  }

  @Nested
  @DisplayName("POST /api/offenders/{offenderNo}/case-notes")
  inner class CreateCaseNote {

    @Value("\${api.caseNote.sourceCode:AUTO}")
    lateinit var caseNoteSource: String

    private val caseNote =
      """ 
        {
          "type": "CHAP",
          "subType": "FAMMAR",
          "text" : "Hello this is a case note"
        }
      """

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.post().uri("/api/offenders/$OFFENDER_NUMBER/case-notes")
        .bodyValue(caseNote)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.post().uri("/api/offenders/$OFFENDER_NUMBER/case-notes")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(caseNote)
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Client not authorised to access booking with id -2.")
    }

    @Test
    fun `returns 400 when client has override role ROLE_ADD_CASE_NOTES but no user in context`() {
      webTestClient.post().uri("/api/offenders/$OFFENDER_NUMBER/case-notes")
        .headers(setClientAuthorisation(listOf("ROLE_ADD_CASE_NOTES")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "ACP",
              "subType": "PPR",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"
            }
            """,
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("createCaseNote.caseNote: CaseNote (type,subtype)=(ACP,PPR) does not exist")
    }

    @Test
    fun `Create case note success for offender not in user caseloads but has override role ROLE_SYSTEM_USER`() {
      val response = webTestClient.post().uri("/api/offenders/$OFFENDER_NUMBER/case-notes")
        .headers(setAuthorisation("WAI_USER", listOf("ROLE_SYSTEM_USER")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "GEN",
              "subType": "OSE",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isOk
        .expectBody(CaseNote::class.java).returnResult().responseBody

      removeCaseNoteCreated(response.caseNoteId)
    }

    @Test
    fun `Create case note success for offender not in user caseloads but has override role ROLE_ADD_CASE_NOTES`() {
      val response = webTestClient.post().uri("/api/offenders/A1234AP/case-notes")
        .headers(setAuthorisation("WAI_USER", listOf("ROLE_ADD_CASE_NOTES")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "GEN",
              "subType": "OSE",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isOk
        .expectBody(CaseNote::class.java).returnResult().responseBody

      removeCaseNoteCreated(response.caseNoteId)
    }

    @Test
    fun `returns 200 when has override role ROLE_ADD_CASE_NOTES`() {
      val response = webTestClient.post().uri("/api/offenders/$OFFENDER_NUMBER/case-notes")
        .headers(setAuthorisation(listOf("ROLE_ADD_CASE_NOTES")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(caseNote)
        .exchange()
        .expectStatus().isOk
        .expectBody(CaseNote::class.java).returnResult().responseBody

      removeCaseNoteCreated(response.caseNoteId)
    }

    @Test
    fun testCreateMovedCellCaseNote() {
      val caseNote = webTestClient.post().uri("/api/offenders/A9876RS/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "MOVED_CELL",
              "subType": "BEH",
              "text": "This is a test comment"
            }
          """,
        )
        .exchange()
        .expectStatus().isOk
        .expectBody(CaseNote::class.java).returnResult().responseBody

      removeCaseNoteCreated(caseNote.caseNoteId)
    }

    @Test
    fun `A case note is successfully created for an offender`() {
      val caseNote = webTestClient.post().uri("/api/offenders/A1176RS/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "OBSERVE",
              "subType": "OBS_GEN",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isOk
        .expectBody(CaseNote::class.java).returnResult().responseBody

      assertThat(caseNote.caseNoteId).isGreaterThan(0)
      assertThat(caseNote.source).isEqualTo(caseNoteSource)
      assertThat(caseNote.type).isEqualTo("OBSERVE")
      assertThat(caseNote.subType).isEqualTo("OBS_GEN")
      assertThat(caseNote.text).isEqualTo("A new case note")
      assertThat(caseNote.occurrenceDateTime).isEqualTo("2017-04-14T10:15:30")
      assertThat(caseNote.creationDateTime).isNotNull()

      removeCaseNoteCreated(caseNote.caseNoteId)
    }

    @Test
    fun `Multiple case notes successfully created for offender`() {
      assertThat(offenderCaseNoteRepository.findAll(builder().bookingId(-32).build(), unpaged())).size().isEqualTo(8)

      val caseNote1 = webTestClient.post().uri("/api/offenders/A1176RS/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "OBSERVE",
              "subType": "OBS_GEN",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isOk
        .returnResult(CaseNote::class.java).responseBody.blockFirst()

      assertThat(caseNote1.caseNoteId).isGreaterThan(0)
      assertThat(caseNote1.source).isEqualTo(caseNoteSource)

      val caseNote2 = webTestClient.post().uri("/api/offenders/A1176RS/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "OBSERVE",
              "subType": "OBS_GEN",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isOk
        .returnResult(CaseNote::class.java).responseBody.blockFirst()

      assertThat(offenderCaseNoteRepository.findAll(builder().bookingId(-32).build(), unpaged())).size().isEqualTo(10)

      removeCaseNoteCreated(caseNote1.caseNoteId)
      removeCaseNoteCreated(caseNote2.caseNoteId)
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
        "/api/offenders/{offenderNo}/case-notes",
        POST,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
        OFFENDER_NUMBER,
      )
      assertThat(response.statusCode).isEqualTo(BAD_REQUEST)
    }

    @Test
    fun `Validation error when create a case note with invalid type`() {
      webTestClient.post().uri("/api/offenders/A1176RS/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "doesnotexist",
              "subType": "OSE",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("createCaseNote.caseNote: CaseNote (type,subtype)=(doesnotexist,OSE) does not exist")
    }

    @Test
    fun `Validation error when create a case note with invalid subtype`() {
      webTestClient.post().uri("/api/offenders/A1176RS/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type":"GEN",
              "subType":"doesnotexist",
              "text":"A new case note",
              "occurrenceDateTime":"2017-04-14T10:15:30"
            }
          """,
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("createCaseNote.caseNote: CaseNote (type,subtype)=(GEN,doesnotexist) does not exist")
    }

    @Test
    fun `Validation error when create a case note with invalid combination of type and sub-type`() {
      webTestClient.post().uri("/api/offenders/A1176RS/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "DRR",
              "subType": "HIS",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("createCaseNote.caseNote: CaseNote (type,subtype)=(DRR,HIS) does not exist")
    }

    @Test
    fun `Validation error when create a case note with type and sub-type combination that is valid for different caseload but not current caseload`() {
      webTestClient.post().uri("/api/offenders/A1176RS/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "REC",
              "subType": "RECRP",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("createCaseNote.caseNote: CaseNote (type,subtype)=(REC,RECRP) does not exist")
    }

    @Test
    fun `Validation error when create a case note with type too long`() {
      webTestClient.post().uri("/api/offenders/A1176RS/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "toolongtoolongtoolong",
              "subType": "OSE",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("$.userMessage").value<String> { message ->
          assertThat(message).contains("createCaseNote.caseNote.type: Value is too long: max length is 12")
          assertThat(message).contains("createCaseNote.caseNote: CaseNote (type,subtype)=(toolongtoolongtoolong,OSE) does not exist")
        }
    }

    @Test
    fun `Validation error when create a case note with subtype too long`() {
      webTestClient.post().uri("/api/offenders/A1176RS/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "GEN",
              "subType": "toolongtoolongtoolong",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("$.userMessage").value<String> { message ->
          assertThat(message).contains("createCaseNote.caseNote.subType: Value is too long: max length is 12")
          assertThat(message).contains("createCaseNote.caseNote: CaseNote (type,subtype)=(GEN,toolongtoolongtoolong) does not exist")
        }
    }

    @Test
    fun `Validation error when create a case note with blank type`() {
      webTestClient.post().uri("/api/offenders/A1176RS/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "",
              "subType": "OSE",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("$.userMessage").value<String> { message ->
          assertThat(message).contains("createCaseNote.caseNote.type: Value cannot be blank")
          assertThat(message).contains("createCaseNote.caseNote: CaseNote (type,subtype)=(,OSE) does not exist")
        }
    }

    @Test
    fun `Validation error when create a case note with blank subtype`() {
      webTestClient.post().uri("/api/offenders/A1176RS/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "GEN",
              "subType": "",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("$.userMessage").value<String> { message ->
          assertThat(message).contains("createCaseNote.caseNote.subType: Value cannot be blank")
          assertThat(message).contains("createCaseNote.caseNote: CaseNote (type,subtype)=(GEN,) does not exist")
        }
    }

    @Test
    fun `Attempt to create case note for offender is not part of any of logged on staff user's caseloads`() {
      webTestClient.post().uri("/api/offenders/A1234AP/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "GEN",
              "subType": "OSE",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [A1234AP] not found.")
    }

    @Test
    fun `Attempt to create case note for offender that does not exist`() {
      webTestClient.post().uri("/api/offenders/A1111ZZ/case-notes")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "GEN",
              "subType": "OSE",
              "text": "A new case note",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
          """,
        )
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [A1111ZZ] not found.")
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

  private fun removeCaseNoteCreated(caseNoteId: Long) {
    val ocn = offenderCaseNoteRepository.findById(caseNoteId).get()
    offenderCaseNoteRepository.delete(ocn)
  }
}
