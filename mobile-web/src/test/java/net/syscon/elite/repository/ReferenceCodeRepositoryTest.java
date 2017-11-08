package net.syscon.elite.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import net.syscon.elite.api.model.ReferenceCode;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.web.config.CacheConfig;
import net.syscon.elite.web.config.PersistenceConfigs;

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
import java.util.Optional;

@ActiveProfiles("nomis,nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = { PersistenceConfigs.class, CacheConfig.class })
public class ReferenceCodeRepositoryTest {

    @Autowired
    private ReferenceCodeRepository repository;

    @Test
    public final void testGetAlertTypeByCode() {
        final ReferenceCode alertTypeCodesByAlertCode = repository.getReferenceCodeByDomainAndCode("ALERT", "X")
                .orElseThrow(new EntityNotFoundException("not found"));
        assertThat(alertTypeCodesByAlertCode).isNotNull();
    }

    @Test
    public final void testGetCaseNoteTypeByCurrentCaseLoad() {
        final List<ReferenceCode> types = repository.getCaseNoteTypeByCurrentCaseLoad("COMM", true, null, null,
                null, 0, 1000).getItems();
        // Spot check
        final Optional<ReferenceCode> type = types.stream().filter(x -> {
            return x.getCode().equals("DRR");
        }).findFirst();
        assertTrue(type.get().getSubCodes().stream().anyMatch(x -> {
            return x.getCode().equals("DTEST");
        }));
    }
}
