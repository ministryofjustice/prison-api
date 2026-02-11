package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeed
import uk.gov.justice.hmpps.prison.api.model.ReasonableAdjustment
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.repository.InmateRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtType
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPropertyContainer
import uk.gov.justice.hmpps.prison.repository.jpa.model.PropertyContainer
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.service.ReferenceDomainService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

class BookingResourceImplIntTest : ResourceTest() {
  @MockitoBean
  private lateinit var inmateRepository: InmateRepository

  @MockitoSpyBean
  private lateinit var bookingRepository: BookingRepository

  @MockitoBean
  private lateinit var offenderBookingRepository: OffenderBookingRepository

  @MockitoBean
  private lateinit var referenceDomainService: ReferenceDomainService

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
      whenever(inmateRepository.findPersonalCareNeeds(ArgumentMatchers.anyLong(), ArgumentMatchers.anySet())).thenReturn(listOf(createPersonalCareNeeds()))
      val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
      val responseEntity = testRestTemplate.exchange("/api/bookings/-1/personal-care-needs?type=MATSTAT&type=DISAB+RM&type=DISAB+RC", GET, requestEntity, String::class.java)
      assertThatJsonFileAndStatus(responseEntity, 200, "personalcareneeds.json")
      verify(inmateRepository).findPersonalCareNeeds(bookingId.toLong(), setOf("DISAB", "MATSTAT"))
    }

    @Test
    fun getAllpersonalCareNeeds() {
      val bookingId = -1
      val referenceCodesByDomain: List<ReferenceCode> = listOf(
        ReferenceCode.builder().code("DISAB").domain("domain").description("Description 1").activeFlag("Y").build(),
        ReferenceCode.builder().code("MATSTAT").domain("domain").description("Description 2").activeFlag("Y").build(),
      )
      whenever(referenceDomainService.getReferenceCodesByDomain("HEALTH")).thenReturn(referenceCodesByDomain)
      whenever(inmateRepository.findPersonalCareNeeds(ArgumentMatchers.anyLong(), ArgumentMatchers.anySet())).thenReturn(
        listOf(createPersonalCareNeeds()),
      )
      val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
      val responseEntity =
        testRestTemplate.exchange("/api/bookings/-1/personal-care-needs/all", GET, requestEntity, String::class.java)
      assertThatJsonFileAndStatus(responseEntity, 200, "personalcareneeds.json")
      verify(inmateRepository).findPersonalCareNeeds(bookingId.toLong(), setOf("DISAB", "MATSTAT"))
    }

    @Test
    fun getAllReasonableAdjustments() {
      val bookingId = -1
      val referenceCodesByDomain: List<ReferenceCode> = listOf(
        ReferenceCode.builder().code("WHEELCHR_ACC").domain("domain").description("Description 1").activeFlag("Y")
          .build(),
        ReferenceCode.builder().code("PEEP").domain("domain").description("Description 2").activeFlag("Y").build(),
      )
      whenever(referenceDomainService.getReferenceCodesByDomain("HEALTH_TREAT")).thenReturn(referenceCodesByDomain)
      val treatmentCodeStrings = referenceCodesByDomain.map { it.code }
      whenever(inmateRepository.findReasonableAdjustments(bookingId.toLong(), treatmentCodeStrings)).thenReturn(
        listOf(
          ReasonableAdjustment(
            "WHEELCHR_ACC",
            "abcd",
            LocalDate.of(2010, 6, 21),
            null,
            "LEI",
            "Leeds (HMP)",
            "Wheelchair accessibility",
            -202L,
          ),
          ReasonableAdjustment(
            "PEEP",
            "efgh",
            LocalDate.of(2010, 6, 21),
            null,
            "LEI",
            "Leeds (HMP)",
            "Some other description",
            -202L,
          ),
        ),
      )
      val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
      val responseEntity = testRestTemplate.exchange(
        "/api/bookings/-1/reasonable-adjustments/all",
        GET,
        requestEntity,
        String::class.java,
      )
      assertThatJsonFileAndStatus(responseEntity, 200, "reasonableadjustment.json")
      verify(inmateRepository).findReasonableAdjustments(bookingId.toLong(), treatmentCodeStrings)
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
      whenever(inmateRepository.findPersonalCareNeeds(ArgumentMatchers.anyList(), ArgumentMatchers.anySet())).thenReturn(createPersonalCareNeedsForOffenders())
      val requestEntity =
        createHttpEntity(clientToken(listOf("GLOBAL_SEARCH")), listOf("A1234AA", "A1234AB", "A1234AC"))
      val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/personal-care-needs?type=MATSTAT&type=DISAB+RM&type=DISAB+RC", POST, requestEntity, String::class.java)
      assertThatJsonFileAndStatus(responseEntity, 200, "personalcareneeds_offenders.json")
      verify(inmateRepository).findPersonalCareNeeds(listOf("A1234AA", "A1234AB", "A1234AC"), setOf("DISAB", "MATSTAT"))
    }

    @Test
    fun postPersonalCareNeedsForOffenders_missingOffenders() {
      val requestEntity =
        createHttpEntity(clientToken(listOf("GLOBAL_SEARCH")), emptyList<String>())
      val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/personal-care-needs?type=MATSTAT", POST, requestEntity, String::class.java)
      assertThatJsonFileAndStatus(responseEntity, 400, "personalcareneeds_offender_validation.json")
    }

    @Test
    fun postPersonalCareNeedsForOffenders_missingProblemType() {
      val requestEntity =
        createHttpEntity(clientToken(listOf("GLOBAL_SEARCH")), listOf("A1234AA", "A1234AB", "A1234AC"))
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

  @Test
  fun reasonableAdjustment() {
    val bookingId = -1
    val treatmentCodes = listOf("WHEELCHR_ACC", "PEEP")
    whenever(inmateRepository.findReasonableAdjustments(bookingId.toLong(), treatmentCodes)).thenReturn(
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

  private fun createPersonalCareNeeds(): PersonalCareNeed = PersonalCareNeed.builder().personalCareNeedId(-201L).problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON").problemDescription("Preg, acc under 9mths").startDate(LocalDate.of(2010, 6, 21)).build()

  private fun createPersonalCareNeedsForOffenders(): List<PersonalCareNeed> = listOf(
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
    whenever(bookingRepository.getBookingActivities(ArgumentMatchers.anyLong(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(
      listOf(
        createEvent("act", "10:11:12"),
        createEvent("act", "08:59:50"),
      ),
    )
    whenever(bookingRepository.getBookingVisits(ArgumentMatchers.anyLong(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(
      listOf(createEvent("vis", "09:02:03")),
    )
    whenever(bookingRepository.getBookingAppointments(ArgumentMatchers.anyLong(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(
      listOf(createEvent("app", null)),
    )
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/-1/events", GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 200, "events.json")
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
      whenever(offenderBookingRepository.findById(-1L)).thenReturn(
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
    whenever(offenderBookingRepository.findById(-1L)).thenReturn(
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
    whenever(offenderBookingRepository.findById(-1L)).thenReturn(
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
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_VIEW_PRISONER_DATA"), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/A1234AA/offenceHistory?convictionsOnly=false", GET, requestEntity, String::class.java)
    assertThatJsonFileAndStatus(responseEntity, 200, "offender_offences.json")
  }

  @Test
  fun offencesNotAuthorised() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf(), mapOf())
    val responseEntity = testRestTemplate.exchange("/api/bookings/offenderNo/A1234AA/offenceHistory?convictionsOnly=false", GET, requestEntity, String::class.java)
    assertThat(responseEntity.statusCode.value()).isEqualTo(403)
  }

  private fun createEvent(type: String, time: String?): ScheduledEvent = ScheduledEvent.builder().bookingId(-1L)
    .startTime(Optional.ofNullable(time).map { t: String -> "2019-01-02T$t" }.map { LocalDateTime.parse(it) }.orElse(null))
    .eventType(type + time)
    .eventSubType("some sub $type")
    .build()
}
