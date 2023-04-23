package uk.gov.justice.hmpps.prison.api.resource.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.AgyPrisonerPayProfile;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class AgencyPrisonerPayProfileIntTest extends ResourceTest {

  @Test
  public void agencyPayProfile_returnsSuccessAndData() {
    final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

    final var responseEntity = testRestTemplate.exchange(
        "/api/agencies/LEI/pay-profile",
        HttpMethod.GET,
        requestEntity,
        new ParameterizedTypeReference<AgyPrisonerPayProfile>() {}
    );

    assertThatStatus(responseEntity, 200);
    assertThat(responseEntity.getBody()).isInstanceOf(AgyPrisonerPayProfile.class);

    final var payProfile = responseEntity.getBody();

    assertThat(payProfile).isNotNull();
    assertThat(payProfile.getAgencyId()).isEqualTo("LEI");
    assertThat(payProfile.getStartDate()).isEqualTo(LocalDate.of(2020, 10, 1));
    assertThat(payProfile.getEndDate()).isNull();
    assertThat(payProfile.getAutoPayFlag()).isEqualTo("Y");
    assertThat(payProfile.getMinHalfDayRate()).isEqualTo(new BigDecimal("1.25"));
    assertThat(payProfile.getMaxHalfDayRate()).isEqualTo(new BigDecimal("5.25"));
    assertThat(payProfile.getMaxBonusRate()).isEqualTo(new BigDecimal("4.00"));
    assertThat(payProfile.getMaxPieceWorkRate()).isEqualTo(new BigDecimal("8.00"));
    assertThat(payProfile.getPayFrequency()).isEqualTo(1);
    assertThat(payProfile.getBackdateDays()).isEqualTo(7);
    assertThat(payProfile.getDefaultPayBandCode()).isEqualTo("1");
    assertThat(payProfile.getWeeklyAbsenceLimit()).isEqualTo(21);
  }

  @Test
  public void agencyPrisonerPayProfile_returnsNotFound() {
    final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of());

    final var responseEntity = testRestTemplate.exchange(
        "/api/agencies/XXX/pay-profile",
        HttpMethod.GET,
        requestEntity,
        ErrorResponse.class
    );

    assertThatStatus(responseEntity, 404);
    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().getUserMessage()).isEqualTo("Resource with id [XXX] not found.");
  }
}
