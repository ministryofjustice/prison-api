package uk.gov.justice.hmpps.prison.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
@WithMockAuthUser
public class IncidentCaseRepositoryTest {

    @Autowired
    private IncidentCaseRepository repository;

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
    public void testGetIncidentCasesByOffenderNo() {
        final var incidentCases = repository.getIncidentCasesByOffenderNo("A1234AA", null, List.of("ASSIAL", "POR"));
        assertThat(incidentCases).hasSize(1);
        final var incidentCase1 = incidentCases.get(0);
        assertThat(incidentCase1.getIncidentCaseId()).isEqualTo(-1L);
        assertThat(incidentCase1.getResponses()).hasSize(19);
        assertThat(incidentCase1.getParties()).hasSize(6);
    }
}
