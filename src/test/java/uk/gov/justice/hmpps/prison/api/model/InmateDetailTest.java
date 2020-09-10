package uk.gov.justice.hmpps.prison.api.model;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InmateDetailTest {
    @Test
    public void whenNoInmateProfile_thenNoReligion() {
        assertThat(new InmateDetail().getReligion()).isNull();
    }

    @Test
    public void givenProfileInformationWithoutReligion_whenProfileSetOnInmateDetail_thenNoReligion() {
        assertThat(InmateDetail
                .builder()
                .profileInformation(List.of(
                        ProfileInformation.builder().type("ERIC").resultValue("A").build(),
                        ProfileInformation.builder().type("ERIC").resultValue("A").build()))
                .build()
                .getReligion())
                .isNull();
    }

    @Test
    public void givenProfileInformationWithReligion_whenProfileSetOnInmateDetail_thenReligionPropertyIsSet() {
        val inmateDetail = new InmateDetail();
        inmateDetail.setProfileInformation(List.of(
                ProfileInformation.builder().type("ERIC").resultValue("A").build(),
                ProfileInformation.builder().type("RELF").resultValue("B").build()));

        assertThat(inmateDetail.getReligion())
                .isEqualTo("B");
    }

    @Test
    public void givenProfileInformationWithMultipleReligions_whenProfileSetOnInmateDetail_thenReligionPropertyIsSetWithFirstReligion() {
        val inmateDetail = new InmateDetail();
        inmateDetail.setProfileInformation(List.of(
                ProfileInformation.builder().type("ERIC").resultValue("A").build(),
                ProfileInformation.builder().type("RELF").resultValue("B").build(),
                ProfileInformation.builder().type("ERIC").resultValue("C").build(),
                ProfileInformation.builder().type("RELF").resultValue("D").build()));

        assertThat(inmateDetail.getReligion())
                .isEqualTo("B");
    }
}
