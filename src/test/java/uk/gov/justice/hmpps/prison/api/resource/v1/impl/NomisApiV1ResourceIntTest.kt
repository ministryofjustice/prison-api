package uk.gov.justice.hmpps.prison.api.resource.v1.impl

import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.whenever
import org.springframework.boot.test.json.JsonContent
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.ResolvableType
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import uk.gov.justice.hmpps.prison.api.model.v1.ContactList
import uk.gov.justice.hmpps.prison.api.model.v1.CreateTransaction
import uk.gov.justice.hmpps.prison.api.model.v1.Events
import uk.gov.justice.hmpps.prison.api.model.v1.Hold
import uk.gov.justice.hmpps.prison.api.model.v1.LiveRoll
import uk.gov.justice.hmpps.prison.api.model.v1.Offender
import uk.gov.justice.hmpps.prison.api.model.v1.StorePaymentRequest
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest
import uk.gov.justice.hmpps.prison.repository.v1.model.AccountTransactionSP
import uk.gov.justice.hmpps.prison.repository.v1.model.AliasSP
import uk.gov.justice.hmpps.prison.repository.v1.model.AvailableDatesSP
import uk.gov.justice.hmpps.prison.repository.v1.model.ContactPersonSP
import uk.gov.justice.hmpps.prison.repository.v1.model.EventSP
import uk.gov.justice.hmpps.prison.repository.v1.model.HoldSP
import uk.gov.justice.hmpps.prison.repository.v1.model.LiveRollSP
import uk.gov.justice.hmpps.prison.repository.v1.model.OffenderSP
import uk.gov.justice.hmpps.prison.repository.v1.model.UnavailabilityReasonSP
import uk.gov.justice.hmpps.prison.repository.v1.model.VisitSlotsSP
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.CoreProcs.GetActiveOffender
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.EventProcs
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.EventProcs.GetEvents
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.GetAccountBalances
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.GetAccountTransactions
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.GetHolds
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.GetTransactionByClientUniqueRef
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.PostStorePayment
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.PostTransaction
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.FinanceProcs.PostTransfer
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.OffenderProcs.GetOffenderDetails
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.OffenderProcs.GetOffenderImage
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.OffenderProcs.GetOffenderPssDetail
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.PrisonProcs.GetLiveRoll
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.StoreProcMetadata
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.VisitsProc.GetAvailableDates
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.VisitsProc.GetContactList
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.VisitsProc.GetUnavailability
import uk.gov.justice.hmpps.prison.repository.v1.storedprocs.VisitsProc.GetVisitSlotsWithCapacity
import java.math.BigDecimal
import java.sql.Blob
import java.sql.SQLException
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import javax.sql.rowset.serial.SerialBlob
import javax.sql.rowset.serial.SerialClob

class NomisApiV1ResourceIntTest : ResourceTest() {
  @MockBean
  private lateinit var postTransaction: PostTransaction

  @MockBean
  private lateinit var postTransfer: PostTransfer

  @MockBean
  private lateinit var offenderPssDetail: GetOffenderPssDetail

  @MockBean
  private lateinit var offenderDetails: GetOffenderDetails

  @MockBean
  private lateinit var offenderImage: GetOffenderImage

  @MockBean
  private lateinit var getHolds: GetHolds

  @MockBean
  private lateinit var getEvents: GetEvents

  @MockBean
  private lateinit var getLiveRoll: GetLiveRoll

  @MockBean
  private lateinit var postStorePayment: PostStorePayment

  @MockBean
  private lateinit var getAccountBalances: GetAccountBalances

  @MockBean
  private lateinit var getAccountTransactions: GetAccountTransactions

  @MockBean
  private lateinit var getTransactionByClientUniqueRef: GetTransactionByClientUniqueRef

  @MockBean
  private lateinit var getActiveOffender: GetActiveOffender

  @MockBean
  private lateinit var getAvailableDates: GetAvailableDates

  @MockBean
  private lateinit var getContactList: GetContactList

  @MockBean
  private lateinit var getUnavailability: GetUnavailability

