package net.syscon.elite.repository;

import net.syscon.elite.api.support.Order;
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

import java.util.List;

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
        final CustodyStatusRecord custodyStatusRecord = repository.getCustodyStatusRecord("Z0023ZZ").orElse(null);
        assertNotNull(custodyStatusRecord);

        assertEquals("Z0023ZZ", custodyStatusRecord.getOffender_id_display());
        assertEquals("LEI", custodyStatusRecord.getAgy_loc_id());
        assertEquals("O", custodyStatusRecord.getBooking_status());
        assertEquals("N", custodyStatusRecord.getActive_flag());
        assertEquals("OUT", custodyStatusRecord.getDirection_code());
        assertEquals("REL", custodyStatusRecord.getMovement_type());
        assertEquals("ESCP", custodyStatusRecord.getMovement_reason_code());
    }

    @Test
    public final void retrieveARecordForAKnownOffenderThatHasNoMovementRecord() {
        final CustodyStatusRecord custodyStatusRecord = repository.getCustodyStatusRecord("Z0022ZZ").orElse(null);
        assertNotNull(custodyStatusRecord);
        assertEquals("Z0022ZZ", custodyStatusRecord.getOffender_id_display());
        assertEquals("LEI", custodyStatusRecord.getAgy_loc_id());
        assertEquals("O", custodyStatusRecord.getBooking_status());
        assertEquals("N", custodyStatusRecord.getActive_flag());
        assertNull(custodyStatusRecord.getDirection_code());
        assertNull(custodyStatusRecord.getMovement_type());
        assertNull(custodyStatusRecord.getMovement_reason_code());
    }

    @Test
    public final void retrieveAListOfRecordsOrderedByLocationAscending() {
        final List<CustodyStatusRecord> results = repository.listCustodyStatusRecords(null, "locationId", Order.ASC);

        assertTrue(results.size() > 0);
        assertTrue(results.get(0).getAgy_loc_id().compareTo(results.get(results.size() - 1).getAgy_loc_id()) != 1);
        for (int i = 1; i < results.size() - 1; i++) {
            assertTrue(results.get(i - 1).getAgy_loc_id().compareTo(results.get(i).getAgy_loc_id()) != 1);
        }
    }

    @Test
    public final void retrieveAListOfRecordsOrderedByLocationDecending() {
        final List<CustodyStatusRecord> results = repository.listCustodyStatusRecords(null, "locationId", Order.DESC);

        assertTrue(results.size() > 0);
        assertTrue(results.get(0).getAgy_loc_id().compareTo(results.get(results.size() - 1).getAgy_loc_id()) != -1);
        for (int i = 1; i < results.size() - 1; i++) {
            assertTrue(results.get(i - 1).getAgy_loc_id().compareTo(results.get(i).getAgy_loc_id()) != -1);
        }
    }

}