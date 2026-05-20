package uk.gov.justice.hmpps.prison.service.transformers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.api.model.Agency
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType

class AgencyTransformerTest {
  private val builder: AgencyLocation.AgencyLocationBuilder = AgencyLocation.builder()

  @BeforeEach
  fun setup() {
    builder.id("MDI").type(AgencyLocationType("INST")).description("moorland")
  }

  @Test
  fun transform_active_and_description_capitalisation() {
    val agency = builder.active(true).build()

    assertThat(AgencyTransformer.transform(agency, false, false)).isEqualTo(
      Agency.builder()
        .agencyId("MDI")
        .agencyType("INST")
        .description("Moorland")
        .active(true)
        .build(),
    )
  }

  @Test
  fun transform_inactive() {
    val agency = builder.active(false).build()

    assertThat(AgencyTransformer.transform(agency, false, false)).isEqualTo(
      Agency.builder()
        .agencyId("MDI")
        .agencyType("INST")
        .description("Moorland")
        .active(false)
        .build(),
    )
  }

  @Test
  fun transform_active_unspecified() {
    val agency = builder.build()

    assertThat(AgencyTransformer.transform(agency, false, false)).isEqualTo(
      Agency.builder()
        .agencyId("MDI")
        .agencyType("INST")
        .description("Moorland")
        .active(false)
        .build(),
    )
  }
}