  @MockBean
  private lateinit var getVisitSlotsWithCapacity: GetVisitSlotsWithCapacity

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  inner class SecureEndpoints {
    private fun secureGetEndpoints() =
      listOf(
        "/api/v1/offenders/events?prison_id=MDI&offender_id=A1492AE&event_type=e&from_datetime=2019-07-07 07:15:20.090&limit=100",
        "/api/v1/lookup/active_offender?noms_id=G0797UA&date_of_birth=1958-04-07",
        "/api/v1/offenders/G7806VO",
        "/api/v1/offenders/G7806VO/image",
        "/api/v1/offenders/G7806VO/location",
        "/api/v1/offenders/G7806VO/charges",
        "/api/v1/offenders/G7806VO/alerts",
        "/api/v1/offenders/G7806VO/pss_detail",
        "/api/v1/offenders/2425215/visits/available_dates?start_date=2077-01-01&end_date=2077-02-01",
        "/api/v1/offenders/2425215/visits/contact_list",
        "/api/v1/offenders/2425215/visits/unavailability?dates=2077-02-01",
        "/api/v1/prison/WLI/offenders/2425215/holds",
        "/api/v1/prison/WLI/live_roll",
        "/api/v1/prison/WLI/offenders/2425215/accounts",
        "/api/v1/prison/WLI/offenders/2425215/accounts/",
        "/api/v1/prison/WLI/offenders/2425215/accounts/spends/transactions",
        "/api/v1/prison/WLI/offenders/2425215/transactions/some-transactions",
        "/api/v1/prison/WLI/slots?start_date=2077-01-01&end_date=2077-02-01",
      )

    private fun securePostEndpoints() =
      listOf(
        "/api/v1/prison/WLI/offenders/G1408GC/transfer_transactions",
        "/api/v1/prison/WLI/offenders/G1408GC/transactions",
        "/api/v1/prison/WLI/offenders/G1408GC/payment",
      )

    @ParameterizedTest
    @MethodSource("secureGetEndpoints")
    internal fun `requires a valid authentication token`(uri: String) {
      webTestClient.get()
        .uri(uri)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @ParameterizedTest
    @MethodSource("secureGetEndpoints")
    internal fun `requires the correct role`(uri: String) {
      webTestClient.get()
        .uri(uri)
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    private val createTransaction = """
      {
        "type": "CANT",
        "description": "Canteen Purchase of £16.34",
        "amount": 1634,
        "client_transaction_id": "CL123212",
        "client_unique_ref": "CLIENT121131-0_11"
      }
    """

    @ParameterizedTest
    @MethodSource("securePostEndpoints")
    internal fun `requires a valid authentication token for post`(uri: String) {
      webTestClient.post()
        .uri(uri)
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(createTransaction)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @ParameterizedTest
    @MethodSource("securePostEndpoints")
    internal fun `requires the correct role - post`(uri: String) {
      webTestClient.post()
        .uri(uri)
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(createTransaction)
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Test
  fun transferTransaction() {
    val transaction = CreateTransaction()
    transaction.amount = 1234L
    transaction.clientUniqueRef = "clientRef"
    transaction.description = "desc"
    transaction.type = "type"
    transaction.clientTransactionId = "transId"

    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), transaction)

    whenever(postTransfer.execute(any(SqlParameterSource::class.java))).thenReturn(
      mapOf<String, Any>(
        StoreProcMetadata.P_TXN_ID to "someId",
        StoreProcMetadata.P_TXN_ENTRY_SEQ to "someSeq",
        StoreProcMetadata.P_CURRENT_AGY_DESC to "someDesc",
        StoreProcMetadata.P_CURRENT_AGY_LOC_ID to "someLoc",
      ),
    )

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/transfer_transactions", HttpMethod.POST, requestEntity, String::class.java)

    assertThatJson(responseEntity.body!!).isEqualTo("{current_location: {code: \"someLoc\", desc: \"someDesc\"}, transaction: {id:\"someId-someSeq\"}}")
  }

  @Test
  fun transferTransaction_unilink_role() {
    val transaction = CreateTransaction()
    transaction.amount = 1234L
    transaction.clientUniqueRef = "clientRef"
    transaction.description = "desc"
    transaction.type = "type"
    transaction.clientTransactionId = "transId"

    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_UNILINK"), transaction)

    whenever(postTransfer.execute(any(SqlParameterSource::class.java))).thenReturn(
      mapOf<String, Any>(
        StoreProcMetadata.P_TXN_ID to "someId",
        StoreProcMetadata.P_TXN_ENTRY_SEQ to "someSeq",
        StoreProcMetadata.P_CURRENT_AGY_DESC to "someDesc",
        StoreProcMetadata.P_CURRENT_AGY_LOC_ID to "someLoc",
      ),
    )

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/transfer_transactions", HttpMethod.POST, requestEntity, String::class.java)

    assertThatJson(responseEntity.body!!).isEqualTo("{current_location: {code: \"someLoc\", desc: \"someDesc\"}, transaction: {id:\"someId-someSeq\"}}")
  }

  @Test
  fun transferTransaction_wrong_role() {
    val transaction = CreateTransaction()
    transaction.amount = 1234L
    transaction.clientUniqueRef = "clientRef"
    transaction.description = "desc"
    transaction.type = "type"
    transaction.clientTransactionId = "transId"

    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_WRONG"), transaction)

    whenever(postTransfer.execute(any(SqlParameterSource::class.java))).thenReturn(
      mapOf<String, Any>(
        StoreProcMetadata.P_TXN_ID to "someId",
        StoreProcMetadata.P_TXN_ENTRY_SEQ to "someSeq",
        StoreProcMetadata.P_CURRENT_AGY_DESC to "someDesc",
        StoreProcMetadata.P_CURRENT_AGY_LOC_ID to "someLoc",
      ),
    )

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/transfer_transactions", HttpMethod.POST, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
  }

  @Test
  fun transferTransaction_duplicate() {
    val transaction = CreateTransaction()
    transaction.amount = 1234L
    transaction.clientUniqueRef = "clientRef"
    transaction.description = "desc"
    transaction.type = "type"
    transaction.clientTransactionId = "transId"

    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), transaction)

    whenever(postTransfer.execute(any(SqlParameterSource::class.java))).thenThrow(DuplicateKeyException("Duplicate key"))

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/transfer_transactions", HttpMethod.POST, requestEntity, String::class.java)

    assertThatJson(responseEntity.body!!).isEqualTo("{status: 409, userMessage: \"Duplicate key\", developerMessage: \"Duplicate key\"}")
    assertThat(responseEntity.statusCode).isEqualTo(CONFLICT)
  }

  @Test
  fun createTransaction() {
    val transaction = CreateTransaction()
    transaction.amount = 1234L
    transaction.clientUniqueRef = "clientRef"
    transaction.description = "desc"
    transaction.type = "type"
    transaction.clientTransactionId = "transId"

    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), transaction)

