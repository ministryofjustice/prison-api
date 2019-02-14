package net.syscon.elite.repository;

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
        var incidentCase = repository.getIncidentCase(-1L);
        assertThat(incidentCase).isPresent();
        assertThat(incidentCase.get().getIncidentCaseId()).isEqualTo(-1L);
    }

    @Test
    public void testGetQuestionnaire() {
        var questionnaire = repository.getQuestionnaire("IR_TYPE", "ASSAULT").orElse(null);
        assertThat(questionnaire).isNotNull();
        assertThat(questionnaire.getQuestions()).hasSize(28);
        assertThat(questionnaire.getQuestions().first().getAnswers()).hasSize(2);
    }

}
