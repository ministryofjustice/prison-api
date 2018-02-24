package net.syscon.elite.service.impl.keyworker;

import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.NewAllocation;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.ws.rs.BadRequestException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Validated
public class KeyWorkerAllocationServiceImpl implements KeyWorkerAllocationService {

    private final KeyWorkerAllocationRepository repository;
    private final AuthenticationFacade authenticationFacade;
    private final BookingService bookingService;

    @Value("${api.keyworker.deallocation.buffer.hours:48}")
    private int deallocationBufferHours;

    public KeyWorkerAllocationServiceImpl(KeyWorkerAllocationRepository repository,
            AuthenticationFacade authenticationFacade, BookingService bookingService) {
        this.repository = repository;
        this.authenticationFacade = authenticationFacade;
        this.bookingService = bookingService;
    }

    @Override
    @Transactional
    public void createAllocation(KeyWorkerAllocation allocation, String username) {
        repository.getCurrentAllocationForOffenderBooking(allocation.getBookingId())
                .orElseThrow(AllocationException.withMessage(String.format("Existing allocation found for offenderBookingId %s", allocation.getBookingId())));
        repository.createAllocation(allocation, username);
    }

    @Override
    @Transactional
    public void deactivateAllocationForKeyWorker(Long staffId, String reason, String username) {
        repository.deactivateAllocationsForKeyWorker(staffId, reason, username);
    }

    @Override
    @Transactional
    @VerifyBookingAccess
    public void deactivateAllocationForOffenderBooking(Long bookingId, String reason, String username) {
        repository.deactivateAllocationForOffenderBooking(bookingId, reason, username);
    }

    @Override
    @Transactional
    public void allocate(@Valid NewAllocation newAllocation) {

        final Long bookingId = newAllocation.getBookingId();
        final String agencyId = bookingService.getBookingAgency(bookingId);
        if (!bookingService.isSystemUser()) {
            bookingService.verifyBookingAccess(bookingId);
        }
        validateStaffId(bookingId, newAllocation.getStaffId());
        final String username = authenticationFacade.getCurrentUsername();

        // Remove current allocation if any
        deactivateAllocationForOffenderBooking(bookingId, "OVERRIDDEN", username);

        KeyWorkerAllocation allocation = KeyWorkerAllocation.builder()//
                .bookingId(bookingId)//
                .staffId(newAllocation.getStaffId())//
                .agencyId(agencyId)//
                .reason(newAllocation.getReason())//
                .active("Y")//
                .assigned(LocalDateTime.now())//
                .type(newAllocation.getType())//
                .build();

        repository.createAllocation(allocation, username);
    }

    /**
     * Check that staffId denotes a keyworker at the offender's prison
     * @param bookingId
     * @param staffId
     */
    private void validateStaffId(Long bookingId, Long staffId) {
        repository.checkAvailableKeyworker(bookingId, staffId);
    }

