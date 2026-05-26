@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.service

import jakarta.validation.constraints.NotNull
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.dao.CannotAcquireLockException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.api.model.Agency
import uk.gov.justice.hmpps.prison.api.model.BookingActivity
import uk.gov.justice.hmpps.prison.api.model.BookingSentenceAndRecallTypes
import uk.gov.justice.hmpps.prison.api.model.CourtCase
import uk.gov.justice.hmpps.prison.api.model.CourtEventOutcome
import uk.gov.justice.hmpps.prison.api.model.CourtHearing
import uk.gov.justice.hmpps.prison.api.model.Location
import uk.gov.justice.hmpps.prison.api.model.OffenderFinePaymentDto
import uk.gov.justice.hmpps.prison.api.model.OffenderOffence
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceAndOffences
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceDetailDto
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerm
import uk.gov.justice.hmpps.prison.api.model.PropertyContainer
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent
import uk.gov.justice.hmpps.prison.api.model.SentenceCalculationSummary
import uk.gov.justice.hmpps.prison.api.model.SentenceTypeRecallType
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance
import uk.gov.justice.hmpps.prison.api.model.VisitBalances
import uk.gov.justice.hmpps.prison.api.model.VisitDetails
import uk.gov.justice.hmpps.prison.api.model.VisitWithVisitors
import uk.gov.justice.hmpps.prison.api.model.Visitor
import uk.gov.justice.hmpps.prison.api.support.Order
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.BookingRepository
import uk.gov.justice.hmpps.prison.repository.OffenderBookingIdSeq
import uk.gov.justice.hmpps.prison.repository.SentenceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEventCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtOrder
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtType
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventOutcome
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceIndicator
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceResult
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderContactPerson
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase.OffenderCourtCaseBuilder
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderFinePayment
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPropertyContainer
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentenceCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.Person
import uk.gov.justice.hmpps.prison.repository.jpa.model.RelationshipType
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalcType
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm
import uk.gov.justice.hmpps.prison.repository.jpa.model.Statute
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitInformation
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitVisitor
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventChargeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderContactPersonsRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderFinePaymentRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRestrictionRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SentenceTermRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationFilter
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitVisitorRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitorRepository
import uk.gov.justice.hmpps.prison.service.support.PayableAttendanceOutcomeDto
import uk.gov.justice.hmpps.prison.service.transformers.OffenderBookingTransformer
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

/**
 * Test cases for [BookingService].
 */
class BookingServiceTest {
  private val bookingRepository: BookingRepository = mock()
  private val courtEventRepository: CourtEventRepository = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val visitInformationRepository: VisitInformationRepository = mock()
  private val visitorRepository: VisitorRepository = mock()
  private val visitVisitorRepository: VisitVisitorRepository = mock()
  private val agencyService: AgencyService = mock()
  private val offenderContactPersonsRepository: OffenderContactPersonsRepository = mock()
  private val offenderRestrictionRepository: OffenderRestrictionRepository = mock()
  private val staffUserAccountRepository: StaffUserAccountRepository = mock()
  private val hmppsAuthenticationHolder: HmppsAuthenticationHolder = mock()
  private val offenderBookingTransformer: OffenderBookingTransformer = mock()
  private val offenderSentenceRepository: OffenderSentenceRepository = mock()
  private val sentenceRepository: SentenceRepository = mock()
  private val sentenceTermRepository: SentenceTermRepository = mock()
  private val offenderFinePaymentRepository: OffenderFinePaymentRepository = mock()
  private val caseloadToAgencyMappingService: CaseloadToAgencyMappingService = mock()
  private val caseLoadService: CaseLoadService = mock()
  private val courtEventChargeRepository: CourtEventChargeRepository = mock()

  private val bookingService = BookingService(
    bookingRepository,
    courtEventRepository, offenderBookingRepository,
    visitorRepository,
    visitInformationRepository,
    visitVisitorRepository,
    sentenceRepository,
    sentenceTermRepository,
    agencyService,
    caseLoadService,
    caseloadToAgencyMappingService,
    offenderContactPersonsRepository,
    staffUserAccountRepository,
    offenderBookingTransformer,
    hmppsAuthenticationHolder,
    offenderSentenceRepository,
    offenderFinePaymentRepository,
    offenderRestrictionRepository,
    courtEventChargeRepository,
    10,
  )

