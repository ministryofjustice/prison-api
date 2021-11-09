package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderRestrictions;
import uk.gov.justice.hmpps.prison.PrisonApiServer;
import uk.gov.justice.hmpps.prison.RepositoryConfiguration;

import java.util.List;

import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = { PrisonApiServer.class, RepositoryConfiguration.class})
@Sql(value = "define_regexp_like.sql")
@Sql(value = "drop_regexp_like.sql", executionPhase = AFTER_TEST_METHOD)
class OffenderRestrictionsRepositoryTest {

    @Autowired
    private OffenderRestrictionsRepository repository;


    @Test
    void findOffenderRestrictions() {
        final List<OffenderRestrictions> restriction = repository.findOffenderRestrictions(of(-1L), of("RESTRICTED"), ".*Text.*");

        assertThat(restriction).extracting(OffenderRestrictions::getOffenderRestrictionId).containsOnly(-1L);
        assertThat(restriction).extracting(OffenderRestrictions::getOffenderBookId).containsOnly(-1L);
        assertThat(restriction).extracting(OffenderRestrictions::getRestrictionType).containsOnly("RESTRICTED");
        assertThat(restriction).extracting(OffenderRestrictions::getCommentText).containsOnly("Some Comment Text");
    }


    @Test
    void findOffenderRestrictionsRestrictionCodeMatchesAndRegexDoesNotMatch() {
        final List<OffenderRestrictions> restriction = repository.findOffenderRestrictions(of(-1L), of("RESTRICTED"), ".*NoMatchRegex.*");

        assertThat(restriction).extracting(OffenderRestrictions::getOffenderRestrictionId).containsOnly(-1L);
        assertThat(restriction).extracting(OffenderRestrictions::getOffenderBookId).containsOnly(-1L);
        assertThat(restriction).extracting(OffenderRestrictions::getRestrictionType).containsOnly("RESTRICTED");
        assertThat(restriction).extracting(OffenderRestrictions::getCommentText).containsOnly("Some Comment Text");
    }

    @Test
    void findOffenderRestrictionsRestrictionCodeDoesNotMatchAndRegexMatches() {
        final List<OffenderRestrictions> restriction = repository.findOffenderRestrictions(of(-1L), of("CHILD"), ".*Text.*");

        assertThat(restriction).extracting(OffenderRestrictions::getOffenderRestrictionId).containsOnly(-1L);
        assertThat(restriction).extracting(OffenderRestrictions::getOffenderBookId).containsOnly(-1L);
        assertThat(restriction).extracting(OffenderRestrictions::getRestrictionType).containsOnly("RESTRICTED");
        assertThat(restriction).extracting(OffenderRestrictions::getCommentText).containsOnly("Some Comment Text");
    }

    @Test
    void findOffenderRestrictionsReturnsEmptyWhenRestrictionCodeDoesNotMatchAndRegexDoesNotMatch() {
        assertThat(repository.findOffenderRestrictions(of(-1L), of("CHILD"), ".*NoMatchRegex.*")).isEmpty();
    }

    @Test
    void findOffenderRestrictionsReturnsEmptyWhenInvalidBookingId() {
        assertThat(repository.findOffenderRestrictions(of(-100L), of("RESTRICTED"), ".*Text.*")).isEmpty();
    }


}
