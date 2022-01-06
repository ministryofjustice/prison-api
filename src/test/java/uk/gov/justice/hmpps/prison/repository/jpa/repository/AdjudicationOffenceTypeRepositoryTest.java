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

import java.util.List;

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
    void offencesFindByOffenceCodes() {
        final var offenceCode1ToSearch = "51:1J";
        final var offenceCode2ToSearch = "51:2A";

        final var foundCodes = repository.findByOffenceCodeIn(List.of(offenceCode1ToSearch, offenceCode2ToSearch));

        final var expectedOffenceTypes = List.of(
            AdjudicationOffenceType.builder()
                .offenceId(80L)
                .offenceCode(offenceCode1ToSearch)
                .description("Commits any assault - assault on prison officer")
                .build(),
            AdjudicationOffenceType.builder()
                .offenceId(82L)
                .offenceCode(offenceCode2ToSearch)
                .description("Detains any person against his will - detention against will of an inmate")
                .build()
        );

        assertThat(foundCodes).usingRecursiveComparison()
            .ignoringFields("createDatetime", "createUserId", "modifyDatetime", "modifyUserId", "parties")
            .ignoringCollectionOrder().isEqualTo(expectedOffenceTypes);
    }
}


