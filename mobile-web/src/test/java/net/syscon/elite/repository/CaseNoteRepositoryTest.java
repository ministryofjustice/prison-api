package net.syscon.elite.repository;

import net.syscon.elite.api.model.ReferenceCode;
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

import static org.junit.Assert.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = { PersistenceConfigs.class, CacheConfig.class })
public class CaseNoteRepositoryTest {

    @Autowired
    private CaseNoteRepository repository;

    @Test
    public void testGetCaseNoteTypesByCaseLoadType() {
        List<ReferenceCode> types = repository.getCaseNoteTypesByCaseLoadType("COMM");

        assertNotNull(types);
        assertFalse(types.isEmpty());

        // Ensure each type has null value for sub-types
        types.forEach(type -> assertNull(type.getSubCodes()));
    }

    @Test
    public void testGetCaseNoteTypeWithSubTypesByCaseLoadType() {
        List<ReferenceCode> types = repository.getCaseNoteTypesWithSubTypesByCaseLoadType("COMM");

        // Spot check
        Optional<ReferenceCode> type = types.stream().filter(x -> x.getCode().equals("DRR")).findFirst();

        assertTrue(type.isPresent());

        List<ReferenceCode> subTypes = type.get().getSubCodes();

        assertNotNull(subTypes);
        assertFalse(subTypes.isEmpty());

        assertTrue(subTypes.stream().anyMatch(x -> x.getCode().equals("DTEST")));
    }
}
