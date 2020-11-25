package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.repository.BookingRepository;
import uk.gov.justice.hmpps.prison.repository.OffenderBookingIdSeq;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderDamageObligationRepository;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OffenderMovementsResourceImplIntTest_offenderDamageObligations extends ResourceTest {
    private String token;

    @MockBean
    private OffenderDamageObligationRepository offenderDamageObligationRepository;

    @MockBean
    private BookingRepository bookingRepository;

    @BeforeEach
    public void setup() {
        token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
    }

    @Test
    public void retrieveOffenderTransactionHistory() {
        stubVerifyOffenderAccess("A12345");

        when(offenderDamageObligationRepository.findOffenderDamageObligationByOffender_NomsId(any())).thenReturn(List.of(
                OffenderDamageObligation.builder()
                .id(1L)
                .comment("Damages made to canteen furniture")
                .amountPaid(BigDecimal.valueOf(500))
                .amountToPay(BigDecimal.ZERO)
                .startDateTime(LocalDateTime.parse("2020-10-10T10:00"))
                .endDateTime(LocalDateTime.parse("2020-10-22T10:00"))
                .offender(Offender.builder().nomsId("A12345").build())
                .referenceNumber("123")
                .status("ACTIVE")
                .prison(AgencyLocation.builder().id("MDI").description("Moorland").build())
                .build()
        ));

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/{offenderNo}/damage-obligations",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                }, "A12345");

        assertThatJsonFileAndStatus(response, 200, "offender_damage_obligations.json");
    }

    @Test
    public void offenderNotFound() {
        when(offenderDamageObligationRepository.findOffenderDamageObligationByOffender_NomsId(any())).thenThrow(new EntityNotFoundException());

        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/{offenderNo}/damage-obligations",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                }, "A12345");

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void offenderInADifferentCaseLoad() {
        final var request = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offenders/{offenderNo}/damage-obligations",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<String>() {
                }, "Z00028");

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    private void stubVerifyOffenderAccess(final String offenderNo) {
        when(bookingRepository.getLatestBookingIdentifierForOffender(any()))
                .thenReturn(Optional.of(new OffenderBookingIdSeq(offenderNo, 1L, 1)));
        when(bookingRepository.verifyBookingAccess(any(), any())).thenReturn(true);
    }

    @Test
    public void when_NoStatus_Then_DefaultToALL() {
        stubVerifyOffenderAccess("A12345");

        final var request = createHttpEntity(token, null);
        final var response = testRestTemplate.exchange(
            "/api/offenders/{offenderNo}/damage-obligations",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<String>() {
            }, "A12345");

        verify(offenderDamageObligationRepository, times(1)).findOffenderDamageObligationByOffender_NomsId("A12345");
        verify(offenderDamageObligationRepository, times(0)).findOffenderDamageObligationByOffender_NomsIdAndStatus(anyString(), anyString());
    }

    @Test
    public void when_StatusIsACTIVE_Then_DoNotDefaultToALL() {
        stubVerifyOffenderAccess("A12345");

        final var request = createHttpEntity(token, null);
        final var response = testRestTemplate.exchange(
            "/api/offenders/{offenderNo}/damage-obligations?status=ACTIVE",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<String>() {
            }, "A12345");

        verify(offenderDamageObligationRepository, times(0)).findOffenderDamageObligationByOffender_NomsId("A12345");
        verify(offenderDamageObligationRepository, times(1)).findOffenderDamageObligationByOffender_NomsIdAndStatus("A12345", "ACTIVE");
    }

    @Test
    public void when_StatusIsBadValue_Then_DefaultToALL() {
        stubVerifyOffenderAccess("A12345");

        final var request = createHttpEntity(token, null);
        final var response = testRestTemplate.exchange(
            "/api/offenders/{offenderNo}/damage-obligations?status=BADSTATUS",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<String>() {
            }, "A12345");

        verify(offenderDamageObligationRepository, times(1)).findOffenderDamageObligationByOffender_NomsId("A12345");
        verify(offenderDamageObligationRepository, times(0)).findOffenderDamageObligationByOffender_NomsIdAndStatus(anyString(), anyString());
    }
}