    @Override
    @VerifyBookingAccess
    public List<KeyWorkerAllocation> getAllocationHistoryForPrisoner(Long bookingId, String orderByFields, Order order) {

        String sortFields = StringUtils.defaultString(orderByFields, "assigned");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.DESC);
        return repository.getAllocationHistoryForPrisoner(bookingId, sortFields, sortOrder);
    }

    @Override
    @VerifyBookingAccess
    public KeyWorkerAllocation getCurrentAllocationForOffenderBooking(Long bookingId) {
        KeyWorkerAllocation keyWorkerAllocation = repository.getCurrentAllocationForOffenderBooking(bookingId)
                .orElseThrow(EntityNotFoundException.withMessage(String.format("Active allocation not found for offenderBookingId %s", bookingId)));
        return keyWorkerAllocation;
    }

    @Override
    @VerifyBookingAccess
    public KeyWorkerAllocation getLatestAllocationForOffenderBooking(Long bookingId) {
        KeyWorkerAllocation keyWorkerAllocation = repository.getLatestAllocationForOffenderBooking(bookingId)
                .orElseThrow(EntityNotFoundException.withMessage(String.format("Allocation not found for offenderBookingId %s", bookingId)));
        return keyWorkerAllocation;
    }

    @Override
    @VerifyAgencyAccess
    public Page<OffenderSummary> getUnallocatedOffenders(String agencyId, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        String sortFieldsDefaulted = StringUtils.defaultString(sortFields, "lastName");
        Order sortOrderDefaulted = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);
        Long pageOffsetDefaulted = ObjectUtils.defaultIfNull(pageOffset, 0L);
        Long pageLimitDefaulted = ObjectUtils.defaultIfNull(pageLimit, 10L);

        return repository.getUnallocatedOffenders(Collections.singleton(agencyId), pageOffsetDefaulted, pageLimitDefaulted, sortFieldsDefaulted, sortOrderDefaulted);
    }

    @Override
    @VerifyAgencyAccess
    public Page<KeyWorkerAllocationDetail> getAllocations(String agencyId, LocalDate fromDate, LocalDate toDate, String allocationType, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        validateAllocationsRequestDateRange(fromDate, toDate);

        String sortFieldsDefaulted = StringUtils.defaultString(sortFields, "lastName,firstName");
        Order sortOrderDefaulted = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);
        Long pageOffsetDefaulted = ObjectUtils.defaultIfNull(pageOffset, 0L);
        Long pageLimitDefaulted = ObjectUtils.defaultIfNull(pageLimit, 10L);

        return repository.getAllocatedOffenders(Collections.singleton(agencyId), fromDate, toDate, allocationType, pageOffsetDefaulted, pageLimitDefaulted,  sortFieldsDefaulted, sortOrderDefaulted);
    }

    @Override
    @VerifyAgencyAccess
    public List<Keyworker> getAvailableKeyworkers(String agencyId) {
        return repository.getAvailableKeyworkers(agencyId);
    }

    @Override
    public Keyworker getKeyworkerDetails(Long staffId) {
        final Keyworker keyworker = repository.getKeyworkerDetails(staffId)
                .orElseThrow(EntityNotFoundException.withMessage(String.format("Key worker with id %d not found", staffId)));

        int activeAllocCount = Optional.ofNullable(keyworker.getNumberAllocated()).orElse(0);

        List<KeyWorkerAllocation> recentDeallocations = getRecentDeallocations(staffId, Duration.ofHours(deallocationBufferHours));

        recentDeallocations = filterDeallocations(recentDeallocations);

        keyworker.setNumberAllocated(activeAllocCount + recentDeallocations.size());

        return keyworker;
    }

    // Gets allocations for Key worker that have expired recently (between start of look back period and current time)
    private List<KeyWorkerAllocation> getRecentDeallocations(Long staffId, Duration lookBackDuration) {
        List<KeyWorkerAllocation> keyWorkerAllocations = getAllocationsForKeyworker(staffId);
        LocalDateTime cutOff = LocalDateTime.now().minus(lookBackDuration);

        // Extract set of booking id for active allocations
        Set<Long> activeBookingIds = keyWorkerAllocations.stream()
                .filter(kwa -> StringUtils.equalsIgnoreCase("Y", kwa.getActive()))
                .map(KeyWorkerAllocation::getBookingId)
                .collect(Collectors.toSet());

        // Allocation record is of interest if:
        //  - if it is for booking that is not in set of active allocation booking ids, and
        //  - it has an expiry datetime set, and
        //  - expiry datetime is after start of 'lookBackDuration' period.
        List<KeyWorkerAllocation> deallocations = keyWorkerAllocations.stream()
                .filter(kwa -> !activeBookingIds.contains(kwa.getBookingId()))
                .filter(kwa -> Objects.nonNull(kwa.getExpiry()))
                .filter(kwa -> kwa.getExpiry().isAfter(cutOff))
                .collect(Collectors.toList());

        return deallocations;
    }

    // Removes deallocations where:
    //  - offender has active booking (in different agency)
    //  - offender has active booking in same agency and is allocated to a different Key worker
    private List<KeyWorkerAllocation> filterDeallocations(List<KeyWorkerAllocation> deallocations) {
        Predicate<KeyWorkerAllocation> kwaDeallocPredicate = (kwa -> {
            boolean keepDeallocation = true;

            OffenderSummary summary = bookingService.getLatestBookingByBookingId(kwa.getBookingId());

            // Keep deallocation if:
            //  - no offender summary found for booking id (should not happen)
            //  - latest offender booking indicates they are not currently in prison, or
            //  - latest offender booking indicates they are in same agency as allocation and
            //    they have an active allocation (to same or a different Key worker)
            if (Objects.nonNull(summary) && StringUtils.equalsIgnoreCase(summary.getCurrentlyInPrison(), "Y")) {
                if (StringUtils.equals(kwa.getAgencyId(), summary.getAgencyLocationId())) {
                    Optional<KeyWorkerAllocation> latestOffenderAlloc =
                            repository.getCurrentAllocationForOffenderBooking(summary.getBookingId());

                    keepDeallocation = !latestOffenderAlloc.isPresent();
                } else {
                    keepDeallocation = false;
                }
            }

            return keepDeallocation;
        });

        return deallocations.stream().filter(kwaDeallocPredicate).collect(Collectors.toList());
    }

    @Override
    public List<KeyWorkerAllocation> getAllocationsForKeyworker(Long staffId) {
        Validate.notNull(staffId, "Key worker staffId must be specified.");

        return repository.getAllocationsForKeyworker(staffId);
    }

    private void validateAllocationsRequestDateRange(LocalDate fromDate, LocalDate toDate) {
        // Validate date range
        if (Objects.nonNull(toDate) && toDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Invalid date range: toDate cannot be in the future.");
        }
        if (Objects.nonNull(fromDate) && Objects.nonNull(toDate) && toDate.isBefore(fromDate)) {
            throw new BadRequestException("Invalid date range: toDate is before fromDate.");
        }
    }
}
