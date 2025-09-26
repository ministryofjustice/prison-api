package uk.gov.justice.hmpps.prison.api.resource.impl

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.hmpps.prison.api.model.Email
import uk.gov.justice.hmpps.prison.api.model.LegalStatus
import uk.gov.justice.hmpps.prison.api.model.OffenderLanguageDto
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeed
import uk.gov.justice.hmpps.prison.api.model.PhysicalMark
import uk.gov.justice.hmpps.prison.api.model.PrisonerSearchDetails
import uk.gov.justice.hmpps.prison.api.model.ProfileInformation
import uk.gov.justice.hmpps.prison.api.model.Telephone
import uk.gov.justice.hmpps.prison.dsl.NomisDataBuilder
import uk.gov.justice.hmpps.prison.dsl.OffenderBookingId
import uk.gov.justice.hmpps.prison.dsl.OffenderId
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation.NonDtoReleaseDateType
import java.time.LocalDate
import java.time.LocalDateTime

class PrisonerSearchResourceIntTest : ResourceTest() {

  @MockitoSpyBean
  lateinit var telemetryClient: TelemetryClient

  @Autowired
  private lateinit var builder: NomisDataBuilder

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
      webTestClient.getPrisonerSearchDetails("A1234AB")
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
            assertThat(lastMovementTime).isEqualTo("2018-10-01T17:00")
            assertThat(lastAdmissionTime).isEqualTo("2018-10-02T11:00")

            assertThat(previousPrisonId).isEqualTo("MDI")
            assertThat(previousPrisonLeavingDate).isEqualTo("2018-10-02T10:50")

