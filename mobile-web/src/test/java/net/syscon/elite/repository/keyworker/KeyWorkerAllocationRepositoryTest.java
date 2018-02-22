package net.syscon.elite.repository.keyworker;

import com.google.common.collect.ImmutableSet;
import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("nomis-hsqldb")
@RunWith(SpringRunner.class)
@JdbcTest()
@Transactional
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class KeyWorkerAllocationRepositoryTest {
    private static final long OFFENDER_BOOKING_ID_WITH_ALLOCATION_1 = -31L;
    private static final long OFFENDER_BOOKING_ID_WITH_ALLOCATION_2 = -32L;
    private static final long OFFENDER_BOOKING_ID_WITH_INACTIVE_ALLOCATION = -29L;
    private static final long OFFENDER_BOOKING_ID_WITHOUT_ALLOCATION = -30L;
    private static final long OFFENDER_BOOKING_ID_HAVING_ROOT_OFFENDER_ALIASES = -9L;
    private static final long KEY_WORKER_WITH_ALLOCATIONS = -5;
    private static final String NEW_ALLOCATION_REASON = "new reason";
    private static final String DEALLOCATION_REASON = "annual leave";
    private static final String DEFAULT_ALLOCATION_REASON = "MANUAL";
    private static final String AUTO_ALLOCATION_TYPE = "A";
    private static final String MANUAL_ALLOCATION_TYPE = "M";
    private static final String USERNAME = "testuser";
    private static final String OFFENDER_NO_WITH_INACTIVE_ALLOCATIONS = "A6676RS";
    private static final String AGENCY_ID = "LEI";

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
        repo.deactivateAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITH_ALLOCATION_2, DEALLOCATION_REASON, USERNAME);
        final Optional<KeyWorkerAllocation> allocation = repo.getLatestAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITH_ALLOCATION_2);
        assertTrue(allocation.isPresent());
        assertThat(allocation.get().getExpiry()).isNotNull();
        assertThat(allocation.get().getActive()).isEqualTo("N");
        assertThat(allocation.get().getDeallocationReason()).isEqualTo(DEALLOCATION_REASON);
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
    public void shouldHandleEmptyResultGetLatestAllocationForOffenderBooking() throws Exception {

        final Optional<KeyWorkerAllocation> allocation = repo.getLatestAllocationForOffenderBooking(OFFENDER_BOOKING_ID_WITHOUT_ALLOCATION);

        assertThat(allocation).isNotPresent();
    }

    @Test
    public void shouldGetAllocationHistoryForPrisonerInOrderAssignedDesc() throws Exception {
        final List<KeyWorkerAllocation> historyForPrisoner = repo.getAllocationHistoryForPrisoner(OFFENDER_BOOKING_ID_WITH_INACTIVE_ALLOCATION, "assigned", Order.DESC);
        assertThat(historyForPrisoner).extracting("bookingId").containsExactly(-29L, -29L);
        assertThat(historyForPrisoner).isSortedAccordingTo((o1, o2) -> o2.getAssigned().compareTo(o1.getAssigned()));
    }

    @Test
    public void shouldGetAllocationHistoryForPrisonerAliases() throws Exception {
        List<KeyWorkerAllocation> historyForPrisoner = repo.getAllocationHistoryForPrisoner(OFFENDER_BOOKING_ID_HAVING_ROOT_OFFENDER_ALIASES, "assigned", Order.DESC);

        assertThat(historyForPrisoner).extracting("bookingId").containsExactly(-15L, -14L, -9L);
    }

    @Test
    public void shouldDeactivateAllocationForKeyWorker() throws Exception {
        repo.deactivateAllocationsForKeyWorker(KEY_WORKER_WITH_ALLOCATIONS, DEALLOCATION_REASON, USERNAME);

        final List<KeyWorkerAllocation> historyForPrisoner = repo.getAllocationHistoryForPrisoner(OFFENDER_BOOKING_ID_WITH_ALLOCATION_1, "assigned", Order.DESC);
        assertThat(historyForPrisoner).extracting("active").containsExactly("N");
        assertThat(historyForPrisoner).extracting("deallocationReason").containsExactly(DEALLOCATION_REASON);
    }

    @Test
    public void shouldGetUnallocatedOffendersPaginatedAndSorted() throws Exception {
        final Page<OffenderSummary> unallocatedOffenders = repo.getUnallocatedOffenders(ImmutableSet.of("LEI", "BXI"), 0L, 5L, "lastName", Order.ASC);

        assertThat(unallocatedOffenders.getItems()).hasSize(5);
        assertThat(unallocatedOffenders.getItems()).extracting("offenderNo").contains(OFFENDER_NO_WITH_INACTIVE_ALLOCATIONS);
        assertThat(unallocatedOffenders.getItems()).extracting("lastName").isSorted();

        final OffenderSummary os = unallocatedOffenders.getItems().get(1);

        assertThat(os.getAgencyLocationDesc()).isEqualTo("LEEDS");
        assertThat(os.getAgencyLocationId()).isEqualTo("LEI");
        assertThat(os.getBookingId()).isEqualTo(-29);
        assertThat(os.getFirstName()).isEqualTo("NEIL");
        assertThat(os.getLastName()).isEqualTo("BRADLEY");
        assertThat(os.getMiddleNames()).isEqualTo("IAN");
        assertThat(os.getInternalLocationDesc()).isEqualTo("H-1");
        assertThat(os.getInternalLocationId()).isEqualTo("-14");
        assertThat(os.getOffenderNo()).isEqualTo("A6676RS");
        assertThat(os.getSuffix()).isEqualTo("SUF");
        assertThat(os.getTitle()).isEqualTo("MR");
    }

    @Test
    public void shouldGetAvailableKeyworkers() {
        final List<Keyworker> availableKeyworkers = repo.getAvailableKeyworkers(AGENCY_ID);

        assertThat(availableKeyworkers).asList().hasSize(1);
        assertThat(availableKeyworkers.get(0).getFirstName()).isEqualTo("Another");
        assertThat(availableKeyworkers.get(0).getLastName()).isEqualTo("User");
        assertThat(availableKeyworkers.get(0).getStaffId()).isEqualTo(-5L);
        assertThat(availableKeyworkers.get(0).getNumberAllocated()).isEqualTo(4);
    }

    @Test
    public void shouldGetAutoAllocatedOffenders() {
        final Page<KeyWorkerAllocationDetail> allocatedOffenders = repo.getAllocatedOffenders(ImmutableSet.of("LEI", "BXI"), LocalDate.parse("2017-04-01"), LocalDate.parse("2017-07-01"),"A", 0L, 5L, "assigned", Order.ASC);

        assertThat(allocatedOffenders.getItems()).hasSize(1);
        assertThat(allocatedOffenders.getItems()).hasSize(1);
        final KeyWorkerAllocationDetail keyWorkerAllocationDetail = allocatedOffenders.getItems().get(0);
        assertThat(keyWorkerAllocationDetail.getFirstName()).isEqualTo("HARRY");
        assertThat(keyWorkerAllocationDetail.getLastName()).isEqualTo("SARLY");
        assertThat(keyWorkerAllocationDetail.getStaffId()).isEqualTo(-5);
        assertThat(keyWorkerAllocationDetail.getInternalLocationDesc()).isEqualTo("H-1");
        assertThat(keyWorkerAllocationDetail.getAllocationType()).isEqualTo("A");
    }

    @Test
    public void shouldGetManuallyAllocatedOffenders() {
        final Page<KeyWorkerAllocationDetail> allocatedOffenders = repo.getAllocatedOffenders(ImmutableSet.of("LEI"), LocalDate.parse("2017-04-01"),
                LocalDate.parse("2017-07-01"),"M", 0L, 5L, "assigned", Order.ASC);

        assertThat(allocatedOffenders.getItems()).hasSize(1);
        final KeyWorkerAllocationDetail keyWorkerAllocationDetail = allocatedOffenders.getItems().get(0);
        assertThat(keyWorkerAllocationDetail.getFirstName()).isEqualTo("FRED");
        assertThat(keyWorkerAllocationDetail.getLastName()).isEqualTo("JAMES");
        assertThat(keyWorkerAllocationDetail.getStaffId()).isEqualTo(-5);
        assertThat(keyWorkerAllocationDetail.getInternalLocationDesc()).isEqualTo("H-1");
        assertThat(keyWorkerAllocationDetail.getAllocationType()).isEqualTo("M");
    }

    @Test
    public void shouldGetAllAllocatedOffenders() {
        final Page<KeyWorkerAllocationDetail> allocatedOffenders = repo.getAllocatedOffenders(ImmutableSet.of("LEI", "BXI"),
                LocalDate.parse("2017-04-01"), LocalDate.parse("2017-07-01"), null, 0L, 30L, "assigned", Order.DESC);

        assertThat(allocatedOffenders.getItems()).hasSize(2);
        assertThat(allocatedOffenders.getItems()).isSortedAccordingTo((o1, o2) -> o2.getAssigned().compareTo(o1.getAssigned()));
    }
    
    @Test
    public void shouldGetKeyworkerDetails() {
       final Optional<Keyworker> keyworker = repo.getKeyworkerDetails(KEY_WORKER_WITH_ALLOCATIONS);

        assertThat(keyworker.isPresent()).isTrue();
        final Keyworker kw = keyworker.get();
        assertThat(kw.getFirstName()).isEqualTo("Another");
        assertThat(kw.getLastName()).isEqualTo("User");
        assertThat(kw.getStaffId()).isEqualTo(KEY_WORKER_WITH_ALLOCATIONS);
        assertThat(kw.getNumberAllocated()).isEqualTo(4);
    }

    @Test
    public void testGetAllocationsForKeyworker() {
        List<KeyWorkerAllocation> allocations = repo.getAllocationsForKeyworker(KEY_WORKER_WITH_ALLOCATIONS);

        assertThat(allocations.size()).isEqualTo(7);
    }

    private KeyWorkerAllocation buildKeyWorkerAllocation(Long bookingId) {
        return KeyWorkerAllocation.builder().agencyId("LEI").bookingId(bookingId).reason(NEW_ALLOCATION_REASON).staffId(KEY_WORKER_WITH_ALLOCATIONS).type(AUTO_ALLOCATION_TYPE).build();
    }
}