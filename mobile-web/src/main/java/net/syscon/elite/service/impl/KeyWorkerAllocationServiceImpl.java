package net.syscon.elite.service.impl;

import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.KeyWorkerAllocationService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class KeyWorkerAllocationServiceImpl implements KeyWorkerAllocationService{

    private KeyWorkerAllocationRepository repository;

    public KeyWorkerAllocationServiceImpl(KeyWorkerAllocationRepository repository) {
        this.repository = repository;
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
    public void deactivateAllocationForOffenderBooking(Long bookingId, String reason, String username) {
        repository.deactivateAllocationForOffenderBooking(bookingId, reason, username);
    }

    @Override
    public List<KeyWorkerAllocation> getAllocationHistoryForPrisoner(Long offenderId, String orderByFields, Order order) {

        String sortFields = StringUtils.defaultString(orderByFields, "assigned");
        Order sortOrder = ObjectUtils.defaultIfNull(order, Order.DESC);
        return repository.getAllocationHistoryForPrisoner(offenderId, sortFields, sortOrder);
    }

    @Override
    public KeyWorkerAllocation getCurrentAllocationForOffenderBooking(Long bookingId) {
        KeyWorkerAllocation keyWorkerAllocation = repository.getCurrentAllocationForOffenderBooking(bookingId)
                .orElseThrow(EntityNotFoundException.withMessage(String.format("Active allocation not found for offenderBookingId %s", bookingId)));
        return keyWorkerAllocation;
    }

    @Override
    public KeyWorkerAllocation getLatestAllocationForOffenderBooking(Long bookingId) {
        KeyWorkerAllocation keyWorkerAllocation = repository.getLatestAllocationForOffenderBooking(bookingId)
                .orElseThrow(EntityNotFoundException.withMessage(String.format("Allocation not found for offenderBookingId %s", bookingId)));
        return keyWorkerAllocation;
    }

    @Override
    public Page<OffenderSummary> getUnallocatedOffenders(Set<String> agencyFilter, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        String sortFieldsDefaulted = StringUtils.defaultString(sortFields, "lastName");
        Order sortOrderDefaulted = ObjectUtils.defaultIfNull(sortOrder, Order.ASC);

        return repository.getUnallocatedOffenders(agencyFilter, pageOffset, pageLimit, sortFieldsDefaulted, sortOrderDefaulted);
    }

}
