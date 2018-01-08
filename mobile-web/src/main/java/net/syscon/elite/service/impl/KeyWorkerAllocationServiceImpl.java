package net.syscon.elite.service.impl;

import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
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
        repository.deactivateCurrentAllocationForOffenderBooking(allocation.getBookingId(), username);
        repository.createAllocation(allocation, username);
    }

    @Override
    public List<KeyWorkerAllocation> getAllocationHistoryForPrisoner(Long offenderId) {
        return repository.getAllocationHistoryForPrisoner(offenderId);
    }
}
