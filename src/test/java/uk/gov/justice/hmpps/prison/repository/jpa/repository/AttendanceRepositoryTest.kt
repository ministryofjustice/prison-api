package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl
import java.time.LocalDate

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuthenticationFacade::class, AuditorAwareImpl::class)
@WithMockUser
class AttendanceRepositoryTest {
  @Autowired
  private lateinit var repository: AttendanceRepository

  @Test
  fun attendanceByDate() {
    val activities = repository.findByEventDateBetweenAndOutcome(
      "A1234AB",
      LocalDate.of(2010, 1, 1),
      LocalDate.now(),
      null,
      PageRequest.of(1, 4, Sort.Direction.ASC, "eventId"),
    )
    assertThat(activities.totalElements).isEqualTo(7)
    assertThat(activities.content).asList().extracting(
      "eventId",
      "eventDate",
      "eventOutcome",
      "courseActivity.activityId",
      "courseActivity.description",
      "courseActivity.code",
      "courseActivity.prisonId",
      "courseActivity.scheduleStartDate",
      "offenderBooking.bookingId",
      "programService.activity",
      "comment",
    ).contains(
      Tuple.tuple(
        -13L,
        LocalDate.of(2017, 9, 13),
        "UNACAB",
        -1L,
        "Chapel Cleaner",
        "CC1",
        "LEI",
        LocalDate.of(2016, 8, 8),
        -2L,
        null,
        null,
      ),
      Tuple.tuple(
        -12L,
        LocalDate.now(),
        null,
        -3L,
        "Substance misuse course",
        "SUBS",
        "LEI",
        LocalDate.of(2011, 1, 4),
        -2L,
        null,
        "Comment 12",
      ),
      Tuple.tuple(
        -11L,
        LocalDate.now(),
        null,
        -2L,
        "Woodwork",
        "WOOD",
        "LEI",
        LocalDate.of(2012, 2, 28),
        -2L,
        "Test Prog 2",
        null,
      ),
    )
  }

  @Test
  fun attendanceByDateAndOutcome() {
    val activities = repository.findByEventDateBetweenAndOutcome(
      "A1234AB",
      LocalDate.of(2010, 1, 1),
      LocalDate.now(),
      "UNACAB",
      PageRequest.of(0, 4, Sort.Direction.ASC, "eventId"),
    )
    assertThat(activities.totalElements).isEqualTo(1)
    assertThat(activities.content).asList().extracting(
      "eventId",
      "eventDate",
      "eventOutcome",
      "courseActivity.activityId",
      "courseActivity.description",
      "courseActivity.code",
      "courseActivity.prisonId",
      "courseActivity.scheduleStartDate",
      "offenderBooking.bookingId",
      "programService.activity",
      "comment",
    ).contains(
      Tuple.tuple(
        -13L,
        LocalDate.of(2017, 9, 13),
        "UNACAB",
        -1L,
        "Chapel Cleaner",
        "CC1",
        "LEI",
        LocalDate.of(2016, 8, 8),
        -2L,
        null,
        null,
      ),
    )
  }
}
