package net.syscon.elite.service.impl.keyworker;

import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;
import net.syscon.elite.service.keyworker.KeyworkerAutoAllocationService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class KeyworkerTestHelper {
    public static final int CAPACITY_TIER_1 = 6;
    public static final int CAPACITY_TIER_2 = 9;
    public static final int FULLY_ALLOCATED = CAPACITY_TIER_2;

    public static void verifyException(Throwable thrown, Class<? extends Throwable> expectedException, String expectedMessage) {
        assertThat(thrown).isInstanceOf(expectedException).hasMessage(expectedMessage);
    }

    // Provides a Key worker with specified staff id and number of allocations
    public static Keyworker getKeyworker(long staffId, int numberOfAllocations) {
        return Keyworker.builder()
                .staffId(staffId)
                .numberAllocated(numberOfAllocations)
                .firstName(RandomStringUtils.randomAscii(35))
                .lastName(RandomStringUtils.randomAscii(35))
                .build();
    }

    // Provides list of Key workers with varying number of allocations (within specified range)
    public static List<Keyworker> getKeyworkers(long total, int minAllocations, int maxAllocations) {
        List<Keyworker> keyworkers = new ArrayList<>();

        for (long i = 1; i <= total; i++) {
            keyworkers.add(Keyworker.builder()
                    .staffId(i)
                    .numberAllocated(RandomUtils.nextInt(minAllocations, maxAllocations + 1))
                    .build());
        }

        return keyworkers;
    }

    public static OffenderSummary getOffender(long bookingId, String agencyId) {
        return OffenderSummary.builder()
                .bookingId(bookingId)
                .agencyLocationId(agencyId)
                .build();
    }

    public static void verifyAutoAllocation(KeyWorkerAllocation kwAlloc, long bookingId, long staffId) {
        assertThat(kwAlloc.getBookingId()).isEqualTo(bookingId);
        assertThat(kwAlloc.getStaffId()).isEqualTo(staffId);
        assertThat(kwAlloc.getType()).isEqualTo(AllocationType.AUTO.getIndicator());
        assertThat(kwAlloc.getReason()).isEqualTo(KeyworkerAutoAllocationService.ALLOCATION_REASON_AUTO);
    }

    public static void mockPrisonerAllocationHistory(KeyWorkerAllocationService keyWorkerAllocationService,
                                                     KeyWorkerAllocation... allocations) {
        List<KeyWorkerAllocation> allocationHistory =
                (allocations == null) ? Collections.emptyList() : Arrays.asList(allocations);

        when(keyWorkerAllocationService
                .getAllocationHistoryForPrisoner(anyLong(), anyString(), any(Order.class)))
                .thenReturn(allocationHistory);
    }

    public static KeyworkerPool initKeyworkerPool(KeyWorkerAllocationService keyWorkerAllocationService,
                                                  Collection<Keyworker> keyworkers, Collection<Integer> capacityTiers) {
        KeyworkerPool keyworkerPool = new KeyworkerPool(keyworkers, capacityTiers);

        keyworkerPool.setKeyWorkerAllocationService(keyWorkerAllocationService);

        return keyworkerPool;
    }

    // Provides a previous Key worker allocation between specified offender and Key worker with an assigned datetime 7
    // days prior to now.
    public static KeyWorkerAllocation getPreviousKeyworkerAutoAllocation(String agencyId, long bookingId, long staffId) {
        return getPreviousKeyworkerAutoAllocation(agencyId, bookingId, staffId, LocalDateTime.now().minusDays(7));
    }

    // Provides a previous Key worker allocation between specified offender and Key worker, assigned at specified datetime.
    public static KeyWorkerAllocation getPreviousKeyworkerAutoAllocation(String agencyId, long bookingId, long staffId, LocalDateTime assigned) {
        Validate.notNull(assigned, "Allocation must have assigned datetime.");

        return KeyWorkerAllocation.builder()
                .agencyId(agencyId)
                .bookingId(bookingId)
                .staffId(staffId)
                .active("Y")
                .assigned(assigned)
                .type(AllocationType.AUTO.getIndicator())
                .build();
    }

    // Expires a Key worker allocation using specified reason and expiry datetime.
    public static KeyWorkerAllocation expireAllocation(KeyWorkerAllocation allocation, String reason, LocalDateTime expiry) {
        Validate.notNull(allocation, "Allocation to expire must be specified.");
        Validate.notNull(expiry, "Expiry datetime must be specified.");

        return KeyWorkerAllocation.builder()
                .agencyId(allocation.getAgencyId())
                .bookingId(allocation.getBookingId())
                .staffId(allocation.getStaffId())
                .active("N")
                .deallocationReason(StringUtils.trimToNull(reason))
                .expiry(expiry)
                .build();
    }
}
