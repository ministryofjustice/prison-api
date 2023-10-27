package uk.gov.justice.hmpps.prison.repository.keyworker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.repository.KeyWorkerAllocationRepository;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")

@JdbcTest()
@Transactional
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class KeyWorkerAllocationRepositoryTest {
    private static final String AGENCY_ID = "LEI";

    @Autowired
    private KeyWorkerAllocationRepository repo;

    @BeforeEach
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void shouldGetAvailableKeyworkers() {
        final var availableKeyworkers = repo.getAvailableKeyworkers(AGENCY_ID);
        assertThat(availableKeyworkers).asList().hasSize(4);
    }
}
