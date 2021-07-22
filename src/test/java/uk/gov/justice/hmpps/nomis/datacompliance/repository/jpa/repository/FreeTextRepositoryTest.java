package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.FreeTextMatch;
import uk.gov.justice.hmpps.prison.PrisonApiServer;
import uk.gov.justice.hmpps.prison.RepositoryConfiguration;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = { PrisonApiServer.class, RepositoryConfiguration.class})
@Sql(value = "define_regexp_like.sql")
@Sql(value = "drop_regexp_like.sql", executionPhase = AFTER_TEST_METHOD)
class FreeTextRepositoryTest {

    @Autowired
    private FreeTextRepository repository;

    @Test
    void findMatchUsingBookIds() {
        assertThat(repository.findMatchUsingBookIds(Set.of(-1L), ".*Text.*"))
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
                        "IWP_DOCUMENTS",
                        "OFFENDER_ALERTS",
                        "OFFENDER_ASSESSMENTS",
                        "OFFENDER_ASSESSMENT_ITEMS",
                        "OFFENDER_BELIEFS",
                        "OFFENDER_CASES",
                        "OFFENDER_CASE_NOTES",
                        "OFFENDER_CASE_STATUSES",
                        "OFFENDER_CONTACT_PERSONS",
                        "OFFENDER_COURSE_ATTENDANCES",
                        "OFFENDER_CSIP_ATTENDEES",
                        "OFFENDER_CSIP_FACTORS",
                        "OFFENDER_CSIP_INTVW",
                        "OFFENDER_CSIP_PLANS",
                        "OFFENDER_CSIP_REPORTS",
                        "OFFENDER_CSIP_REVIEWS",
                        "OFFENDER_CURFEWS",
                        "OFFENDER_DATA_CORRECTIONS_HTY",
                        "OFFENDER_EDUCATIONS",
                        "OFFENDER_EMPLOYMENTS",
                        "OFFENDER_EXTERNAL_MOVEMENTS",
                        "OFFENDER_FINE_PAYMENTS",
                        "OFFENDER_FIXED_TERM_RECALLS",
                        "OFFENDER_GANG_AFFILIATIONS",
                        "OFFENDER_GANG_INVESTS",
                        "OFFENDER_HEALTH_PROBLEMS",
                        "OFFENDER_IDENTIFYING_MARKS",
                        "OFFENDER_IEP_LEVELS",
                        "OFFENDER_IMPRISON_STATUSES",
                        "OFFENDER_IND_SCHEDULES",
                        "OFFENDER_KEY_DATE_ADJUSTS",
                        "OFFENDER_LANGUAGES",
                        "OFFENDER_LICENCE_RECALLS",
                        "OFFENDER_MEDICAL_TREATMENTS",
                        "OFFENDER_MILITARY_RECORDS",
                        "OFFENDER_MOVEMENT_APPS",
                        "OFFENDER_NA_DETAILS",
                        "OFFENDER_NO_PAY_PERIODS",
                        "OFFENDER_OIC_SANCTIONS",
                        "OFFENDER_PAY_STATUSES",
                        "OFFENDER_PERSON_RESTRICTS",
                        "OFFENDER_PPTY_CONTAINERS",
                        "OFFENDER_PROGRAM_PROFILES",
                        "OFFENDER_REHAB_DECISIONS",
                        "OFFENDER_REHAB_PROVIDERS",
                        "OFFENDER_RELEASE_DETAILS",
                        "OFFENDER_RELEASE_DETAILS_HTY",
                        "OFFENDER_RESTRICTIONS",
                        "OFFENDER_SENTENCES",
                        "OFFENDER_SENTENCE_ADJUSTS",
                        "OFFENDER_SENTENCE_STATUSES",
                        "OFFENDER_SENT_CALCULATIONS",
                        "OFFENDER_SENT_CONDITIONS",
                        "OFFENDER_SENT_COND_STATUSES",
                        "OFFENDER_SUBSTANCE_DETAILS",
                        "OFFENDER_SUBSTANCE_TREATMENTS",
                        "OFFENDER_TEST_SELECTIONS",
                        "OFFENDER_VISITS",
                        "OFFENDER_VISIT_BALANCE_ADJS",
                        "OFFENDER_VISIT_ORDERS",
                        "OFFENDER_VSC_SENTENCES",
                        "OFFENDER_VSC_SENT_CALCULATIONS",
                        "OFFENDER_VISIT_VISITORS",
                        "OFF_CASE_NOTE_RECIPIENTS",
                        "OIC_HEARINGS",
                        "OIC_HEARING_NOTICES",
                        "ORDERS",
                        "TASK_ASSIGNMENT_HTY"
                );
    }

    @Test
    void findMatchUsingBookIdsReturnsEmpty() {
        assertThat(repository.findMatchUsingBookIds(Set.of(-1L), ".*NO MATCH.*"))
                .isEmpty();
    }

    @Test
    void findMatchUsingOffenderIds() {
        assertThat(repository.findMatchUsingOffenderIds(Set.of(-1001L), ".*Text.*"))
                .extracting(FreeTextMatch::getTableName)
                .extracting(String::trim)
                .containsExactlyInAnyOrder(
                        "OFFENDER_DAMAGE_OBLIGATIONS",
                        "OFFENDER_FREEZE_DISBURSEMENTS",
                        "OFFENDER_IDENTIFIERS",
                        "OFFENDER_PAYMENT_PROFILES"
                );
    }

    @Test
    void findMatchUsingOffenderIdsReturnsEmpty() {
        assertThat(repository.findMatchUsingOffenderIds(Set.of(-1001L), ".*NO MATCH.*"))
                .isEmpty();
    }
}
