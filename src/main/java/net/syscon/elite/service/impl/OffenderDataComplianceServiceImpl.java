package net.syscon.elite.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.OffenderNumber;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.OffenderDeletionRepository;
import net.syscon.elite.repository.OffenderRepository;
import net.syscon.elite.service.OffenderDataComplianceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OffenderDataComplianceServiceImpl implements OffenderDataComplianceService {

    private final OffenderRepository offenderRepository;
    private final OffenderDeletionRepository offenderDeletionRepository;

    @Override
    @Transactional
    public void deleteOffender(final String offenderNumber) {
        offenderDeletionRepository.deleteOffender(offenderNumber);
    }

    @Override
    public Page<OffenderNumber> getOffenderNomsIds(long offset, long limit) {
        return offenderRepository.listAllOffenders(new PageRequest(offset, limit));
    }
}