  @Test
  fun testVerifyCanAccessLatestBooking() {
    val agencyIds = setOf("agency-1")
    val bookingId = 1L

    whenever(bookingRepository.getLatestBookingIdentifierForOffender("off-1"))
      .thenReturn(
        Optional.of(OffenderBookingIdSeq("off-1", bookingId, 1)),
      )
    whenever(agencyService.getAgencyIds(false)).thenReturn(agencyIds)
    whenever(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(true)

    bookingService.getOffenderIdentifiers("off-1")
  }

  @Test
  fun testVerifyCannotAccessLatestBooking() {
    val agencyIds = setOf("agency-1")
    val bookingId = 1L

    whenever(bookingRepository.getLatestBookingIdentifierForOffender("off-1"))
      .thenReturn(
        Optional.of(OffenderBookingIdSeq("off-1", bookingId, 1)),
      )
    whenever(bookingRepository.checkBookingExists(bookingId)).thenReturn(true)
    whenever(agencyService.getAgencyIds(false)).thenReturn(agencyIds)
    whenever(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(false)

    assertThatThrownBy { bookingService.getOffenderIdentifiers("off-1") }
      .isInstanceOf(AccessDeniedException::class.java)
  }

  @Test
  fun testVerifyCannotAccessLatestBookingForAccessDenied() {
    val agencyIds = setOf("agency-1")
    val bookingId = 1L

    whenever(bookingRepository.getLatestBookingIdentifierForOffender("off-1"))
      .thenReturn(
        Optional.of(OffenderBookingIdSeq("off-1", bookingId, 1)),
      )
    whenever(bookingRepository.checkBookingExists(bookingId)).thenReturn(true)
    whenever(agencyService.getAgencyIds(false)).thenReturn(agencyIds)
    whenever(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(false)

    assertThatThrownBy { bookingService.getOffenderIdentifiers("off-1") }
      .isInstanceOf(AccessDeniedException::class.java)
  }

  @Test
  fun verifyCanViewSensitiveBookingInfo() {
    val agencyIds = setOf("agency-1")
    val bookingId = 1L

    whenever(bookingRepository.getLatestBookingIdentifierForOffender("off-1"))
      .thenReturn(
        Optional.of(OffenderBookingIdSeq("off-1", bookingId, 1)),
      )
    whenever(agencyService.getAgencyIds(false)).thenReturn(agencyIds)
    whenever(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(true)

    bookingService.getOffenderIdentifiers("off-1")
  }

  @Test
  fun verifyCanViewSensitiveBookingInfo_systemUser() {
    whenever(hmppsAuthenticationHolder.isOverrideRole(anyVararg())).thenReturn(true)

    whenever(bookingRepository.getLatestBookingIdentifierForOffender("off-1"))
      .thenReturn(
        Optional.of(OffenderBookingIdSeq("off-1", -1L, 1)),
      )
    whenever(bookingRepository.checkBookingExists(-1L)).thenReturn(true)

    bookingService.getOffenderIdentifiers("off-1", "SYSTEM_USER", "GLOBAL_SEARCH")

    verify(hmppsAuthenticationHolder).isOverrideRole(
      "SYSTEM_USER",
      "GLOBAL_SEARCH",
    )
  }

  @Test
  fun verifyCanViewSensitiveBookingInfo_not() {
    val agencyIds = setOf("agency-1")
    val bookingId = 1L

    whenever(bookingRepository.getLatestBookingIdentifierForOffender("off-1"))
      .thenReturn(
        Optional.of(OffenderBookingIdSeq("off-1", bookingId, 1)),
      )
    whenever(bookingRepository.checkBookingExists(bookingId)).thenReturn(true)
    whenever(agencyService.getAgencyIds(false)).thenReturn(agencyIds)
    whenever(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(false)

    assertThatThrownBy { bookingService.getOffenderIdentifiers("off-1") }
      .isInstanceOf(AccessDeniedException::class.java)
  }

  @Test
  fun testThatUpdateAttendanceIsCalledForEachBooking() {
    whenever(bookingRepository.getPayableAttendanceOutcome("PRISON_ACT", "ATT"))
      .thenReturn(
        PayableAttendanceOutcomeDto
          .builder()
          .paid(true)
          .build(),
      )

    val updateAttendance = UpdateAttendance
      .builder()
      .eventOutcome("ATT")
      .performance("STANDARD")
      .build()

    val bookingActivities = setOf(
      BookingActivity.builder().bookingId(1L).activityId(10L).build(),
      BookingActivity.builder().bookingId(2L).activityId(20L).build(),
      BookingActivity.builder().bookingId(3L).activityId(30L).build(),
      BookingActivity.builder().bookingId(3L).activityId(31L).build(),
      BookingActivity.builder().bookingId(3L).activityId(32L).build(),
    )

    bookingService.updateAttendanceForMultipleBookingIds(bookingActivities, updateAttendance)

    val expectedOutcome = UpdateAttendance.builder().performance("STANDARD").eventOutcome("ATT").build()

    verify(bookingRepository).getPayableAttendanceOutcome(any(), any())
    verify(bookingRepository).updateAttendance(1L, 10L, expectedOutcome, true, false)
    verify(bookingRepository).updateAttendance(2L, 20L, expectedOutcome, true, false)
    verify(bookingRepository).updateAttendance(3L, 30L, expectedOutcome, true, false)
    verify(bookingRepository).updateAttendance(3L, 31L, expectedOutcome, true, false)
    verify(bookingRepository).updateAttendance(3L, 32L, expectedOutcome, true, false)
    verifyNoMoreInteractions(bookingRepository)
  }

  @Test
  fun getBookingVisitBalances() {
    val bookingId = -1L
    whenever(bookingRepository.getBookingVisitBalances(bookingId)).thenReturn(
      Optional.of(VisitBalances(25, 2, LocalDate.now(), LocalDate.now())),
    )

    bookingService.getBookingVisitBalances(bookingId)

    verify(bookingRepository).getBookingVisitBalances(bookingId)
  }

  @Nested
  inner class getEvents {
    @Test
    fun getEvents_CallsBookingRepository() {
      val bookingId = -1L
      val from = LocalDate.parse("2019-02-03")
      val to = LocalDate.parse("2019-03-03")
      bookingService.getEvents(bookingId, from, to)
      verify(bookingRepository)
        .getBookingActivities(bookingId, from, to, "startTime", Order.ASC)
      verify(bookingRepository)
        .getBookingVisits(bookingId, from, to, "startTime", Order.ASC)
      verify(bookingRepository)
        .getBookingAppointments(bookingId, from, to, "startTime", Order.ASC)
    }

    @Test
    fun getEvents_SortsAndCombinesNullsLast() {
      val bookingId = -1L
      val from = LocalDate.parse("2019-02-03")
      val to = LocalDate.parse("2019-03-03")
      whenever(
        bookingRepository.getBookingActivities(
          anyLong(),
          any(),
          any(),
          anyString(),
          any(),
        ),
      ).thenReturn(
        listOf(
          createEvent("act", "10:11:12"),
          createEvent("act", "08:59:50"),
        ),
      )
      whenever(
        bookingRepository.getBookingVisits(
          anyLong(),
          any(),
          any(),
          anyString(),
          any(),
        ),
      ).thenReturn(
        listOf(createEvent("vis", null)),
      )
      whenever(
        bookingRepository.getBookingAppointments(
          anyLong(),
          any(),
          any(),
          anyString(),
          any(),
        ),
      ).thenReturn(
        listOf(createEvent("app", "09:02:03")),
      )
      val events = bookingService.getEvents(bookingId, from, to)
      assertThat(events)
        .extracting<String, RuntimeException> { it.eventType }
        .containsExactly("act08:59:50", "app09:02:03", "act10:11:12", "visnull")
    }
  }

  @Nested
  inner class getScheduledEvents {
    @Test
    fun getScheduledEvents_CallsBookingRepository() {
      val bookingId = -1L
      val from = LocalDate.now()
      val to = from.plusDays(1)
      bookingService.getScheduledEvents(bookingId, from, to)
      verify(bookingRepository)
        .getBookingActivities(bookingId, from, to, "startTime", Order.ASC)
      verify(bookingRepository)
        .getBookingVisits(bookingId, from, to, "startTime", Order.ASC)
      verify(bookingRepository)
        .getBookingAppointments(bookingId, from, to, "startTime", Order.ASC)
    }

    @Test
    fun getScheduledEvents_MustBeInFuture() {
      val bookingId = -1L
      val from = LocalDate.parse("2019-05-05")
      val to = from.plusDays(1)
      assertThatThrownBy {
        bookingService.getScheduledEvents(
          bookingId,
          from,
          to,
        )
      }
        .isInstanceOf(HttpClientErrorException::class.java)
        .hasMessage("400 Invalid date range: fromDate is before today.")
    }

    @Test
    fun getScheduledEvents_DefaultsToToday() {
      val bookingId = -1L
      bookingService.getScheduledEvents(bookingId, null, null)
      verify(bookingRepository)
        .getBookingActivities(bookingId, LocalDate.now(), LocalDate.now(), "startTime", Order.ASC)
    }

    @Test
    fun getScheduledEvents_FiltersOutNonScheduledEvents() {
      val bookingId = -1L
      whenever(
        bookingRepository.getBookingActivities(
          anyLong(),
          any(),
          any(),
          anyString(),
          any(),
        ),
      ).thenReturn(
        listOf(
          ScheduledEvent.builder().bookingId(-1L).eventType("act").eventStatus("SCH").build(),
          ScheduledEvent.builder().bookingId(-2L).eventType("act").eventStatus("CANC").build(),
        ),
      )
      whenever(
        bookingRepository.getBookingVisits(
          anyLong(),
          any(),
          any(),
          anyString(),
          any(),
        ),
      ).thenReturn(
        listOf(
          ScheduledEvent.builder().bookingId(-3L).eventType("vis").eventStatus("SCH").build(),
          ScheduledEvent.builder().bookingId(-4L).eventType("vis").eventStatus("EXP").build(),
        ),
      )
      whenever(
        bookingRepository.getBookingAppointments(
          anyLong(),
          any(),
          any(),
          anyString(),
          any(),
        ),
      ).thenReturn(
        listOf(
          ScheduledEvent.builder().bookingId(-5L).eventType("app").eventStatus("SCH").build(),
          ScheduledEvent.builder().bookingId(-6L).eventType("app").eventStatus("CANC").build(),
        ),
      )
      val events = bookingService.getScheduledEvents(bookingId, null, null)
      assertThat(events)
        .extracting<String, RuntimeException> { it.eventStatus }
        .containsOnly("SCH")
    }

    @Test
    fun getScheduledEvents_SortsAndCombinesNullsLast() {
      val bookingId = -1L
      val from = LocalDate.now()
      whenever(
        bookingRepository.getBookingActivities(
          anyLong(),
          any(),
          any(),
          anyString(),
          any(),
        ),
      ).thenReturn(
        listOf(
          createEvent("act", "10:11:12"),
          createEvent("act", "08:59:50"),
        ),
      )
      whenever(
        bookingRepository.getBookingVisits(
          anyLong(),
          any(),
          any(),
          anyString(),
          any(),
        ),
      ).thenReturn(
        listOf(createEvent("vis", null)),
      )
      whenever(
        bookingRepository.getBookingAppointments(
          anyLong(),
          any(),
          any(),
          anyString(),
          any(),
        ),
      ).thenReturn(
        listOf(createEvent("app", "09:02:03")),
      )
      val events = bookingService.getScheduledEvents(bookingId, from, null)
      assertThat(events)
        .extracting<String, RuntimeException> { it.eventType }
        .containsExactly("act08:59:50", "app09:02:03", "act10:11:12", "visnull")
    }
  }

  @Test
  fun getBookingVisitsWithVisitor() {
    val pageable: Pageable = PageRequest.of(0, 20)
    val visits = listOf(
      VisitInformation
        .builder()
        .visitId(-1L)
        .visitorPersonId(-1L)
        .cancellationReason(null)
        .cancelReasonDescription(null)
        .eventStatus("ATT")
        .eventStatusDescription("Attended")
        .eventOutcome("ATT")
        .eventOutcomeDescription("Attended")
        .startTime(LocalDateTime.parse("2019-10-10T14:00"))
        .endTime(LocalDateTime.parse("2019-10-10T15:00"))
        .location("Visits")
        .visitType("SOC")
        .visitTypeDescription("Social")
        .leadVisitor("John Smith")
        .searchType("FULL")
        .searchTypeDescription("Full Search")
        .build(),
    )

    val page = PageImpl(visits)
    whenever(
      offenderContactPersonsRepository.findAllByOffenderBooking_BookingIdAndPersonIdIn(
        -1L,
        listOf(-1L, -2L),
      ),
    ).thenReturn(
      listOf(
        OffenderContactPerson.builder()
          .relationshipType(RelationshipType("UN", "Uncle"))
          .modifyDateTime(LocalDateTime.parse("2019-10-10T14:00"))
          .createDateTime(LocalDateTime.parse("2019-10-10T14:00"))
          .personId(-1L)
          .id(-1L)
          .build(),
        OffenderContactPerson.builder()
          .relationshipType(RelationshipType("FRI", "Friend"))
          .modifyDateTime(null)
          .createDateTime(LocalDateTime.parse("2019-10-11T14:00"))
          .personId(-1L)
          .id(-2L)
          .build(),
        OffenderContactPerson.builder()
          .relationshipType(RelationshipType("NIE", "Niece"))
          .modifyDateTime(LocalDateTime.parse("2019-10-10T14:00"))
          .id(-3L)
          .personId(-2L)
          .build(),

      ),
    )
    whenever(
      visitInformationRepository.findAll(
        VisitInformationFilter.builder().bookingId(-1L).build(),
        pageable,
      ),
    )
      .thenReturn(page)

    whenever(
      visitVisitorRepository.findByVisitIdInAndOffenderBookingIsNullOrderByPerson_BirthDateDesc(
        listOf(-1L),
      ),
    ).thenReturn(
      listOf(
        VisitVisitor.builder()
          .visitId(-1L)
          .person(
            Person.builder()
              .id(-1L)
              .birthDate(LocalDate.parse("1980-10-01"))
              .firstName("John")
              .lastName("Smith")
              .build(),
          )
          .groupLeader(true)
          .eventOutcome(EventOutcome("ABS", "Absent"))
          .build(),
        VisitVisitor.builder()
          .visitId(-1L)
          .person(
            Person.builder()
              .id(-2L)
              .birthDate(LocalDate.parse("2010-10-01"))
              .firstName("Jenny")
              .lastName("Smith")
              .build(),
          )
          .groupLeader(false)
          .eventOutcome(EventOutcome("ATT", "Attended"))
          .build(),

      ),
    )

    val visitsWithVisitors =
      bookingService.getBookingVisitsWithVisitor(VisitInformationFilter.builder().bookingId(-1L).build(), pageable)
    assertThat(visitsWithVisitors).containsOnly(
      VisitWithVisitors.builder()
        .visitDetail(
          VisitDetails
            .builder()
            .cancellationReason(null)
            .cancelReasonDescription(null)
            .eventStatus("ATT")
            .eventStatusDescription("Attended")
            .eventOutcome("ATT")
            .eventOutcomeDescription("Attended")
            .startTime(LocalDateTime.parse("2019-10-10T14:00"))
            .endTime(LocalDateTime.parse("2019-10-10T15:00"))
            .location("Visits")
            .visitType("SOC")
            .visitTypeDescription("Social")
            .leadVisitor("John Smith")
            .relationship("FRI")
            .relationshipDescription("Friend")
            .attended(true)
            .searchType("FULL")
            .searchTypeDescription("Full Search")
            .build(),
        )
        .visitors(
          listOf(
            Visitor
              .builder()
              .dateOfBirth(LocalDate.parse("1980-10-01"))
              .firstName("John")
              .lastName("Smith")
              .leadVisitor(true)
              .personId(-1L)
              .relationship("Friend")
              .attended(false)
              .build(),
            Visitor
              .builder()
              .dateOfBirth(LocalDate.parse("2010-10-01"))
              .firstName("Jenny")
              .lastName("Smith")
              .leadVisitor(false)
              .personId(-2L)
              .relationship("Niece")
              .attended(true)
              .build(),
          ),
        )
        .build(),
    )
  }

  @Test
  fun getBookingVisitsWithVisitor_filtered() {
    val pageable: Pageable = PageRequest.of(0, 20)
    val visits = listOf(
      VisitInformation
        .builder()
        .visitId(-1L)
        .visitorPersonId(-1L)
        .cancellationReason(null)
        .cancelReasonDescription(null)
        .eventStatus("ATT")
        .eventStatusDescription("Attended")
        .eventOutcome("ATT")
        .eventOutcomeDescription("Attended")
        .startTime(LocalDateTime.parse("2019-10-10T14:00"))
        .endTime(LocalDateTime.parse("2019-10-10T15:00"))
        .location("Visits")
        .visitType("SCON")
        .visitTypeDescription("Social")
        .leadVisitor("John Smith")
        .build(),
      VisitInformation
        .builder()
        .visitId(-2L)
        .bookingId(-1L)
        .visitorPersonId(-1L)
        .cancellationReason(null)
        .cancelReasonDescription(null)
        .eventStatus("ATT")
        .eventStatusDescription("Attended")
        .eventOutcome("ATT")
        .eventOutcomeDescription("Attended")
        .startTime(LocalDateTime.parse("2019-10-12T14:00"))
        .endTime(LocalDateTime.parse("2019-10-12T15:00"))
        .location("Visits")
        .visitType("SCON")
        .visitTypeDescription("Social")
        .leadVisitor("John Smith")
        .build(),
    )

    val page = PageImpl(visits)
    whenever(
      offenderContactPersonsRepository.findAllByOffenderBooking_BookingIdAndPersonIdIn(
        -1L,
        listOf(-1L, -2L, -1L, -2L),
      ),
    ).thenReturn(
      listOf(
        OffenderContactPerson.builder()
          .relationshipType(RelationshipType("UN", "Uncle"))
          .modifyDateTime(LocalDateTime.parse("2019-10-10T14:00"))
          .id(-1L)
          .personId(-1L)
          .build(),
        OffenderContactPerson.builder()
          .relationshipType(RelationshipType("FRI", "Friend"))
          .modifyDateTime(LocalDateTime.parse("2019-10-11T14:00"))
          .id(-2L)
          .personId(-1L)
          .build(),
        OffenderContactPerson.builder()
          .relationshipType(RelationshipType("NIE", "Niece"))
          .modifyDateTime(LocalDateTime.parse("2019-10-10T14:00"))
          .id(-3L)
          .personId(-2L)
          .build(),

      ),
    )
    whenever(
      visitInformationRepository.findAll(
        VisitInformationFilter.builder()
          .bookingId(-1L)
          .fromDate(LocalDate.of(2019, 10, 10))
          .toDate(LocalDate.of(2019, 10, 12))
          .visitType("SCON")
          .build(),
        pageable,
      ),
    )
      .thenReturn(page)

    whenever(
      visitVisitorRepository.findByVisitIdInAndOffenderBookingIsNullOrderByPerson_BirthDateDesc(
        listOf(-1L, -2L),
      ),
    ).thenReturn(
      listOf(
        VisitVisitor.builder()
          .visitId(-1L)
          .person(
            Person.builder()
              .id(-1L)
              .birthDate(LocalDate.parse("1980-10-01"))
              .firstName("John")
              .lastName("Smith")
              .build(),
          )
          .groupLeader(true)
          .eventOutcome(EventOutcome("ABS", "Absent"))
          .build(),
        VisitVisitor.builder()
          .visitId(-1L)
          .person(
            Person.builder()
              .id(-2L)
              .birthDate(LocalDate.parse("2010-10-01"))
              .firstName("Jenny")
              .lastName("Smith")
              .build(),
          )
          .groupLeader(false)
          .eventOutcome(EventOutcome("ATT", "Attended"))
          .build(),
        VisitVisitor.builder()
          .visitId(-2L)
          .person(
            Person.builder()
              .id(-1L)
              .birthDate(LocalDate.parse("1980-10-01"))
              .firstName("John")
              .lastName("Smith")
              .build(),
          )
          .groupLeader(true)
          .eventOutcome(EventOutcome("ABS", "Absent"))
          .build(),
        VisitVisitor.builder()
          .visitId(-2L)
          .person(
            Person.builder()
              .id(-2L)
              .birthDate(LocalDate.parse("2010-10-01"))
              .firstName("Jenny")
              .lastName("Smith")
              .build(),
          )
          .groupLeader(false)
          .eventOutcome(EventOutcome("ATT", "Attended"))
          .build(),

      ),
    )

    val visitsWithVisitors = bookingService.getBookingVisitsWithVisitor(
      VisitInformationFilter.builder()
        .bookingId(-1L)
        .fromDate(LocalDate.of(2019, 10, 10))
        .toDate(LocalDate.of(2019, 10, 12))
        .visitType("SCON")
        .build(),
      pageable,
    )
    assertThat(visitsWithVisitors).containsOnly(
      VisitWithVisitors.builder()
        .visitDetail(
          VisitDetails
            .builder()
            .cancellationReason(null)
            .cancelReasonDescription(null)
            .eventStatus("ATT")
            .eventStatusDescription("Attended")
            .eventOutcome("ATT")
            .eventOutcomeDescription("Attended")
            .startTime(LocalDateTime.parse("2019-10-10T14:00"))
            .endTime(LocalDateTime.parse("2019-10-10T15:00"))
            .location("Visits")
            .visitType("SCON")
            .visitTypeDescription("Social")
            .leadVisitor("John Smith")
            .relationship("FRI")
            .relationshipDescription("Friend")
            .attended(true)
            .build(),
        )
        .visitors(
          listOf(
            Visitor
              .builder()
              .dateOfBirth(LocalDate.parse("1980-10-01"))
              .firstName("John")
              .lastName("Smith")
              .leadVisitor(true)
              .personId(-1L)
              .relationship("Friend")
              .attended(false)
              .build(),
            Visitor
              .builder()
              .dateOfBirth(LocalDate.parse("2010-10-01"))
              .firstName("Jenny")
              .lastName("Smith")
              .leadVisitor(false)
              .personId(-2L)
              .relationship("Niece")
              .attended(true)
              .build(),
          ),
        )
        .build(),
      VisitWithVisitors.builder()
        .visitDetail(
          VisitDetails
            .builder()
            .cancellationReason(null)
            .cancelReasonDescription(null)
            .eventStatus("ATT")
            .eventStatusDescription("Attended")
            .eventOutcome("ATT")
            .eventOutcomeDescription("Attended")
            .startTime(LocalDateTime.parse("2019-10-12T14:00"))
            .endTime(LocalDateTime.parse("2019-10-12T15:00"))
            .location("Visits")
            .visitType("SCON")
            .visitTypeDescription("Social")
            .leadVisitor("John Smith")
            .relationship("FRI")
            .relationshipDescription("Friend")
            .attended(true)
            .build(),
        )
        .visitors(
          listOf(
            Visitor
              .builder()
              .dateOfBirth(LocalDate.parse("1980-10-01"))
              .firstName("John")
              .lastName("Smith")
              .leadVisitor(true)
              .personId(-1L)
              .relationship("Friend")
              .attended(false)
              .build(),
            Visitor
              .builder()
              .dateOfBirth(LocalDate.parse("2010-10-01"))
              .firstName("Jenny")
              .lastName("Smith")
              .leadVisitor(false)
              .personId(-2L)
              .relationship("Niece")
              .attended(true)
              .build(),
          ),
        )
        .build(),
    )
  }

  @Test
  fun getBookingVisitsWithVisitor_noVisitors() {
    val pageable: Pageable = PageRequest.of(0, 20)
    val visits = listOf(
      VisitInformation
        .builder()
        .visitId(-1L)
        .cancellationReason(null)
        .cancelReasonDescription(null)
        .startTime(LocalDateTime.parse("2019-10-10T14:00"))
        .endTime(LocalDateTime.parse("2019-10-10T15:00"))
        .location("Visits")
        .visitType("SOC")
        .visitTypeDescription("Social")
        .build(),
    )

    val page = PageImpl(visits)
    whenever(
      visitInformationRepository.findAll(
        VisitInformationFilter.builder().bookingId(-1L).build(),
        pageable,
      ),
    )
      .thenReturn(page)

    whenever(
      visitVisitorRepository.findByVisitIdInAndOffenderBookingIsNullOrderByPerson_BirthDateDesc(
        listOf(-1L),
      ),
    )
      .thenReturn(listOf())

    val visitsWithVisitors =
      bookingService.getBookingVisitsWithVisitor(VisitInformationFilter.builder().bookingId(-1L).build(), pageable)
    assertThat(visitsWithVisitors).containsOnly(
      VisitWithVisitors.builder()
        .visitDetail(
          VisitDetails
            .builder()
            .cancellationReason(null)
            .cancelReasonDescription(null)
            .eventStatus(null)
            .eventStatusDescription(null)
            .eventOutcome(null)
            .eventOutcomeDescription(null)
            .startTime(LocalDateTime.parse("2019-10-10T14:00"))
            .endTime(LocalDateTime.parse("2019-10-10T15:00"))
            .location("Visits")
            .visitType("SOC")
            .visitTypeDescription("Social")
            .leadVisitor(null)
            .attended(false)
            .build(),
        )
        .visitors(listOf<Visitor?>())
        .build(),
    )
  }

  @Test
  fun getOffenderCourtCases_active_only_mapped() {
    val activeCourtCase = caseWithDefaults().id(-1L).caseSeq(-1).caseStatus(CaseStatus("A", "Active")).build()
    val inactiveCourtCase = caseWithDefaults().id(-2L).caseSeq(-2).caseStatus(CaseStatus("I", "Inactive")).build()

    whenever(offenderBookingRepository.findById(-1L)).thenReturn(
      Optional.of(
        OffenderBooking.builder()
          .courtCases(listOf(activeCourtCase, inactiveCourtCase))
          .build(),
      ),
    )

    val activeOnlyCourtCases = bookingService.getOffenderCourtCases(-1L, true)

    assertThat(activeOnlyCourtCases).containsExactly(
      CourtCase.builder()
        .id(-1L)
        .caseSeq(-1)
        .beginDate(LocalDate.EPOCH)
        .agency(
          Agency.builder()
            .agencyId("agency_id")
            .active(true)
            .agencyType("CRT")
            .description("The Agency Description")
            .build(),
        )
        .caseType("Adult")
        .caseInfoPrefix("cip")
        .caseInfoNumber("cin")
        .caseStatus("Active")
        .courtHearings(listOf<CourtHearing?>())
        .build(),
    )
  }

  @Test
  fun getOffenderCourtCases_all_mapped() {
    val activeCourtCase = caseWithDefaults().id(-1L).caseSeq(-1).caseStatus(CaseStatus("A", "Active")).build()
    val inactiveCourtCase = caseWithDefaults().id(-2L).caseSeq(-2).caseStatus(CaseStatus("I", "Inactive")).build()

    whenever(offenderBookingRepository.findById(-1L)).thenReturn(
      Optional.of(
        OffenderBooking.builder()
          .courtCases(listOf(activeCourtCase, inactiveCourtCase))
          .build(),
      ),
    )

    val allCourtCases = bookingService.getOffenderCourtCases(-1L, false)

    assertThat(allCourtCases).containsExactly(
      CourtCase.builder()
        .id(-1L)
        .caseSeq(-1)
        .beginDate(LocalDate.EPOCH)
        .agency(
          Agency.builder()
            .agencyId("agency_id")
            .active(true)
            .agencyType("CRT")
            .description("The Agency Description")
            .build(),
        )
        .caseType("Adult")
        .caseInfoPrefix("cip")
        .caseInfoNumber("cin")
        .caseStatus("Active")
        .courtHearings(listOf<CourtHearing?>())
        .build(),
      CourtCase.builder()
        .id(-2L)
        .caseSeq(-2)
        .beginDate(LocalDate.EPOCH)
        .agency(
          Agency.builder()
            .agencyId("agency_id")
            .active(true)
            .agencyType("CRT")
            .description("The Agency Description")
            .build(),
        )
        .caseType("Adult")
        .caseInfoPrefix("cip")
        .caseInfoNumber("cin")
        .caseStatus("Inactive")
        .courtHearings(listOf<CourtHearing?>())
        .build(),
    )
  }

  private fun caseWithDefaults(): OffenderCourtCaseBuilder = OffenderCourtCase.builder().beginDate(LocalDate.EPOCH)
    .agencyLocation(
      AgencyLocation.builder()
        .id("agency_id")
        .active(true)
        .type(AgencyLocationType.COURT_TYPE)
        .description("The agency description")
        .build(),
    )
    .legalCaseType(LegalCaseType("A", "Adult"))
    .caseInfoPrefix("cip")
    .caseInfoNumber("cin")

  @Test
  fun getOffenderPropertyContainers() {
    val activePropertyContainer = containerWithDefaults().containerId(-1L).active(true).build()
    val inactivePropertyContainer = containerWithDefaults().containerId(-2L).active(false).build()

    whenever(offenderBookingRepository.findById(-1L)).thenReturn(
      Optional.of(
        OffenderBooking.builder()
          .propertyContainers(listOf(activePropertyContainer, inactivePropertyContainer))
          .build(),
      ),
    )

    val propertyContainers = bookingService.getOffenderPropertyContainers(-1L)

    assertThat(propertyContainers).containsExactly(
      PropertyContainer.builder()
        .sealMark("TEST1")
        .location(
          Location.builder()
            .locationId(10L)
            .description(null)
            .subLocations(false)
            .build(),
        )
        .containerType("Bulk")
        .build(),
    )
  }

  @Test
  fun getOffenderPropertyContainers_missingLocation() {
    val activePropertyContainer = containerWithDefaults().containerId(-1L).internalLocation(null).active(true).build()

    whenever(offenderBookingRepository.findById(-1L)).thenReturn(
      Optional.of(
        OffenderBooking.builder()
          .propertyContainers(listOf(activePropertyContainer))
          .build(),
      ),
    )

    val propertyContainers = bookingService.getOffenderPropertyContainers(-1L)

    assertThat(propertyContainers).containsExactly(
      PropertyContainer.builder()
        .sealMark("TEST1")
        .location(null)
        .containerType("Bulk")
        .build(),
    )
  }

  private fun containerWithDefaults(): OffenderPropertyContainer.OffenderPropertyContainerBuilder = OffenderPropertyContainer.builder()
    .sealMark("TEST1")
    .internalLocation(
      AgencyInternalLocation.builder()
        .active(true)
        .locationId(10L)
        .build(),
    )
    .containerType(uk.gov.justice.hmpps.prison.repository.jpa.model.PropertyContainer("BULK", "Bulk"))

  @Test
  fun getOffenderSentenceDetail_most_recent_active_booking() {
    val offenderBooking =
      OffenderBooking.builder()
        .bookingId(-1L)
        .bookingSequence(1)
        .active(true)
        .offender(
          Offender.builder()
            .nomsId("A1234AA")
            .firstName("John")
            .lastName("Smith")
            .build(),
        )
        .location(
          AgencyLocation.builder()
            .description("Agency Description 1 An Active Booking")
            .id("MDI")
            .build(),
        )
        .build()

    whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId("NomsId"))
      .thenReturn(Optional.of(offenderBooking))
    val offenderSentenceDetail = bookingService.getOffenderSentenceDetail("NomsId")

    assertThat(offenderSentenceDetail)
      .extracting { it.mostRecentActiveBooking }
      .isEqualTo(true)

    assertThat(offenderSentenceDetail)
      .extracting { it.agencyLocationDesc }
      .isEqualTo("Agency Description 1 An Active Booking")
  }

  @Test
  fun getOffenderSentencesSummary_most_recent_active_booking() {
    val offenderSentenceDetailDtos = listOf(
      OffenderSentenceDetailDto.builder()
        .bookingId(1L)
        .mostRecentActiveBooking(true)
        .offenderNo("A1234AA")
        .firstName("John")
        .lastName("Smith")
        .agencyLocationId("MDI")
        .build(),
      OffenderSentenceDetailDto.builder()
        .bookingId(2L)
        .offenderNo("A1234AA")
        .firstName("John")
        .lastName("Smith")
        .agencyLocationId("MDI")
        .mostRecentActiveBooking(false)
        .build(),
    )

    whenever(
      bookingRepository.getOffenderSentenceSummary(
        any(),
        any(),
        any(),
        any(),
      ),
    ).thenReturn(offenderSentenceDetailDtos)
    val offenderSentenceDetails = bookingService.getOffenderSentencesSummary(null, listOf("NomsId"))

    assertThat(offenderSentenceDetails).hasSize(2)
    assertThat(offenderSentenceDetails[0]).extracting { it.bookingId }.isEqualTo(1L)
    assertThat(offenderSentenceDetails[0]).extracting { it.mostRecentActiveBooking }.isEqualTo(true)
    assertThat(offenderSentenceDetails[1]).extracting { it.bookingId }.isEqualTo(2L)
    assertThat(offenderSentenceDetails[1]).extracting { it.mostRecentActiveBooking }.isEqualTo(false)
  }

  @Test
  fun getOffenderCourtCases_errors_for_unknown_booking() {
    whenever(offenderBookingRepository.findById(-1L))
      .thenReturn(Optional.empty())

    assertThatThrownBy {
      bookingService.getOffenderCourtCases(
        -1L,
        true,
      )
    }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Offender booking with id -1 not found.")
  }

  @Test
  fun getOffenderPropertyContainers_errors_for_unknown_booking() {
    whenever(offenderBookingRepository.findById(-1L))
      .thenReturn(Optional.empty())

    assertThatThrownBy { bookingService.getOffenderPropertyContainers(-1L) }
      .isInstanceOf(EntityNotFoundException::class.java)
      .hasMessage("Offender booking with id -1 not found.")
  }

  private fun createEvent(type: String?, time: String?): ScheduledEvent? = ScheduledEvent.builder().bookingId(-1L)
    .startTime(
      Optional.ofNullable<String>(time).map { "2019-01-02T$it" }
        .map { LocalDateTime.parse(it) }.orElse(null),
    )
    .eventStatus("SCH")
    .eventType(type + time).build()

  @Test
  fun getOffenderSentenceSummaries_forOveriddenRole() {
    whenever(hmppsAuthenticationHolder.isOverrideRole(anyVararg())).thenReturn(true)
    whenever(
      caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(
        any(),
      ),
    ).thenReturn(listOf())
    assertThatThrownBy {
      bookingService.getOffenderSentencesSummary(
        null,
        listOf(),
      )
    }
      .isInstanceOf(HttpClientErrorException::class.java)
      .hasMessage("400 Request must be restricted to either a caseload, agency or list of offenders")
  }

  @Test
  fun getSentenceAndOffenceDetails_withMinimalData() {
    val bookingId = -1L
    whenever(
      courtEventChargeRepository.findByChargeAndOutcome(
        any(),
        eq(listOf(OffenceResult.RECALL_COURT_RESULT_OUTCOME)),
      ),
    ).thenReturn(ArrayList())
    whenever(
      offenderSentenceRepository.findByOffenderBooking_BookingId_AndCalculationType_CalculationTypeNotLikeAndCalculationType_CategoryNot(
        bookingId,
        "%AGG%",
        "LICENCE",
      ),
    )
      .thenReturn(
        listOf(
          OffenderSentence.builder()
            .id(OffenderSentence.PK(-99L, 1))
            .calculationType(
              SentenceCalcType.builder().build(),
            ).build(),
        ),
      )

    val sentencesAndOffences = bookingService.getSentenceAndOffenceDetails(bookingId)

    assertThat(sentencesAndOffences).containsExactly(
      OffenderSentenceAndOffences.builder()
        .bookingId(-99L)
        .sentenceSequence(1)
        .offences(listOf<OffenderOffence?>())
        .revocationDates(listOf<LocalDate?>())
        .build(),
    )
  }

  @Test
  fun getSentenceAndOffenceDetails_withFullData() {
    val bookingId = -1L
    val recallEvents = listOf(
      CourtEventCharge.builder()
        .courtEvent(
          CourtEvent.builder()
            .eventDate(LocalDate.of(2025, 1, 1))
            .build(),
        )
        .offenderCharge(
          OffenderCharge.builder()
            .resultCodeOne(OffenceResult().withCode(OffenceResult.RECALL_COURT_RESULT_OUTCOME))
            .build(),
        )
        .build(),
    )
    val terms = listOf(
      SentenceTerm.builder()
        .id(SentenceTerm.PK(1L, 1, 1))
        .years(2)
        .sentenceTermCode("IMP")
        .build(),
      SentenceTerm.builder()
        .id(SentenceTerm.PK(1L, 1, 2))
        .years(1)
        .sentenceTermCode("LI")
        .build(),
    )

    whenever(
      courtEventChargeRepository.findByChargeAndOutcome(
        any(),
        eq(listOf(OffenceResult.RECALL_COURT_RESULT_OUTCOME)),
      ),
    ).thenReturn(recallEvents)
    whenever(
      offenderSentenceRepository.findByOffenderBooking_BookingId_AndCalculationType_CalculationTypeNotLikeAndCalculationType_CategoryNot(
        bookingId,
        "%AGG%",
        "LICENCE",
      ),
    )
      .thenReturn(
        listOf(
          OffenderSentence.builder()
            .id(OffenderSentence.PK(-98L, 2))
            .lineSequence(5L)
            .consecutiveToSentenceSequence(1)
            .status("A")
            .calculationType(
              SentenceCalcType.builder()
                .category("CAT")
                .calculationType("CALC")
                .description("Calc description")
                .build(),
            )
            .courtOrder(
              CourtOrder.builder()
                .courtDate(LocalDate.of(2021, 1, 1))
                .build(),
            )
            .terms(terms)
            .offenderSentenceCharges(
              listOf(
                OffenderSentenceCharge.builder()
                  .offenderCharge(
                    OffenderCharge.builder()
                      .dateOfOffence(LocalDate.of(2021, 1, 2))
                      .endDate(LocalDate.of(2021, 1, 25))
                      .offence(
                        Offence.builder()
                          .offenceIndicators(
                            listOf(
                              OffenceIndicator.builder().indicatorCode("INDICATOR").build(),
                            ),
                          )
                          .code("STA1234")
                          .statute(
                            Statute.builder().code("STA").build(),
                          )
                          .build(),
                      )
                      .build(),
                  )
                  .build(),
              ),
            )
            .courtCase(
              OffenderCourtCase.builder()
                .caseSeq(10)
                .caseInfoNumber("XYZ789")
                .courtEvents(
                  listOf(
                    CourtEvent.builder()
                      .eventDate(LocalDate.of(2021, 1, 1))
                      .courtLocation(
                        AgencyLocation.builder()
                          .id("ACRT")
                          .description("A court")
                          .courtType(CourtType("DCM", "District Court Martial"))
                          .build(),
                      )
                      .build(),
                  ),
                )
                .build(),
            )
            .build(),
        ),
      )

    val sentencesAndOffences = bookingService.getSentenceAndOffenceDetails(bookingId)

    assertThat(sentencesAndOffences).containsExactly(
      OffenderSentenceAndOffences.builder()
        .bookingId(-98L)
        .sentenceSequence(2)
        .lineSequence(5L)
        .caseSequence(10)
        .caseReference("XYZ789")
        .courtId("ACRT")
        .courtDescription("A court")
        .courtTypeCode("DCM")
        .consecutiveToSequence(1)
        .sentenceStatus("A")
        .sentenceCategory("CAT")
        .sentenceCalculationType("CALC")
        .sentenceTypeDescription("Calc description")
        .sentenceDate(LocalDate.of(2021, 1, 1))
        .terms(
          listOf(
            OffenderSentenceTerm.builder()
              .days(0)
              .weeks(0)
              .months(0)
              .years(2)
              .code("IMP")
              .build(),
            OffenderSentenceTerm.builder()
              .days(0)
              .weeks(0)
              .months(0)
              .years(1)
              .code("LI")
              .build(),
          ),
        )
        .offences(
          listOf(
            OffenderOffence.builder()
              .offenceStartDate(LocalDate.of(2021, 1, 2))
              .offenceEndDate(LocalDate.of(2021, 1, 25))
              .offenceStatute("STA")
              .offenceCode("STA1234")
              .indicators(listOf("INDICATOR"))
              .build(),
          ),
        )
        .revocationDates(listOf(LocalDate.of(2025, 1, 1)))
        .build(),
    )
  }

  @Test
  fun getSentenceAndOffenceDetailsForBookingIds_withMinimalData() {
    val bookingIds = setOf(1L, 8L, 3L)
    whenever(
      offenderSentenceRepository.findByOffenderBooking_BookingIdInAndCalculationType_CalculationTypeNotLikeAndCalculationType_CategoryNot(
        bookingIds,
        "%AGG%",
        "LICENCE",
      ),
    )
      .thenReturn(
        listOf(
          OffenderSentence.builder()
            .id(OffenderSentence.PK(8L, 1))
            .offenderBooking(OffenderBooking.builder().bookingId(8L).build())
            .status("A")
            .calculationType(
              SentenceCalcType.builder().calculationType(SentenceTypeRecallType.FTR.name).build(),
            ).build(),
          OffenderSentence.builder()
            .id(OffenderSentence.PK(3L, 1))
            .offenderBooking(OffenderBooking.builder().bookingId(3L).build())
            .status("A")
            .calculationType(
              SentenceCalcType.builder().calculationType(SentenceTypeRecallType.ADIMP.name).build(),
            ).build(),
          OffenderSentence.builder()
            .id(OffenderSentence.PK(3L, 2))
            .offenderBooking(OffenderBooking.builder().bookingId(3L).build())
            .status("A")
            .calculationType(
              SentenceCalcType.builder().calculationType(SentenceTypeRecallType.FTR_SCH15.name).build(),
            ).build(),
          OffenderSentence.builder()
            .id(OffenderSentence.PK(1L, 1))
            .offenderBooking(OffenderBooking.builder().bookingId(1L).build())
            .status("A")
            .calculationType(
              SentenceCalcType.builder().calculationType(SentenceTypeRecallType.LR_LASPO_AR.name).build(),
            ).build(),
          OffenderSentence.builder()
            .id(OffenderSentence.PK(1L, 2))
            .offenderBooking(OffenderBooking.builder().bookingId(1L).build())
            .status("I")
            .calculationType(
              SentenceCalcType.builder().calculationType(SentenceTypeRecallType.EDS21.name).build(),
            ).build(),
        ),
      )

    val sentencesAndOffences = bookingService.getSentenceAndRecallTypes(bookingIds)

    assertThat(sentencesAndOffences.size).isEqualTo(3)
    assertThat(sentencesAndOffences).containsExactlyInAnyOrder(
      BookingSentenceAndRecallTypes(8L, listOf(SentenceTypeRecallType.FTR)),
      BookingSentenceAndRecallTypes(
        3,
        listOf(SentenceTypeRecallType.ADIMP, SentenceTypeRecallType.FTR_SCH15),
      ),
      BookingSentenceAndRecallTypes(1, listOf(SentenceTypeRecallType.LR_LASPO_AR)),
    )
  }

  @Test
  fun getOffenderFinePayments() {
    val bookingId = -1L
    whenever(
      offenderFinePaymentRepository.findByOffenderBooking_BookingId(
        bookingId,
      ),
    )
      .thenReturn(
        listOf(
          OffenderFinePayment.builder()
            .offenderBooking(OffenderBooking.builder().bookingId(-99L).build())
            .sequence(5)
            .paymentDate(LocalDate.of(2022, 1, 1))
            .paymentAmount(BigDecimal("9.99"))
            .build(),
        ),
      )

    val sentencesAndOffences = bookingService.getOffenderFinePayments(bookingId)

    assertThat(sentencesAndOffences).containsExactly(
      OffenderFinePaymentDto.builder()
        .bookingId(-99L)
        .sequence(5)
        .paymentDate(LocalDate.of(2022, 1, 1))
        .paymentAmount(BigDecimal("9.99"))
        .build(),
    )
  }

  @Test
  fun getOffenderCourtEventOutcomesWhenOutcomeCodesProvidedShouldFilterResults() {
    // Given
    val bookingIds = setOf(1L)
    val outcomeCodes = setOf("4016")

    val courtEvent = CourtEventOutcome(1L, 99L, "4016")
    whenever(
      courtEventRepository.findCourtEventOutcomesByBookingIdsAndCaseStatusAndOutcomeCodes(
        bookingIds,
        "A",
        outcomeCodes,
      ),
    )
      .thenReturn(listOf(courtEvent))

    // When
    val results = bookingService.getOffenderCourtEventOutcomes(bookingIds, outcomeCodes)

    // Then
    assertThat(results).hasSize(1)
    assertThat(results.first().outcomeReasonCode).isEqualTo("4016")
    assertThat(results.first().bookingId).isEqualTo(1L)

    verify(courtEventRepository, Mockito.never())
      .findCourtEventOutcomesByBookingIdsAndCaseStatus(bookingIds, "A")
    verify(courtEventRepository)
      .findCourtEventOutcomesByBookingIdsAndCaseStatusAndOutcomeCodes(bookingIds, "A", outcomeCodes)
  }

  @Test
  fun getOffenderCourtEventOutcomesWhenNoOutcomeCodesShouldReturnResults() {
    // Given
    val bookingIds = setOf(1L, 2L)
    val courtEvent1 = CourtEventOutcome(1L, 54L, "4016")
    val courtEvent2 = CourtEventOutcome(2L, 93L, "5011")
    val expectedEvents = listOf(courtEvent1, courtEvent2)

    whenever(
      courtEventRepository.findCourtEventOutcomesByBookingIdsAndCaseStatus(
        bookingIds,
        "A",
      ),
    ).thenReturn(expectedEvents)

    // When
    val results = bookingService.getOffenderCourtEventOutcomes(bookingIds, null)

    // Then
    assertThat(results).hasSize(2)
    assertThat(results[0].outcomeReasonCode).isEqualTo("4016")
    assertThat(results[0].bookingId).isEqualTo(1L)
    assertThat(results[1].outcomeReasonCode).isEqualTo("5011")
    assertThat(results[1].bookingId).isEqualTo(2L)

    verify(courtEventRepository)
      .findCourtEventOutcomesByBookingIdsAndCaseStatus(bookingIds, "A")
    verify(courtEventRepository, Mockito.never())
      .findCourtEventOutcomesByBookingIdsAndCaseStatusAndOutcomeCodes(bookingIds, "A", null)
  }

  @Test
  fun getOffenderCourtEventOutcomesWhenEmptyOutcomeCodeSetShouldReturnResults() {
    // Given
    val bookingIds = setOf(99L)
    whenever(
      courtEventRepository.findCourtEventOutcomesByBookingIdsAndCaseStatus(
        bookingIds,
        "A",
      ),
    ).thenReturn(listOf())

    // When
    bookingService.getOffenderCourtEventOutcomes(bookingIds, setOf())

    // Then
    verify(courtEventRepository)
      .findCourtEventOutcomesByBookingIdsAndCaseStatus(bookingIds, "A")
    verify(courtEventRepository, Mockito.never())
      .findCourtEventOutcomesByBookingIdsAndCaseStatusAndOutcomeCodes(bookingIds, "A", null)
  }

  @Test
  fun getOffenderCourtEventOutcomesWhenNoResultsShouldReturnEmptyList() {
    // Given
    val bookingIds = setOf(99L)
    whenever(
      courtEventRepository.findCourtEventOutcomesByBookingIdsAndCaseStatus(
        bookingIds,
        "A",
      ),
    ).thenReturn(listOf())

    // When
    val results = bookingService.getOffenderCourtEventOutcomes(bookingIds, null)

    // Then
    assertThat(results).isEmpty()
    verify(courtEventRepository)
      .findCourtEventOutcomesByBookingIdsAndCaseStatus(bookingIds, "A")
    verify(courtEventRepository, Mockito.never())
      .findCourtEventOutcomesByBookingIdsAndCaseStatusAndOutcomeCodes(bookingIds, "A", null)
  }

  @Nested
  internal inner class UpdateLivingUnit {

    @Test
    fun bookingNotFound_throws() {
      whenever(offenderBookingRepository.findById(BAD_BOOKING_ID)).thenReturn(
        Optional.empty<OffenderBooking>(),
      )

      assertThatThrownBy {
        bookingService.updateLivingUnit(
          BAD_BOOKING_ID,
          aCellSwapLocation(),
          false,
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessageContaining(BAD_BOOKING_ID.toString())
    }

    @Test
    fun livingUnitNotCell_throws() {
      whenever(offenderBookingRepository.findById(SOME_BOOKING_ID))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID))

      assertThatThrownBy {
        bookingService.updateLivingUnit(
          SOME_BOOKING_ID,
          aLocation(NEW_LIVING_UNIT_ID, SOME_AGENCY_ID, "WING"),
          false,
        )
      }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining(NEW_LIVING_UNIT_DESC)
        .hasMessageContaining("WING")
    }

    @Test
    fun differentAgency_throws() {
      whenever(offenderBookingRepository.findById(SOME_BOOKING_ID))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID))

      assertThatThrownBy {
        bookingService.updateLivingUnit(
          SOME_BOOKING_ID,
          aLocation(NEW_LIVING_UNIT_ID, DIFFERENT_AGENCY_ID),
          false,
        )
      }
        .isInstanceOf(IllegalArgumentException::class.java)
        .hasMessageContaining(SOME_AGENCY_ID)
        .hasMessageContaining(DIFFERENT_AGENCY_ID)
    }

    @Test
    fun lockTimeout_throws() {
      whenever<@NotNull Optional<OffenderBooking?>?>(
        offenderBookingRepository.findWithLockTimeoutByBookingId(
          SOME_BOOKING_ID,
        ),
      )
        .thenThrow(CannotAcquireLockException("test"))

      assertThatThrownBy {
        bookingService.updateLivingUnit(
          SOME_BOOKING_ID,
          aLocation(NEW_LIVING_UNIT_ID, DIFFERENT_AGENCY_ID),
          true,
        )
      }
        .isInstanceOf(DatabaseRowLockedException::class.java)
        .hasMessage("Resource locked, possibly in use in P-Nomis.")
        .hasFieldOrPropertyWithValue(
          "developerMessage",
          "Failed to get OFFENDER_BOOKINGS lock for bookingId=$SOME_BOOKING_ID after 10000 milliseconds",
        )
    }

    @Test
    fun ok_updatesRepo() {
      whenever(offenderBookingRepository.findById(SOME_BOOKING_ID))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID))

      bookingService.updateLivingUnit(SOME_BOOKING_ID, aLocation(NEW_LIVING_UNIT_ID, SOME_AGENCY_ID), false)

      val updatedOffenderBooking =
        ArgumentCaptor.forClass(OffenderBooking::class.java)
      verify(offenderBookingRepository)
        .save<OffenderBooking>(updatedOffenderBooking.capture())
      assertThat(updatedOffenderBooking.getValue().assignedLivingUnit.locationId)
        .isEqualTo(NEW_LIVING_UNIT_ID)
    }

    @Test
    fun ok_withTimeout() {
      whenever(
        offenderBookingRepository.findWithLockTimeoutByBookingId(
          SOME_BOOKING_ID,
        ),
      )
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID))

