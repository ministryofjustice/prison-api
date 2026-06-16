package uk.gov.justice.hmpps.prison.repository

import jakarta.validation.constraints.NotBlank
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.OffenderIn
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception
import uk.gov.justice.hmpps.prison.api.model.OffenderLatestArrivalDate
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement
import uk.gov.justice.hmpps.prison.api.model.OffenderOut
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.util.Optional

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
class MovementsRepositoryTest(
  @Autowired private val repository: MovementsRepository,
) {
  @Test
  fun canRetrieveAListOfMovementDetails1() {
    val threshold = LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0, 0)
    val recentMovements =
      repository.getRecentMovementsByDate(threshold, LocalDate.of(2017, Month.JULY, 16), listOf())
    assertThat(recentMovements).hasSize(1) // TAP is excluded
    assertThat(recentMovements)
      .extracting("offenderNo", "createDateTime", "fromAgency", "toAgency", "movementType", "directionCode")
      .contains(
        tuple(
          "Z0024ZZ",
          LocalDateTime.of(2017, Month.FEBRUARY, 24, 0, 0),
          "OUT",
          "LEI",
          "ADM",
          "IN",
        ),
      )
  }

  @Test
  fun canRetrieveAListOfMovementDetails2() {
    val threshold = LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0, 0)
    val recentMovements =
      repository.getRecentMovementsByDate(threshold, LocalDate.of(2017, Month.AUGUST, 16), listOf())
    assertThat(recentMovements).hasSize(2)
    assertThat(recentMovements)
      .extracting("offenderNo", "createDateTime", "fromAgency", "toAgency", "movementType", "directionCode")
      .contains(
        tuple("Z0021ZZ", LocalDateTime.of(2017, Month.FEBRUARY, 21, 0, 0), "LEI", "OUT", "REL", "OUT"),
        tuple("Z0019ZZ", LocalDateTime.of(2017, Month.FEBRUARY, 19, 0, 0), "LEI", "BMI", "TRN", "OUT"),
      )
  }

  @Test
  fun canRetrieveRollCountMovements1() {
    val movementCount = repository.getMovementCount("LEI", LocalDate.of(2017, Month.JULY, 16))
    assertThat(movementCount.getIn()).isEqualTo(1)
    assertThat(movementCount.getOut()).isEqualTo(1)
  }

  @Test
  fun canRetrieveRollCountMovements2() {
    val movementCount = repository.getMovementCount("LEI", LocalDate.of(2012, Month.JULY, 5))
    assertThat(movementCount.getIn()).isEqualTo(5)
    assertThat(movementCount.getOut()).isEqualTo(0)
  }

  @Test
  fun canRetrieveRollCountMovements3() {
    val movementCount = repository.getMovementCount("LEI", LocalDate.of(2018, Month.FEBRUARY, 2))
    assertThat(movementCount.getIn()).isEqualTo(0)
    assertThat(movementCount.getOut()).isEqualTo(0)
  }

  @Test
  fun canRetrieveRecentMovementsByOffendersAndMovementTypes() {
    val movements = repository.getMovementsByOffenders(
      listOf("A6676RS"),
      listOf("TRN"),
      true,
      false,
    )

    assertThat(movements)
      .extracting<String, RuntimeException> { it.toAgency }
      .containsExactly("MDI")
  }

  @Test
  fun canRetrieveMovementsByOffendersAndMovementTypes() {
    val movements = repository.getMovementsByOffenders(
      listOf("A6676RS"),
      listOf("TRN"),
      false,
      false,
    )

    assertThat(movements)
      .extracting<String, RuntimeException> { it.toAgency }
      .containsOnly("BMI", "MDI")
  }

  @Test
  fun canRetrieveRecentMovementsByOffenders() {
    val movements =
      repository.getMovementsByOffenders(listOf("A6676RS"), listOf(), true, false)

    assertThat(movements)
      .extracting<String, RuntimeException> { it.toCity }
      .containsExactly("Wadhurst")
  }

  @Test
  fun canRetrieveMovementsByOffenders() {
    val movements =
      repository.getMovementsByOffenders(listOf("A6676RS"), listOf(), false, false)

    assertThat(movements)
      .extracting<String, RuntimeException> { it.fromAgency }
      .containsOnly("BMI", "LEI")
  }

  @Test
  fun canRetrieveEnRouteOffenderMovements() {
    val movements = repository.getEnrouteMovementsOffenderMovementList("LEI", LocalDate.of(2017, 10, 12))

    assertThat(movements)
      .extracting<@NotBlank String, RuntimeException> { it.offenderNo }
      .containsOnly("A1183SH", "A1183AD")
  }

  @Test
  fun canRetrieveEnRouteOffenderCount() {
    val count = repository.getEnRouteMovementsOffenderCount("LEI", LocalDate.of(2017, 10, 12))

    assertThat(count).isEqualTo(2)
  }

  @Test
  fun canRetrieveOffendersIn() {
    val offendersIn = repository.getOffendersIn("LEI", LocalDate.of(2017, 10, 12))

    assertThat(offendersIn).containsExactlyInAnyOrder(
      OffenderIn(
        "A6676RS",
        -29L,
        LocalDate.of(1945, 1, 10),
        "Neil",
        null,
        "Bradley",
        "BMI",
        "Birmingham",
        "LEI",
        "Leeds",
        null,
        null,
        LocalTime.of(10, 45, 0),
        LocalDateTime.of(2017, 10, 12, 10, 45, 0),
        "LANDING H/1",
        "ADM",
        "Unconvicted Remand",
        "Birmingham Youth Court, Justice Avenue",
      ),

    )
  }

  @Test
  fun canRetrieveOffendersOutForAGivenDate() {
    val offender = OffenderMovement.builder()
      .offenderNo("Z0020ZZ")
      .dateOfBirth(LocalDate.of(1966, 1, 1))
      .firstName("BURT")
      .lastName("REYNOLDS")
      .fromAgency("LEI")
      .directionCode("OUT")
      .movementType("TAP")
      .movementTime(LocalTime.of(0, 0))
      .movementDate(LocalDate.of(2017, 7, 16))
      .movementReasonDescription("Funerals And Deaths")
      .movementSequence("2")
      .build()

    assertThat(
      repository.getOffendersOut(
        "LEI",
        LocalDate.of(2017, Month.JULY, 16),
        null,
      ),
    ).containsExactly(offender)
  }

  @Test
  fun canRetrieveOffendersOutForAGivenDateAndMoveType() {
    val offender = OffenderMovement.builder()
      .offenderNo("Z0020ZZ")
      .dateOfBirth(LocalDate.of(1966, 1, 1))
      .firstName("BURT")
      .lastName("REYNOLDS")
      .fromAgency("LEI")
      .directionCode("OUT")
      .movementType("TAP")
      .movementTime(LocalTime.of(0, 0))
      .movementDate(LocalDate.of(2017, 7, 16))
      .movementReasonDescription("Funerals And Deaths")
      .movementSequence("2")
      .build()

    assertThat(
      repository.getOffendersOut(
        "LEI",
        LocalDate.of(2017, Month.JULY, 16),
        "TAP",
      ),
    ).containsExactly(offender)
    assertThat(
      repository.getOffendersOut(
        "LEI",
        LocalDate.of(2017, Month.JULY, 16),
        "REL",
      ),
    ).isEmpty()
  }

  @Test
  fun canRetrieveOffendersInReception() {
    val offenders = repository.getOffendersInReception("MDI")

    assertThat(offenders).containsExactly(
      OffenderInReception.builder()
        .firstName("AMY")
        .lastName("DUDE")
        .offenderNo("A1181DD")
        .bookingId(-46L)
        .dateOfBirth(LocalDate.of(1980, 1, 2))
        .build(),
    )
  }

  @Test
  fun canRetrieveOffendersCurrentlyOut() {
    val offenders = repository.getOffendersCurrentlyOut(-13)
    assertThat(offenders).containsExactlyInAnyOrder(
      OffenderOut.builder().offenderNo("Z0025ZZ").bookingId(-25L).dateOfBirth(LocalDate.of(1974, 1, 1))
        .firstName("MATTHEW").lastName("SMITH").location("LANDING H/1").build(),
      OffenderOut.builder().offenderNo("Z0024ZZ").bookingId(-24L).dateOfBirth(LocalDate.of(1958, 1, 1))
        .firstName("LUCIUS").lastName("FOX").location("LANDING H/1").build(),
    )
  }

  @Test
  fun canRetrieveOffendersCurrentlyOutOfAgency() {
    val offenders = repository.getOffendersCurrentlyOut("LEI")
    assertThat(offenders).containsExactlyInAnyOrder(
      OffenderOut.builder().offenderNo("Z0025ZZ").bookingId(-25L).dateOfBirth(LocalDate.of(1974, 1, 1))
        .firstName("MATTHEW").lastName("SMITH").location("LANDING H/1").build(),
      OffenderOut.builder().offenderNo("Z0024ZZ").bookingId(-24L).dateOfBirth(LocalDate.of(1958, 1, 1))
        .firstName("LUCIUS").lastName("FOX").location("LANDING H/1").build(),
    )
  }

  @Test
  fun canRetrieveRecentMoves_byMovementTypes() {
    val threshold = LocalDateTime.of(2000, Month.JANUARY, 1, 0, 0, 0)
    val recentMovements = repository.getRecentMovementsByDate(
      threshold,
      LocalDate.of(2017, Month.AUGUST, 16),
      listOf("TRN"),
    )
    assertThat(recentMovements).hasSize(1)
    assertThat(recentMovements)
      .extracting("offenderNo", "createDateTime", "fromAgency", "toAgency", "movementType", "directionCode")
      .contains(
        tuple(
          "Z0019ZZ",
          LocalDateTime.of(2017, Month.FEBRUARY, 19, 0, 0),
          "LEI",
          "BMI",
          "TRN",
          "OUT",
        ),
      )
  }

  @Test
  fun canRetrieveCourtEventsByAgency() {
    val fromDate = LocalDate.of(2017, Month.OCTOBER, 16)
    val courtEvents = repository.getCourtEvents("LEI", fromDate)

    assertThat(courtEvents).isNotEmpty()
    assertThat(courtEvents)
      .extracting("offenderNo", "fromAgency", "toAgency", "eventClass", "eventType", "eventStatus", "directionCode")
      .contains(tuple("A1234AG", "LEI", "LEI", "EXT_MOV", "CRT", "COMP", "IN"))
  }

  @Test
  fun canRetrieveReleaseEventsByAgency() {
    val fromTime = LocalDate.of(2022, Month.FEBRUARY, 2)
    val releaseEvents = repository.getOffenderReleases("LEI", fromTime)

    assertThat(releaseEvents).isNotEmpty()
    assertThat(releaseEvents)
      .extracting(
        "eventClass",
        "eventStatus",
        "movementTypeCode",
        "movementTypeDescription",
        "offenderNo",
        "movementReasonCode",
      )
      .contains(tuple("EXT_MOV", "SCH", "REL", "Release", "Z0024ZZ", "DD"))
  }

  @Test
  fun getIndividualSchedules() {
    // Match with specific rows loaded in the seeded data
    val transferEvents = repository.getIndividualSchedules("LEI", LocalDate.now())

    assertThat(transferEvents)
      .extracting("eventClass", "eventStatus", "eventType", "offenderNo", "fromAgency", "toAgency")
      .contains(tuple("EXT_MOV", "SCH", "TRN", "A1234AC", "LEI", "MDI"))
      .contains(tuple("EXT_MOV", "SCH", "TRN", "A1234AC", "MDI", "LEI"))
  }

  @Test
  fun canRetrieveLatestArrivalDate() {
    val arrivalDate = repository.getLatestArrivalDate("Z0024ZZ")
    assertThat(arrivalDate).isEqualTo(Optional.of(LocalDate.of(2017, 7, 16)))
  }

  @Test
  fun canHandleNoLatestArrivalDate() {
    val arrivalDate = repository.getLatestArrivalDate("Z0020XY")
    assertThat(arrivalDate).isEmpty()
  }

  @Test
  fun canRetrieveLatestArrivalDatesBulk() {
    val offenderNumbers = listOf("Z0018ZZ", "Z0019ZZ", "Z0024ZZ")

    val latestArrivalDates = repository.getLatestArrivalDates(offenderNumbers)
    assertThat(latestArrivalDates).containsExactlyInAnyOrder(
      OffenderLatestArrivalDate("Z0018ZZ", LocalDate.of(2012, 7, 5)),
      OffenderLatestArrivalDate("Z0019ZZ", LocalDate.of(2011, 11, 7)),
      OffenderLatestArrivalDate("Z0024ZZ", LocalDate.of(2017, 7, 16)),
    )
  }

  @Test
  fun canHandleArrivalDatesBulkNotFound() {
    val offenderNumbers = listOf("Z0024ZZ", "Z0020XY")

    val latestArrivalDates = repository.getLatestArrivalDates(offenderNumbers)
    assertThat(latestArrivalDates).containsExactly(
      OffenderLatestArrivalDate("Z0024ZZ", LocalDate.of(2017, 7, 16)),
    )
  }

  @Test
  fun canHandleArrivalDatesEmptyList() {
    val offenderNumbers = listOf<String>()

    val latestArrivalDates = repository.getLatestArrivalDates(offenderNumbers)
    assertThat(latestArrivalDates).isEmpty()
  }

  @Test
  fun canHandleArrivalDatesNullList() {
    val latestArrivalDates = repository.getLatestArrivalDates(null)
    assertThat(latestArrivalDates).isEmpty()
  }
}
