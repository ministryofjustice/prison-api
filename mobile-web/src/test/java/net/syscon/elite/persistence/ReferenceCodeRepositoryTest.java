package net.syscon.elite.persistence;

import net.syscon.elite.web.api.model.ReferenceCode;
import net.syscon.elite.web.api.resource.ReferenceDomainsResource;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("noHikari,memdb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
@Ignore
public class ReferenceCodeRepositoryTest {

    @Autowired
    private ReferenceCodeRepository repository;

    @Test
    public final void testGetAlertTypeByCode() {
        final ReferenceCode alertTypeCodesByAlertCode = repository.getAlertTypeCodesByAlertCode("XX", "XX");
        assertThat(alertTypeCodesByAlertCode).isNotNull();
    }

    @Test
    public final void testGetAllAlerts() {
        final List<ReferenceCode> alerts = repository.getAlertTypes("", "CODE", ReferenceDomainsResource.Order.asc, 0, 10);
        assertThat(alerts).isNotEmpty();
    }

}
