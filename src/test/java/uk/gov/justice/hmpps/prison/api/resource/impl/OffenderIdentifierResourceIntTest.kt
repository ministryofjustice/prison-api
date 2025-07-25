@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.HttpStatus
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse.builder
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifierCreateRequest
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifierUpdateRequest
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier.OffenderIdentifierPK
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifierRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.ReferenceDomainService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier as JpaOffenderIdentifier

class OffenderIdentifierResourceIntTest : ResourceTest() {

  @MockitoBean
  private lateinit var offenderIdentifierRepository: OffenderIdentifierRepository

  @MockitoBean
  private lateinit var offenderRepository: OffenderRepository

  @MockitoBean
  private lateinit var referenceDomainService: ReferenceDomainService

  @Nested
  @DisplayName("GET /api/offenders/{prisonerNumber}/offender-identifiers")
  inner class GetIdentifiers {
    val endpoint = "/api/offenders/A1234AA/offender-identifiers"

    @BeforeEach
    fun setup() {
      whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(anyString())).thenReturn(
        listOf(
          TEST_NINO,
          JpaOffenderIdentifier().apply {
            identifierType = "CRO"
            identifier = "CRO123456"
            issuedAuthorityText = "Court"
            issuedDate = LocalDate.parse("2015-01-01")
            caseloadType = "INST"
            createDatetime = LocalDateTime.parse("2015-01-01T12:00:00")
            rootOffenderId = 1
            offender = Offender().apply {
              id = 1
              offenderIdentifierPK = OffenderIdentifierPK().apply { offenderIdSeq = 2 }
              rootOffender = Offender().apply { id = 1L }
              addBooking(OffenderBooking().apply { bookingId = 1001 })
              nomsId = "A1234AA"
            }
          },

          JpaOffenderIdentifier().apply {
            identifierType = "CRO_ALIAS"
            identifier = "CRO654321"
            issuedAuthorityText = "Alias"
            issuedDate = LocalDate.parse("2015-01-01")
            caseloadType = "INST"
            createDatetime = LocalDateTime.parse("2015-01-01T00:00:00")
            rootOffenderId = 1
            offender = Offender().apply {
              id = 2
              offenderIdentifierPK = OffenderIdentifierPK().apply { offenderIdSeq = 1 }
              rootOffender = Offender().apply { id = 1 }
              addBooking(OffenderBooking().apply { bookingId = 1002 })
              nomsId = "A1234BC"
            }
          },
        ),
      )
    }

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri(endpoint)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if no override role`() {
      webTestClient.get().uri(endpoint)
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 when no privileges`() {
      // run with user that doesn't have access to the caseload
      val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", listOf(), mapOf())
      val response = testRestTemplate.exchange(
        endpoint,
        GET,
        requestEntity,
        ErrorResponse::class.java,
      )
      assertThat(response.body).isEqualTo(
        builder()
          .status(403)
          .userMessage("Access Denied")
          .build(),
      )
      verifyNoInteractions(offenderIdentifierRepository)
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri(endpoint)
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has ROLE_GLOBAL_SEARCH override role`() {
      webTestClient.get().uri(endpoint)
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return offender identifiers without aliases`() {
      val requestEntity =
        createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_VIEW_PRISONER_DATA"), mapOf())
      val responseEntity =
        testRestTemplate.exchange(endpoint, GET, requestEntity, Array<OffenderIdentifier>::class.java)
      assertThat(responseEntity.body).isNotNull
      assertThat(responseEntity.body!!.size).isEqualTo(2)
      assertThat(responseEntity.body!![0].type).isEqualTo("NINO")
      assertThat(responseEntity.body!![0].value).isEqualTo("QQ123456A")
      assertThat(responseEntity.body!![1].type).isEqualTo("CRO")
      assertThat(responseEntity.body!![1].value).isEqualTo("CRO123456")
    }

    @Test
    fun `should return offender identifiers with aliases`() {
      val requestEntity =
        createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_VIEW_PRISONER_DATA"), mapOf())
      val responseEntity = testRestTemplate.exchange(
        "$endpoint?includeAliases=true",
        GET,
        requestEntity,
        Array<OffenderIdentifier>::class.java,
      )
      assertThat(responseEntity.body).isNotNull
      assertThat(responseEntity.body!!.size).isEqualTo(3)
      assertThat(responseEntity.body!![0].type).isEqualTo("NINO")
      assertThat(responseEntity.body!![0].value).isEqualTo("QQ123456A")
      assertThat(responseEntity.body!![1].type).isEqualTo("CRO")
      assertThat(responseEntity.body!![1].value).isEqualTo("CRO123456")
      assertThat(responseEntity.body!![2].type).isEqualTo("CRO_ALIAS")
      assertThat(responseEntity.body!![2].value).isEqualTo("CRO654321")
    }
  }

