package net.syscon.elite.repository;

import net.syscon.elite.repository.impl.KeyWorkerAllocation;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@JdbcTest()
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class KeyWorkerAllocationRepositoryTest {
    private static final long OFFENDER_BOOKING_ID_WITH_ALLOCATION_1 = -29L;
    private static final long OFFENDER_BOOKING_ID_WITH_ALLOCATION_2 = -32L;
    private static final long OFFENDER_BOOKING_ID_WITH_INACTIVE_ALLOCATION = -29L;
    private static final long OFFENDER_BOOKING_ID_WITHOUT_ALLOCATION = -30L;
    private static final long OFFENDER_ID_WITH_ALLOCATION = -29L;
    private static final long KEY_WORKER_WITH_ALLOCATIONS = -5;
    private static final String NEW_ALLOCATION_REASON = "new reason";
    private static final String DEFAULT_ALLOCATION_REASON = "MANUAL";
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
        final Optional<KeyWorkerAllocation> allocation = repo.getCurrentAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITHOUT_ALLOCATION);

        assertTrue(allocation.isPresent());
        assertThat(allocation.get().getReason()).isEqualTo(NEW_ALLOCATION_REASON);
        assertThat(allocation.get().getStaffId()).isEqualTo(KEY_WORKER_WITH_ALLOCATIONS);
    }

    @Test
    public void shouldDeactivateAllocationForOffenderBooking() throws Exception {
        repo.deactivateAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITH_ALLOCATION_1, USERNAME);
        final Optional<KeyWorkerAllocation> allocation = repo.getCurrentAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITH_ALLOCATION_1);
        assertThat(allocation).isNotPresent();
    }

    @Test
    public void shouldGetCurrentAllocationForOffenderBooking() throws Exception {

        final Optional<KeyWorkerAllocation> allocation = repo.getCurrentAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITH_ALLOCATION_2);

        assertTrue(allocation.isPresent());
        assertThat(allocation.get().getBookingId()).isEqualTo(OFFENDER_BOOKING_ID_WITH_ALLOCATION_2);
        assertThat(allocation.get().getStaffId()).isEqualTo(KEY_WORKER_WITH_ALLOCATIONS);
        assertThat(allocation.get().getReason()).isEqualTo(DEFAULT_ALLOCATION_REASON);
        assertThat(allocation.get().getType()).isEqualTo(MANUAL_ALLOCATION_TYPE);

        assertThat(repo.getCurrentAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITH_INACTIVE_ALLOCATION)).isNotNull();
    }

    @Test
    public void shouldGetLatestAllocationForOffenderBooking() throws Exception {

        final Optional<KeyWorkerAllocation> allocation = repo.getLatestAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITH_INACTIVE_ALLOCATION);

        assertTrue(allocation.isPresent());
        assertThat(allocation.get().getBookingId()).isEqualTo(OFFENDER_BOOKING_ID_WITH_INACTIVE_ALLOCATION);
        assertThat(allocation.get().getStaffId()).isEqualTo(KEY_WORKER_WITH_ALLOCATIONS);
        assertThat(allocation.get().getActive()).isEqualTo("N");
    }

    @Test
    public void shouldHandleEmptyResult_getLatestAllocationForOffenderBooking() throws Exception {

        final Optional<KeyWorkerAllocation> allocation = repo.getLatestAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITHOUT_ALLOCATION);

        assertThat(allocation).isNotPresent();
    }

    @Test
    public void shouldGetAllocationHistoryForPrisonerInCronologicalOrder() throws Exception {
        final List<KeyWorkerAllocation> historyForPrisoner = repo.getAllocationHistoryForPrisoner(OFFENDER_ID_WITH_ALLOCATION);
        assertThat(historyForPrisoner).extracting("bookingId").containsExactly(-29L, -29L);
        assertThat(historyForPrisoner).extracting("assigned").isSorted();
    }

    @Test
    public void shouldDeactivateAllocationForKeyWorker() throws Exception {
        repo.deactivateAllocationsForKeyWorker(KEY_WORKER_WITH_ALLOCATIONS, USERNAME);

        final List<KeyWorkerAllocation> historyForPrisoner = repo.getAllocationHistoryForPrisoner(OFFENDER_BOOKING_ID_WITH_INACTIVE_ALLOCATION);
        assertThat(historyForPrisoner).extracting("active").containsExactly("N", "N");
    }

    private KeyWorkerAllocation buildKeyWorkerAllocation(Long bookingId) {
        return KeyWorkerAllocation.builder().agencyId("LEI").bookingId(bookingId).reason(NEW_ALLOCATION_REASON).staffId(KEY_WORKER_WITH_ALLOCATIONS).type(AUTO_ALLOCATION_TYPE).build();
    }

}