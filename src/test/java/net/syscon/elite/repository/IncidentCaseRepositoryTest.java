package net.syscon.elite.repository;

import net.syscon.elite.api.model.IncidentCase;
import net.syscon.elite.repository.impl.IncidentCaseRepository;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class IncidentCaseRepositoryTest {

    @Autowired
    private IncidentCaseRepository repository;

    @Before
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void testGetIncident() {
        var incidentCases = repository.getIncidentCases(List.of(-1L));
        assertThat(incidentCases).hasSize(1);
        IncidentCase incidentCase1 = incidentCases.get(0);
        assertThat(incidentCase1.getIncidentCaseId()).isEqualTo(-1L);
        assertThat(incidentCase1.getResponses()).hasSize(19);
        assertThat(incidentCase1.getParties()).hasSize(6);
    }

    @Test
    public void testGetIncidentCasesByBookingId() {
        var incidentCases = repository.getIncidentCasesByBookingId(-1L, "ASSAULT", null);
        assertThat(incidentCases).hasSize(3);
        IncidentCase incidentCase1 = incidentCases.get(0);
        assertThat(incidentCase1.getIncidentCaseId()).isEqualTo(-1L);
        assertThat(incidentCase1.getResponses()).hasSize(19);
        assertThat(incidentCase1.getParties()).hasSize(6);
    }

    @Test
    public void testGetIncidentCasesByOffenderNo() {
        var incidentCases = repository.getIncidentCasesByOffenderNo("A1234AA", null, List.of("ASSIAL", "POR"));
        assertThat(incidentCases).hasSize(1);
        IncidentCase incidentCase1 = incidentCases.get(0);
        assertThat(incidentCase1.getIncidentCaseId()).isEqualTo(-1L);
        assertThat(incidentCase1.getResponses()).hasSize(19);
        assertThat(incidentCase1.getParties()).hasSize(6);
    }
    @Test
    public void testGetQuestionnaire() {
        var questionnaire = repository.getQuestionnaire("IR_TYPE", "ASSAULT").orElse(null);
        assertThat(questionnaire).isNotNull();
        assertThat(questionnaire.getQuestions()).hasSize(28);
        assertThat(questionnaire.getQuestions().first().getAnswers()).hasSize(2);
    }

}
