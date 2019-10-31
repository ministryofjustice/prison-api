package net.syscon.elite.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.repository.OffenderDeletionRepository;
import net.syscon.elite.service.OffenderDeletionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OffenderDeletionServiceImpl implements OffenderDeletionService {

    private final OffenderDeletionRepository offenderDeletionRepository;

    @Override
    @Transactional
    public void deleteOffender(final String offenderNumber) {
        offenderDeletionRepository.deleteOffender(offenderNumber);
    }
}
