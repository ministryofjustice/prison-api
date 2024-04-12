package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.hmpps.prison.api.model.Alert
import uk.gov.justice.hmpps.prison.api.model.LegalStatus
import uk.gov.justice.hmpps.prison.api.model.PhysicalMark
import uk.gov.justice.hmpps.prison.api.model.PrisonerSearchDetails
import uk.gov.justice.hmpps.prison.api.model.ProfileInformation
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation.NonDtoReleaseDateType
import java.time.LocalDate

class PrisonerSearchResourceIntTest : ResourceTest() {

  @Nested
  @DisplayName("GET /api/prisoner-search/offenders/{offenderNo}")
  inner class OffenderDetails {

    @Test
    fun `should return 401 without an auth token`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1234AB")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 with no roles`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1234AB")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 with wrong role`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1234AB")
        .headers(setAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return ok with correct role`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1234AB")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return not found`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/UNKNOWN")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [UNKNOWN] not found.")
    }

    @Test
    fun `should return all data`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1234AB")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody<PrisonerSearchDetails>()
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(offenderNo).isEqualTo("A1234AB")
            assertThat(bookingId).isEqualTo(-2)
            assertThat(bookingNo).isEqualTo("A00112")
            assertThat(offenderId).isEqualTo(-1002)
            assertThat(rootOffenderId).isEqualTo(-1002)
            assertThat(firstName).isEqualTo("GILLIAN")
            assertThat(middleName).isEqualTo("EVE")
            assertThat(lastName).isEqualTo("ANDERSON")
            assertThat(dateOfBirth).isEqualTo("1998-08-28")
            assertThat(agencyId).isEqualTo("LEI")
            assertThat(alerts).extracting(Alert::getAlertType, Alert::getAlertCode).containsExactlyInAnyOrder(
              tuple("H", "HA"),
              tuple("X", "XTACT"),
            )
            with(assignedLivingUnit!!) {
              assertThat(agencyId).isEqualTo("LEI")
              assertThat(locationId).isEqualTo(-19)
              assertThat(description).isEqualTo("H-1-5")
              assertThat(agencyName).isEqualTo("Leeds")
            }
            with(physicalAttributes!!) {
              assertThat(gender).isEqualTo("Female")
              assertThat(raceCode).isEqualTo("W2")
              assertThat(ethnicity).isEqualTo("White: Irish")
              assertThat(weightPounds).isEqualTo(120)
              assertThat(weightKilograms).isEqualTo(55)
              assertThat(sexCode).isEqualTo("F")
            }
            assertThat(physicalCharacteristics).extracting("type", "characteristic", "detail").containsExactly(
              tuple("FACE", "Shape of Face", "Round"),
            )
            assertThat(profileInformation).containsExactlyInAnyOrder(
              ProfileInformation("RELF", "Religion", "Baptist"),
              ProfileInformation("SMOKE", "Is the Offender a smoker?", "No"),
              ProfileInformation("NAT", "Nationality?", "Israeli"),
            )
            assertThat(physicalMarks).isEmpty()
            assertThat(csra).isNull()
            assertThat(categoryCode).isNull()
            assertThat(inOutStatus).isEqualTo("IN")
            assertThat(identifiers).isEmpty()
            with(sentenceDetail!!) {
              assertThat(sentenceExpiryDate).isEqualTo("2019-05-21")
              assertThat(automaticReleaseDate).isEqualTo("2018-05-21")
              assertThat(releaseOnTemporaryLicenceDate).isEqualTo("2018-02-25")
              assertThat(bookingId).isEqualTo(-2)
              assertThat(sentenceStartDate).isEqualTo("2017-05-22")
              assertThat(automaticReleaseOverrideDate).isEqualTo("2018-04-21")
              assertThat(nonDtoReleaseDate).isEqualTo("2018-04-21")
              assertThat(nonDtoReleaseDateType).isEqualTo(NonDtoReleaseDateType.ARD)
              assertThat(confirmedReleaseDate).isEqualTo("2018-04-19")
              assertThat(releaseDate).isEqualTo("2018-04-19")
            }
            assertThat(mostSeriousOffenceDescription).isNull()
            assertThat(indeterminateSentence).isTrue()
            assertThat(aliases).isEmpty()
            assertThat(status).isEqualTo("ACTIVE IN")
            assertThat(lastMovementTypeCode).isEqualTo("ADM")
            assertThat(lastMovementReasonCode).isEqualTo("24")
            assertThat(legalStatus).isEqualTo(LegalStatus.SENTENCED)
            assertThat(recall).isTrue()
            assertThat(imprisonmentStatus).isEqualTo("SENT")
            assertThat(imprisonmentStatusDescription).isEqualTo("Adult Imprisonment Without Option")
            assertThat(receptionDate).isEqualTo(LocalDate.now().toString())
            assertThat(locationDescription).isEqualTo("Leeds")
            assertThat(latestLocationId).isEqualTo("LEI")
          }
        }
    }

    @Test
    fun `should return more data not on previous test`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1234AC")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody<PrisonerSearchDetails>()
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(offenderNo).isEqualTo("A1234AC")
            assertThat(physicalMarks).isEmpty()
            assertThat(csra).isEqualTo("Low")
            assertThat(categoryCode).isEqualTo("X")
            assertThat(identifiers).extracting("type", "value", "offenderNo", "issuedDate", "caseloadType")
              .containsExactly(
                tuple("CRO", "CRO112233", "A1234AC", LocalDate.parse("2017-07-13"), "INST"),
              )
            assertThat(mostSeriousOffenceDescription).isNull()
            assertThat(aliases).isEmpty()
          }
        }
    }

    @Test
    fun `should return physical marks`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1234AA")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody<PrisonerSearchDetails>()
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(physicalMarks).containsExactly(
              PhysicalMark(null, "Tattoo", null, "Torso", null, "Some Comment Text", null),
            )
          }
        }
    }

    @Test
    fun `should return aliases`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1234AI")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody<PrisonerSearchDetails>()
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(aliases).extracting("firstName", "lastName", "dob", "gender", "ethnicity", "createDate")
              .containsExactlyInAnyOrder(
                tuple(
                  "CHESNEY",
                  "THOMSON",
                  LocalDate.parse("1980-01-02"),
                  "Male",
                  "Mixed: White and Black African",
                  LocalDate.parse("2015-01-01"),
                ),
                tuple(
                  "CHARLEY",
                  "THOMPSON",
                  LocalDate.parse("1998-11-01"),
                  "Male",
                  "White: British",
                  LocalDate.parse("2015-02-01"),
                ),
              )
          }
        }
    }

    @Test
    fun `should return most serious offence`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1234AA")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody<PrisonerSearchDetails>()
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(mostSeriousOffenceDescription).isEqualTo("Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)")
          }
        }
    }

    @Test
    fun `should return minimum details if no booking`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1234DD")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody<PrisonerSearchDetails>()
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(offenderNo).isEqualTo("A1234DD")
            assertThat(offenderId).isEqualTo(-1056)
            assertThat(rootOffenderId).isEqualTo(-1056)
            assertThat(firstName).isEqualTo("JOHN")
            assertThat(lastName).isEqualTo("DOE")
            assertThat(dateOfBirth).isEqualTo(LocalDate.parse("1989-03-02"))
          }
        }
    }
  }
}
