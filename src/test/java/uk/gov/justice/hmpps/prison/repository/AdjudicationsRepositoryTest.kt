package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ListAssert
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.adjudications.Adjudication
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationCharge
import uk.gov.justice.hmpps.prison.api.support.PageRequest
import uk.gov.justice.hmpps.prison.service.AdjudicationSearchCriteria
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import java.time.LocalDateTime

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
class AdjudicationsRepositoryTest(
  @Autowired private val repository: AdjudicationsRepository,
) {
  @Test
  fun findAdjudicationOffences() {
    var offences = repository.findAdjudicationOffences("A1181GG")
    assertThat(offences).extracting("id", "code", "description").containsExactly(
      Tuple.tuple(
        "85",
        "51:2D",
        "Detains any person against his will - detention against will of staff (not prison offr)",
      ),
      Tuple.tuple(
        "86",
        "51:8D",
        "Fails to comply with any condition upon which he is temporarily released under rule 9 - failure to comply with conditions of temp release",
      ),
    )

    offences = repository.findAdjudicationOffences("A1181HH")
    assertThat(offences).extracting("id", "code", "description").containsExactly(
      Tuple.tuple(
        "84",
        "51:2C",
        "Detains any person against his will - detention against will of prison officer grade",
      ),
      Tuple.tuple(
        "85",
        "51:2D",
        "Detains any person against his will - detention against will of staff (not prison offr)",
      ),
    )
  }

  @Test
  fun findAdjudicationLocations() {
    var locations = repository.findAdjudicationAgencies("A1181GG")
    assertThat(locations).extracting("agencyId", "description", "agencyType")
      .containsExactlyInAnyOrder(
        Tuple.tuple("LEI", "LEEDS", "INST"),
        Tuple.tuple("MDI", "MOORLAND", "INST"),
        Tuple.tuple("BXI", "BRIXTON", "INST"),
      )

    locations = repository.findAdjudicationAgencies("A1181HH")
    assertThat(locations).extracting("agencyId", "description", "agencyType")
      .containsExactlyInAnyOrder(
        Tuple.tuple("LEI", "LEEDS", "INST"),
        Tuple.tuple("MDI", "MOORLAND", "INST"),
      )
  }

  @Test
  fun retrieveAdjudicationsForOffender() {
    val results = repository.findAdjudications(
      AdjudicationSearchCriteria.builder()
        .offenderNumber("A1181GG")
        .pageRequest(PageRequest(0L, 10L))
        .build(),
    )

    assertThat(results.getItems())
      .containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION)
  }

  @Sql(
    scripts = ["/sql/adjudicationHistorySort_init.sql"],
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
  )
  @Sql(
    scripts = ["/sql/adjudicationHistorySort_clean.sql"],
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
  )
  @Test
  fun adjudicationsHistorySortTest() {
    val results = repository.findAdjudications(
      AdjudicationSearchCriteria.builder()
        .offenderNumber("A1181GG")
        .pageRequest(PageRequest(0L, 10L))
        .build(),
    )

    assertThat(
      results.getItems().first { it.adjudicationNumber == -3001L }.adjudicationCharges.first().findingCode,
    ).isEqualTo("PROVED")
  }

  @Sql(
    scripts = ["/sql/incident_no_suspect.sql"],
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
  )
  @Sql(
    scripts = ["/sql/incident_no_suspect_cleanup.sql"],
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
    config = SqlConfig(transactionMode = TransactionMode.ISOLATED),
  )
  @Test
  fun adjudicationHistoryContainsAdjudicationWithoutSuspectCode() {
    val results = repository.findAdjudications(
      AdjudicationSearchCriteria.builder()
        .offenderNumber("A1183SH")
        .pageRequest(PageRequest(0L, 10L))
        .build(),
    )

    assertThat(
      results.getItems().filter { it.adjudicationNumber == -3003L },
    ).isNotEmpty()
  }

  @Test
  fun filterByStartDate() {
    val results = repository.findAdjudications(
      AdjudicationSearchCriteria.builder()
        .offenderNumber("A1181GG")
        .startDate(MIDDLE_ADJUDICATION.reportTime.plusDays(1).toLocalDate())
        .pageRequest(PageRequest(0L, 10L))
        .build(),
    )

    assertThat(results.getItems()).containsExactlyInAnyOrder(LATEST_ADJUDICATION)
  }

  @Test
  fun filterByEndDate() {
    val results = repository.findAdjudications(
      AdjudicationSearchCriteria.builder()
        .offenderNumber("A1181GG")
        .endDate(MIDDLE_ADJUDICATION.reportTime.minusDays(1).toLocalDate())
        .pageRequest(PageRequest(0L, 10L))
        .build(),
    )

    assertThat(results.getItems()).containsExactly(EARLIEST_ADJUDICATION)
  }

  @Test
  fun filterByOffence() {
    val results = repository.findAdjudications(
      AdjudicationSearchCriteria.builder()
        .offenderNumber("A1181GG")
        .offenceId("86")
        .pageRequest(PageRequest(0L, 10L))
        .build(),
    )

    assertThat(results.getItems()).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION)
  }

  @Test
  fun filterByLocation() {
    val results = repository.findAdjudications(
      AdjudicationSearchCriteria.builder()
        .offenderNumber("A1181GG")
        .agencyId(EARLIEST_ADJUDICATION.agencyId)
        .pageRequest(PageRequest(0L, 10L))
        .build(),
    )

    assertThat(results.getItems()).containsExactly(EARLIEST_ADJUDICATION)
  }

  @Test
  fun pagination() {
    resultsFor(PageRequest(0L, null)).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION)
    resultsFor(PageRequest(1L, null)).containsExactly(MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION)
    resultsFor(PageRequest(2L, null)).containsExactly(EARLIEST_ADJUDICATION)
    resultsFor(PageRequest(3L, null)).isEmpty()

    resultsFor(PageRequest(null, 0L)).isEmpty()
    resultsFor(PageRequest(null, 1L)).containsExactly(LATEST_ADJUDICATION)
    resultsFor(PageRequest(null, 2L)).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION)
    resultsFor(PageRequest(null, 3L)).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION)
    resultsFor(PageRequest(null, 4L)).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION)

    resultsFor(PageRequest(1L, 1L)).containsExactly(MIDDLE_ADJUDICATION)
    resultsFor(PageRequest(1L, 2L)).containsExactly(MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION)
    resultsFor(PageRequest(2L, 1L)).containsExactly(EARLIEST_ADJUDICATION)
    resultsFor(PageRequest(2L, 2L)).containsExactly(EARLIEST_ADJUDICATION)
    resultsFor(PageRequest(3L, 1L)).isEmpty()
  }

  private fun resultsFor(pageRequest: PageRequest?): ListAssert<Adjudication> = assertThat(
    repository.findAdjudications(
      AdjudicationSearchCriteria.builder()
        .offenderNumber("A1181GG")
        .pageRequest(pageRequest)
        .build(),
    ).getItems(),
  )

  @Test
  fun anotherInmateHasAnAdjudicationForSameIncident() {
    val results = repository.findAdjudications(
      AdjudicationSearchCriteria.builder()
        .offenderNumber("A1181HH")
        .pageRequest(PageRequest(1L, 1L))
        .build(),
    )

    assertThat(results.getItems()).containsExactly(
      Adjudication(
        -2,
        LocalDateTime.of(2017, 2, 23, 0, 1),
        -1,
        "LEI",
        2L,
        listOf(
          AdjudicationCharge(
            "5139/3",
            "51:2C",
            "Detains any person against his will - detention against will of prison officer grade",
            "NOT_PROVEN",
          ),
        ),
      ),
    )
  }

  companion object {
    private val EARLIEST_ADJUDICATION = Adjudication(
      -3,
      LocalDateTime.of(2019, 8, 25, 0, 3),
      -3,
      "MDI",
      1L,
      listOf(
        AdjudicationCharge(
          "5139/4",
          "51:2D",
          "Detains any person against his will - detention against will of staff (not prison offr)",
          "PROVED",
        ),
      ),
    )
    private val MIDDLE_ADJUDICATION = Adjudication(
      -3001,
      LocalDateTime.of(2019, 9, 25, 0, 4),
      -3001,
      "LEI",
      2L,
      listOf(
        AdjudicationCharge(
          "5139/8",
          "51:8D",
          "Fails to comply with any condition upon which he is temporarily released under rule 9 - failure to comply with conditions of temp release",
          null,
        ),
      ),
    )

    private val LATEST_ADJUDICATION = Adjudication(
      -3002,
      LocalDateTime.of(2019, 10, 25, 0, 5),
      -3002,
      "BXI",
      3L,
      listOf(
        AdjudicationCharge(
          "5139/9",
          "51:8D",
          "Fails to comply with any condition upon which he is temporarily released under rule 9 - failure to comply with conditions of temp release",
          null,
        ),
      ),
    )
  }
}