  @Nested
  @DisplayName("GET /api/aliases/{offenderId}/offender-identifiers/{seqId}")
  inner class GetIdentifier {
    val endpoint = "/api/aliases/1/offender-identifiers/1"

    @BeforeEach
    fun setup() {
      whenever(offenderIdentifierRepository.findByOffenderIdAndOffenderIdSeq(1, 1)).thenReturn(
        Optional.of(TEST_NINO),
      )
    }

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri(endpoint)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if no override role`() {
      webTestClient.get().uri(endpoint)
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 when no privileges`() {
      // run with user that doesn't have access to the caseload
      val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", listOf(), mapOf())
      val response = testRestTemplate.exchange(
        endpoint,
        GET,
        requestEntity,
        ErrorResponse::class.java,
      )
      assertThat(response.body).isEqualTo(
        builder()
          .status(403)
          .userMessage("Access Denied")
          .build(),
      )
      verifyNoInteractions(offenderIdentifierRepository)
    }

    @Test
    fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri(endpoint)
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success when has ROLE_GLOBAL_SEARCH override role`() {
      webTestClient.get().uri(endpoint)
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return offender identifier`() {
      val requestEntity =
        createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_VIEW_PRISONER_DATA"), mapOf())
      val responseEntity = testRestTemplate.exchange(
        endpoint,
        GET,
        requestEntity,
        OffenderIdentifier::class.java,
      )
      assertThat(responseEntity.body).isNotNull
      assertThat(responseEntity.body!!.type).isEqualTo("NINO")
      assertThat(responseEntity.body!!.value).isEqualTo("QQ123456A")
    }

    @Test
    fun `should return 404 when identifier with sequence id not found`() {
      val requestEntity =
        createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_VIEW_PRISONER_DATA"), mapOf())
      val response = testRestTemplate.exchange(
        "/api/aliases/1/offender-identifiers/99",
        GET,
        requestEntity,
        ErrorResponse::class.java,
      )
      assertThat(response.body).isEqualTo(
        builder()
          .status(404)
          .userMessage("Offender identifier for alias (offenderId) 1 with sequence 99 not found")
          .developerMessage("Offender identifier for alias (offenderId) 1 with sequence 99 not found")
          .build(),
      )
    }

    @Test
    fun `should return 404 when offender alias not found`() {
      val requestEntity =
        createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_VIEW_PRISONER_DATA"), mapOf())
      val response = testRestTemplate.exchange(
        "/api/aliases/99/offender-identifiers/1",
        GET,
        requestEntity,
        ErrorResponse::class.java,
      )
      assertThat(response.body).isEqualTo(
        builder()
          .status(404)
          .userMessage("Offender identifier for alias (offenderId) 99 with sequence 1 not found")
          .developerMessage("Offender identifier for alias (offenderId) 99 with sequence 1 not found")
          .build(),
      )
    }
  }

