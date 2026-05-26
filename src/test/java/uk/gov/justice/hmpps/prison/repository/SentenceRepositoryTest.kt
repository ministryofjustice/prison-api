package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.OffenceDetail
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.LocalDate

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
@WithMockAuthUser("ITAG_USER")
class SentenceRepositoryTest(
  @Autowired private val repository: SentenceRepository,
) {
  @Test
  fun testGetMainOffenceDetailsSingleOffence() {
    val offenceDetails = repository.getMainOffenceDetails(-1L)
    assertThat(offenceDetails)
      .extracting<String, RuntimeException> { it.offenceDescription }
      .containsExactly("Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)")
  }

  @Test
  fun testGetMainOffenceDetailsMultipleOffences() {
    val offenceDetails = repository.getMainOffenceDetails(-7L)
    assertThat(offenceDetails)
      .extracting<String, RuntimeException> { it.offenceDescription }
      .containsExactly(
        "Cause the carrying of a mascot etc on motor vehicle in position likely to cause injury",
        "Cause another to use a vehicle where the seat belt is not securely fastened to the anchorage point.",
      )
  }

  @Test
  fun testGetMainOffenceDetailsInvalidBookingId() {
    val offenceDetails = repository.getMainOffenceDetails(1001L)
    assertThat(offenceDetails).isEmpty()
  }

  @Test
  fun testGetMainOffenceDetailsMultipleBookings() {
    val offences = repository.getMainOffenceDetails(listOf(-1L, -7L))

    assertThat(offences).containsExactlyInAnyOrder(
      OffenceDetail(
        -1L,
        "Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)",
        "RV98011",
        "RV98",
      ),
      OffenceDetail(
        -7L,
        "Cause another to use a vehicle where the seat belt is not securely fastened to the anchorage point.",
        "RC86360",
        "RC86",
      ),
      OffenceDetail(
        -7L,
        "Cause the carrying of a mascot etc on motor vehicle in position likely to cause injury",
        "RC86355",
        "RC86",
      ),
    )
  }

  @Test
  fun testGetOffenceHistory() {
    val offenceDetails = repository.getOffenceHistory("A1234AA", true)
    assertThat(offenceDetails).extracting(
      "bookingId",
      "offenceDate",
      "offenceRangeDate",
      "offenceDescription",
      "mostSerious",
      "primaryResultCode",
      "secondaryResultCode",
      "courtDate",
    ).containsExactly(
      Tuple.tuple(
        -1L,
        LocalDate.of(2017, 12, 24),
        null,
        "Cause exceed max permitted wt of artic' vehicle - No of axles/configuration (No MOT/Manufacturer's Plate)",
        true,
        "1004",
        null,
        LocalDate.of(2017, 7, 2),
      ),
      Tuple.tuple(
        -1L,
        LocalDate.of(2018, 9, 1),
        LocalDate.of(2018, 9, 15),
        "Cause another to use a vehicle where the seat belt buckle/other fastening was not maintained so that the belt could be readily fastened or unfastened/kept free from temporary or permanent obstruction/readily accessible to a person sitting in the seat.",
        false,
        null,
        "1006",
        null,
      ),
    )
  }

  @Test
  fun testGetOffenceHistoryOffenderWithoutConvictions() {
    val offenceDetails = repository.getOffenceHistory("A1234AB", true)
    assertThat(offenceDetails).isEmpty()
  }

  @Test
  fun testGetOffenceHistoryGetAllOffencesOffenderWithoutConvictions() {
    val offenceDetails = repository.getOffenceHistory("A1234AB", false)
    assertThat(offenceDetails).extracting(
      "bookingId",
      "primaryResultConviction",
      "primaryResultDescription",
      "secondaryResultConviction",
      "secondaryResultDescription",
      "offenceDescription",
      "courtDate",
    ).containsExactly(
      Tuple.tuple(
        -2L,
        false, // no conviction result 1
        "Adjourned for Consideration of an ASBO", // description of result 1
        false, // no conviction result 2
        null, // description of result 2 (no result 2 provided)
        "Actual bodily harm", // offence description
        LocalDate.of(2017, 2, 22),
      ),
    )
  }
}
