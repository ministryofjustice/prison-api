package uk.gov.justice.hmpps.prison.api.resource.impl

import org.apache.commons.lang3.StringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.CaseNote
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository
import uk.gov.justice.hmpps.prison.util.DateTimeConverter
import java.time.ZoneOffset

class BookingResourceIntTest_caseNotes : ResourceTest() {

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
        .jsonPath("totalElements").isEqualTo(4)
        .jsonPath("$.content[0].caseNoteId").isEqualTo(-5)
        .jsonPath("$.content[0].bookingId").isEqualTo(-2)
        .jsonPath("$.content[0].type").isEqualTo("COMMS")
        .jsonPath("$.content[0].typeDescription").isEqualTo("Communication")
        .jsonPath("$.content[0].subType").isEqualTo("COM_OUT")
        .jsonPath("$.content[0].subTypeDescription").isEqualTo("Communication OUT")
    }

    @Test
    fun testCanFilterCaseNotesByType() {
      webTestClient.get()
        .uri("/api/bookings/-2/caseNotes?type=ETE")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("totalElements").isEqualTo(1)
        .jsonPath("$.content[0].caseNoteId").isEqualTo(-4)
        .jsonPath("$.content[0].type").isEqualTo("ETE")
    }

    @Test
    fun testCanFilterCaseNotesBySubType() {
      webTestClient.get()
        .uri("/api/bookings/-2/caseNotes?type=COMMS&subType=COM_IN")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("totalElements").isEqualTo(1)
        .jsonPath("$.content[0].caseNoteId").isEqualTo(-2)
        .jsonPath("$.content[0].subType").isEqualTo("COM_IN")
    }

    @Test
    fun testCanFilterCaseNotesByDates() {
      webTestClient.get()
        .uri("/api/bookings/-2/caseNotes?from=2017-04-06&to=2017-05-05")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("totalElements").isEqualTo(2)
        .jsonPath("$.content[0].caseNoteId").isEqualTo(-4)
        .jsonPath("$.content[0].occurrenceDateTime").isEqualTo("2017-04-17T09:05:00")
        .jsonPath("$.content[1].occurrenceDateTime").isEqualTo("2017-04-11T18:42:00")
    }

    @Test
    fun testCanFilterCaseNotesByPrison() {
      webTestClient.get()
        .uri("/api/bookings/-3/caseNotes?prisonId=BXI")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("totalElements").isEqualTo(1)
        .jsonPath("$.content[0].agencyId").isEqualTo("BXI")
    }
  }

  @Nested
  @DisplayName("PUT /api/bookings/{bookingId}/caseNotes/{caseNoteId}")
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

      val cn = resp.returnResult(CaseNote::class.java).responseBody.blockFirst()!!
      return cn.caseNoteId
    }
  }

  @Nested
  @DisplayName("POST /api/bookings/{bookingId}/caseNotes/{caseNoteId}")
  inner class CreateCaseNote {

    @Value("\${api.caseNote.sourceCode:AUTO}")
    lateinit var caseNoteSource: String

    @Test
    fun `A case note is successfully created for booking`() {
      val resp = webTestClient.post().uri("/api/bookings/-32/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "OBSERVE",
              "subType": "OBS_GEN",
              "text": "A new case note (from Serenity BDD test **)",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
            """,
        )
        .exchange()
        .expectStatus().isCreated

      val caseNote = resp.returnResult(CaseNote::class.java).responseBody.blockFirst()!!
      assertThat(caseNote.caseNoteId).isGreaterThan(0)
      assertThat(caseNote.source).isEqualTo(caseNoteSource)
      assertThat(caseNote.type).isEqualTo("OBSERVE")
      assertThat(caseNote.subType).isEqualTo("OBS_GEN")
      assertThat(caseNote.text).isEqualTo("A new case note (from Serenity BDD test **)")
      assertThat(caseNote.occurrenceDateTime).isEqualTo("2017-04-14T10:15:30")
      assertThat(caseNote.creationDateTime).isNotNull()

      removeCaseNoteCreated(caseNote.caseNoteId)
    }

    @Test
    fun `A second case note is successfully created for booking`() {
      val resp = webTestClient.post().uri("/api/bookings/-32/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "OBSERVE",
              "subType": "OBS_GEN",
              "text": "A new case note (from Serenity BDD test **)",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
            """,
        )
        .exchange()
        .expectStatus().isCreated

      val caseNote = resp.returnResult(CaseNote::class.java).responseBody.blockFirst()!!
      assertThat(caseNote.caseNoteId).isGreaterThan(0)
      assertThat(caseNote.source).isEqualTo(caseNoteSource)

      removeCaseNoteCreated(caseNote.caseNoteId)
    }

    @Test
    fun `Validation error when create a case note with invalid type`() {
      webTestClient.post().uri("/api/bookings/-32/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "doesnotexist",
              "subType": "OSE",
              "text": "A new case note (from Serenity BDD test **)",
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
      webTestClient.post().uri("/api/bookings/-32/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type":"GEN",
              "subType":"doesnotexist",
              "text":"A new case note (from Serenity BDD test **)",
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
      webTestClient.post().uri("/api/bookings/-32/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "DRR",
              "subType": "HIS",
              "text": "A new case note (from Serenity BDD test **)",
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
      webTestClient.post().uri("/api/bookings/-32/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "REC",
              "subType": "RECRP",
              "text": "A new case note (from Serenity BDD test **)",
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
      webTestClient.post().uri("/api/bookings/-32/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "toolongtoolongtoolong",
              "subType": "OSE",
              "text": "A new case note (from Serenity BDD test **)",
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
      webTestClient.post().uri("/api/bookings/-32/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "GEN",
              "subType": "toolongtoolongtoolong",
              "text": "A new case note (from Serenity BDD test **)",
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
      webTestClient.post().uri("/api/bookings/-32/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "",
              "subType": "OSE",
              "text": "A new case note (from Serenity BDD test **)",
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
      webTestClient.post().uri("/api/bookings/-32/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "GEN",
              "subType": "",
              "text": "A new case note (from Serenity BDD test **)",
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
      webTestClient.post().uri("/api/bookings/-16/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "GEN",
              "subType": "OSE",
              "text": "A new case note (from Serenity BDD test **)",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
            """,
        )
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -16 not found.")
    }

    @Test
    fun `Attempt to create case note for offender that does not exist`() {
      webTestClient.post().uri("/api/bookings/-99/caseNotes")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
            {
              "type": "GEN",
              "subType": "OSE",
              "text": "A new case note (from Serenity BDD test **)",
              "occurrenceDateTime": "2017-04-14T10:15:30"      
            }
            """,
        )
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99 not found.")
    }
  }

  private fun removeCaseNoteCreated(caseNoteId: Long) {
    val ocn = offenderCaseNoteRepository.findById(caseNoteId).get()
    offenderCaseNoteRepository.delete(ocn)
  }

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/caseNotes/{type}/{subType}/count")
  inner class CaseNoteCount {

    @Test
    fun `returns 200 if has override ROLE_SYSTEM_USER`() {
      webTestClient.get()
        .uri("/api/bookings/-16/caseNotes/CHAP/FAMMAR/count")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 404 as ROLE_BANANAS is not override role`() {
      webTestClient.get()
        .uri("/api/bookings/-16/caseNotes/CHAP/FAMMAR/count")
        .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -16 not found.")
    }

    @Test
    fun `Case note count is requested for booking that is not part of any of logged on staff user's caseloads`() {
      webTestClient.get()
        .uri("/api/bookings/-16/caseNotes/CHAP/FAMMAR/count")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -16 not found.")
    }

    @Test
    fun `Case note count is requested for existing booking, using a fromDate later than toDate`() {
      webTestClient.get()
        .uri("/api/bookings/-1/caseNotes/CHAP/FAMMAR/count?fromDate=2017-09-18&toDate=2017-09-12")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Invalid date range: toDate is before fromDate.")
    }

    @Test
    fun `Case note count is 0 when no date params passed into request - uses last 3 months`() {
      webTestClient.get()
        .uri("/api/bookings/-1/caseNotes/CHAP/FAMMAR/count")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(0)
    }

    @Test
    fun `Case note count successfully received for same from and to date`() {
      webTestClient.get()
        .uri("/api/bookings/-1/caseNotes/CHAP/FAMMAR/count?fromDate=2000-01-01&toDate=2020-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(1)
        .jsonPath("bookingId").isEqualTo(-1)
        .jsonPath("type").isEqualTo("CHAP")
        .jsonPath("subType").isEqualTo("FAMMAR")
        .jsonPath("fromDate").isEqualTo("2000-01-01")
        .jsonPath("toDate").isEqualTo("2020-01-01")
    }

    @Test
    fun `Case note count of 0 received for same from and to date`() {
      webTestClient.get()
        .uri("/api/bookings/-2/caseNotes/CHAP/FAMMAR/count?fromDate=2000-01-01&toDate=2020-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(0)
    }

    @Test
    fun `Case note count of 8 received for same from and to date`() {
      webTestClient.get()
        .uri("/api/bookings/-3/caseNotes/OBSERVE/OBS_GEN/count?fromDate=2000-01-01&toDate=2020-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(8)
    }

    @Test
    fun `Case note count of 0 received for different from and to date`() {
      webTestClient.get()
        .uri("/api/bookings/-2/caseNotes/APP/OUTCOME/count?fromDate=2022-01-01&toDate=2023-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(0)
    }

    @Test
    fun `Case note count of 8 received for different from and to date`() {
      webTestClient.get()
        .uri("/api/bookings/-2/caseNotes/APP/OUTCOME/count?fromDate=2000-01-01&toDate=2020-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(1)
    }

    @Test
    fun `Case note count received when no to date passed into request`() {
      webTestClient.get()
        .uri("/api/bookings/-3/caseNotes/OBSERVE/OBS_GEN/count?fromDate=2017-08-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(2)
    }

    @Test
    fun `Case note count received when no from date passed into request`() {
      webTestClient.get()
        .uri("/api/bookings/-3/caseNotes/OBSERVE/OBS_GEN/count?toDate=2017-08-31")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(6)
    }

    @Test
    fun `Case note count of 4 received when from date includes case notes on that date`() {
      webTestClient.get()
        .uri("/api/bookings/-3/caseNotes/OBSERVE/OBS_GEN/count?fromDate=2017-07-10&toDate=2020-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(4)
    }

    @Test
    fun `Case note count of 4 received when to date includes case notes one day prior to that date`() {
      webTestClient.get()
        .uri("/api/bookings/-3/caseNotes/OBSERVE/OBS_GEN/count?fromDate=2000-01-02&toDate=2017-07-30")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(6)
    }
  }
}
