package uk.gov.justice.hmpps.prison.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.support.Order
import uk.gov.justice.hmpps.prison.api.support.TimeSlot
import uk.gov.justice.hmpps.prison.repository.support.StatusFilter
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser
import java.time.LocalDate
import java.time.Month

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = [PersistenceConfigs::class])
@WithMockAuthUser("ITAG_USER")
class AgencyRepositoryTest(
  @Autowired private val repository: AgencyRepository,
) {

  @Test
  fun testGetEnabledAgencyWhenActiveOnly() {
    val agency = repository.findAgency("LEI", StatusFilter.ACTIVE_ONLY, "INST")
    assertThat(agency).isPresent()
  }

  @Test
  fun testGetEnabledAgencyWithInactive() {
    val agency = repository.findAgency("LEI", StatusFilter.ALL, "INST")
    assertThat(agency).isPresent()
  }

  @Test
  fun testGetDisabledAgencyWhenActiveOnly() {
    val agency = repository.findAgency("ZZGHI", StatusFilter.ACTIVE_ONLY, "INST")
    assertThat(agency).isEmpty()
  }

  @Test
  fun testGetDisabledAgencyWithInactive() {
    val agency = repository.findAgency("ZZGHI", StatusFilter.ALL, "INST")
    assertThat(agency).isPresent()
  }

  @Test
  fun testGetEnabledAgencyWithNoAgencyTypeFilter() {
    val agency = repository.findAgency("LEI", StatusFilter.ALL, null)
    assertThat(agency).isPresent()
  }

  @Test
  fun testGetAgencyWithWrongTypeFilter() {
    val agency = repository.findAgency("COURT1", StatusFilter.ALL, "INST")
    assertThat(agency).isNotPresent()
  }

  @Test
  fun testGetAgencyWithCorrectTypeFilter() {
    val agency = repository.findAgency("COURT1", StatusFilter.ALL, "CRT")
    assertThat(agency).isPresent()
  }

  @Test
  fun testGetAgencyWithNoypeFilter() {
    val agency = repository.findAgency("COURT1", StatusFilter.ALL, null)
    assertThat(agency).isPresent()
  }

  @Test
  fun testGetAgencyLocations() {
    val locations = repository.getAgencyLocations("LEI", listOf("APP", "VISIT"), null, null)
    assertThat(locations).extracting("locationType").contains("AREA", "AREA", "CLAS", "WSHP")
  }

  @Test
  fun testGetAgencyLocationsEventTypeOccur() {
    val locations = repository.getAgencyLocations("LEI", listOf("OCCUR"), null, null)
    assertThat(locations).extracting("locationType")
      .contains("WING", "WING", "WING", "WING", "WING", "WING")
  }

  @Test
  fun testGetAgencyByType() {
    val agencies = repository.getAgenciesByType("INST")
    assertThat(agencies).extracting("agencyId")
      .contains("BMI", "BXI", "LEI", "MDI", "MUL", "SYI", "TRO", "WAI")

    assertThat(agencies).extracting("agencyType")
      .contains("INST", "INST", "INST", "INST", "INST", "INST", "INST", "INST")
  }

  @Test
  fun testGetAgencies() {
    val agencies = repository.getAgencies("agencyId", Order.ASC, 0, 10)
    assertThat(agencies.getItems()).extracting("agencyId")
      .containsAnyOf("ABDRCT", "BMI", "BXI", "COURT1", "LEI", "MDI", "MUL", "RNI", "SYI", "TRO")
    assertThat(agencies.getItems()).extracting("agencyType")
      .containsAnyOf("CRT", "INST", "INST", "CRT", "INST", "INST", "INST", "INST", "INST", "INST")
    assertThat(agencies.getItems()).extracting("active")
      .containsAnyOf(true, true, true, true, true, true, true, true, true, true)
  }

  @Test
  fun testGetAgencyLocationsNoResults1() {
    val locations = repository.getAgencyLocations("LEI", listOf("OTHER"), null, null)
    assertThat(locations).isEmpty()
  }

  @Test
  fun testGetAgencyLocationsNoResults2() {
    val locations = repository.getAgencyLocations("doesnotexist", listOf("APP"), null, null)
    assertThat(locations).isEmpty()
  }

  @Test
  fun testGetAgencyLocationsAll() {
    val locations = repository.getAgencyLocations("LEI", listOf(), null, null)
    assertThat(locations).hasSize(139)
  }

  @Test
  fun testGetAgencyLocationsWithDates() {
    val locations = repository.getAgencyLocationsBooked("LEI", LocalDate.of(2017, Month.SEPTEMBER, 11), null)
    assertThat(locations).hasSize(3)
  }

  @Test
  fun testGetAgencyLocationsWithDatesAM() {
    val locations = repository.getAgencyLocationsBooked("LEI", LocalDate.of(2017, Month.SEPTEMBER, 11), TimeSlot.AM)
    assertThat(locations).hasSize(1)
  }

  @Test
  fun testGetAgencyLocationsWithDatesPM() {
    val locations = repository.getAgencyLocationsBooked("LEI", LocalDate.of(2017, Month.SEPTEMBER, 11), TimeSlot.PM)
    assertThat(locations).hasSize(2)
  }

  @Test
  fun testGetAgencyLocationsWhenAllOffendersSuspended() {
    val locations = repository.getAgencyLocationsBooked("BXI", LocalDate.of(2021, Month.MAY, 1), null)

    assertThat(locations).extracting("locationId").containsExactly(-3001L, -3002L)
  }
}
