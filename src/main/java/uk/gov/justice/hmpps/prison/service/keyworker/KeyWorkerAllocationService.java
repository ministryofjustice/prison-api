package uk.gov.justice.hmpps.prison.service.keyworker;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.KeyWorkerAllocationDetail;
import uk.gov.justice.hmpps.prison.api.model.Keyworker;
import uk.gov.justice.hmpps.prison.api.model.OffenderKeyWorker;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.repository.KeyWorkerAllocationRepository;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@Validated
public class KeyWorkerAllocationService {

    private final KeyWorkerAllocationRepository repository;
    private final int maxBatchSize;

    public KeyWorkerAllocationService(final KeyWorkerAllocationRepository repository,
                                      @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.repository = repository;
        this.maxBatchSize = maxBatchSize;
    }

    public List<Keyworker> getAvailableKeyworkers(final String agencyId) {
        return repository.getAvailableKeyworkers(agencyId);
    }

    public Keyworker getKeyworkerDetailsByBooking(final Long bookingId) {
        return repository.getKeyworkerDetailsByBooking(bookingId)
                .orElseThrow(EntityNotFoundException.withMessage(String.format("Key worker not found for booking Id %d", bookingId)));
    }

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
                .collect(toList());
    }

    public Page<OffenderKeyWorker> getAllocationHistoryByAgency(final String agencyId, final PageRequest pageRequest) {
        Validate.notBlank(agencyId, "Agency id is required.");
        Validate.notNull(pageRequest, "Page request details are requreid.");

        return repository.getAllocationHistoryByAgency(agencyId, pageRequest);
    }

    public List<OffenderKeyWorker> getAllocationHistoryByOffenderNos(final List<String> offenderNos) {
        Validate.notEmpty(offenderNos, "At lease 1 offender no is required.");
        final var batch = Lists.partition(new ArrayList<>(offenderNos), maxBatchSize);
        final var allocations = batch.stream().flatMap(offenderNosBatch ->
            repository.getAllocationHistoryByOffenderNos(offenderNosBatch).stream()
        ).toList();
        return allocations.stream()
                .sorted(Comparator
                        .comparing(OffenderKeyWorker::getOffenderNo)
                        .thenComparing(OffenderKeyWorker::getAssigned).reversed())
                .collect(toList());
    }
}
