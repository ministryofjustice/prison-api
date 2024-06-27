package uk.gov.justice.hmpps.prison.repository.keyworker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.repository.KeyWorkerAllocationRepository;
import uk.gov.justice.hmpps.test.kotlin.auth.WithMockAuthUser;
import uk.gov.justice.hmpps.prison.web.config.PersistenceConfigs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")

@JdbcTest()
@Transactional
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
@WithMockAuthUser("ITAG_USER")
public class KeyWorkerAllocationRepositoryTest {
    private static final String AGENCY_ID = "LEI";

    @Autowired
    private KeyWorkerAllocationRepository repo;

    @Test
    public void shouldGetAvailableKeyworkers() {
        final var availableKeyworkers = repo.getAvailableKeyworkers(AGENCY_ID);
        assertThat(availableKeyworkers).asList().hasSize(4);
    }

    @Test
    public void testGetAllocationHistoryByAgency() {
        final var pageRequest = new PageRequest();
        final var allocations = repo.getAllocationHistoryByAgency(AGENCY_ID, pageRequest);

        assertThat(allocations.getItems()).hasSize(pageRequest.getLimit().intValue());
        assertThat(allocations.getTotalRecords()).isEqualTo(23L);
    }

}
