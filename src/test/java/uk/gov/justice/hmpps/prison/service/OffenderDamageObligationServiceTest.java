package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderDamageObligationRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class OffenderDamageObligationServiceTest {
    @Mock
    private OffenderDamageObligationRepository repository;

    private OffenderDamageObligationService service;

    @BeforeEach
    public void beforeEach() {
        service = new OffenderDamageObligationService("GBP", repository);
    }


    @Test
    public void callRepositoryWithOffenderNoOnly() {
        service.getDamageObligations("A1234", null);

        verify(repository).findOffenderDamageObligationByOffender_NomsId("A1234");
    }

    @Test
    public void callRepositoryWithOffenderNoAndStatus() {
        service.getDamageObligations("A1234", "ACTIVE");

        verify(repository).findOffenderDamageObligationByOffender_NomsIdAndStatus("A1234", "ACTIVE");
    }

    @Test
    public void transferIntoCorrectApiModel() {
        when(repository.findOffenderDamageObligationByOffender_NomsIdAndStatus(any(), any())).thenReturn(List.of(
                OffenderDamageObligation.builder()
                        .id(1L)
                        .comment("Broken canteen table")
                        .amountToPay(BigDecimal.valueOf(500))
                        .amountPaid(BigDecimal.ZERO)
                        .startDateTime(LocalDateTime.parse("2020-10-10T10:00"))
                        .endDateTime(LocalDateTime.parse("2020-10-22T10:00"))
                        .offender(Offender.builder().nomsId("A12345").build())
                        .referenceNumber("123")
                        .status("ACTIVE")
                        .comment("test")
                        .prison(AgencyLocation.builder().id("MDI").description("Moorland").build())
                        .build()
        ));

        final var outstandingDamageBalance =
                service.getDamageObligations("A1234", "ACTIVE")
                        .stream().findFirst().orElseThrow();

        assertThat(outstandingDamageBalance.getId()).isEqualTo(1L);
        assertThat(outstandingDamageBalance.getOffenderNo()).isEqualTo("A12345");
        assertThat(outstandingDamageBalance.getPrisonId()).isEqualTo("MDI");
        assertThat(outstandingDamageBalance.getReferenceNumber()).isEqualTo("123");
        assertThat(outstandingDamageBalance.getStartDateTime()).isEqualTo("2020-10-10T10:00");
        assertThat(outstandingDamageBalance.getEndDateTime()).isEqualTo("2020-10-22T10:00");
        assertThat(outstandingDamageBalance.getCurrency()).isEqualTo("GBP");
        assertThat(outstandingDamageBalance.getComment()).isEqualTo("test");
        assertThat(outstandingDamageBalance.getAmountPaid()).isEqualTo("0");
        assertThat(outstandingDamageBalance.getAmountToPay()).isEqualTo("500");
        assertThat(outstandingDamageBalance.getStatus()).isEqualTo("ACTIVE");
    }
    
    @Test
    public void handleOffenderNoFound() {
        when(repository.findOffenderDamageObligationByOffender_NomsId(any())).thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> service.getDamageObligations("A2343", null))
                .hasMessage("Offender not found: A2343")
                .isInstanceOf(EntityNotFoundException.class);
    }
}
