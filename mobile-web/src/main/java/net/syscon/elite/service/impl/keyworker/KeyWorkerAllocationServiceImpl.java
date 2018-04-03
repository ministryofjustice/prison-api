package net.syscon.elite.service.impl.keyworker;

import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderKeyWorker;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Validated
public class KeyWorkerAllocationServiceImpl implements KeyWorkerAllocationService {

    private final KeyWorkerAllocationRepository repository;

    @Value("${api.keyworker.deallocation.buffer.hours:48}")
    private int deallocationBufferHours;

    public KeyWorkerAllocationServiceImpl(KeyWorkerAllocationRepository repository) {
        this.repository = repository;
    }


    @Override
    @VerifyAgencyAccess
    public List<Keyworker> getAvailableKeyworkers(String agencyId) {
        return repository.getAvailableKeyworkers(agencyId);
    }

    @Override
    public Keyworker getKeyworkerDetailsByBooking(Long bookingId) {
        return repository.getKeyworkerDetailsByBooking(bookingId)
                .orElseThrow(EntityNotFoundException.withMessage(String.format("Key worker not found for booking Id %d", bookingId)));
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationDetailsForKeyworker(Long staffId) {
        Validate.notNull(staffId, "Key worker staffId must be specified.");
        if (repository.checkKeyworkerExists(staffId)) {
            return repository.getAllocationDetailsForKeyworker(staffId);
        } else {
            throw EntityNotFoundException.withId(staffId);
        }
    }

    @Override
    @VerifyAgencyAccess
    public Page<OffenderKeyWorker> getAllocationHistoryByAgency(String agencyId, PageRequest pageRequest) {
        Validate.notBlank(agencyId, "Agency id is required.");
        Validate.notNull(pageRequest, "Page request details are requreid.");

        return repository.getAllocationHistoryByAgency(agencyId, pageRequest);
    }

}
