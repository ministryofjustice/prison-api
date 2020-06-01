package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import net.syscon.elite.Elite2ApiServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.DuplicateOffender;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = { Elite2ApiServer.class })
@Sql(value = "define_regexp_substr.sql")
@Sql(value = "drop_regexp_substr.sql", executionPhase = AFTER_TEST_METHOD)
class DuplicateOffenderRepositoryTest {

    @Autowired
    private DuplicateOffenderRepository repository;

    @Test
    void getOffendersWithMatchingPncNumbers() {
        assertThat(repository.getOffendersWithMatchingPncNumbers("Z0020ZZ", Set.of("99/1234567B", "20/9N")))
                .extracting(DuplicateOffender::getOffenderNumber)
                .containsExactlyInAnyOrder("A1184JR", "A1184MA");
    }

    @Test
    void getOffendersWithMatchingPncNumbersIsCommutative() {
        assertThat(repository.getOffendersWithMatchingPncNumbers("A1184JR", Set.of("99/1234567B")))
                .extracting(DuplicateOffender::getOffenderNumber)
                .containsExactlyInAnyOrder("Z0020ZZ");
    }

    @Test
    void getOffendersWithMatchingPncNumbersReturnsEmpty() {
        assertThat(repository.getOffendersWithMatchingPncNumbers("A1234AA", emptySet())).isEmpty();
    }
}
