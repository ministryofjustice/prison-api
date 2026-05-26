package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode
import uk.gov.justice.hmpps.prison.api.model.HOCodeDto
import uk.gov.justice.hmpps.prison.api.model.OffenceActivationDto
import uk.gov.justice.hmpps.prison.api.model.OffenceDto
import uk.gov.justice.hmpps.prison.api.model.OffenceToScheduleMappingDto
import uk.gov.justice.hmpps.prison.api.model.Schedule
import uk.gov.justice.hmpps.prison.api.model.StatuteDto
import uk.gov.justice.hmpps.prison.api.resource.impl.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceIndicatorRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceRepository
import java.time.LocalDate

class OffenceResourceTest(
  @Autowired private val offenceIndicatorRepository: OffenceIndicatorRepository,
  @Autowired private val offenceRepository: OffenceRepository,
) : ResourceTest() {
  @Nested
  @DisplayName("Tests for the GET end point")
  inner class GeneralOffencesTests {
    @Test
    fun testCanFindOffencesByOffenceCode() {
      val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)

      val httpEntity = createHttpEntity(token, null)

      val response = testRestTemplate.exchange(
        "/api/offences/code/m?size=20",
        HttpMethod.GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {
        },
      )

      assertThatJsonFileAndStatus(response, 200, "paged_offences_start_with_m.json")
    }
  }

  @Nested
  @DisplayName("Tests creation of HO Codes")
  inner class CreateHOCodesTests {
    private val maintainerToken = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER)

    @Test
    fun testWriteHoCode() {
      val hoCodeDto = HOCodeDto
        .builder()
        .code("123/45")
        .description("123/45")
        .build()
      val httpEntity = createHttpEntity(maintainerToken, listOf(hoCodeDto))

      val response = postRequest(httpEntity, "/api/offences/ho-code")

      assertThatStatus(response, 201)
    }
  }

  @Nested
  @DisplayName("Tests creation of Statutes")
  inner class CreateStatutesTests {
    private val maintainerToken = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER)

    @Test
    fun testWriteStatute() {
      val statuteDto = StatuteDto
        .builder()
        .code("123/45")
        .description("123/45")
        .legislatingBodyCode("UK")
        .build()
      val httpEntity = createHttpEntity(maintainerToken, listOf(statuteDto))

      val response = postRequest(httpEntity, "/api/offences/statute")

      assertThatStatus(response, 201)
    }
  }

  @Nested
  @DisplayName("Tests creation and update of an offence")
  inner class CreateOrUpdateOffenceTests {
    private val maintainerToken = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER)
    private val statuteDto: StatuteDto = StatuteDto
      .builder()
      .code("9235")
      .description("9235")
      .legislatingBodyCode("UK")
      .activeFlag("Y")
      .build()

    private val hoCodeDto: HOCodeDto = HOCodeDto
      .builder()
      .code("923/99")
      .description("923/99")
      .activeFlag("Y")
      .build()

    private val offenceDto: OffenceDto = OffenceDto.builder()
      .code("2XX")
      .statuteCode(statuteDto)
      .hoCode(hoCodeDto)
      .description("2XX Description")
      .severityRanking("58")
      .activeFlag("Y")
      .build()

    @Sql(
      scripts = ["/sql/clean_offences.sql"],
      executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
      config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
    )
    @Test
    fun testCreateOffence() {
      val statuteHttpEntity = createHttpEntity(maintainerToken, listOf(statuteDto))
      val offenceHttpEntity = createHttpEntity(maintainerToken, listOf(offenceDto))
      val hoCodeEntity = createHttpEntity(maintainerToken, listOf(hoCodeDto))
      postRequest(statuteHttpEntity, "/api/offences/statute")
      postRequest(hoCodeEntity, "/api/offences/ho-code")

      val response = postRequest(offenceHttpEntity, "/api/offences/offence")
      assertThatStatus(response, 201)

      val getResponse = testRestTemplate.exchange(
        "/api/offences/code/2XX",
        HttpMethod.GET,
        createHttpEntity(maintainerToken, null),
        object : ParameterizedTypeReference<String>() {
        },
      )

      assertThatJsonFileAndStatus(getResponse, 200, "offence_after_create.json")
    }

    @Sql(
      scripts = ["/sql/clean_offences.sql"],
      executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
      config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
    )
    @Test
    fun testUpdateOffence() {
      val offenceDto = OffenceDto.builder()
        .code("M5")
        .statuteCode(StatuteDto.builder().code("RC86").build())
        .hoCode(HOCodeDto.builder().code("815/90").build())
        .description("Manslaughter Old UPDATED")
        .severityRanking("700")
        .activeFlag("N")
        .expiryDate(LocalDate.of(2020, 10, 13))
        .build()
      val offenceHttpEntity = createHttpEntity(maintainerToken, listOf(offenceDto))

      val response = putRequest(offenceHttpEntity)
      assertThatStatus(response, 204)

      val getResponse = testRestTemplate.exchange(
        "/api/offences/code/M5",
        HttpMethod.GET,
        createHttpEntity(maintainerToken, null),
        object : ParameterizedTypeReference<String>() {
        },
      )

      assertThatJsonFileAndStatus(getResponse, 200, "offence_after_update.json")
    }
  }

  @Nested
  @DisplayName("Tests linking and unlinking of offences to schedules")
  inner class LinkOffencesToSchedulesTests {
    @Sql(
      scripts = ["/sql/create_offence_data.sql"],
      executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
      config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
    )
    @Sql(
      scripts = ["/sql/clean_offences.sql"],
      executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
      config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
    )
    @Test
    fun testLinkAndUnlinkingOffences() {
      val mappingDtos = listOf(
        getMappingDto("COML025", Schedule.SCHEDULE_15),
        getMappingDto("STAT001", Schedule.SCHEDULE_13),
        getMappingDto("RC86355", Schedule.SCHEDULE_13),
        getMappingDto("COML025", Schedule.PCSC_SDS),
        getMappingDto("STAT001", Schedule.PCSC_SDS_PLUS),
        getMappingDto("RC86355", Schedule.PCSC_SEC_250),
      )
      linkOffencesToSchedules(mappingDtos)

      assertThat(doesMappingExist(Schedule.SCHEDULE_15, "COML025")).isTrue()
      assertThat(doesMappingExist(Schedule.SCHEDULE_13, "STAT001")).isTrue()
      assertThat(doesMappingExist(Schedule.SCHEDULE_13, "RC86355")).isTrue()
      assertThat(doesMappingExist(Schedule.PCSC_SDS, "COML025")).isTrue()
      assertThat(doesMappingExist(Schedule.PCSC_SDS_PLUS, "STAT001")).isTrue()
      assertThat(doesMappingExist(Schedule.PCSC_SEC_250, "RC86355")).isTrue()

      unlinkOffencesFromSchedules(mappingDtos)

      assertThat(doesMappingExist(Schedule.SCHEDULE_15, "COML025")).isFalse()
      assertThat(doesMappingExist(Schedule.SCHEDULE_13, "STAT001")).isFalse()
      assertThat(doesMappingExist(Schedule.SCHEDULE_13, "RC86355")).isFalse()
      assertThat(doesMappingExist(Schedule.PCSC_SDS, "COML025")).isFalse()
      assertThat(doesMappingExist(Schedule.PCSC_SDS_PLUS, "STAT001")).isFalse()
      assertThat(doesMappingExist(Schedule.PCSC_SEC_250, "RC86355")).isFalse()
    }

    private fun doesMappingExist(schedule: Schedule, offenceCode: String): Boolean = offenceIndicatorRepository.existsByIndicatorCodeAndOffenceCode(schedule.code, offenceCode)

    private fun getMappingDto(offenceCode: String?, schedule: Schedule?): OffenceToScheduleMappingDto = OffenceToScheduleMappingDto.builder().offenceCode(offenceCode).schedule(schedule).build()
  }

  @Nested
  @DisplayName("Tests for activating and deactivating of offences")
  inner class ActivateOrDeactivateOffencesTests {
    @Sql(
      scripts = ["/sql/create_active_and_inactive_offence.sql"],
      executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
      config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
    )
    @Sql(
      scripts = ["/sql/clean_offences.sql"],
      executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
      config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
    )
    @Test
    fun testActivateOffence() {
      val inactiveOffence = OffenceActivationDto("COML026", "COML", true)

      updateOffenceActiveFlag(inactiveOffence)

      val offence = offenceRepository.findById(Offence.PK("COML026", "COML")).get()
      assertThat(offence.code).isEqualTo("COML026")
      assertThat(offence.isActive).isTrue()
    }

    @Sql(
      scripts = ["/sql/create_active_and_inactive_offence.sql"],
      executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
      config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
    )
    @Sql(
      scripts = ["/sql/clean_offences.sql"],
      executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
      config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
    )
    @Test
    fun testDeactivateOffence() {
      val inactiveOffence = OffenceActivationDto("COML025", "COML", false)

      updateOffenceActiveFlag(inactiveOffence)

      val offence = offenceRepository.findById(Offence.PK("COML025", "COML")).get()
      assertThat(offence.code).isEqualTo("COML025")
      assertThat(offence.isActive).isFalse()
    }
  }

  private fun postRequest(httpEntity: HttpEntity<*>, url: String): ResponseEntity<String> = testRestTemplate.exchange(
    url,
    HttpMethod.POST,
    httpEntity,
    object : ParameterizedTypeReference<String>() {
    },
  )

  private fun linkOffencesToSchedules(mappings: List<OffenceToScheduleMappingDto>) {
    webTestClient.post().uri("/api/offences/link-to-schedule")
      .headers(setAuthorisation(listOf("ROLE_UPDATE_OFFENCE_SCHEDULES")))
      .bodyValue(mappings)
      .exchange()
      .expectStatus().isCreated()
  }

  private fun unlinkOffencesFromSchedules(mappings: List<OffenceToScheduleMappingDto>) {
    webTestClient.post().uri("/api/offences/unlink-from-schedule")
      .headers(setAuthorisation(listOf("ROLE_UPDATE_OFFENCE_SCHEDULES")))
      .bodyValue(mappings)
      .exchange()
      .expectStatus().isOk()
  }

  private fun putRequest(httpEntity: HttpEntity<*>): ResponseEntity<String> = testRestTemplate.exchange(
    "/api/offences/offence",
    HttpMethod.PUT,
    httpEntity,
    object : ParameterizedTypeReference<String>() {
    },
  )

  private fun updateOffenceActiveFlag(offenceActivationDto: OffenceActivationDto) {
    webTestClient.put().uri("/api/offences/update-active-flag")
      .headers(setAuthorisation(listOf("ROLE_NOMIS_OFFENCE_ACTIVATOR")))
      .bodyValue(offenceActivationDto)
      .exchange()
      .expectStatus()
      .isOk()
  }
}
