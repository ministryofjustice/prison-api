package net.syscon.elite.repository;

import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.web.config.PersistenceConfigs;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest()
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class KeyWorkerAllocationRepositoryTest {
    private static final long OFFENDER_BOOKING_ID_WITH_ALLOCATION_1 = -1L;
    private static final long OFFENDER_BOOKING_ID_WITH_ALLOCATION_2 = -2L;
    private static final long OFFENDER_BOOKING_ID_WITHOUT_ALLOCATION = -28L;
    private static final String NEW_ALLOCATION_REASON = "new reason";
    private static final String DEFAULT_ALLOCATION_REASON = "MANUAL";
    private static final long STAFF_ID = -2L;
    private static final String AUTO_ALLOCATION_TYPE = "A";
    private static final String MANUAL_ALLOCATION_TYPE = "M";
    private static final String USERNAME = "testuser";

    @Autowired
    private KeyWorkerAllocationRepository repo;

    @Before
    public void init() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("itag_user", "password"));
    }

    @Test
    public void shouldCreateAllocation() throws Exception {
        repo.createAllocation(buildKeyWorkerAllocation(OFFENDER_BOOKING_ID_WITHOUT_ALLOCATION), USERNAME);
        final KeyWorkerAllocation allocation = repo.getCurrentAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITHOUT_ALLOCATION);
        assertThat(allocation.getReason()).isEqualTo(NEW_ALLOCATION_REASON);
        assertThat(allocation.getStaffId()).isEqualTo(STAFF_ID);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void shouldDeactivateAllocationForOffenderBooking() throws Exception {
        repo.deactivateCurrentAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITH_ALLOCATION_1, USERNAME);
        repo.getCurrentAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITH_ALLOCATION_1);
    }

    @Test
    public void shouldGetCurrentAllocationForOffenderBooking() throws Exception {

        final KeyWorkerAllocation allocation = repo.getCurrentAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITH_ALLOCATION_2);
        assertThat(allocation.getBookingId()).isEqualTo(OFFENDER_BOOKING_ID_WITH_ALLOCATION_2);
        assertThat(allocation.getStaffId()).isEqualTo(STAFF_ID);
        assertThat(allocation.getReason()).isEqualTo(DEFAULT_ALLOCATION_REASON);
        assertThat(allocation.getType()).isEqualTo(MANUAL_ALLOCATION_TYPE);
    }

    @Test
    public void shouldGetAllocationHistoryForPrisonerInCronologicalOrder() throws Exception {
        final List<KeyWorkerAllocation> historyForPrisoner = repo.getAllocationHistoryForPrisoner(-1L);
        assertThat(historyForPrisoner).extracting("assigned").isSorted();
    }

    private KeyWorkerAllocation buildKeyWorkerAllocation(Long bookingId) {
        return KeyWorkerAllocation.builder().agencyId("LEI").bookingId(bookingId).reason(NEW_ALLOCATION_REASON).staffId(STAFF_ID).type(AUTO_ALLOCATION_TYPE).build();
    }

}