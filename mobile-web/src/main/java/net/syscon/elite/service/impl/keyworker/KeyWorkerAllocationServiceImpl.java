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
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
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
        return repository.getAllocationDetailsForKeyworker(userDetail.getStaffId(), agencyIds);
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationDetailsForKeyworker(Long staffId, String agencyId) {
        Validate.notNull(staffId, "Key worker staffId must be specified.");
        Validate.notNull(agencyId, "agencyId must be specified.");

        if (repository.checkKeyworkerExists(staffId)) {
            return repository.getAllocationDetailsForKeyworker(staffId, Collections.singletonList(agencyId));
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