            assertThat(legalStatus).isEqualTo(LegalStatus.SENTENCED)
            assertThat(recall).isTrue()
            assertThat(imprisonmentStatus).isEqualTo("SENT")
            assertThat(imprisonmentStatusDescription).isEqualTo("Adult Imprisonment Without Option")
            assertThat(convictedStatus).isEqualTo("Convicted")
            assertThat(receptionDate).isEqualTo(LocalDate.now().toString())
            assertThat(locationDescription).isEqualTo("Leeds")
            assertThat(latestLocationId).isEqualTo("LEI")
            assertThat(personalCareNeeds).containsExactly(
              PersonalCareNeed(
                -202L,
                "DISAB",
                "ND",
                "ON",
                "No Disability",
                "Comment",
                LocalDate.parse("2010-06-22"),
                LocalDate.parse("2110-06-22"),
                null,
              ),
            )
            assertThat(languages).containsExactly(
              OffenderLanguageDto(
                type = "PRIM",
                code = "ENG",
                readSkill = "P",
                writeSkill = "P",
                speakSkill = "P",
                interpreterRequested = false,
              ),
              OffenderLanguageDto(
                type = "PREF_SPEAK",
                code = "POL",
                readSkill = "N",
                writeSkill = null,
                speakSkill = "N",
                interpreterRequested = false,
              ),
              OffenderLanguageDto(
                type = "PREF_WRITE",
                code = "POL",
                readSkill = "N",
                writeSkill = "N",
                speakSkill = "N",
                interpreterRequested = true,
              ),
            )
            assertThat(imageId).isEqualTo(-2)
            assertThat(militaryRecord).isTrue()
          }
        }
      verify(telemetryClient).trackEvent(
        eq("getPrisonerDetails-previous-prison"),
        check {
          assertThat(it["previousReleasePrisonId"]).isEqualTo("MDI")
          assertThat(it["previousReleaseDate"]).isEqualTo("2018-10-02T10:50")
          assertThat(it["lastTransferPrisonId"]).isEqualTo("MDI")
          assertThat(it["lastTransferDate"]).isEqualTo("2018-10-02T10:50")
        },
        isNull(),
      )
    }

    @Test
    fun `should return more data not on previous test`() {
      webTestClient.getPrisonerSearchDetails("A1234AC")
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
            assertThat(militaryRecord).isFalse()
          }
        }
    }

    @Test
    fun `should return all identifiers and associated offender IDs`() {
      webTestClient.getPrisonerSearchDetails("A1234AL")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(offenderId).isEqualTo(-1012)
            assertThat(allIdentifiers).extracting(
              "type",
              "value",
              "offenderNo",
              "issuedDate",
              "caseloadType",
              "offenderId",
            )
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
      webTestClient.getPrisonerSearchDetails("A1234AA")
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
      webTestClient.getPrisonerSearchDetails("A1234AI")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(aliases).extracting(
              "title",
              "firstName",
              "lastName",
              "dob",
              "gender",
              "raceCode",
              "ethnicity",
              "createDate",
            )
              .containsExactlyInAnyOrder(
                tuple(
                  "Mr",
                  "CHESNEY",
                  "THOMSON",
                  LocalDate.parse("1980-01-02"),
                  "Male",
                  "M2",
                  "Mixed: White and Black African",
                  LocalDate.parse("2015-01-01"),
                ),
                tuple(
                  null,
                  "CHARLEY",
                  "THOMPSON",
                  LocalDate.parse("1998-11-01"),
                  "Male",
                  "W1",
                  "White: British",
                  LocalDate.parse("2015-02-01"),
                ),
              )
          }
        }
    }

    @Test
    fun `should return most serious offence`() {
      webTestClient.getPrisonerSearchDetails("A1234AA")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(mostSeriousOffence).isEqualTo("Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)")
          }
        }
    }

    @Test
    fun `should return all convicted offences`() {
      webTestClient.getPrisonerSearchDetails("A1234AL")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(allConvictedOffences)
              .extracting("bookingId", "offenceCode", "sentenceStartDate", "primarySentence")
              // ignores charges with no convicted result
              .containsExactlyInAnyOrder(
                tuple(-12L, "M1", LocalDate.parse("2017-07-05"), true),
                tuple(-13L, "M2", null, null),
                // Note that the duplicate for SYS/MERGE is included
                tuple(-13L, "M2", null, null),
              )
          }
        }
    }

    @Test
    fun `should handle unknown imprisonment status`() {
      webTestClient.getPrisonerSearchDetails("A1184MA")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(legalStatus).isNull()
            assertThat(imprisonmentStatus).isNull()
            assertThat(imprisonmentStatusDescription).isNull()
            assertThat(convictedStatus).isNull()
          }
        }
    }

    @Test
    fun `should return highest severity offence if multiple flagged as most serious`() {
      webTestClient.getPrisonerSearchDetails("A5577RS")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(mostSeriousOffence).isEqualTo("Common assault")
          }
        }
    }

    @Test
    fun `should return addresses`() {
      webTestClient.getPrisonerSearchDetails("A1234AI")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(addresses).extracting(
              "addressId",
              "addressType",
              "flat",
              "premise",
              "street",
              "locality",
              "town",
              "postalCode",
              "county",
              "startDate",
              "primary",
              "noFixedAddress",
            )
              .containsExactlyInAnyOrder(
                // test data has a -13L which is filtered out as it is a non-primary no fixed address record
                tuple(
                  -11L,
                  "Business Address",
                  "Flat 1",
                  "Brook Hamlets",
                  "Mayfield Drive",
                  "Nether Edge",
                  "Sheffield",
                  "B5",
                  "South Yorkshire",
                  LocalDate.parse("2015-10-01"),
                  false,
                  false,
                ),
                tuple(
                  -12L,
                  "Home Address",
                  null,
                  "9",
                  "Abbydale Road",
                  null,
                  "Sheffield",
                  null,
                  "South Yorkshire",
                  LocalDate.parse("2014-07-01"),
                  false,
                  false,
                ),
                tuple(
                  -10L,
                  "Home Address",
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  null,
                  LocalDate.parse("2017-03-01"),
                  true,
                  true,
                ),
              )
            addresses?.first { it.addressId == -11L }?.phones.also {
              assertThat(it).extracting("number", "type", "ext").containsExactlyInAnyOrder(
                tuple(
                  "0114 2345345",
                  "HOME",
                  "345",
                ),
              )
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
      webTestClient.getPrisonerSearchDetails("A1065AA")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(offenderId).isEqualTo(-1067L)
            assertThat(addresses).extracting("addressId").containsExactly(-23L)
          }
        }
    }

    @Test
    fun `should return phones where the active booking is for an alias`() {
      webTestClient.getPrisonerSearchDetails("A1065AA")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(offenderId).isEqualTo(-1067L)
            assertThat(phones).extracting("phoneId").containsExactly(-18L)
          }
        }
    }

    @Test
    fun `should return emails where the active booking is for an alias`() {
      webTestClient.getPrisonerSearchDetails("A1065AA")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(offenderId).isEqualTo(-1067L)
            assertThat(emailAddresses).extracting("email").containsExactly("prisoner@somewhere.com")
          }
        }
    }

    @Test
    fun `should return offender phones and emails`() {
      webTestClient.getPrisonerSearchDetails("A1234AI")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(phones).containsExactlyInAnyOrder(
              Telephone(-16L, "0114 878787", "HOME", "345"),
              Telephone(-17L, "07878 787878", "MOB", null),
            )
            assertThat(emailAddresses).containsExactlyInAnyOrder(
              Email(-7L, "prisoner@home.com"),
              Email(-8L, "prisoner@backup.com"),
            )
          }
        }
    }

    @Test
    fun `should return last modified physical attributes`() {
      webTestClient.getPrisonerSearchDetails("A1234AA")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(physicalAttributes?.heightCentimetres).isEqualTo(155)
            assertThat(physicalAttributes?.weightKilograms).isEqualTo(77)
          }
        }
    }

    @Test
    fun `should calculate recall flag true if charges and court case are active`() {
      webTestClient.getPrisonerSearchDetails("Z0020ZZ")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(recall).isEqualTo(true)
          }
        }
    }

    @Test
    fun `should calculate recall flag false if charge is inactive`() {
      webTestClient.getPrisonerSearchDetails("Z0021ZZ")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(recall).isEqualTo(false)
          }
        }
    }

    @Test
    fun `should calculate recall flag false if court case is inactive`() {
      webTestClient.getPrisonerSearchDetails("Z0022ZZ")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(recall).isEqualTo(false)
          }
        }
    }

    @Test
    fun `should return correct location data when prisoner is INACTIVE OUT`() {
      var offender: OffenderId? = null
      var booking: OffenderBookingId? = null
      try {
        builder.build {
          offender = offender {
            booking = booking(prisonId = "SYI", bookingInTime = LocalDateTime.parse("2012-07-03T00:00")) {
              transferOut(prisonId = "MDI", transferTime = LocalDateTime.parse("2012-07-04T00:00"))
              transferIn(receiveTime = LocalDateTime.parse("2012-07-05T00:00"))
              release(releaseTime = LocalDateTime.parse("2012-07-06T00:00"))
            }
          }
        }
        webTestClient.getPrisonerSearchDetails(offender!!.offenderNo)
          .consumeWith { response ->
            with(response.responseBody!!) {
              assertThat(offenderNo).isEqualTo(offender.offenderNo)
              assertThat(bookingId).isEqualTo(booking!!.bookingId)
              assertThat(status).isEqualTo("INACTIVE OUT")
              assertThat(inOutStatus).isEqualTo("OUT")
              assertThat(recall).isFalse()
              assertThat(lastAdmissionTime).isEqualTo("2012-07-05T00:00")
              assertThat(latestLocationId).isEqualTo("MDI")
              assertThat(lastMovementTypeCode).isEqualTo("REL")
              assertThat(lastMovementReasonCode).isEqualTo("CR")
              assertThat(lastMovementTime).isEqualTo("2012-07-06T00:00")
              assertThat(previousPrisonId).isNull()
              assertThat(previousPrisonLeavingDate).isNull()
            }
          }
      } finally {
        offender?.run {
          builder.deletePrisoner(offenderNo)
        }
      }
    }

    @Test
    fun `should return correct location data when prisoner has been a restricted patient`() {
      var offender: OffenderId? = null
      var booking: OffenderBookingId? = null
      val secureHospital = "ARNOLD"
      try {
        builder.build {
          offender = offender {
            booking = booking(
              prisonId = "SYI",
              bookingInTime = LocalDateTime.parse("2025-07-10T00:00"),
            ) {
              release(
                releaseTime = LocalDateTime.parse("2025-07-11T00:00"),
                movementReasonCode = "HP",
                toLocationCode = secureHospital,
              )
              recall(
                recallTime = LocalDateTime.parse("2025-07-12T00:00"),
                prisonId = "LEI",
                movementReasonCode = "L",
              )
            }
          }
        }
        webTestClient.getPrisonerSearchDetails(offender!!.offenderNo)
          .consumeWith { response ->
            with(response.responseBody!!) {
              assertThat(offenderNo).isEqualTo(offender.offenderNo)
              assertThat(bookingId).isEqualTo(booking!!.bookingId)
              assertThat(status).isEqualTo("ACTIVE IN")
              assertThat(inOutStatus).isEqualTo("IN")
              assertThat(lastAdmissionTime).isEqualTo("2025-07-12T00:00")
              assertThat(latestLocationId).isEqualTo("LEI")
              assertThat(lastMovementTypeCode).isEqualTo("ADM")
              assertThat(lastMovementReasonCode).isEqualTo("L")
              assertThat(lastMovementTime).isEqualTo("2025-07-12T00:00")
              assertThat(previousPrisonId).isEqualTo("SYI")
              assertThat(previousPrisonLeavingDate).isEqualTo("2025-07-11T00:00")
            }
          }
      } finally {
        offender?.run {
          builder.deletePrisoner(offenderNo)
        }
      }
    }

    @Test
    fun `should return correct location data when there is a transfer via court`() {
      var offender: OffenderId? = null
      var booking: OffenderBookingId? = null
      try {
        builder.build {
          offender = offender {
            booking = booking(
              prisonId = "LEI",
              bookingInTime = LocalDateTime.parse("2025-07-10T00:00"),
            ) {
              sendToCourt(
                releaseTime = LocalDateTime.parse("2025-07-11T00:00"),
                // toLocation = "",
                shouldReleaseBed = true,
                movementReasonCode = "CRT",
              )
              returnFromCourt(
                prisonId = "SYI",
                returnTime = LocalDateTime.parse("2025-07-12T00:00"),
                movementReasonCode = "TRNCRT",
              )
            }
          }
        }
        webTestClient.getPrisonerSearchDetails(offender!!.offenderNo)
          .consumeWith { response ->
            with(response.responseBody!!) {
              assertThat(offenderNo).isEqualTo(offender.offenderNo)
              assertThat(bookingId).isEqualTo(booking!!.bookingId)
              assertThat(status).isEqualTo("ACTIVE IN")
              assertThat(inOutStatus).isEqualTo("IN")
              assertThat(lastAdmissionTime).isEqualTo("2025-07-12T00:00")
              assertThat(latestLocationId).isEqualTo("SYI")
              assertThat(lastMovementTypeCode).isEqualTo("ADM")
              assertThat(lastMovementReasonCode).isEqualTo("TRNCRT")
              assertThat(lastMovementTime).isEqualTo("2025-07-12T00:00")
              assertThat(previousPrisonId).isEqualTo("LEI")
              assertThat(previousPrisonLeavingDate).isEqualTo("2025-07-11T00:00")
            }
          }
      } finally {
        offender?.run {
          builder.deletePrisoner(offenderNo)
        }
      }
    }

    @Test
    fun `should return minimum details if no booking`() {
      webTestClient.getPrisonerSearchDetails("A1234DD")
        .consumeWith { response ->
          with(response.responseBody!!) {
            assertThat(offenderNo).isEqualTo("A1234DD")
            assertThat(firstName).isEqualTo("JOHN")
            assertThat(lastName).isEqualTo("DOE")
            assertThat(dateOfBirth).isEqualTo(LocalDate.parse("1989-03-02"))
          }
        }
    }

    private fun WebTestClient.getPrisonerSearchDetails(offenderNo: String) = get()
      .uri("/api/prisoner-search/offenders/$offenderNo")
      .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<PrisonerSearchDetails>()
  }
}
