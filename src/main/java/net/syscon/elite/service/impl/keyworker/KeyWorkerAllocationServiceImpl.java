package net.syscon.elite.service.impl.keyworker;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderKeyWorker;
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

    public KeyWorkerAllocationServiceImpl(final KeyWorkerAllocationRepository repository,
                                          final AgencyRepository agencyRepository,
                                          final UserRepository userRepository) {
        this.repository = repository;
        this.agencyRepository = agencyRepository;
        this.userRepository = userRepository;
    }


    @Override
    @VerifyAgencyAccess
    public List<Keyworker> getAvailableKeyworkers(final String agencyId) {
        return repository.getAvailableKeyworkers(agencyId);
    }

    @Override
    public Keyworker getKeyworkerDetailsByBooking(final Long bookingId) {
        return repository.getKeyworkerDetailsByBooking(bookingId)
                .orElseThrow(EntityNotFoundException.withMessage(String.format("Key worker not found for booking Id %d", bookingId)));
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationsForCurrentCaseload(final String username) {
        Validate.notNull(username, "Key worker username must be specified.");

        final var userDetail = userRepository.findByUsername(username).orElseThrow(EntityNotFoundException.withId(username));
        final var agencyIds = agencyRepository.findAgenciesByCaseload(userDetail.getActiveCaseLoadId())
                                        .stream().map(Agency::getAgencyId).collect(Collectors.toList());
        final var allocations = repository.getAllocationDetailsForKeyworker(userDetail.getStaffId(), agencyIds);
        allocations.forEach(a -> a.setInternalLocationDesc(LocationProcessor.stripAgencyId(a.getInternalLocationDesc(), a.getAgencyId())));

        return allocations;
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationDetailsForKeyworkers(final List<Long> staffIds, final String agencyId) {
        Validate.notEmpty(staffIds, "Key worker staffIds must be specified.");
        Validate.notNull(agencyId, "agencyId must be specified.");

        if (staffIds.size() == 1) {
            final var staffId = staffIds.get(0);
            if (!repository.checkKeyworkerExists(staffId)) {
                throw EntityNotFoundException.withId(staffId);
            }
        }
        final var allocations = repository.getAllocationDetailsForKeyworkers(staffIds, Collections.singletonList(agencyId));
        allocations.forEach(a -> a.setInternalLocationDesc(LocationProcessor.stripAgencyId(a.getInternalLocationDesc(), a.getAgencyId())));

        return allocations.stream()
                .sorted(Comparator
                .comparing(KeyWorkerAllocationDetail::getStaffId)
                .thenComparing(KeyWorkerAllocationDetail::getBookingId)
                .thenComparing(KeyWorkerAllocationDetail::getAssigned).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationDetailsForOffenders(final List<String> offenderNos, final String agencyId) {
        Validate.notEmpty(offenderNos, "Offender Nos must be specified.");
        Validate.notNull(agencyId, "agencyId must be specified.");

        final var allocations = repository.getAllocationDetailsForOffenders(offenderNos, Collections.singletonList(agencyId));

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
    public Page<OffenderKeyWorker> getAllocationHistoryByAgency(final String agencyId, final PageRequest pageRequest) {
        Validate.notBlank(agencyId, "Agency id is required.");
        Validate.notNull(pageRequest, "Page request details are requreid.");

        return repository.getAllocationHistoryByAgency(agencyId, pageRequest);
    }

    @Override
    public List<OffenderKeyWorker> getAllocationHistoryByStaffIds(final List<Long> staffIds) {
        Validate.notEmpty(staffIds, "At least 1 staff Id is required.");
        final var allocations = repository.getAllocationHistoryByStaffIds(staffIds);
        return allocations.stream()
                .sorted(Comparator
                        .comparing(OffenderKeyWorker::getOffenderNo)
                        .thenComparing(OffenderKeyWorker::getAssigned).reversed())
                .collect(Collectors.toList());

    }

    @Override
    public List<OffenderKeyWorker> getAllocationHistoryByOffenderNos(final List<String> offenderNos) {
        Validate.notEmpty(offenderNos, "At lease 1 offender no is required.");
        final var allocations = repository.getAllocationHistoryByOffenderNos(offenderNos);
        return allocations.stream()
                .sorted(Comparator
                        .comparing(OffenderKeyWorker::getOffenderNo)
                        .thenComparing(OffenderKeyWorker::getAssigned).reversed())
                .collect(Collectors.toList());
    }

}