    whenever(postTransaction.execute(any(SqlParameterSource::class.java))).thenReturn(
      mapOf<String, Any>(
        StoreProcMetadata.P_TXN_ID to "someId",
        StoreProcMetadata.P_TXN_ENTRY_SEQ to "someSeq",
      ),
    )

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/transactions", HttpMethod.POST, requestEntity, String::class.java)

    assertThatJson(responseEntity.body!!).isEqualTo("{id:\"someId-someSeq\"}")
  }

  @Test
  @Throws(SQLException::class)
  fun getOffenderPssDetail() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)
    val eventData = """
          {
              "offender_details": {
                  "personal_details": {
                      "offender_surname": "ABDORIA",
                      "offender_given_name_1": "ONGMETAIN",
                      "offender_dob": "1990-12-06 00:00:00",
                      "gender": {
                          "code": "M",
                          "desc": "Male"
                      },
                      "religion": {
                          "code": "NIL",
                          "desc": "EfJSmIEfJSm"
                      },
                      "security_category": {
                          "code": "C",
                          "desc": "Cat C"
                      },
                      "nationality": {
                          "code": "BRIT",
                          "desc": "sxiVsxi"
                      },
                      "ethnicity": {
                          "code": "W1",
                          "desc": "White: Eng./Welsh/Scot./N.Irish/British"
                      }
                  },
                  "sentence_information": {
                      "reception_arrival_date_and_time": "2017-05-03 15:50:00",
                      "status": "Convicted",
                      "imprisonment_status": {
                          "code": "LR",
                          "desc": "Recalled to Prison from Parole (Non HDC)"
                      }
                  },
                  "location": {
                      "agency_location": "LEI",
                      "internal_location": "LEI-E-5-004",
                      "location_type": "CELL"
                  },
                  "warnings": [
                      {
                          "warning_type": {
                              "code": "P",
                              "desc": "MAPPP Case"
                          },
                          "warning_sub_type": {
                              "code": "P2",
                              "desc": "MAPPA Level 2 Case"
                          },
                          "warning_date": "2015-06-03 00:00:00",
                          "status": "ACTIVE"
                      },
                      {
                          "warning_type": {
                              "code": "R",
                              "desc": "Risk"
                          },
                          "warning_sub_type": {
                              "code": "RCS",
                              "desc": "Risk to Children - Custody"
                          },
                          "warning_date": "2013-06-04 00:00:00",
                          "status": "ACTIVE"
                      }
                  ],
                  "entitlement": {
                      "canteen_adjudication": false,
                      "iep_level": {
                          "code": "STD",
                          "desc": "Standard"
                      }
                  },
                  "case_details": {
                      "personal_officer": "Griffine, Ymmnatpher"
                  }
              }
          }
    """.trimIndent()

    val testClob = SerialClob(eventData.toCharArray())
    val timestamp = Timestamp.valueOf("2019-07-09 00:00:00.000")

    val procedureResponse = mapOf<String, Any>(
      StoreProcMetadata.P_NOMS_ID to "G7806VO",
      StoreProcMetadata.P_ROOT_OFFENDER_ID to 0L,
      StoreProcMetadata.P_SINGLE_OFFENDER_ID to "",
      StoreProcMetadata.P_AGY_LOC_ID to "LEI",
      StoreProcMetadata.P_DETAILS_CLOB to testClob,
      StoreProcMetadata.P_TIMESTAMP to timestamp,
    )

    whenever(offenderPssDetail.execute(any(SqlParameterSource::class.java))).thenReturn(procedureResponse)

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/G7806VO/pss_detail", GET, requestEntity, String::class.java)

    assertThatJsonFileAndStatus(responseEntity, 200, "pss-detail.json")
  }

  @Test
  fun offenderDetail() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    val expectedSurname = "HALIBUT"
    val alias = AliasSP()
    alias.lastName = "PLAICE"
    val procedureResponse = mapOf(
      StoreProcMetadata.P_OFFENDER_CSR to
        listOf(
          OffenderSP.builder().lastName(expectedSurname)
            .offenderAliases(listOf(alias))
            .build(),
        ) as Any,
    )

    whenever(offenderDetails.execute(any(SqlParameterSource::class.java))).thenReturn(procedureResponse)

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/A1404AE", GET, requestEntity, Offender::class.java)

    assertThatStatus(responseEntity, 200)

    val offenderActual = responseEntity.body as Offender

    assertThat(offenderActual).isNotNull()
    assertThat(offenderActual.surname).isNotNull()
    assertThat(offenderActual.surname).isEqualToIgnoringCase(expectedSurname)
    assertThat(offenderActual.aliases).hasSize(1)
  }

  @Test
  @Throws(SQLException::class)
  fun offenderImage() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    val imageBytes = "XXX".toByteArray()
    val blob: Blob = SerialBlob(imageBytes)
    val procedureResponse = mapOf(StoreProcMetadata.P_IMAGE to blob as Any)

    whenever(offenderImage.execute(any(SqlParameterSource::class.java))).thenReturn(procedureResponse)

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/A1404AE/image", GET, requestEntity, String::class.java)
    assertThatStatus(responseEntity, 200)

    // Encoded image returns this value for the test XXX value used
    val actualJson = responseEntity.body!!
    assertThatJson(actualJson).isEqualTo("{\"image\":\"WFhY\"}")
  }

  @Test
  fun getHolds() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    val holds = listOf(
      HoldSP(3L, "ref", "12345", "entry", null, BigDecimal("123.45"), null),
      HoldSP(4L, "ref2", "12346", "entry2", LocalDate.of(2019, 1, 2), BigDecimal("123.46"), LocalDate.of(2018, 12, 30)),
    )

    whenever(getHolds.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_HOLDS_CSR to holds))

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/holds", GET, requestEntity, String::class.java)

    assertThat(JsonContent<Hold>(javaClass, ResolvableType.forType(Hold::class.java), responseEntity.body!!)).isEqualToJson("holds.json")
  }

  @Test
  fun getHoldsWithClientReference() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", listOf("ROLE_NOMIS_API_V1"), mapOf("X-Client-Name" to "some-client"))

    val holds = listOf(
      HoldSP(3L, "ref", "12345", "entry", null, BigDecimal("123.45"), null),
      HoldSP(4L, "some-client-ref2", "12346", "entry2", LocalDate.of(2019, 1, 2), BigDecimal("123.46"), LocalDate.of(2018, 12, 30)),
    )

    val captor = ArgumentCaptor.forClass(SqlParameterSource::class.java)
    whenever(getHolds.execute(captor.capture())).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_HOLDS_CSR to holds))

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/holds?client_unique_ref=some-reference", GET, requestEntity, String::class.java)

    assertThat(JsonContent<Hold>(javaClass, ResolvableType.forType(Hold::class.java), responseEntity.body!!)).isEqualToJson("holds.json")

    assertThat(captor.value.getValue(StoreProcMetadata.P_CLIENT_UNIQUE_REF)).isEqualTo("some-client-some-reference")
  }

  @Test
  fun getEvents() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    val events = listOf(
      EventSP(
        3L,
        LocalDateTime.parse("2019-03-31T00:01:00.12456"),
        "LEI",
        "AB1256B",
        "ALERT",
        null,
        null,
        """
                    {"case_note":{"id":47004657,"contact_datetime":"2019-03-31 00:00:00"
                    ,"source":{"code":"AUTO"
                    ,"desc":"System"
                    },"type":{"code":"ALERT"
                    ,"desc":"Alert"
                    },"sub_type":{"code":"INACTIVE"
                    ,"desc":"Made Inactive"
                    },"staff_member":{"id":1,"name":"Cnomis, Admin&Onb"
                    ,"userid":""
                    },"text":"Alert Other and Charged under Harassment Act made inactive."
                    ,"amended":false}}
        """.trimIndent(),
      ),
      EventSP(
        4L,
        LocalDateTime.parse("2019-04-30T00:00:01.234567"),
        "MDI",
        "BC1256B",
        "INTERNAL_LOCATION_CHANGED",
        null,
        """
                    {"account":{"code":"REG"
                    ,"desc":"Private Cash"
                    },"balance":0}
        """.trimIndent(),
        null,
      ),
      EventSP(5L, LocalDateTime.parse("2019-03-31T00:00:01"), "MDI", "CD1256B", "PERSONAL_DETAILS_CHANGED", null, null, null),
    )

    val captor = ArgumentCaptor.forClass(SqlParameterSource::class.java)
    whenever(getEvents.execute(captor.capture())).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_EVENTS_CSR to events))

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/events?prison_id=MDI&offender_id=A1492AE&event_type=e&from_datetime=2019-07-07 07:15:20.090&limit=100", GET, requestEntity, String::class.java)

    assertThat(captor.value.getValue(StoreProcMetadata.P_AGY_LOC_ID)).isEqualTo("MDI")
    assertThat(captor.value.getValue(StoreProcMetadata.P_NOMS_ID)).isEqualTo("A1492AE")
    assertThat(captor.value.getValue(StoreProcMetadata.P_ROOT_OFFENDER_ID)).isNull()
    assertThat(captor.value.getValue(StoreProcMetadata.P_SINGLE_OFFENDER_ID)).isNull()
    assertThat(captor.value.getValue(EventProcs.P_EVENT_TYPE)).isEqualTo("e")
    assertThat(captor.value.getValue(EventProcs.P_FROM_TS)).isEqualTo(LocalDateTime.parse("2019-07-07T07:15:20.090"))
    assertThat(captor.value.getValue(EventProcs.P_LIMIT)).isEqualTo(100L)

    assertThat(JsonContent<Events>(javaClass, ResolvableType.forType(Events::class.java), responseEntity.body!!)).isEqualToJson("events.json")
  }

  @Test
  fun getEvents_WithSpacesBetweenEventData() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    val events = listOf(
      EventSP(
        3L,
        LocalDateTime.parse("2019-03-31T00:01:00.12456"),
        "LEI",
        "AB1256B",
        "ALERT",
        "{\"case_note\":{\"id\":47004657,\"contact_datetime\":\"2019-03-31 ",
        null,
        """
          00:00:00",
          "source":{"code":"AUTO" ,"desc":"System"},
          "type":{"code":"ALERT","desc":"Alert"},
          "sub_type":{"code":"INACTIVE","desc":"Made Inactive"},
          "staff_member":{"id":1,"name":"Cnomis, Admin&Onb","userid":""},
          "text":"Alert Other and Charged under Harassment Act made inactive.","amended":false}
          }
        """.trimIndent(),
      ),
      EventSP(
        4L,
        LocalDateTime.parse("2019-04-30T00:00:01.234567"),
        "MDI",
        "BC1256B",
        "INTERNAL_LOCATION_CHANGED",
        """
         {"account":{"code":"REG","desc":"Private
        """.trimIndent(),
        """ Cash" },"balance":0}""",
        null,
      ),
      EventSP(5L, LocalDateTime.parse("2019-03-31T00:00:01"), "MDI", "CD1256B", "PERSONAL_DETAILS_CHANGED", null, null, null),
    )

    whenever(getEvents.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_EVENTS_CSR to events))

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/events?prison_id=MDI&offender_id=A1492AE&event_type=e&from_datetime=2019-07-07 07:15:20.090&limit=100", GET, requestEntity, String::class.java)

    assertThat(JsonContent<Events>(javaClass, ResolvableType.forType(Events::class.java), responseEntity.body!!)).isEqualToJson("events.json")
  }

  @Test
  fun getLiveRoll() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    val roll = listOf(LiveRollSP("A12345B"), LiveRollSP("B23456C"))

    val captor = ArgumentCaptor.forClass(SqlParameterSource::class.java)
    whenever(getLiveRoll.execute(captor.capture())).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_ROLL_CSR to roll))

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/MDI/live_roll", GET, requestEntity, String::class.java)

    assertThat(captor.value.getValue(StoreProcMetadata.P_AGY_LOC_ID)).isEqualTo("MDI")

    assertThat(JsonContent<LiveRoll>(javaClass, ResolvableType.forType(LiveRoll::class.java), responseEntity.body!!)).isEqualToJson("roll.json")
  }

  @Test
  fun storePaymentOk() {
    val request = StorePaymentRequest.builder().type("ADJ").amount(1324L).clientTransactionId("CS123").description("Earnings for May").build()
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), request)

    // No response parameters for this method so return an emtpy map to satisfy Mockito stub
    whenever(postStorePayment.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf())

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/payment", HttpMethod.POST, requestEntity, String::class.java)

    assertThatJson(responseEntity.body!!).isEqualTo("{ \"message\": \"Payment accepted\"}")
  }

  @Test
  fun storePaymentInvalidDetailsSupplied() {
    // Invalid request - client transaction too long - 12 character max

    val request = StorePaymentRequest.builder().type("ADJ").amount(123L).clientTransactionId("This-is-too-long").description("bad payment args").build()
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), request)

    // No response parameters for this method so return an empty map to satisfy Mockito stub
    whenever(postStorePayment.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf())

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/payment", HttpMethod.POST, requestEntity, String::class.java)
    assertThatStatus(responseEntity, 400)
  }

  @Test
  fun getAccountBalances() {
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    whenever(getAccountBalances.execute(any(SqlParameterSource::class.java))).thenReturn(
      mapOf<String, Any>(
        StoreProcMetadata.P_CASH_BALANCE to BigDecimal("12.34"),
        StoreProcMetadata.P_SPENDS_BALANCE to BigDecimal("56.78"),
        StoreProcMetadata.P_SAVINGS_BALANCE to BigDecimal("34.34"),
      ),
    )

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/accounts", GET, requestEntity, String::class.java)

    assertThatJson(responseEntity.body!!).isEqualTo("{ \"spends\": 5678, \"savings\": 3434, \"cash\": 1234 }")
  }

  @Test
  fun getAccountBalancesTrailingSlash() {
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    whenever(getAccountBalances.execute(any(SqlParameterSource::class.java))).thenReturn(
      mapOf<String, Any>(
        StoreProcMetadata.P_CASH_BALANCE to BigDecimal("12.34"),
        StoreProcMetadata.P_SPENDS_BALANCE to BigDecimal("56.78"),
        StoreProcMetadata.P_SAVINGS_BALANCE to BigDecimal("34.34"),
      ),
    )

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/accounts/", GET, requestEntity, String::class.java)

    assertThatJson(responseEntity.body!!).isEqualTo("{ \"spends\": 5678, \"savings\": 3434, \"cash\": 1234 }")
  }

  @Test
  fun getCashTransactions() {
    val responseEntity = getTransactions("cash")

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo("{ \"transactions\": [ { \"id\": \"111-1\", \"type\": { \"code\": \"A\", \"desc\": \"AAA\" }, \"description\": \"Transaction test\", \"amount\": 1234, \"date\": \"2019-12-01\" } ] }")
  }

  @Test
  fun getsSpendsTransactions() {
    val responseEntity = getTransactions("spends")

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo("{ \"transactions\": [ { \"id\": \"111-1\", \"type\": { \"code\": \"A\", \"desc\": \"AAA\" }, \"description\": \"Transaction test\", \"amount\": 1234, \"date\": \"2019-12-01\" } ] }")
  }

  @Test
  fun getSavingsTransactions() {
    val responseEntity = getTransactions("savings")

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo("{ \"transactions\": [ { \"id\": \"111-1\", \"type\": { \"code\": \"A\", \"desc\": \"AAA\" }, \"description\": \"Transaction test\", \"amount\": 1234, \"date\": \"2019-12-01\" } ] }")
  }

  @Test
  fun getTransactionByClientUniqueRef() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", listOf("ROLE_NOMIS_API_V1"), mapOf("X-Client-Name" to "some-client"))

    val transactions = listOf(
      AccountTransactionSP.builder()
        .txnId(111L)
        .txnEntrySeq(1)
        .txnEntryDate(LocalDate.of(2019, 12, 1))
        .txnEntryDesc("Transaction test")
        .txnType("A")
        .txnTypeDesc("AAA")
        .txnEntryAmount(BigDecimal("12.34"))
        .build(),
    )

    whenever(getTransactionByClientUniqueRef.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_TRANS_CSR to transactions))

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/transactions/some-reference", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo("{ \"id\": \"111-1\", \"type\": { \"code\": \"A\", \"desc\": \"AAA\" }, \"description\": \"Transaction test\", \"amount\": 1234, \"date\": \"2019-12-01\" }")
  }

  @Test
  fun getTransactionByClientUniqueRefTransactionNotFound() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", listOf("ROLE_NOMIS_API_V1"), mapOf("X-Client-Name" to "some-client"))
    val transactions = listOf<Any>()
    whenever(getTransactionByClientUniqueRef.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_TRANS_CSR to transactions))

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/transactions/some-reference", GET, requestEntity, String::class.java)
    assertThat(responseEntity.statusCode.value()).isEqualTo(404)
  }

  @Test
  fun getActiveOffender() {
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    whenever(getActiveOffender.executeFunction(eq(BigDecimal::class.java), any(SqlParameterSource::class.java))).thenReturn(
      BigDecimal("1111111"),
    )

    val responseEntity = testRestTemplate.exchange("/api/v1/lookup/active_offender?noms_id=G0797UA&date_of_birth=1958-04-07", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo("{ \"found\": true, \"offender\": { \"id\": 1111111 } }")
  }

  @Test
  fun getActiveOffenderNotFound() {
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    whenever(getActiveOffender.executeFunction(eq(BigDecimal::class.java), any(SqlParameterSource::class.java))).thenReturn(null)

    val responseEntity = testRestTemplate.exchange("/api/v1/lookup/active_offender?noms_id=G0797UA&date_of_birth=1958-04-07", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo("{ \"found\": false }")
  }

  @Test
  fun getAvailableDates() {
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)
    val dates = listOf(
      AvailableDatesSP
        .builder()
        .slotDate(LocalDate.of(2019, 1, 21))
        .build(),
      AvailableDatesSP
        .builder()
        .slotDate(LocalDate.of(2019, 1, 22))
        .build(),
    )
    whenever(getAvailableDates.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_DATE_CSR to dates))

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/available_dates?start_date=" + LocalDate.now() + "&end_date=" + LocalDate.now().plusDays(10), GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo("{ \"dates\": [ \"2019-01-21\", \"2019-01-22\" ] }")
  }

  @Test
  fun getAvailableDatesInvalidDate() {
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/available_dates?start_date=2017-01-01&end_date=2017-02-01", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(400)
    assertThatJson(responseEntity.body!!).isEqualTo("{\"status\":400,\"userMessage\":\"Start date cannot be in the past\",\"developerMessage\":\"400 Start date cannot be in the past\"}")
  }

  @Test
  fun getAvailableDatesInvalidOffenderId() {
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/AB2425215C/visits/available_dates?start_date=2017-01-01&end_date=2017-02-01", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(400)
    assertThatJson(responseEntity.body!!).isEqualTo("{\"status\":400,\"userMessage\":\"For input string: \\\"AB2425215C\\\"\",\"developerMessage\":\"For input string: \\\"AB2425215C\\\"\"}")
  }

  @Test
  fun getContactListWithoutRestrictions() {
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)
    val contacts = listOf(
      ContactPersonSP
        .builder()
        .personId(1111111L)
        .firstName("first")
        .middleName("mid")
        .lastName("last")
        .birthDate(LocalDate.of(2000, 1, 1))
        .sexCode("M")
        .sexDesc("Male")
        .relationshipTypeCode("Other")
        .relationshipTypeDesc("Other - Social")
        .contactTypeCode("S")
        .contactTypeDesc("Social/ Family")
        .approvedVisitorFlag("Y")
        .activeFlag("Y")
        .build(),
    )
    whenever(getContactList.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_CONTACT_CSR to contacts))

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/contact_list", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo("{\"contacts\":[{\"id\":1111111,\"given_name\":\"first\",\"middle_names\":\"mid\",\"surname\":\"last\",\"date_of_birth\":\"2000-01-01\",\"gender\":{\"code\":\"M\",\"desc\":\"Male\"},\"relationship_type\":{\"code\":\"Other\",\"desc\":\"Other - Social\"},\"contact_type\":{\"code\":\"S\",\"desc\":\"Social/ Family\"},\"approved_visitor\":true,\"active\":true,\"restrictions\":[]}]}\n")
    assertThat(JsonContent<ContactList>(javaClass, ResolvableType.forType(ContactList::class.java), responseEntity.body!!)).isEqualToJson("contact-list-without-restrictions.json")
  }

  @Test
  fun getContactListWithRestrictions() {
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)
    val contacts = listOf(
      ContactPersonSP
        .builder()
        .personId(1111111L)
        .firstName("first")
        .middleName("mid")
        .lastName("last")
        .birthDate(LocalDate.of(2000, 1, 1))
        .sexCode("M")
        .sexDesc("Male")
        .relationshipTypeCode("Other")
        .relationshipTypeDesc("Other - Social")
        .contactTypeCode("S")
        .contactTypeDesc("Social/ Family")
        .approvedVisitorFlag("Y")
        .activeFlag("Y")
        .restrictionTypeCode("PREINF")
        .restrictionTypeDesc("Previous Info")
        .restrictionEffectiveDate(LocalDate.of(2015, 1, 1))
        .restrictionExpiryDate(LocalDate.of(2020, 1, 1))
        .commentText("xxxxxx")
        .build(),
    )
    whenever(getContactList.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_CONTACT_CSR to contacts))

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/contact_list", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThat(JsonContent<ContactList>(javaClass, ResolvableType.forType(ContactList::class.java), responseEntity.body!!)).isEqualToJson("contact-list-with-restrictions.json")
  }

  private fun getTransactions(accountType: String): ResponseEntity<*> {
    val transactions = listOf(
      AccountTransactionSP.builder()
        .txnId(111L)
        .txnEntrySeq(1)
        .txnEntryDate(LocalDate.of(2019, 12, 1))
        .txnEntryDesc("Transaction test")
        .txnType("A")
        .txnTypeDesc("AAA")
        .txnEntryAmount(BigDecimal("12.34"))
        .build(),
    )

    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    whenever(getAccountTransactions.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_TRANS_CSR to transactions))

    return testRestTemplate.exchange("/api/v1/prison/WLI/offenders/G0797UA/accounts/$accountType/transactions", GET, requestEntity, String::class.java)
  }

  @Test
  fun getVisitUnavailabilityFoundCourtForDate() {
    val day1 = LocalDate.now().plusDays(1)
    val day2 = LocalDate.now().plusDays(2)
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)
    val unavailability = listOf(UnavailabilityReasonSP.builder().reason("COURT").eventDate(day1).build())

    whenever(getUnavailability.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_REASON_CSR to unavailability))

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/unavailability?dates=$day1,$day2", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo(
      "{" +
        "\"" + day1 + "\":{\"external_movement\":true,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":false}," +
        "\"" + day2 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":false}}",
    )
  }

  @Test
  fun getVisitUnavailabilityFoundBannedForDates() {
    val day1 = LocalDate.now().plusDays(1)
    val day2 = LocalDate.now().plusDays(2)
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)
    val unavailability = listOf(UnavailabilityReasonSP.builder().reason("BAN").eventDate(day1).build(), UnavailabilityReasonSP.builder().reason("BAN").eventDate(day2).build())

    whenever(getUnavailability.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_REASON_CSR to unavailability))

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/unavailability?dates=$day1,$day2", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo(
      "{" +
        "\"" + day1 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":true}," +
        "\"" + day2 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":true}}",
    )
  }

  @Test
  fun getVisitUnavailabilityFoundOutOfVOForDates() {
    val day1 = LocalDate.now().plusDays(1)
    val day2 = LocalDate.now().plusDays(2)
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)
    val unavailability = listOf(UnavailabilityReasonSP.builder().reason("VO").eventDate(day1).build(), UnavailabilityReasonSP.builder().reason("VO").eventDate(day2).build())

    whenever(getUnavailability.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_REASON_CSR to unavailability))

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/unavailability?dates=$day1,$day2", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo(
      "{" +
        "\"" + day1 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":true,\"banned\":false}," +
        "\"" + day2 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":true,\"banned\":false}}",
    )
  }

  @Test
  fun getVisitUnavailabilityFoundVisitsDates() {
    val day1 = LocalDate.now().plusDays(1)
    val day2 = LocalDate.now().plusDays(2)
    val visitSlot1Json = day1.toString() + "T09:00/12:00"
    val visitSlot2Json = day1.toString() + "T13:00/16:00"
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    val unavailability = listOf(
      UnavailabilityReasonSP
        .builder()
        .reason("VISIT")
        .eventDate(day1)
        .visitId(10309199L)
        .slotStart(LocalDateTime.of(day1.year, day1.monthValue, day1.dayOfMonth, 9, 0))
        .slotEnd(LocalDateTime.of(day1.year, day1.monthValue, day1.dayOfMonth, 12, 0))
        .build(),
      UnavailabilityReasonSP
        .builder()
        .reason("VISIT")
        .eventDate(day1)
        .visitId(10309200L)
        .slotStart(LocalDateTime.of(day1.year, day1.monthValue, day1.dayOfMonth, 13, 0))
        .slotEnd(LocalDateTime.of(day1.year, day1.monthValue, day1.dayOfMonth, 16, 0))
        .build(),
    )

    whenever(getUnavailability.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_REASON_CSR to unavailability))

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/unavailability?dates=$day1,$day2", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo(
      "{" +
        "\"" + day1 + "\":{\"external_movement\":false,\"existing_visits\":[{\"id\":10309199,\"slot\":\"" + visitSlot1Json + "\"},{\"id\":10309200,\"slot\":\"" + visitSlot2Json + "\"}],\"out_of_vo\":false,\"banned\":false}," +
        "\"" + day2 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":false}}",
    )
  }

  @Test
  fun getVisitUnavailabilityNonFoundForDates() {
    val day1 = LocalDate.now().plusDays(1)
    val day2 = LocalDate.now().plusDays(2)
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)
    val unavailability = listOf<Any>()
    whenever(getUnavailability.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_REASON_CSR to unavailability))

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/unavailability?dates=$day1,$day2", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo(
      "{" +
        "\"" + day1 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":false}," +
        "\"" + day2 + "\":{\"external_movement\":false,\"existing_visits\":[],\"out_of_vo\":false,\"banned\":false}}",
    )
  }

  @Test
  fun getVisitUnavailabilityInvalidDate() {
    val day1 = LocalDate.now().minusDays(1)
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)
    val unavailability = listOf<Any>()
    whenever(getUnavailability.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_REASON_CSR to unavailability))

    val responseEntity = testRestTemplate.exchange("/api/v1/offenders/2425215/visits/unavailability?dates=$day1", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(400)
    assertThatJson(responseEntity.body!!).isEqualTo("{\"status\":400,\"userMessage\":\"Dates requested must be in future\",\"developerMessage\":\"400 Dates requested must be in future\"}")
  }

  @Test
  fun getVisitSlotsWithCapacity() {
    val visitSlot1Json = LocalDate.now().plusDays(1).toString() + "T13:30/16:00"
    val visitSlot2Json = LocalDate.now().plusDays(2).toString() + "T13:30/16:00"
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)
    val visitSlotsSP = listOf(
      VisitSlotsSP
        .builder()
        .slotStart(LocalDateTime.now().plusDays(1).withHour(13).withMinute(30))
        .slotEnd(LocalDateTime.now().plusDays(1).withHour(16).withMinute(0))
        .capacity(402L)
        .maxGroups(999L)
        .maxAdults(999L)
        .groupsBooked(1L)
        .visitorsBooked(2L)
        .adultsBooked(3L)
        .build(),
      VisitSlotsSP
        .builder()
        .slotStart(LocalDateTime.now().plusDays(2).withHour(13).withMinute(30))
        .slotEnd(LocalDateTime.now().plusDays(2).withHour(16).withMinute(0))
        .capacity(402L)
        .maxGroups(999L)
        .maxAdults(999L)
        .groupsBooked(4L)
        .visitorsBooked(5L)
        .adultsBooked(6L)
        .build(),
    )

    whenever(getVisitSlotsWithCapacity.execute(any(SqlParameterSource::class.java))).thenReturn(mapOf<String, Any>(StoreProcMetadata.P_DATE_CSR to visitSlotsSP))

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/MDI/slots?start_date=" + LocalDate.now().plusDays(1) + "&end_date=" + LocalDate.now().plusDays(2), GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(200)
    assertThatJson(responseEntity.body!!).isEqualTo(
      "{\"slots\":[" +
        "{\"time\":\"" + visitSlot1Json + "\",\"capacity\":402,\"max_groups\":999,\"max_adults\":999,\"groups_booked\":1,\"visitors_booked\":2,\"adults_booked\":3}," +
        "{\"time\":\"" + visitSlot2Json + "\",\"capacity\":402,\"max_groups\":999,\"max_adults\":999,\"groups_booked\":4,\"visitors_booked\":5,\"adults_booked\":6}" +
        "]}\n",
    )
  }

  @Test
  fun getVisitSlotsWithCapacityInvalidDate() {
    val requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", listOf("ROLE_NOMIS_API_V1"), null)

    val responseEntity = testRestTemplate.exchange("/api/v1/prison/MDI/slots?start_date=3000-01-01&end_date=3017-01-01", GET, requestEntity, String::class.java)

    assertThat(responseEntity.statusCode.value()).isEqualTo(400)
    assertThatJson(responseEntity.body!!).isEqualTo("{\"status\":400,\"userMessage\":\"End date cannot be more than 60 days in the future\",\"developerMessage\":\"400 End date cannot be more than 60 days in the future\"}")
  }
}