  @Nested
  @DisplayName("POST /api/offenders/{prisonerNumber}/offender-identifiers")
  inner class AddIdentifiers {
    val endpoint = "/api/offenders/A1234AA/offender-identifiers"

    @BeforeEach
    fun setup() {
      whenever(offenderRepository.findLinkedToLatestBooking(anyString())).thenReturn(
        Optional.of(
          Offender.builder()
            .id(1L)
            .nomsId("A1234AA")
            .rootOffenderId(1L)
            .identifiers(ArrayList())
            .build(),
        ),
      )

      whenever(referenceDomainService.isReferenceCodeActive(anyString(), anyString())).thenReturn(true)

      whenever(offenderIdentifierRepository.saveAll<JpaOffenderIdentifier>(any())).thenReturn(
        listOf(TEST_NINO),
      )
    }

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.post().uri(endpoint)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when missing required role`() {
      val requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
        "ITAG_USER",
        listOf(),
        listOf(
          OffenderIdentifierCreateRequest(
            identifierType = "MERGED",
            identifier = "A9876ZZ",
            issuedAuthorityText = "PRISON",
          ),
        ),
      )
      val response = testRestTemplate.exchange(
        endpoint,
        POST,
        requestEntity,
        ErrorResponse::class.java,
      )
      assertThat(response.body).isEqualTo(
        builder()
          .status(403)
          .userMessage("Access Denied")
          .build(),
      )
    }

    @Test
    fun `should add identifier with required role`() {
      val requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
        "ITAG_USER",
        listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW"),
        listOf(
          OffenderIdentifierCreateRequest(
            identifierType = "MERGED",
            identifier = "A9876ZZ",
            issuedAuthorityText = "PRISON",
          ),
        ),
      )
      val response = testRestTemplate.exchange(
        endpoint,
        POST,
        requestEntity,
        Void::class.java,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
    }
  }

  @Nested
  @DisplayName("PUT /api/aliases/{offenderId}/offender-identifiers/{seqId}")
  inner class UpdateIdentifier {
    val endpoint = "/api/aliases/1/offender-identifiers/1"

    @BeforeEach
    fun setup() {
      val testOffender = Offender().apply {
        id = 1
        nomsId = "A1234AA"
      }

      whenever(offenderIdentifierRepository.findByOffenderIdAndOffenderIdSeq(1, 1)).thenReturn(
        Optional.of(TEST_NINO),
      )

      whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(anyString())).thenReturn(
        listOf(TEST_NINO),
      )

      whenever(offenderRepository.findById(1)).thenReturn(Optional.of(testOffender))

      whenever(referenceDomainService.isReferenceCodeActive(anyString(), anyString())).thenReturn(true)
    }

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.put().uri(endpoint)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when missing required role`() {
      val requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
        "ITAG_USER",
        listOf(),
        OffenderIdentifierUpdateRequest(
          identifier = "QQ123456B",
          issuedAuthorityText = "Updated",
        ),
      )
      val response = testRestTemplate.exchange(
        endpoint,
        PUT,
        requestEntity,
        ErrorResponse::class.java,
      )
      assertThat(response.body).isEqualTo(
        builder()
          .status(403)
          .userMessage("Access Denied")
          .build(),
      )
    }

    @Test
    fun `should return 404 when identifier with sequence id not found`() {
      val requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
        "ITAG_USER",
        listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW"),
        OffenderIdentifierUpdateRequest(
          identifier = "QQ123456B",
          issuedAuthorityText = "Updated",
        ),
      )
      val response = testRestTemplate.exchange(
        "/api/aliases/1/offender-identifiers/99",
        PUT,
        requestEntity,
        ErrorResponse::class.java,
      )
      assertThat(response.body).isEqualTo(
        builder()
          .status(404)
          .userMessage("Offender identifier for alias (offenderId) 1 with sequence 99 not found")
          .developerMessage("Offender identifier for alias (offenderId) 1 with sequence 99 not found")
          .build(),
      )
    }

    @Test
    fun `should return 404 when offender alias not found`() {
      val requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
        "ITAG_USER",
        listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW"),
        OffenderIdentifierUpdateRequest(
          identifier = "QQ123456B",
          issuedAuthorityText = "Updated",
        ),
      )
      val response = testRestTemplate.exchange(
        "/api/aliases/99/offender-identifiers/1",
        PUT,
        requestEntity,
        ErrorResponse::class.java,
      )
      assertThat(response.body).isEqualTo(
        builder()
          .status(404)
          .userMessage("Offender identifier for alias (offenderId) 99 with sequence 1 not found")
          .developerMessage("Offender identifier for alias (offenderId) 99 with sequence 1 not found")
          .build(),
      )
    }

    @Test
    fun `should update identifier with required role`() {
      val requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
        "ITAG_USER",
        listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW"),
        OffenderIdentifierUpdateRequest(
          identifier = "QQ123456B",
          issuedAuthorityText = "Updated",
        ),
      )
      val response = testRestTemplate.exchange(
        endpoint,
        PUT,
        requestEntity,
        Void::class.java,
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
    }
  }

  private companion object {
    val TEST_NINO = JpaOffenderIdentifier().apply {
      identifierType = "NINO"
      identifier = "QQ123456A"
      issuedAuthorityText = "Test"
      issuedDate = LocalDate.parse("2015-01-01")
      caseloadType = "INST"
      createDatetime = LocalDateTime.parse("2015-01-01T12:00:00")
      rootOffenderId = 1
      offender = Offender().apply {
        id = 1
        offenderIdentifierPK = OffenderIdentifierPK().apply { offenderIdSeq = 1 }
        rootOffender = Offender().apply { id = 1L }
        addBooking(OffenderBooking().apply { bookingId = 1001 })
        nomsId = "A1234AA"
      }
    }
  }
}
