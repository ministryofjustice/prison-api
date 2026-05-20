package uk.gov.justice.hmpps.prison.service.v1

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.web.client.HttpClientErrorException
import uk.gov.justice.hmpps.prison.api.model.v1.Event
import uk.gov.justice.hmpps.prison.api.model.v1.OffenderIdentifier
import uk.gov.justice.hmpps.prison.repository.v1.AlertV1Repository
import uk.gov.justice.hmpps.prison.repository.v1.BookingV1Repository
import uk.gov.justice.hmpps.prison.repository.v1.CoreV1Repository
import uk.gov.justice.hmpps.prison.repository.v1.EventsV1Repository
import uk.gov.justice.hmpps.prison.repository.v1.FinanceV1Repository
import uk.gov.justice.hmpps.prison.repository.v1.LegalV1Repository
import uk.gov.justice.hmpps.prison.repository.v1.OffenderV1Repository
import uk.gov.justice.hmpps.prison.repository.v1.PrisonV1Repository
import uk.gov.justice.hmpps.prison.repository.v1.VisitV1Repository
import uk.gov.justice.hmpps.prison.repository.v1.model.EventSP
import java.time.LocalDate
import java.time.LocalDateTime

class NomisApiV1ServiceTest {
  private val bookingV1Repository: BookingV1Repository = mock()
  private val offenderV1Repository: OffenderV1Repository = mock()
  private val legalV1Repository: LegalV1Repository = mock()
  private val financeV1Repository: FinanceV1Repository = mock()
  private val alertV1Repository: AlertV1Repository = mock()
  private val eventsV1Repository: EventsV1Repository = mock()
  private val prisonV1Repository: PrisonV1Repository = mock()
  private val coreV1Repository: CoreV1Repository = mock()
  private val visitV1Repository: VisitV1Repository = mock()

  private val service = NomisApiV1Service(
    bookingV1Repository,
    offenderV1Repository,
    legalV1Repository,
    financeV1Repository,
    alertV1Repository,
    eventsV1Repository,
    prisonV1Repository,
    coreV1Repository,
    visitV1Repository,
  )

  @Test
  fun getVisitAvailableDates_fromInPast() {
    val from = LocalDate.now().minusDays(1)
    val to = LocalDate.now().plusDays(1)
    assertThatThrownBy {
      service.getVisitAvailableDates(
        12345L,
        from,
        to,
      )
    }
      .isInstanceOf(HttpClientErrorException::class.java)
      .hasMessageContaining("Start date cannot be in the past")
  }

  @Test
  fun getVisitAvailableDates_fromTooFarInFuture() {
    val from = LocalDate.now().plusDays(5)
    val to = LocalDate.now().plusDays(1)
    assertThatThrownBy {
      service.getVisitAvailableDates(
        12345L,
        from,
        to,
      )
    }
      .isInstanceOf(HttpClientErrorException::class.java)
      .hasMessageContaining("End date cannot be before the start date")
  }

  @Test
  fun getVisitAvailableDates_TooFarInFuture() {
    val from = LocalDate.now().plusDays(60)
    val to = LocalDate.now().plusDays(61)
    assertThatThrownBy {
      service.getVisitAvailableDates(
        12345L,
        from,
        to,
      )
    }
      .isInstanceOf(HttpClientErrorException::class.java)
      .hasMessageContaining("End date cannot be more than 60 days in the future")
  }

  @Test
  fun getVisitAvailableDates() {
    val from = LocalDate.now()
    val to = LocalDate.now().plusDays(5)
    service.getVisitAvailableDates(12345L, from, to)
    verify(visitV1Repository).getAvailableDates(12345L, from, to)
  }

  @Test
  fun getEvents() {
    val date = LocalDateTime.parse("2020-01-02T03:02:01")
    whenever(
      eventsV1Repository.getEvents(
        anyString(),
        isNull(),
        anyLong(),
        isNull(),
        anyString(),
        any(),
        anyLong(),
      ),
    ).thenReturn(
      listOf(EventSP(5L, date, "MDI", "A1234", "ETYPE", "Event ", "Data 2", " and 3")),
    )
    val events = service.getEvents("prison", OffenderIdentifier("12345"), "type", LocalDateTime.now(), 5L)
    assertThat(events)
      .containsExactly(Event("ETYPE", 5L, "A1234", "MDI", date, "Event Data 2 and 3"))
  }

  @Test
  fun getEventsCopesWithAllNull() {
    val date = LocalDateTime.parse("2020-01-02T03:02:01")
    whenever(
      eventsV1Repository.getEvents(
        anyString(),
        isNull(),
        anyLong(),
        isNull(),
        anyString(),
        any(),
        anyLong(),
      ),
    ).thenReturn(
      listOf(EventSP(5L, date, "MDI", "A1234", "ETYPE", null, null, null)),
    )
    val events = service.getEvents("prison", OffenderIdentifier("12345"), "type", LocalDateTime.now(), 5L)
    assertThat(events).containsExactly(Event("ETYPE", 5L, "A1234", "MDI", date, "{}"))
  }

  @Test
  fun getEventsCopesWithSomeNull() {
    val date = LocalDateTime.parse("2020-01-02T03:02:01")
    whenever(
      eventsV1Repository.getEvents(
        anyString(),
        isNull(),
        anyLong(),
        isNull(),
        anyString(),
        any(),
        anyLong(),
      ),
    ).thenReturn(
      listOf(EventSP(5L, date, "MDI", "A1234", "ETYPE", null, " a value ", null)),
    )
    val events = service.getEvents("prison", OffenderIdentifier("12345"), "type", LocalDateTime.now(), 5L)
    assertThat(events).containsExactly(Event("ETYPE", 5L, "A1234", "MDI", date, " a value "))
  }
}
