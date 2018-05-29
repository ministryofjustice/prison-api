package net.syscon.elite.repository;


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

import java.util.Collection;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)

public class OffenderCurfewRepositoryTest {

    @Autowired
    private OffenderCurfewRepository repository;

    @Before
    public void init() {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void shouldReturnOffenderBookIdsWhereLatestCurfewRecordsHaveNullApprovalStatus_LEI_only() {
        Collection<Long> results = repository.offendersWithoutCurfewApprovalStatus("agencyLocationId:eq:'LEI'");
        assertThat(results).containsOnly(-3L, -4L, -6L, -7L);
    }

    @Test
    public void shouldReturnOffenderBookIdsWhereLatestCurfewRecordsHaveNullApprovalStatus_BXI_only() {
        Collection<Long> results = repository.offendersWithoutCurfewApprovalStatus("agencyLocationId:eq:'BXI'");
        assertThat(results).containsOnly(-36L);
    }

}
