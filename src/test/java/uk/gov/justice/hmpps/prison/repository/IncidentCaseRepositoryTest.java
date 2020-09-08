package uk.gov.justice.hmpps.prison.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.repository.impl.IncidentCaseRepository;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class IncidentCaseRepositoryTest {

    @Autowired
    private IncidentCaseRepository repository;

    @BeforeEach
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void testGetIncident() {
        final var incidentCases = repository.getIncidentCases(List.of(-1L));
        assertThat(incidentCases).hasSize(1);
        final var incidentCase1 = incidentCases.get(0);
        assertThat(incidentCase1.getIncidentCaseId()).isEqualTo(-1L);
        assertThat(incidentCase1.getResponses()).hasSize(19);
        assertThat(incidentCase1.getParties()).hasSize(6);
    }

    @Test
    public void testGetIncidentCasesByBookingId() {
        final var incidentCases = repository.getIncidentCasesByBookingId(-1L, List.of("ASSAULT", "ASSULTS3"), null);
        assertThat(incidentCases).hasSize(3);
        final var incidentCase1 = incidentCases.get(0);
        assertThat(incidentCase1.getIncidentCaseId()).isEqualTo(-1L);
        assertThat(incidentCase1.getResponses()).hasSize(19);
        assertThat(incidentCase1.getParties()).hasSize(6);
    }

    @Test
    public void testGetIncidentCasesNoIncidents() {
        final var incidentCases = repository.getIncidentCasesByBookingId(-10L, List.of("ASSAULT", "ASSULTS3"), null);
        assertThat(incidentCases).hasSize(0);
    }

    @Test
    public void testGetIncidentCasesByOffenderNo() {
        final var incidentCases = repository.getIncidentCasesByOffenderNo("A1234AA", null, List.of("ASSIAL", "POR"));
        assertThat(incidentCases).hasSize(1);
        final var incidentCase1 = incidentCases.get(0);
        assertThat(incidentCase1.getIncidentCaseId()).isEqualTo(-1L);
        assertThat(incidentCase1.getResponses()).hasSize(19);
        assertThat(incidentCase1.getParties()).hasSize(6);
    }

    @Test
    public void testGetQuestionnaire() {
        final var questionnaire = repository.getQuestionnaire("IR_TYPE", "ASSAULT").orElse(null);
        assertThat(questionnaire).isNotNull();
        assertThat(questionnaire.getQuestions()).hasSize(28);
        assertThat(questionnaire.getQuestions().first().getAnswers()).hasSize(2);
    }

    @Test
    public void testGetIncidentCandidates() {
        final var results = repository.getIncidentCandidates(LocalDateTime.of(2016, 1, 1, 0, 0), 0, 10);
        assertThat(results.getItems()).containsExactlyInAnyOrder("A1234AA", "A1234AB", "A1234AC", "A1234AD");
    }

    @Test
    public void testGetIncidentCandidatesPage1() {
        final var results = repository.getIncidentCandidates(LocalDateTime.of(2016, 1, 1, 0, 0), 0, 1);
        assertThat(results.getItems()).containsExactlyInAnyOrder("A1234AA");
    }

    @Test
    public void testGetIncidentCandidatesPage2() {
        final var results = repository.getIncidentCandidates(LocalDateTime.of(2016, 1, 1, 0, 0), 1, 5);
        assertThat(results.getPageOffset()).isEqualTo(1);
        assertThat(results.getPageLimit()).isEqualTo(5);
        assertThat(results.getTotalRecords()).isEqualTo(4);
        assertThat(results.getItems()).containsExactlyInAnyOrder("A1234AB", "A1234AC", "A1234AD");
    }

    @Test
    public void testGetIncidentCandidatesNone() {
        final var results = repository.getIncidentCandidates(LocalDateTime.of(2017, 1, 1, 0, 0), 0, 10);
        assertThat(results.getItems()).hasSize(0);
    }
}
