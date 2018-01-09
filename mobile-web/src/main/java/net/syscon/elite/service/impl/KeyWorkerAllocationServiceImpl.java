package net.syscon.elite.service.impl;

import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.KeyWorkerAllocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class KeyWorkerAllocationServiceImpl implements KeyWorkerAllocationService{

    private KeyWorkerAllocationRepository repository;

    public KeyWorkerAllocationServiceImpl(KeyWorkerAllocationRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void createAllocation(KeyWorkerAllocation allocation, String username) {
        repository.deactivateAllocationForOffenderBooking(allocation.getBookingId(), username);
        repository.createAllocation(allocation, username);
    }

    @Override
    public void deactivateAllocationForKeyWorker(Long staffId, String username) {
        repository.deactivateAllocationsForKeyWorker(staffId, username);
    }

    @Override
    public void deactivateAllocationForOffenderBooking(Long bookingId, String username) {
        repository.deactivateAllocationForOffenderBooking(bookingId, username);
    }

    @Override
    public List<KeyWorkerAllocation> getAllocationHistoryForPrisoner(Long offenderId) {
        return repository.getAllocationHistoryForPrisoner(offenderId);
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
}
