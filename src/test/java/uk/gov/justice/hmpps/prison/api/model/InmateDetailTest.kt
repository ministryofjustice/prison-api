package uk.gov.justice.hmpps.prison.api.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InmateDetailTest {
  @Test
  fun whenNoInmateProfile_thenNoReligion() {
    assertThat(InmateDetail().religion).isNull()
  }

  @Test
  fun givenProfileInformationWithoutReligion_whenProfileSetOnInmateDetail_thenNoReligion() {
    assertThat(
      InmateDetail
        .builder()
        .profileInformation(
          listOf(
            ProfileInformation.builder().type("ERIC").resultValue("A").build(),
            ProfileInformation.builder().type("ERIC").resultValue("A").build(),
          ),
        )
        .build()
        .religion,
    )
      .isNull()
  }

  @Test
  fun givenProfileInformationWithReligion_whenProfileSetOnInmateDetail_thenReligionPropertyIsSet() {
    val inmateDetail = InmateDetail()
    inmateDetail.setProfileInformation(
      listOf(
        ProfileInformation.builder().type("ERIC").resultValue("A").build(),
        ProfileInformation.builder().type("RELF").resultValue("B").build(),
      ),
    )

    assertThat(inmateDetail.religion)
      .isEqualTo("B")
  }

  @Test
  fun givenProfileInformationWithMultipleReligions_whenProfileSetOnInmateDetail_thenReligionPropertyIsSetWithFirstReligion() {
    val inmateDetail = InmateDetail()
    inmateDetail.setProfileInformation(
      listOf(
        ProfileInformation.builder().type("ERIC").resultValue("A").build(),
        ProfileInformation.builder().type("RELF").resultValue("B").build(),
        ProfileInformation.builder().type("ERIC").resultValue("C").build(),
        ProfileInformation.builder().type("RELF").resultValue("D").build(),
      ),
    )

    assertThat(inmateDetail.religion)
      .isEqualTo("B")
  }
}
