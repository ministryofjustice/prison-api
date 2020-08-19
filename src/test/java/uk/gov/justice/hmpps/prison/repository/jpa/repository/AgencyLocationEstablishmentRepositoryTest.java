package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationEstablishment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EstablishmentType;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class AgencyLocationEstablishmentRepositoryTest {

    @Autowired
    private AgencyLocationEstablishmentRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void agency_establishment_is_persisted() {
        assertThat(repository.findById(new AgencyLocationEstablishment.Pk("MDI", "IM"))).isNotPresent();

        repository.save(AgencyLocationEstablishment
                        .builder()
                        .agencyLocId("MDI")
                        .establishmentType("IM")
                        .build());

        entityManager.flush();

        final var agencyEstablishmentType = repository.findById(new AgencyLocationEstablishment.Pk("MDI", "IM"));

        assertThat(agencyEstablishmentType).isPresent();
        assertThat(agencyEstablishmentType.get().getAgencyLocId()).isEqualTo("MDI");
        assertThat(agencyEstablishmentType.get().getEstablishmentType()).isEqualTo("IM");
    }
}
