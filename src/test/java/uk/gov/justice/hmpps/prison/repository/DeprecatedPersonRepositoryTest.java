package uk.gov.justice.hmpps.prison.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.PersonIdentifier;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class DeprecatedPersonRepositoryTest {
    private static final long UNKNOWN_PERSON_ID = 1000L;
    private static final long PERSON_ID_MULTIPLE_IDENTIFIERS = -1L;

    @Autowired
    private DeprecatedPersonRepository repository;

    @Test
    public void findPersonIdentifiersForUnknownPersonId() {
        assertThat(repository.getPersonIdentifiers(UNKNOWN_PERSON_ID)).isEmpty();
    }

    @Test
    public void selectsLatestPersonIdentifiers() {
        assertThat(repository.getPersonIdentifiers(PERSON_ID_MULTIPLE_IDENTIFIERS))
                .containsExactlyInAnyOrder(
                        PersonIdentifier.builder().identifierType("EXTERNAL_REL").identifierValue("DELIUS_1_2").build(),
                        PersonIdentifier.builder().identifierType("DL").identifierValue("NCDON805157PJ9FR").build(),
                        PersonIdentifier.builder().identifierType("PASS").identifierValue("PB1575411").build(),
                        PersonIdentifier.builder().identifierType("CRO").identifierValue("135196/95W").build(),
                        PersonIdentifier.builder().identifierType("MERGED").identifierValue("A1408CM").build()
                );
    }

}
