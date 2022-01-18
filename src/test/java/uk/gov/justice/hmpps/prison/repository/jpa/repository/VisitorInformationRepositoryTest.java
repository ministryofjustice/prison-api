package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitorInformation;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class VisitorInformationRepositoryTest {

    @Autowired
    private VisitorRepository repository;

    @Test
    public void findAllByVisitId() {
        final var visits = repository.findAllByVisitId(-15L);

        assertThat(visits).extracting(VisitorInformation::getPersonId).containsOnly(-1L, -2L);
        assertThat(visits).extracting(VisitorInformation::getFirstName).containsOnly("JESSY", "John");
        assertThat(visits).extracting(VisitorInformation::getLastName).containsOnly("SMITH1", "Smith");
        assertThat(visits).extracting(VisitorInformation::getEventOutcome).containsOnly("ABS", "ATT");
    }
}


