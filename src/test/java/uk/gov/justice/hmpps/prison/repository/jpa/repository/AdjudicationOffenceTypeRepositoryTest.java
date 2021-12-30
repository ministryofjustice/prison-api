package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationOffenceType;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class AdjudicationOffenceTypeRepositoryTest {

    @Autowired
    private AdjudicationOffenceTypeRepository repository;

    @Test
    void offencesFindByOffenceCode() {
        final var offenceCodeToSearch = "51:1J";

        final var foundCode = repository.findByOffenceCode(offenceCodeToSearch);

        final var expectedOffenceType = AdjudicationOffenceType.builder()
            .offenceId(80L)
            .offenceCode(offenceCodeToSearch)
            .description("Commits any assault - assault on prison officer")
            .build();

        assertThat(foundCode).usingRecursiveComparison()
            .ignoringFields("createDatetime", "createUserId", "modifyDatetime", "modifyUserId", "parties")
            .isEqualTo(expectedOffenceType);
    }
}