      bookingService.updateLivingUnit(SOME_BOOKING_ID, aLocation(NEW_LIVING_UNIT_ID, SOME_AGENCY_ID), true)

      val updatedOffenderBooking =
        ArgumentCaptor.forClass(OffenderBooking::class.java)
      verify(offenderBookingRepository)
        .save<OffenderBooking>(updatedOffenderBooking.capture())
      assertThat(updatedOffenderBooking.getValue().assignedLivingUnit.locationId)
        .isEqualTo(NEW_LIVING_UNIT_ID)
    }

    @Test
    fun ok_updatesRepoForReception() {
      whenever(offenderBookingRepository.findById(SOME_BOOKING_ID))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID))

      bookingService.updateLivingUnit(SOME_BOOKING_ID, receptionLocation(NEW_LIVING_UNIT_ID, LOCATION_CODE), false)

      val updatedOffenderBooking =
        ArgumentCaptor.forClass(OffenderBooking::class.java)
      verify(offenderBookingRepository)
        .save<OffenderBooking>(updatedOffenderBooking.capture())
      assertThat(updatedOffenderBooking.getValue().assignedLivingUnit.locationId)
        .isEqualTo(NEW_LIVING_UNIT_ID)
    }

    @Test
    fun cellSwap() {
      whenever(offenderBookingRepository.findById(SOME_BOOKING_ID))
        .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID))

      val cellSwapLocation = aCellSwapLocation()

      bookingService.updateLivingUnit(SOME_BOOKING_ID, cellSwapLocation, false)

      val updatedOffenderBooking =
        ArgumentCaptor.forClass(OffenderBooking::class.java)
      verify(offenderBookingRepository)
        .save<OffenderBooking>(updatedOffenderBooking.capture())

      assertThat(updatedOffenderBooking.getValue().assignedLivingUnit.locationId)
        .isEqualTo(cellSwapLocation.locationId)
    }

    private fun anOffenderBooking(bookingId: Long?, livingUnitId: Long?): Optional<OffenderBooking> {
      val agencyLocation = AgencyLocation.builder().id("MDI").build()
      val livingUnit = AgencyInternalLocation.builder().locationId(livingUnitId).build()
      val offender = Offender.builder().nomsId("any noms id").build()
      return Optional.of(
        OffenderBooking.builder()
          .bookingId(bookingId)
          .assignedLivingUnit(livingUnit)
          .location(agencyLocation)
          .offender(offender)
          .build(),
      )
    }

    private fun aCellSwapLocation(): AgencyInternalLocation = AgencyInternalLocation.builder()
      .locationId(-99L)
      .locationCode("CSWAP")
      .certifiedFlag(false)
      .active(true)
      .agencyId("MDI")
      .description("CSWAP-MDI")
      .build()

    private fun aLocation(
      locationId: Long?,
      agencyId: String?,
      locationType: String? = "CELL",
    ): AgencyInternalLocation = AgencyInternalLocation.builder()
      .locationId(locationId)
      .description("Z-1")
      .agencyId(agencyId)
      .locationType(locationType)
      .build()

    private fun receptionLocation(locationId: Long?, locationCode: String?): AgencyInternalLocation = AgencyInternalLocation.builder()
      .locationId(locationId)
      .operationalCapacity(null)
      .currentOccupancy(1)
      .locationType("AREA")
      .agencyId("MDI")
      .locationCode(locationCode)
      .userDescription(null)
      .certifiedFlag(false)
      .active(true)
      .capacity(100)
      .description("MDI-RECP")
      .livingUnit(null)
      .build()
  }

  @Nested
  internal inner class GetBookingVisitsSummary {
    @Test
    fun hasVisits() {
      whenever(visitInformationRepository.countByBookingId(anyLong())).thenReturn(5L)
      val startTime = LocalDateTime.parse("2020-02-01T10:20:30")
      whenever(
        bookingRepository.getBookingVisitNext(
          anyLong(),
          any(),
        ),
      ).thenReturn(Optional.of(VisitDetails.builder().startTime(startTime).build()))
      val summary = bookingService.getBookingVisitsSummary(-1L)
      assertThat(summary.startDateTime).isEqualTo(startTime)
      assertThat(summary.hasVisits).isTrue()
    }

    @Test
    fun noVisits() {
      whenever(visitInformationRepository.countByBookingId(anyLong())).thenReturn(0L)
      whenever(
        bookingRepository.getBookingVisitNext(anyLong(), any()),
      ).thenReturn(Optional.empty())
      val summary = bookingService.getBookingVisitsSummary(-1L)
      assertThat(summary.startDateTime).isNull()
      assertThat(summary.hasVisits).isFalse()
    }
  }

  @Test
  fun givenCalculationsThenReturnAll() {
    val sentenceCalculationSummaries = offenderSentenceCalculationSummaries()
    whenever(
      bookingRepository.getOffenderSentenceCalculationsForPrisoner(
        anyString(),
        eq(true),
      ),
    ).thenReturn(sentenceCalculationSummaries)

    val results = bookingService.getOffenderSentenceCalculationsForPrisoner("ABZ123A", null)

    verify(bookingRepository)
      .getOffenderSentenceCalculationsForPrisoner(eq("ABZ123A"), eq(true))
    assertThat(results).hasSize(sentenceCalculationSummaries.size)
  }

  @Test
  fun givenCalculationsOnlyLatestThenReturnAll() {
    val sentenceCalculationSummaries = offenderSentenceCalculationSummaries()
    whenever(
      bookingRepository.getOffenderSentenceCalculationsForPrisoner(
        anyString(),
        eq(true),
      ),
    ).thenReturn(sentenceCalculationSummaries)

    val results = bookingService.getOffenderSentenceCalculationsForPrisoner("ABZ123A", true)

    verify(bookingRepository)
      .getOffenderSentenceCalculationsForPrisoner(eq("ABZ123A"), eq(true))
    assertThat(results).hasSize(sentenceCalculationSummaries.size)
  }

  @Test
  fun givenCalculationsAllThenReturnAll() {
    val sentenceCalculationSummaries = offenderSentenceCalculationSummaries()
    whenever(
      bookingRepository.getOffenderSentenceCalculationsForPrisoner(
        anyString(),
        eq(false),
      ),
    ).thenReturn(sentenceCalculationSummaries)

    val results = bookingService.getOffenderSentenceCalculationsForPrisoner("ABZ123A", false)

    verify(bookingRepository)
      .getOffenderSentenceCalculationsForPrisoner(eq("ABZ123A"), eq(false))
    assertThat(results).hasSize(sentenceCalculationSummaries.size)
  }

  private fun offenderSentenceCalculationSummaries(): List<SentenceCalculationSummary> = listOf(
    sentenceCalculationSummary(1L),
    sentenceCalculationSummary(2L),
    sentenceCalculationSummary(3L),
    sentenceCalculationSummary(4L),
    sentenceCalculationSummary(5L),
  )

  private fun sentenceCalculationSummary(bookingId: Long): SentenceCalculationSummary = SentenceCalculationSummary(
    bookingId,
    "ABC",
    "first name",
    "last name",
    "SYI",
    "Shrewsbury",
    1,
    LocalDateTime.now(),
    1L,
    "comment",
    "Adjust Sentence",
    "user",
    "First",
    "Last",
  )

  private companion object {
    private const val SOME_BOOKING_ID: Long = 1L
    private const val BAD_BOOKING_ID: Long = 2L
    private const val OLD_LIVING_UNIT_ID: Long = 11L
    private const val NEW_LIVING_UNIT_ID: Long = 12L
    private const val NEW_LIVING_UNIT_DESC: String = "Z-1"
    private const val SOME_AGENCY_ID: String = "MDI"
    private const val LOCATION_CODE: String = "RECP"
    private const val DIFFERENT_AGENCY_ID: String = "NOT_MDI"
  }
}
