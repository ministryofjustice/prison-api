package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import net.syscon.elite.Elite2ApiServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.FreeTextMatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = { Elite2ApiServer.class })
@Sql(value = "define_regexp_like.sql")
@Sql(value = "drop_regexp_like.sql", executionPhase = AFTER_TEST_METHOD)
class FreeTextRepositoryTest {

    @Autowired
    private FreeTextRepository repository;

    @Test
    void findMatch() {
        assertThat(repository.findMatch(-1, ".*Text.*"))
                .extracting(FreeTextMatch::getTableName)
                .extracting(String::trim)
                .containsExactlyInAnyOrder("OFFENDER_ALERTS", "OFFENDER_ASSESSMENTS");
    }

    @Test
    void findMatchReturnsEmpty() {
        assertThat(repository.findMatch(-1, ".*NO MATCH.*"))
                .isEmpty();
    }
}
