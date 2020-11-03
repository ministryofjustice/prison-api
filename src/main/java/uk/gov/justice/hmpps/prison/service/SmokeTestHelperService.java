package uk.gov.justice.hmpps.prison.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;

@Service
public class SmokeTestHelperService {
    @Transactional
    @HasWriteScope
    @PreAuthorize("hasRole('SMOKE_TEST')")
    public void imprisonmentDataSetup(String offenderNo) {

    }
}
