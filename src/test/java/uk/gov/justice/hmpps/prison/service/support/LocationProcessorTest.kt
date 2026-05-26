package uk.gov.justice.hmpps.prison.service.support

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.api.model.Location

class LocationProcessorTest {
  @Test
  fun stripAgencyId() {
    var newDescription = LocationProcessor.stripAgencyId(TEST_LOCATION_DESCRIPTION, TEST_AGENCY_ID)

    assertThat(newDescription).isEqualTo(TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED)

    newDescription = LocationProcessor.stripAgencyId(TEST_LOCATION_USER_DESCRIPTION, TEST_AGENCY_ID)

    assertThat(newDescription).isEqualTo(TEST_LOCATION_USER_DESCRIPTION)

    newDescription = LocationProcessor.stripAgencyId(null, TEST_AGENCY_ID)

    assertThat(newDescription).isNull()

    newDescription = LocationProcessor.stripAgencyId(TEST_LOCATION_DESCRIPTION, null)

    assertThat(newDescription).isEqualTo(TEST_LOCATION_DESCRIPTION)
  }

  @Test
  fun processLocationUserDescNotPreferred() {
    val testLocation = buildTestLocation(TEST_LOCATION_USER_DESCRIPTION)

    val processedLocation = LocationProcessor.processLocation(testLocation)

    assertThat(processedLocation.agencyId).isEqualTo(TEST_AGENCY_ID)
    assertThat(processedLocation.locationId).isEqualTo(TEST_LOCATION_ID)
    assertThat(processedLocation.locationType).isEqualTo(TEST_LOCATION_TYPE)
    assertThat(processedLocation.locationPrefix).isEqualTo(TEST_LOCATION_DESCRIPTION)
    assertThat(processedLocation.description).isEqualTo(
      TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED,
    )
    assertThat(processedLocation.userDescription).isEqualTo(TEST_LOCATION_USER_DESCRIPTION)
  }

  @Test
  fun processLocationUserDescPreferred() {
    val testLocation = buildTestLocation(TEST_LOCATION_USER_DESCRIPTION)

    val processedLocation = LocationProcessor.processLocation(testLocation, true, false)

    assertThat(processedLocation.agencyId).isEqualTo(TEST_AGENCY_ID)
    assertThat(processedLocation.locationId).isEqualTo(TEST_LOCATION_ID)
    assertThat(processedLocation.locationType).isEqualTo(TEST_LOCATION_TYPE)
    assertThat(processedLocation.locationPrefix).isEqualTo(TEST_LOCATION_DESCRIPTION)
    assertThat(processedLocation.description).isEqualTo(TEST_LOCATION_USER_DESCRIPTION)
    assertThat(processedLocation.userDescription).isEqualTo(TEST_LOCATION_USER_DESCRIPTION)
  }

  @Test
  fun processLocationUserDescPreferredButNull() {
    val testLocation = buildTestLocation(null)

    val processedLocation = LocationProcessor.processLocation(testLocation, true, false)

    assertThat(processedLocation.agencyId).isEqualTo(TEST_AGENCY_ID)
    assertThat(processedLocation.locationId).isEqualTo(TEST_LOCATION_ID)
    assertThat(processedLocation.locationType).isEqualTo(TEST_LOCATION_TYPE)
    assertThat(processedLocation.locationPrefix).isEqualTo(TEST_LOCATION_DESCRIPTION)
    assertThat(processedLocation.description).isEqualTo(
      TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED,
    )
    assertThat(processedLocation.userDescription).isNull()
  }

  @Test
  fun processLocationsUserDescNotPreferred() {
    val testLocations = buildTestLocationList(TEST_LOCATION_USER_DESCRIPTION, TEST_SECOND_LOCATION_USER_DESCRIPTION)

    val processedLocations = LocationProcessor.processLocations(testLocations)

    assertThat(processedLocations).hasSize(testLocations.size)

    assertThat(processedLocations[0].agencyId).isEqualTo(TEST_AGENCY_ID)
    assertThat(processedLocations[0].locationId).isEqualTo(TEST_LOCATION_ID)
    assertThat(processedLocations[0].locationType).isEqualTo(TEST_LOCATION_TYPE)
    assertThat(processedLocations[0].locationPrefix).isEqualTo(TEST_LOCATION_DESCRIPTION)
    assertThat(processedLocations[0].description).isEqualTo(
      TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED,
    )
    assertThat(processedLocations[0].userDescription).isEqualTo(TEST_LOCATION_USER_DESCRIPTION)

    assertThat(processedLocations[1].agencyId).isEqualTo(TEST_AGENCY_ID)
    assertThat(processedLocations[1].locationId).isEqualTo(TEST_SECOND_LOCATION_ID)
    assertThat(processedLocations[1].locationType).isEqualTo(TEST_LOCATION_TYPE)
    assertThat(processedLocations[1].locationPrefix).isEqualTo(
      TEST_SECOND_LOCATION_DESCRIPTION,
    )
    assertThat(processedLocations[1].description).isEqualTo(
      TEST_SECOND_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED,
    )
    assertThat(processedLocations[1].userDescription).isEqualTo(
      TEST_SECOND_LOCATION_USER_DESCRIPTION,
    )
  }

