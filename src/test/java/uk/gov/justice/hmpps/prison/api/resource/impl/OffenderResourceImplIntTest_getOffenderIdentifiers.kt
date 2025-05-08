@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
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
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier.OffenderIdentifierPK
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifierRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.ReferenceDomainService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier as JpaOffenderIdentifier

class OffenderResourceImplIntTest_getOffenderIdentifiers : ResourceTest() {

  private fun stubRepositoryCallForGetIdentifiers() {
    whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(anyString())).thenReturn(
      listOf(
        JpaOffenderIdentifier.builder()
          .offenderIdentifierPK(OffenderIdentifierPK(1L, 1L))
          .offender(
            Offender.builder()
              .id(1L)
              .nomsId("A1234AA")
              .rootOffenderId(1L)
              .build(),
          )
          .identifierType("NINO")
          .identifier("QQ123456A")
          .issuedDate(LocalDate.parse("2015-01-01"))
          .rootOffenderId(1L)
          .caseloadType("INST")
          .issuedAuthorityText("Test")
          .createDateTime(LocalDateTime.parse("2015-01-01T12:00:00"))
          .build(),
        JpaOffenderIdentifier.builder()
          .offenderIdentifierPK(OffenderIdentifierPK(1L, 2L))
          .offender(
            Offender.builder()
              .id(1L)
              .nomsId("A1234AA")
              .rootOffenderId(1L)
              .build(),
          )
          .identifierType("CRO")
          .identifier("CRO123456")
          .issuedDate(LocalDate.parse("2015-01-01"))
          .rootOffenderId(1L)
          .caseloadType("INST")
          .issuedAuthorityText("Court")
          .createDateTime(LocalDateTime.parse("2015-01-01T12:00:00"))
          .build(),
      ),
    )
  }

  private fun stubRepositoryCallForGetIdentifier() {
    whenever(offenderIdentifierRepository.findByOffender_NomsIdAndOffenderIdentifierPK_OffenderIdSeq(anyString(), anyLong())).thenReturn(
      Optional.of(
        JpaOffenderIdentifier.builder()
          .offenderIdentifierPK(OffenderIdentifierPK(1L, 1L))
          .offender(
            Offender.builder()
              .id(1L)
              .nomsId("A1234AA")
              .rootOffenderId(1L)
              .build(),
          )
          .identifierType("NINO")
          .identifier("QQ123456A")
          .issuedDate(LocalDate.parse("2015-01-01"))
          .rootOffenderId(1L)
          .caseloadType("INST")
          .issuedAuthorityText("Test")
          .createDateTime(LocalDateTime.parse("2015-01-01T12:00:00"))
          .build(),
      ),
    )
  }

  private fun stubRepositoryCallForAddIdentifiers() {
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
      listOf(
        JpaOffenderIdentifier.builder()
          .offenderIdentifierPK(OffenderIdentifierPK(1L, 3L))
          .offender(
            Offender.builder()
              .id(1L)
              .nomsId("A1234AA")
              .rootOffenderId(1L)
              .build(),
          )
          .identifierType("MERGED")
          .identifier("A9876ZZ")
          .rootOffenderId(1L)
          .issuedAuthorityText("PRISON")
          .build(),
      ),
    )
  }

  private fun stubRepositoryCallForUpdateIdentifier() {
    whenever(offenderIdentifierRepository.findByOffender_NomsIdAndOffenderIdentifierPK_OffenderIdSeq(anyString(), anyLong())).thenReturn(
      Optional.of(
        JpaOffenderIdentifier.builder()
          .offenderIdentifierPK(OffenderIdentifierPK(1L, 1L))
          .offender(
            Offender.builder()
              .id(1L)
              .nomsId("A1234AA")
              .rootOffenderId(1L)
              .build(),
          )
          .identifierType("NINO")
          .identifier("QQ123456A")
          .issuedDate(LocalDate.parse("2015-01-01"))
          .rootOffenderId(1L)
          .caseloadType("INST")
          .issuedAuthorityText("Test")
          .createDateTime(LocalDateTime.parse("2015-01-01T12:00:00"))
          .build(),
      ),
    )

    whenever(offenderIdentifierRepository.findOffenderIdentifiersByOffender_NomsId(anyString())).thenReturn(
      listOf(
        JpaOffenderIdentifier.builder()
          .offenderIdentifierPK(OffenderIdentifierPK(1L, 1L))
          .offender(
            Offender.builder()
              .id(1L)
              .nomsId("A1234AA")
              .rootOffenderId(1L)
              .build(),
          )
          .identifierType("NINO")
          .identifier("QQ123456A")
          .issuedDate(LocalDate.parse("2015-01-01"))
          .rootOffenderId(1L)
          .caseloadType("INST")
          .issuedAuthorityText("Test")
          .createDateTime(LocalDateTime.parse("2015-01-01T12:00:00"))
          .build(),
      ),
    )

    whenever(referenceDomainService.isReferenceCodeActive(anyString(), anyString())).thenReturn(true)
  }

  @MockitoBean
  private lateinit var offenderIdentifierRepository: OffenderIdentifierRepository

  @MockitoBean
  private lateinit var offenderRepository: OffenderRepository

  @MockitoBean
  private lateinit var referenceDomainService: ReferenceDomainService

  @Test
  fun `should return 401 when user does not even have token`() {
    webTestClient.get().uri("/api/offenders/A1234AA/offender-identifiers")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 if no override role`() {
    webTestClient.get().uri("/api/offenders/A1234AA/offender-identifiers")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
    stubRepositoryCallForGetIdentifiers()
    webTestClient.get().uri("/api/offenders/A1234AA/offender-identifiers")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `should return success when has ROLE_GLOBAL_SEARCH override role`() {
    stubRepositoryCallForGetIdentifiers()
    webTestClient.get().uri("/api/offenders/A1234AA/offender-identifiers")
      .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun shouldReturnOffenderIdentifiers() {
    stubRepositoryCallForGetIdentifiers()
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_VIEW_PRISONER_DATA"), mapOf())
    val responseEntity = testRestTemplate.exchange(
      "/api/offenders/A1234AA/offender-identifiers",
      GET,
      requestEntity,
      Array<OffenderIdentifier>::class.java,
    )
    assertThat(responseEntity.body).isNotNull
    assertThat(responseEntity.body!!.size).isEqualTo(2)
    assertThat(responseEntity.body!![0].type).isEqualTo("NINO")
    assertThat(responseEntity.body!![0].value).isEqualTo("QQ123456A")
    assertThat(responseEntity.body!![1].type).isEqualTo("CRO")
    assertThat(responseEntity.body!![1].value).isEqualTo("CRO123456")
  }

  @Test
  fun shouldReturnOffenderIdentifier() {
    stubRepositoryCallForGetIdentifier()
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_VIEW_PRISONER_DATA"), mapOf())
    val responseEntity = testRestTemplate.exchange(
      "/api/offenders/A1234AA/offender-identifiers/1",
      GET,
      requestEntity,
      OffenderIdentifier::class.java,
    )
    assertThat(responseEntity.body).isNotNull
    assertThat(responseEntity.body!!.type).isEqualTo("NINO")
    assertThat(responseEntity.body!!.value).isEqualTo("QQ123456A")
  }

  @Test
  fun shouldReturn404WhenIdentifierNotFound() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_VIEW_PRISONER_DATA"), mapOf())
    val response = testRestTemplate.exchange(
      "/api/offenders/A1234AA/offender-identifiers/99",
      GET,
      requestEntity,
      ErrorResponse::class.java,
    )
    assertThat(response.body).isEqualTo(
      builder()
        .status(404)
        .userMessage("Offender identifier for prisoner A1234AA with sequence 99 not found")
        .developerMessage("Offender identifier for prisoner A1234AA with sequence 99 not found")
        .build(),
    )
  }

  @Test
  fun shouldReturn403WhenNoPrivileges() {
    // run with user that doesn't have access to the caseload
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", listOf(), mapOf())
    val response = testRestTemplate.exchange(
      "/api/offenders/A1234AA/offender-identifiers",
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
  fun `should return 403 when adding identifier without required role`() {
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
      "/api/offenders/A1234AA/offender-identifiers",
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
    stubRepositoryCallForAddIdentifiers()
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
      "/api/offenders/A1234AA/offender-identifiers",
      POST,
      requestEntity,
      Void::class.java,
    )
    assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
  }

  @Test
  fun `should return 403 when updating identifier without required role`() {
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
      "ITAG_USER",
      listOf(),
      OffenderIdentifierUpdateRequest(
        identifier = "QQ123456B",
        issuedAuthorityText = "Updated",
      ),
    )
    val response = testRestTemplate.exchange(
      "/api/offenders/A1234AA/offender-identifiers/1",
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
  fun `should update identifier with required role`() {
    stubRepositoryCallForUpdateIdentifier()
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody(
      "ITAG_USER",
      listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW"),
      OffenderIdentifierUpdateRequest(
        identifier = "QQ123456B",
        issuedAuthorityText = "Updated",
      ),
    )
    val response = testRestTemplate.exchange(
      "/api/offenders/A1234AA/offender-identifiers/1",
      PUT,
      requestEntity,
      Void::class.java,
    )
    assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
  }
}
