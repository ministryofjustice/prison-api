package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.hmpps.prison.api.model.Alert
import uk.gov.justice.hmpps.prison.api.model.Email
import uk.gov.justice.hmpps.prison.api.model.LegalStatus
import uk.gov.justice.hmpps.prison.api.model.PhysicalMark
import uk.gov.justice.hmpps.prison.api.model.PrisonerSearchDetails
import uk.gov.justice.hmpps.prison.api.model.ProfileInformation
import uk.gov.justice.hmpps.prison.api.model.Telephone
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation.NonDtoReleaseDateType
import uk.gov.justice.hmpps.prison.service.InmateService
import java.time.LocalDate

class PrisonerSearchResourceIntTest : ResourceTest() {

  @Autowired
  private lateinit var inmateService: InmateService

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
            assertThat(title).isEqualTo("Mrs")
            assertThat(firstName).isEqualTo("GILLIAN")
            assertThat(middleName).isEqualTo("EVE")
            assertThat(lastName).isEqualTo("ANDERSON")
            assertThat(dateOfBirth).isEqualTo("1998-08-28")
            assertThat(agencyId).isEqualTo("LEI")
            assertThat(alerts).extracting(Alert::getAlertId, Alert::getAlertType, Alert::getAlertCode).containsExactlyInAnyOrder(
              tuple(1L, "H", "HA"),
              tuple(2L, "X", "XTACT"),
            )
            with(assignedLivingUnit!!) {
              assertThat(agencyId).isEqualTo("LEI")
              assertThat(locationId).isEqualTo(-19)
              assertThat(description).isEqualTo("H-1-5")
              assertThat(agencyName).isEqualTo("Leeds")
            }
            assertThat(religion).isEqualTo("Baptist")
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
            assertThat(profileInformation).containsExactly(
              ProfileInformation("RELF", "Religion", "Baptist"),
              ProfileInformation("SMOKE", "Is the Offender a smoker?", "No"),
              ProfileInformation("NAT", "Nationality?", "Israeli"),
              ProfileInformation("CHILD", "Number of Children?", "3"),
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
              assertThat(confirmedReleaseDate).isEqualTo("2018-04-19") // TODO test where OFFENDER_RELEASE_DETAILS.RELEASE_DATE is null, this should be null and NOT default to AUTO_RELEASE_DATE
              assertThat(releaseDate).isEqualTo("2018-04-19")
            }
            assertThat(sentenceDetail?.additionalDaysAwarded).isNull()
            assertThat(mostSeriousOffence).isNull()
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
                tuple("PNC", "24/1234567L", "A1234AC", LocalDate.parse("2024-01-01"), "INST"),
              )
            assertThat(identifiers?.first()?.whenCreated?.toLocalDate()).isEqualTo(LocalDate.now())
            assertThat(mostSeriousOffence).isNull()
            assertThat(aliases).isEmpty()
          }
        }
    }

    @Test
    fun `should return all identifiers and associated offender IDs`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1234AL")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody<PrisonerSearchDetails>()
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(offenderId).isEqualTo(-1012)
            assertThat(allIdentifiers).extracting("type", "value", "offenderNo", "issuedDate", "caseloadType", "offenderId")
              .containsExactlyInAnyOrder(
                tuple("NINO", "DR784343E", "A1234AL", LocalDate.parse("2024-01-01"), "INST", -1012L),
                tuple("PNC", "2024/1234588L", "A1234AL", LocalDate.parse("2024-01-01"), "INST", -1012L),
                tuple("CRO", "SF99/12388M", "A1234AL", LocalDate.parse("2024-01-01"), "INST", -1013L),
                tuple("PNC", "24/1234588L", "A1234AL", LocalDate.parse("2024-01-01"), "INST", -1013L),
              )
            assertThat(aliases).extracting("firstName", "lastName", "offenderId")
              .containsExactly(
                tuple(
                  "DANNY",
                  "SMILEY",
                  -1013L,
                ),
              )
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
            assertThat(aliases).extracting("title", "firstName", "lastName", "dob", "gender", "ethnicity", "createDate")
              .containsExactlyInAnyOrder(
                tuple(
                  "Mr",
                  "CHESNEY",
                  "THOMSON",
                  LocalDate.parse("1980-01-02"),
                  "Male",
                  "Mixed: White and Black African",
                  LocalDate.parse("2015-01-01"),
                ),
                tuple(
                  null,
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
            assertThat(mostSeriousOffence).isEqualTo("Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)")
          }
        }
    }

    @Test
    fun `should handle unknown imprisonment status`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1184MA")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody<PrisonerSearchDetails>()
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(legalStatus).isNull()
            assertThat(imprisonmentStatus).isNull()
            assertThat(imprisonmentStatusDescription).isNull()
          }
        }
    }

    @Test
    fun `should return highest severity offence if multiple flagged as most serious`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A5577RS")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody<PrisonerSearchDetails>()
        .consumeWith { response ->
          with(response.responseBody!!) {
            // Note that this ignores charge M2=Common Assault with the highest severity - because it has AUDIT_MODULE_NAME = 'MERGE'
            assertThat(mostSeriousOffence).isEqualTo("Attempted Murder")
          }
        }
    }

    @Test
    fun `should return addresses`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1234AI")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody<PrisonerSearchDetails>()
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(addresses).extracting("addressId", "addressType", "flat", "premise", "street", "locality", "town", "postalCode", "county", "startDate", "primary")
              .containsExactlyInAnyOrder(
                tuple(-11L, "Business Address", "Flat 1", "Brook Hamlets", "Mayfield Drive", "Nether Edge", "Sheffield", "B5", "South Yorkshire", LocalDate.parse("2015-10-01"), false),
                tuple(-12L, "Home Address", null, "9", "Abbydale Road", null, "Sheffield", null, "South Yorkshire", LocalDate.parse("2014-07-01"), false),
              )
            addresses?.first { it.addressId == -11L }?.phones.also {
              assertThat(it).extracting("number", "type", "ext").containsExactlyInAnyOrder(tuple("0114 2345345", "HOME", "345"))
            }
            addresses?.first { it.addressId == -12L }?.phones.also {
              assertThat(it).extracting("number", "type", "ext").containsExactlyInAnyOrder(
                tuple("0114 2345345", "HOME", "345"),
                tuple("0114 2345346", "BUS", null),
              )
            }
          }
        }
    }

    @Test
    fun `should return addresses where the active booking is for an alias`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1065AA")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody<PrisonerSearchDetails>()
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(offenderId).isEqualTo(-1067L)
            assertThat(addresses).extracting("addressId").containsExactly(-23L)
          }
        }
    }

    @Test
    fun `should return offender phones and emails`() {
      webTestClient.get().uri("/api/prisoner-search/offenders/A1234AI")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody<PrisonerSearchDetails>()
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(phones).containsExactlyInAnyOrder(
              Telephone(-16L, "0114 878787", "HOME", "345"),
              Telephone(-17L, "07878 787878", "MOB", null),
            )
            assertThat(emailAddresses).containsExactlyInAnyOrder(
              Email("prisoner@home.com"),
              Email("prisoner@backup.com"),
            )
          }
        }
    }

    @Test
    fun `should return alerts in the same order as the Inmate Service`() {
      val inmateServiceAlerts = inmateService.findOffender("A1179MT", true, false).alerts

      webTestClient.get().uri("/api/prisoner-search/offenders/A1179MT")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody<PrisonerSearchDetails>()
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(alerts).containsExactlyElementsOf(inmateServiceAlerts)
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
            assertThat(firstName).isEqualTo("JOHN")
            assertThat(lastName).isEqualTo("DOE")
            assertThat(dateOfBirth).isEqualTo(LocalDate.parse("1989-03-02"))
          }
        }
    }
  }
}
