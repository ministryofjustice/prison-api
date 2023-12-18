package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeed
import uk.gov.justice.hmpps.prison.api.model.ReasonableAdjustment
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.repository.InmateRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtType
import uk.gov.justice.hmpps.prison.repository.jpa.model.DisciplinaryAction
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryBranch
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryDischarge
import uk.gov.justice.hmpps.prison.repository.jpa.model.MilitaryRank
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderMilitaryRecord
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPropertyContainer
import uk.gov.justice.hmpps.prison.repository.jpa.model.PropertyContainer
import uk.gov.justice.hmpps.prison.repository.jpa.model.WarZone
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class BookingResourceImplIntTest : ResourceTest() {
  @MockBean
  private lateinit var inmateRepository: InmateRepository

  @SpyBean
  private lateinit var bookingRepository: BookingRepository

  @MockBean
  private lateinit var offenderBookingRepository: OffenderBookingRepository

  @DisplayName("GET /api/bookings/{bookingId}/personal-care-needs")
  @Nested
  inner class PersonalCareNeeds {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-1/personal-care-needs?type=MATSTAT")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/bookings/-1/personal-care-needs?type=MATSTAT")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ROLE_GLOBAL_SEARCH override role`() {
      webTestClient.get().uri("/api/bookings/-1/personal-care-needs?type=MATSTAT")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri("/api/bookings/-1/personal-care-needs?type=MATSTAT")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun personalCareNeeds() {
      val bookingId = -1
      `when`(inmateRepository.findPersonalCareNeeds(ArgumentMatchers.anyLong(), ArgumentMatchers.anySet())).thenReturn(listOf(createPersonalCareNeeds()))
      val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
      val responseEntity = testRestTemplate.exchange("/api/bookings/-1/personal-care-needs?type=MATSTAT&type=DISAB+RM&type=DISAB+RC", GET, requestEntity, String::class.java)
      assertThatJsonFileAndStatus(responseEntity, 200, "personalcareneeds.json")
      verify(inmateRepository).findPersonalCareNeeds(bookingId.toLong(), setOf("DISAB", "MATSTAT"))
    }

    @Test
    fun personalCareNeeds_missingProblemType() {
      val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
      val responseEntity = testRestTemplate.exchange("/api/bookings/-1/personal-care-needs", GET, requestEntity, String::class.java)
      assertThatJsonFileAndStatus(responseEntity, 400, "personalcareneeds_validation.json")
    }
  }

  @DisplayName("POST /api/bookings/offenderNo/personal-care-needs")
  @Nested
  inner class GetMultipleOffenderPersonalCareNeeds {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.post().uri("/api/bookings/offenderNo/personal-care-needs?type=MATSTAT")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\" ]")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    @Disabled("this test fails - code/role update needed")
    fun `returns 403 when client has no override role`() {
      webTestClient.post().uri("/api/bookings/offenderNo/personal-care-needs?type=MATSTAT")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\" ]")
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun postPersonalCareNeedsForOffenders() {
      `when`(inmateRepository.findPersonalCareNeeds(ArgumentMatchers.anyList(), ArgumentMatchers.anySet())).thenReturn(createPersonalCareNeedsForOffenders())
      val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf(), listOf("A1234AA", "A1234AB", "A1234AC"))
      val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/personal-care-needs?type=MATSTAT&type=DISAB+RM&type=DISAB+RC", POST, requestEntity, String::class.java)
      assertThatJsonFileAndStatus(responseEntity, 200, "personalcareneeds_offenders.json")
      verify(inmateRepository).findPersonalCareNeeds(listOf("A1234AA", "A1234AB", "A1234AC"), setOf("DISAB", "MATSTAT"))
    }

    @Test
    fun postPersonalCareNeedsForOffenders_missingOffenders() {
      val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf(), listOf<Any>())
      val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/personal-care-needs?type=MATSTAT", POST, requestEntity, String::class.java)
      assertThatJsonFileAndStatus(responseEntity, 400, "personalcareneeds_offender_validation.json")
    }

    @Test
    fun postPersonalCareNeedsForOffenders_missingProblemType() {
      val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf(), listOf("A1234AA", "A1234AB", "A1234AC"))
      val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/personal-care-needs", POST, requestEntity, String::class.java)
      assertThatJsonFileAndStatus(responseEntity, 400, "personalcareneeds_validation.json")
    }

    @Test
    fun postPersonalCareNeedsForOffenders_emptyBody() {
      val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
      val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/personal-care-needs?type=MATSTAT", POST, requestEntity, String::class.java)
      assertThatStatus(responseEntity, 400)
      assertThat(responseEntity.body).contains("Malformed request")
    }
  }

  @DisplayName("POST /api/bookings/offenderNo/alerts")
  @Nested
  inner class OffenderAlerts {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.post().uri("/api/bookings/offenderNo/alerts")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\" ]")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client has no override role`() {
      webTestClient.post().uri("/api/bookings/offenderNo/alerts")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\" ]")
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when client has role ROLE_SYSTEM_USER`() {
      webTestClient.post().uri("/api/bookings/offenderNo/alerts")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\", \"A1234AF\" ]")
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has override role ROLE_GlOBAL_SEARCH`() {
      webTestClient.post().uri("/api/bookings/offenderNo/alerts")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\", \"A1234AF\" ]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(6)
    }

    @Test
    fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.post().uri("/api/bookings/offenderNo/alerts")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\"]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(4)
    }

    @Test
    fun `returns success when client has override role ROLE_CREATE_CATEGORISATION`() {
      webTestClient.post().uri("/api/bookings/offenderNo/alerts")
        .headers(setClientAuthorisation(listOf("ROLE_CREATE_CATEGORISATION")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\"]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(4)
    }

    @Test
    fun `returns success when client has override role ROLE_APPROVE_CATEGORISATION`() {
      webTestClient.post().uri("/api/bookings/offenderNo/alerts")
        .headers(setClientAuthorisation(listOf("ROLE_APPROVE_CATEGORISATION")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ \"A1234AA\"]")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(4)
    }

    @Test
    fun offenderAlerts_respondsWithOKWhenOffenderNumberSupplied() {
      val oneOffendersInRequest = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_VIEW_PRISONER_DATA"), listOf("A1234AA"))
      val minimumOfOneOffenderRequiredResponse = testRestTemplate.exchange("/api/bookings/offenderNo/alerts", POST, oneOffendersInRequest, String::class.java)
      assertThatStatus(minimumOfOneOffenderRequiredResponse, 200)
    }

    @Test
    fun offenderAlerts_respondsWithBadRequestWhenNoOffendersNumbersSupplied() {
      val noOffendersInRequest = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_VIEW_PRISONER_DATA"), listOf<Any>())
      val minimumOfOneOffenderRequiredResponse = testRestTemplate.exchange("/api/bookings/offenderNo/alerts", POST, noOffendersInRequest, String::class.java)
      assertThatStatus(minimumOfOneOffenderRequiredResponse, 400)
      assertThat(minimumOfOneOffenderRequiredResponse.body).contains("A minimum of one offender number is required")
    }

    @Test
    fun offenderAlerts_emptyBody() {
      val noOffendersInRequest = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_VIEW_PRISONER_DATA"), null)
      val minimumOfOneOffenderRequiredResponse = testRestTemplate.exchange("/api/bookings/offenderNo/alerts", POST, noOffendersInRequest, String::class.java)
      assertThatStatus(minimumOfOneOffenderRequiredResponse, 400)
      assertThat(minimumOfOneOffenderRequiredResponse.body).contains("Malformed request")
    }
  }

  @Test
  fun reasonableAdjustment() {
    val bookingId = -1
    val treatmentCodes = listOf("WHEELCHR_ACC", "PEEP")
    `when`(inmateRepository.findReasonableAdjustments(bookingId.toLong(), treatmentCodes)).thenReturn(
      listOf(
        ReasonableAdjustment("WHEELCHR_ACC", "abcd", LocalDate.of(2010, 6, 21), null, "LEI", "Leeds (HMP)", "Wheelchair accessibility", -202L),
        ReasonableAdjustment("PEEP", "efgh", LocalDate.of(2010, 6, 21), null, "LEI", "Leeds (HMP)", "Some other description", -202L),
      ),
    )
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/-1/reasonable-adjustments?type=WHEELCHR_ACC&type=PEEP", GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 200, "reasonableadjustment.json")
    verify(inmateRepository).findReasonableAdjustments(bookingId.toLong(), treatmentCodes)
  }

  private fun createPersonalCareNeeds(): PersonalCareNeed {
    return PersonalCareNeed.builder().personalCareNeedId(-201L).problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON").problemDescription("Preg, acc under 9mths").startDate(LocalDate.of(2010, 6, 21)).build()
  }

  private fun createPersonalCareNeedsForOffenders(): List<PersonalCareNeed> {
    return listOf(
      PersonalCareNeed.builder().personalCareNeedId(-201L).problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON")
        .problemDescription("Preg, acc under 9mths").commentText("P1")
        .startDate(LocalDate.parse("2010-06-21")).endDate(null).offenderNo("A1234AA").build(),
      PersonalCareNeed.builder().personalCareNeedId(-202L).problemType("DISAB").problemCode("RM").problemStatus("ON")
        .problemDescription("No Disability").commentText("description 1")
        .startDate(LocalDate.parse("2010-06-21")).endDate(null).offenderNo("A1234AA").build(),
      PersonalCareNeed.builder().personalCareNeedId(-203L).problemType("DISAB").problemCode("RC").problemStatus("ON")
        .problemDescription("No Disability").commentText(null)
        .startDate(LocalDate.parse("2010-06-22")).endDate(null).offenderNo("A1234AB").build(),
      PersonalCareNeed.builder().personalCareNeedId(-204L).problemType("DISAB").problemCode("RC").problemStatus("ON")
        .problemDescription("No Disability").commentText(null)
        .startDate(LocalDate.parse("2010-06-22")).endDate(null).offenderNo("A1234AC").build(),
      PersonalCareNeed.builder().personalCareNeedId(-205L).problemType("DISAB").problemCode("ND").problemStatus("ON")
        .problemDescription("No Disability").commentText("description 2")
        .startDate(LocalDate.parse("2010-06-24")).endDate(null).offenderNo("A1234AD").build(),
    )
  }

  @Test
  fun reasonableAdjustment_missingTreatmentCodes() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/-1/reasonable-adjustments", GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 400, "reasonableadjustment_validation.json")
  }

  @Test
  fun visitBalances() {
    val offenderNo = "A1234AA"
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/$offenderNo/visit/balances", GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 200, "visitbalances.json")
  }

  @Test
  fun visitBalances_no_iep_adjustments() {
    val offenderNo = "A1234AB"
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val response = testRestTemplate.exchange("/api/bookings/offenderNo/$offenderNo/visit/balances", GET, requestEntity, String::class.java)
    assertThat(getBodyAsJsonContent<Any>(response)).extractingJsonPathNumberValue("$.remainingVo").isEqualTo(10)
    assertThat(getBodyAsJsonContent<Any>(response)).extractingJsonPathNumberValue("$.remainingPvo").isEqualTo(1)
    assertThat(getBodyAsJsonContent<Any>(response)).extractingJsonPathValue("$.latestPrivIepAdjustDate").isNull()
    assertThat(getBodyAsJsonContent<Any>(response)).extractingJsonPathValue("$.latestIepAdjustDate").isNull()
  }

  @Test
  fun visitBalances_invalidBookingId() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/-3/visit/balances", GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 404, "visitbalancesinvalidbookingid.json")
  }

  @Test
  fun visitBalances_allowNoContent() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/A1234AE/visit/balances?allowNoContent=true", GET, requestEntity, String::class.java)
    assertThatStatus(responseEntity, 204)
  }

  @Test
  fun visitBalances_noBalancesNotFound() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/A1234AE/visit/balances", GET, requestEntity, String::class.java)
    assertThatStatus(responseEntity, 404)
  }

  @Test
  fun offenderBalances() {
    val bookingId = -1
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/$bookingId/balances", GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 200, "offender_balance.json")
  }

  @Test
  fun events() {
    `when`(bookingRepository.getBookingActivities(ArgumentMatchers.anyLong(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(
      listOf(
        createEvent("act", "10:11:12"),
        createEvent("act", "08:59:50"),
      ),
    )
    `when`(bookingRepository.getBookingVisits(ArgumentMatchers.anyLong(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(
      listOf(createEvent("vis", "09:02:03")),
    )
    `when`(bookingRepository.getBookingAppointments(ArgumentMatchers.anyLong(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(
      listOf(createEvent("app", null)),
    )
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/-1/events", GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 200, "events.json")
  }

  @Test
  fun militaryRecords() {
    `when`(offenderBookingRepository.findById(ArgumentMatchers.anyLong())).thenReturn(
      Optional.of(
        OffenderBooking.builder()
          .militaryRecords(
            listOf(
              OffenderMilitaryRecord.builder()
                .startDate(LocalDate.parse("2000-01-01"))
                .endDate(LocalDate.parse("2020-10-17"))
                .militaryDischarge(MilitaryDischarge("DIS", "Dishonourable"))
                .warZone(WarZone("AFG", "Afghanistan"))
                .militaryBranch(MilitaryBranch("ARM", "Army"))
                .description("left")
                .unitNumber("auno")
                .enlistmentLocation("Somewhere")
                .militaryRank(MilitaryRank("LCPL_RMA", "Lance Corporal  (Royal Marines)"))
                .serviceNumber("asno")
                .disciplinaryAction(DisciplinaryAction("CM", "Court Martial"))
                .dischargeLocation("Sheffield")
                .build(),
              OffenderMilitaryRecord.builder()
                .startDate(LocalDate.parse("2001-01-01"))
                .militaryBranch(MilitaryBranch("NAV", "Navy"))
                .description("second record")
                .build(),
            ),
          )
          .build(),
      ),
    )
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/-1/military-records", GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 200, "military_records.json")
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/court-cases")
  inner class GetCourtCases {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/bookings/-1/court-cases")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have override role`() {
      webTestClient.get().uri("/api/bookings/-1/court-cases")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when has ROLE_GLOBAL_SEARCH override role`() {
      stubRepositoryCall()
      webTestClient.get().uri("/api/bookings/-1/court-cases")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      stubRepositoryCall()
      webTestClient.get().uri("/api/bookings/-1/court-cases")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun courtCases_returnsMatchingActiveCourtCase() {
      stubRepositoryCall()
      val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
      val responseEntity = testRestTemplate.exchange("/api/bookings/-1/court-cases", GET, requestEntity, String::class.java)
      assertThatJsonFileAndStatus(responseEntity, 200, "court_cases.json")
    }

    private fun stubRepositoryCall() {
      `when`(offenderBookingRepository.findById(-1L)).thenReturn(
        Optional.of(
          OffenderBooking.builder()
            .courtCases(
              listOf(
                OffenderCourtCase.builder()
                  .id(-1L)
                  .caseSeq(-1)
                  .beginDate(LocalDate.EPOCH)
                  .agencyLocation(
                    AgencyLocation.builder()
                      .id("MDI")
                      .active(true)
                      .type(AgencyLocationType.COURT_TYPE)
                      .description("Moorland")
                      .build(),
                  )
                  .legalCaseType(LegalCaseType("A", "Adult"))
                  .caseInfoPrefix("CIP")
                  .caseInfoNumber("CIN20177010")
                  .caseStatus(CaseStatus("A", "Active"))
                  .courtEvents(
                    listOf(
                      CourtEvent.builder()
                        .id(-1L)
                        .eventDate(LocalDate.EPOCH)
                        .startTime(LocalDate.EPOCH.atStartOfDay())
                        .courtLocation(
                          AgencyLocation.builder()
                            .id("COURT1")
                            .description("Court 1")
                            .type(AgencyLocationType.COURT_TYPE)
                            .courtType(CourtType("MC", "Mag Court"))
                            .active(true)
                            .build(),
                        )
                        .build(),
                    ),
                  )
                  .build(),
              ),
            )
            .build(),
        ),
      )
    }
  }

  @Test
  fun courtCases_returnsAllMatchingCourtCases() {
    `when`(offenderBookingRepository.findById(-1L)).thenReturn(
      Optional.of(
        OffenderBooking.builder()
          .courtCases(
            listOf(
              OffenderCourtCase.builder()
                .id(-1L)
                .caseSeq(-1)
                .beginDate(LocalDate.EPOCH)
                .agencyLocation(
                  AgencyLocation.builder()
                    .id("MDI")
                    .active(true)
                    .type(AgencyLocationType.COURT_TYPE)
                    .description("Moorland")
                    .build(),
                )
                .legalCaseType(LegalCaseType("A", "Adult"))
                .caseInfoPrefix("CIP")
                .caseInfoNumber("CIN20177010")
                .caseStatus(CaseStatus("A", "Active"))
                .courtEvents(
                  listOf(
                    CourtEvent.builder()
                      .id(-1L)
                      .eventDate(LocalDate.EPOCH)
                      .startTime(LocalDate.EPOCH.atStartOfDay())
                      .courtLocation(
                        AgencyLocation.builder()
                          .id("COURT1")
                          .description("Court 1")
                          .type(AgencyLocationType.COURT_TYPE)
                          .courtType(CourtType("MC", "Mag Court"))
                          .active(true)
                          .build(),
                      )
                      .build(),
                  ),
                )
                .build(),
              OffenderCourtCase.builder()
                .id(-2L)
                .caseSeq(-2)
                .beginDate(LocalDate.EPOCH)
                .agencyLocation(
                  AgencyLocation.builder()
                    .id("MDI")
                    .active(true)
                    .type(AgencyLocationType.COURT_TYPE)
                    .description("Moorland")
                    .build(),
                )
                .legalCaseType(LegalCaseType("A", "Adult"))
                .caseInfoPrefix("CIP")
                .caseInfoNumber("CIN20177010")
                .caseStatus(CaseStatus("I", "Inactive"))
                .courtEvents(
                  listOf(
                    CourtEvent.builder()
                      .id(-1L)
                      .eventDate(LocalDate.EPOCH)
                      .startTime(LocalDate.EPOCH.atStartOfDay())
                      .courtLocation(
                        AgencyLocation.builder()
                          .id("COURT1")
                          .description("Court 1")
                          .type(AgencyLocationType.COURT_TYPE)
                          .courtType(CourtType("MC", "Mag Court"))
                          .active(true)
                          .build(),
                      )
                      .build(),
                  ),
                )
                .build(),
            ),
          )
          .build(),
      ),
    )
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/-1/court-cases?activeOnly=false", GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 200, "court_cases_active_and_inactive.json")
  }

  @Test
  fun propertyContainers() {
    val parentParentLocation = AgencyInternalLocation.builder().locationId(-1L).locationType("WING").agencyId("LEI")
      .currentOccupancy(null).operationalCapacity(13).description("LEI-A").userDescription("Block A").capacity(14)
      .certifiedFlag(true).locationCode("A").active(true).build()
    val parentLocation = AgencyInternalLocation.builder().locationId(-2L).locationType("LAND").agencyId("LEI").capacity(14)
      .currentOccupancy(null).operationalCapacity(13).description("LEI-A-1").parentLocation(parentParentLocation).userDescription("Landing A/1")
      .certifiedFlag(true).locationCode("1").active(true).build()
    `when`(offenderBookingRepository.findById(-1L)).thenReturn(
      Optional.of(
        OffenderBooking.builder()
          .propertyContainers(
            listOf(
              OffenderPropertyContainer.builder()
                .containerId(-1L)
                .active(true)
                .internalLocation(
                  AgencyInternalLocation.builder()
                    .locationId(-10L)
                    .active(true)
                    .locationType("CELL")
                    .agencyId("LEI")
                    .description("LEI-A-1-8")
                    .parentLocation(parentLocation)
                    .currentOccupancy(0)
                    .operationalCapacity(1)
                    .userDescription(null)
                    .locationCode("8")
                    .build(),
                )
                .sealMark("TEST10")
                .containerType(PropertyContainer("BULK", "Bulk"))
                .build(),
            ),
          )
          .build(),
      ),
    )
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/-1/property", GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 200, "offender_property_containers.json")
  }

  @Test
  fun offences() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_SYSTEM_USER"), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/A1234AA/offenceHistory?convictionsOnly=false", GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 200, "offender_offences.json")
  }

  @Test
  fun offencesNotAuthorised() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/A1234AA/offenceHistory?convictionsOnly=false", GET, requestEntity, String::class.java)
    assertThat(responseEntity.statusCode.value()).isEqualTo(403)
  }

  private fun createEvent(type: String, time: String?): ScheduledEvent {
    return ScheduledEvent.builder().bookingId(-1L)
      .startTime(Optional.ofNullable(time).map { t: String -> "2019-01-02T$t" }.map { text: String? -> LocalDateTime.parse(text) }.orElse(null))
      .eventType(type + time)
      .eventSubType("some sub $type")
      .build()
  }
}
