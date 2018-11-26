package net.syscon.elite.service.impl.keyworker;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.AgencyRepository;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;
import net.syscon.elite.service.support.LocationProcessor;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Validated
public class KeyWorkerAllocationServiceImpl implements KeyWorkerAllocationService {

    private final KeyWorkerAllocationRepository repository;
    private final AgencyRepository agencyRepository;
    private final UserRepository userRepository;

    public KeyWorkerAllocationServiceImpl(KeyWorkerAllocationRepository repository,
                                          AgencyRepository agencyRepository,
                                          UserRepository userRepository) {
        this.repository = repository;
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
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
    public List<KeyWorkerAllocationDetail> getAllocationsForCurrentCaseload(String username) {
        Validate.notNull(username, "Key worker username must be specified.");

        UserDetail userDetail = userRepository.findByUsername(username).orElseThrow(EntityNotFoundException.withId(username));
        List<String> agencyIds = agencyRepository.findAgenciesByCaseload(userDetail.getActiveCaseLoadId())
                                        .stream().map(Agency::getAgencyId).collect(Collectors.toList());
        List<KeyWorkerAllocationDetail> allocations = repository.getAllocationDetailsForKeyworker(userDetail.getStaffId(), agencyIds);
        allocations.forEach(a -> a.setInternalLocationDesc(LocationProcessor.stripAgencyId(a.getInternalLocationDesc(), a.getAgencyId())));

        return allocations;
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationDetailsForKeyworkers(List<Long> staffIds, String agencyId) {
        Validate.notEmpty(staffIds, "Key worker staffIds must be specified.");
        Validate.notNull(agencyId, "agencyId must be specified.");

        if (staffIds.size() == 1) {
            Long staffId = staffIds.get(0);
            if (!repository.checkKeyworkerExists(staffId)) {
                throw EntityNotFoundException.withId(staffId);
            }
        }
        List<KeyWorkerAllocationDetail> allocations = repository.getAllocationDetailsForKeyworkers(staffIds, Collections.singletonList(agencyId));
        allocations.forEach(a -> a.setInternalLocationDesc(LocationProcessor.stripAgencyId(a.getInternalLocationDesc(), a.getAgencyId())));

        return allocations.stream()
                .sorted(Comparator
                .comparing(KeyWorkerAllocationDetail::getStaffId)
                .thenComparing(KeyWorkerAllocationDetail::getBookingId)
                .thenComparing(KeyWorkerAllocationDetail::getAssigned).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationDetailsForOffenders(List<String> offenderNos, String agencyId) {
        Validate.notEmpty(offenderNos, "Offender Nos must be specified.");
        Validate.notNull(agencyId, "agencyId must be specified.");

        List<KeyWorkerAllocationDetail> allocations = repository.getAllocationDetailsForOffenders(offenderNos, Collections.singletonList(agencyId));

        allocations.forEach(a -> a.setInternalLocationDesc(LocationProcessor.stripAgencyId(a.getInternalLocationDesc(), a.getAgencyId())));

        return allocations.stream()
                .sorted(Comparator
                        .comparing(KeyWorkerAllocationDetail::getBookingId)
                        .thenComparing(KeyWorkerAllocationDetail::getStaffId)
                        .thenComparing(KeyWorkerAllocationDetail::getAssigned).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @VerifyAgencyAccess
    public Page<OffenderKeyWorker> getAllocationHistoryByAgency(String agencyId, PageRequest pageRequest) {
        Validate.notBlank(agencyId, "Agency id is required.");
        Validate.notNull(pageRequest, "Page request details are requreid.");

        return repository.getAllocationHistoryByAgency(agencyId, pageRequest);
    }

    @Override
    public List<OffenderKeyWorker> getAllocationHistoryByStaffIds(List<Long> staffIds) {
        Validate.notEmpty(staffIds, "At least 1 staff Id is required.");
        List<OffenderKeyWorker> allocations = repository.getAllocationHistoryByStaffIds(staffIds);
        return allocations.stream()
                .sorted(Comparator
                        .comparing(OffenderKeyWorker::getOffenderNo)
                        .thenComparing(OffenderKeyWorker::getAssigned).reversed())
                .collect(Collectors.toList());

    }

    @Override
    public List<OffenderKeyWorker> getAllocationHistoryByOffenderNos(List<String> offenderNos) {
        Validate.notEmpty(offenderNos, "At lease 1 offender no is required.");
        List<OffenderKeyWorker> allocations = repository.getAllocationHistoryByOffenderNos(offenderNos);
        return allocations.stream()
                .sorted(Comparator
                        .comparing(OffenderKeyWorker::getOffenderNo)
                        .thenComparing(OffenderKeyWorker::getAssigned).reversed())
                .collect(Collectors.toList());
    }

}
