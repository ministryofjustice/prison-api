package uk.gov.justice.hmpps.prison.api.resource.impl

import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.CaseNote
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository
import uk.gov.justice.hmpps.prison.util.DateTimeConverter
import java.time.ZoneOffset

class BookingCaseNotesResourceIntTest : ResourceTest() {

  @Autowired
  lateinit var offenderCaseNoteRepository: OffenderCaseNoteRepository

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/caseNotes")
  inner class GetCaseNotes {
    @Test
    fun testCanRetrieveCaseNotes() {
      webTestClient.get()
        .uri("/api/bookings/-2/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("case_notes_offender_1.json".readFile())
    }

    @Test
    fun testCanFilterCaseNotesByType() {
      webTestClient.get()
        .uri("/api/bookings/-2/caseNotes?type=ETE")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("case_notes_offender_filter_by_type.json".readFile())
    }

    @Test
    fun testCanFilterCaseNotesBySubType() {
      webTestClient.get()
        .uri("/api/bookings/-2/caseNotes?type=COMMS&subType=COM_IN")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("case_notes_offender_filter_by_subtype.json".readFile())
    }

    @Test
    fun testCanFilterCaseNotesByDates() {
      webTestClient.get()
        .uri("/api/bookings/-2/caseNotes?from=2017-04-06&to=2017-05-05")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("case_notes_offender_filter_by_dates.json".readFile())
    }

    @Test
    fun testCanFilterCaseNotesByPrison() {
      webTestClient.get()
        .uri("/api/bookings/-3/caseNotes?prisonId=BXI")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("case_notes_offender_filter_by_prison.json".readFile())
    }
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/caseNotes/{caseNoteId}")
  inner class GetCaseNote {
    @Test
    fun `A specific case note is requested for booking that is not part of any of logged on staff user's caseloads`() {
      webTestClient.get()
        .uri("/api/bookings/-16/caseNotes/-1")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `A specific case note part of staff user's caseload is requested successfully`() {
      webTestClient.get()
        .uri("/api/bookings/-9/caseNotes/-90")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `A specific case note is requested for booking that does not exist`() {
      webTestClient.get()
        .uri("/api/bookings/-99/caseNotes/-1")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isNotFound
    }
  }

  @Nested
  @DisplayName("PUT /api/bookings/{bookingId}/caseNotes/{caseNoteId}")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  inner class UpdateCaseNotes {

    @Test
    fun `Attempt to update case note for offender that is not part of any of logged on staff user's caseloads`() {
      webTestClient.put().uri("/api/bookings/-16/caseNotes/34")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(
          BodyInserters.fromValue(
            """ {
                "type": "CHAP",
                "subType": "FAMMAR",
                "text" : "Hello this is a new case note"
              }
              """,
          ),
        )
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `Attempt to update case note for offender that does not exist`() {
      webTestClient.put().uri("/api/bookings/-99/caseNotes/34")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(
          BodyInserters.fromValue(
            """ {
                "type": "CHAP",
                "subType": "FAMMAR",
                "text" : "Hello this is a new case note"
              }
              """,
          ),
        )
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `validation error when update a case note with blank data`() {
      val caseNoteId = createCaseNote()

      webTestClient.put().uri("/api/bookings/-32/caseNotes/$caseNoteId")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .body(
          BodyInserters.fromValue(
            """ {
                "type": "CHAP",
                "subType": "FAMMAR",
                "text" : " "
              }
              """,
          ),
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

      webTestClient.put().uri("/api/bookings/-32/caseNotes/$caseNoteId")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .body(
          BodyInserters.fromValue(
            """ {
                "type": "CHAP",
                "subType": "FAMMAR",
                "text" : "$caseNoteText"
              }
              """,
          ),
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

      webTestClient.put().uri("/api/bookings/-32/caseNotes/$caseNoteId")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .body(
          BodyInserters.fromValue(
            """ {
                "type": "CHAP",
                "subType": "FAMMAR",
                "text" : "$caseNoteText"
              }
              """,
          ),
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

      val resp = webTestClient.put().uri("/api/bookings/-32/caseNotes/$caseNoteId")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .body(
          BodyInserters.fromValue(
            """ {
                "type": "CHAP",
                "subType": "FAMMAR",
                "text" : "$caseNoteText"
              }
              """,
          ),
        )
        .exchange()
        .expectStatus().isCreated

      val cn = resp.returnResult(CaseNote::class.java).responseBody.blockFirst()!!

      assertThat(cn.text).contains("Hello this is a new case note")
      assertThat(cn.text).contains(caseNoteText)

      removeCaseNoteCreated(caseNoteId)
    }

    @Test
    fun `A staff user cannot amend a case note that they did not create`() {
      val caseNoteText = StringUtils.repeat("a", 100)

      webTestClient.put().uri("/api/bookings/-1/caseNotes/-1")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .body(
          BodyInserters.fromValue(
            """ {
                "type": "CHAP",
                "subType": "FAMMAR",
                "text" : "$caseNoteText"
              }
              """,
          ),
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

      val resp = webTestClient.post().uri("/api/bookings/-32/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(
          BodyInserters.fromValue(newCaseNote),
        )
        .exchange()
        .expectStatus().isCreated

      val cn = resp.returnResult(CaseNote::class.java).responseBody.blockFirst()
      return cn.caseNoteId
    }

    private fun removeCaseNoteCreated(caseNoteId: Long) {
      val ocn = offenderCaseNoteRepository.findById(caseNoteId).get()
      offenderCaseNoteRepository.delete(ocn)
    }
  }

  internal fun String.readFile(): String = this@BookingCaseNotesResourceIntTest::class.java.getResource(this).readText()
}
