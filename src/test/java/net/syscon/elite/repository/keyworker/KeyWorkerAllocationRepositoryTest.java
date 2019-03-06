package net.syscon.elite.repository.keyworker;

import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
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
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@JdbcTest()
@Transactional
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class KeyWorkerAllocationRepositoryTest {
    private static final String AGENCY_ID = "LEI";

    @Autowired
    private KeyWorkerAllocationRepository repo;

    @Before
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void shouldGetAvailableKeyworkers() {
        final var availableKeyworkers = repo.getAvailableKeyworkers(AGENCY_ID);
        assertThat(availableKeyworkers).asList().hasSize(4);
    }

    @Test
    public void testGetAllocationHistoryByAgency() {
        final var pageRequest = new PageRequest();
        final var allocations = repo.getAllocationHistoryByAgency(AGENCY_ID, pageRequest);

        assertThat(allocations.getItems().size()).isEqualTo(pageRequest.getLimit().intValue());
        assertThat(allocations.getTotalRecords()).isEqualTo(23L);
    }

}
