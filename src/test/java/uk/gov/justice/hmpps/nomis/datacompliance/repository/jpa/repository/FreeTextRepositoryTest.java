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

import java.util.Set;

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
        assertThat(repository.findMatch(Set.of(-1L), ".*Text.*"))
                .extracting(FreeTextMatch::getTableName)
                .extracting(String::trim)
                .containsExactlyInAnyOrder(
                        "ADDRESSES",
                        "AGENCY_INCIDENTS",
                        "AGENCY_INCIDENT_CHARGES",
                        "AGENCY_INCIDENT_PARTIES",
                        "AGY_INC_INVESTIGATIONS",
                        "AGY_INC_INV_STATEMENTS",
                        "COURT_EVENTS",
                        "CURFEW_ADDRESS_OCCUPANTS",
                        "HDC_BOARD_DECISIONS",
                        "HDC_GOVERNOR_DECISIONS",
                        "HDC_PRISON_STAFF_COMMENTS",
                        "HDC_PROB_STAFF_COMMENTS",
                        "HDC_REQUEST_REFERRALS",
                        "INCIDENT_CASES",
                        "INCIDENT_CASE_PARTIES",
                        "INCIDENT_CASE_REQUIREMENTS",
                        "INCIDENT_CASE_RESPONSES",
                        "INCIDENT_QUE_RESPONSE_HTY",
                        "OFFENDER_ALERTS",
                        "OFFENDER_ASSESSMENTS",
                        "OFFENDER_CASE_NOTES",
                        "OFFENDER_COURSE_ATTENDANCES",
                        "OFFENDER_VISIT_VISITORS"
                );
    }

    @Test
    void findMatchReturnsEmpty() {
        assertThat(repository.findMatch(Set.of(-1L), ".*NO MATCH.*"))
                .isEmpty();
    }
}