  @Test
  fun processLocationsUserDescPreferred() {
    val testLocations = buildTestLocationList(TEST_LOCATION_USER_DESCRIPTION, TEST_SECOND_LOCATION_USER_DESCRIPTION)

    val processedLocations = LocationProcessor.processLocations(testLocations, true)

    assertThat(processedLocations).hasSize(testLocations.size)

    assertThat(processedLocations[0].agencyId).isEqualTo(TEST_AGENCY_ID)
    assertThat(processedLocations[0].locationId).isEqualTo(TEST_LOCATION_ID)
    assertThat(processedLocations[0].locationType).isEqualTo(TEST_LOCATION_TYPE)
    assertThat(processedLocations[0].locationPrefix).isEqualTo(TEST_LOCATION_DESCRIPTION)
    assertThat(processedLocations[0].description).isEqualTo(TEST_LOCATION_USER_DESCRIPTION)
    assertThat(processedLocations[0].userDescription).isEqualTo(TEST_LOCATION_USER_DESCRIPTION)

    assertThat(processedLocations[1].agencyId).isEqualTo(TEST_AGENCY_ID)
    assertThat(processedLocations[1].locationId).isEqualTo(TEST_SECOND_LOCATION_ID)
    assertThat(processedLocations[1].locationType).isEqualTo(TEST_LOCATION_TYPE)
    assertThat(processedLocations[1].locationPrefix).isEqualTo(
      TEST_SECOND_LOCATION_DESCRIPTION,
    )
    assertThat(processedLocations[1].description).isEqualTo(
      TEST_SECOND_LOCATION_USER_DESCRIPTION,
    )
    assertThat(processedLocations[1].userDescription).isEqualTo(
      TEST_SECOND_LOCATION_USER_DESCRIPTION,
    )
  }

  @Test
  fun processLocationsUserDescPreferredButNull() {
    val testLocations = buildTestLocationList(null, null)

    val processedLocations = LocationProcessor.processLocations(testLocations, true)

    assertThat(processedLocations).hasSize(testLocations.size)

    assertThat(processedLocations[0].agencyId).isEqualTo(TEST_AGENCY_ID)
    assertThat(processedLocations[0].locationId).isEqualTo(TEST_LOCATION_ID)
    assertThat(processedLocations[0].locationType).isEqualTo(TEST_LOCATION_TYPE)
    assertThat(processedLocations[0].locationPrefix).isEqualTo(TEST_LOCATION_DESCRIPTION)
    assertThat(processedLocations[0].description).isEqualTo(
      TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED,
    )
    assertThat(processedLocations[0].userDescription).isNull()

    assertThat(processedLocations[1].agencyId).isEqualTo(TEST_AGENCY_ID)
    assertThat(processedLocations[1].locationId).isEqualTo(TEST_SECOND_LOCATION_ID)
    assertThat(processedLocations[1].locationType).isEqualTo(TEST_LOCATION_TYPE)
    assertThat(processedLocations[1].locationPrefix).isEqualTo(
      TEST_SECOND_LOCATION_DESCRIPTION,
    )
    assertThat(processedLocations[1].description).isEqualTo(
      TEST_SECOND_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED,
    )
    assertThat(processedLocations[1].userDescription).isNull()
  }

  private fun buildTestLocation(userDescription: String?): Location? = Location.builder()
    .agencyId(TEST_AGENCY_ID)
    .locationId(TEST_LOCATION_ID)
    .locationType(TEST_LOCATION_TYPE)
    .description(TEST_LOCATION_DESCRIPTION)
    .userDescription(userDescription)
    .build()

  private fun buildTestLocationList(userDescription1: String?, userDescription2: String?): List<Location> = listOf(
    Location.builder()
      .agencyId(TEST_AGENCY_ID)
      .locationId(TEST_LOCATION_ID)
      .locationType(TEST_LOCATION_TYPE)
      .description(TEST_LOCATION_DESCRIPTION)
      .userDescription(userDescription1)
      .build(),
    Location.builder()
      .agencyId(TEST_AGENCY_ID)
      .locationId(TEST_SECOND_LOCATION_ID)
      .locationType(TEST_LOCATION_TYPE)
      .description(TEST_SECOND_LOCATION_DESCRIPTION)
      .userDescription(userDescription2)
      .build(),
  )

  companion object {
    private const val TEST_AGENCY_ID = "LEI"
    private const val TEST_LOCATION_ID = 1L
    private const val TEST_LOCATION_TYPE = "INST"
    private const val TEST_LOCATION_DESCRIPTION = "LEI-A"
    private const val TEST_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED = "A"
    private const val TEST_LOCATION_USER_DESCRIPTION = "Block A"

    private const val TEST_SECOND_LOCATION_ID = 2L
    private const val TEST_SECOND_LOCATION_DESCRIPTION = "LEI-B"
    private const val TEST_SECOND_LOCATION_DESCRIPTION_WITH_AGENCY_ID_STRIPPED = "B"
    private const val TEST_SECOND_LOCATION_USER_DESCRIPTION = "Block B"
  }
}
