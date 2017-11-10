package net.syscon.elite.repository;

import net.syscon.elite.service.support.CustodyStatusDto;
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

import static org.junit.Assert.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis,nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class CustodyStatusRepositoryImplTest {

    @Autowired
    private CustodyStatusRepository repository;

    @Before
    public final void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public final void retrieveARecordForAKnownOffenderThatHasAMovementRecord() {
        final CustodyStatusDto custodyStatusDto = repository.getCustodyStatus("Z0023ZZ").orElse(null);
        assertNotNull(custodyStatusDto);

        assertEquals("Z0023ZZ", custodyStatusDto.getOffenderIdDisplay());
        assertEquals("O", custodyStatusDto.getBookingStatus());
        assertEquals("N", custodyStatusDto.getActiveFlag());
        assertEquals("OUT", custodyStatusDto.getDirectionCode());
        assertEquals("REL", custodyStatusDto.getMovementType());
        assertEquals("ESCP", custodyStatusDto.getMovementReasonCode());
    }

    @Test
    public final void retrieveARecordForAKnownOffenderThatHasNoMovementRecord() {
        final CustodyStatusDto custodyStatusDto = repository.getCustodyStatus("Z0022ZZ").orElse(null);
        assertNotNull(custodyStatusDto);

        assertEquals("Z0022ZZ", custodyStatusDto.getOffenderIdDisplay());
        assertEquals("O", custodyStatusDto.getBookingStatus());
        assertEquals("N", custodyStatusDto.getActiveFlag());
        assertNull(custodyStatusDto.getDirectionCode());
        assertNull(custodyStatusDto.getMovementType());
        assertNull(custodyStatusDto.getMovementReasonCode());
    }

    @Test
    public final void failToRetrieveARecordForAKnownOffenderThatIsLocatedInTheGhostPrison() {
        final CustodyStatusDto custodyStatusDto = repository.getCustodyStatus("Z0026ZZ").orElse(null);
        assertNull(custodyStatusDto);
    }

}