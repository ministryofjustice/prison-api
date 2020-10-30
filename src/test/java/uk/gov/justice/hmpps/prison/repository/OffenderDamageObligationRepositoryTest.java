package uk.gov.justice.hmpps.prison.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderDamageObligation;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderDamageObligationRepository;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class OffenderDamageObligationRepositoryTest {

    @Autowired
    private OffenderDamageObligationRepository repository;

    private final Offender anOffender = Offender.builder()
            .id(-1002L)
            .idSourceCode("SEQ")
            .firstName("GILLIAN")
            .middleName("EVE")
            .lastName("ANDERSON")
            .birthDate(LocalDate.parse("1998-08-28"))
            .rootOffenderId(-1002L)
            .gender(new Gender("F", "Female"))
            .createDate(LocalDate.now())
            .lastNameKey("ANDERSON")
            .nomsId("A1234AB")
            .build();

    private final AgencyLocation someAgencyLocation = AgencyLocation.builder()
            .id("LEI")
            .description("LEEDS")
            .type("INST")
            .activeFlag(ActiveFlag.Y)
            .longDescription("HMP LEEDS")
            .build();

    @Test
    public void testReturnsDamageObligationsForAnOffender() {
        final var damageObligations =
                repository.findOffenderDamageObligationByOffender_NomsId("A1234AB");

        assertThat(damageObligations).containsExactlyInAnyOrder(
                OffenderDamageObligation
                        .builder()
                        .id(-2L)
                        .offender(anOffender)
                        .prison(someAgencyLocation)
                        .referenceNumber("124")
                        .startDateTime(LocalDateTime.parse("2002-01-01T00:00"))
                        .endDateTime(LocalDateTime.parse("2002-01-02T00:00"))
                        .amountToPay(BigDecimal.valueOf(50000, 2))
                        .comment("Some Comment Text")
                        .status("PAID")
                        .build(),
                OffenderDamageObligation
                        .builder()
                        .id(-3L)
                        .offender(anOffender)
                        .prison(someAgencyLocation)
                        .referenceNumber("125")
                        .startDateTime(LocalDateTime.parse("2002-01-01T00:00"))
                        .endDateTime(LocalDateTime.parse("2002-01-02T00:00"))
                        .amountToPay(BigDecimal.valueOf(10000, 2))
                        .comment("Some Comment Text")
                        .status("ACTIVE")
                        .build());
    }

    @Test
    public void testReturnDamageObligationsByOffenderNoAndStatus() {
        final var damageObligations =
                repository.findOffenderDamageObligationByOffender_NomsIdAndStatus("A1234AB", "ACTIVE");

        assertThat(damageObligations).containsExactlyInAnyOrder(
                OffenderDamageObligation
                        .builder()
                        .id(-3L)
                        .offender(anOffender)
                        .prison(someAgencyLocation)
                        .referenceNumber("125")
                        .startDateTime(LocalDateTime.parse("2002-01-01T00:00"))
                        .endDateTime(LocalDateTime.parse("2002-01-02T00:00"))
                        .amountToPay(BigDecimal.valueOf(10000, 2))
                        .comment("Some Comment Text")
                        .status("ACTIVE")
                        .build());
    }

    @Test
    public void testThatAnEmptyListIsReturnedForNoData() {
        final var aa = repository.findOffenderDamageObligationByOffender_NomsIdAndStatus("ZZZZZZ", "ACTIVE");
        assertThat(aa).isEmpty();
    }
}
