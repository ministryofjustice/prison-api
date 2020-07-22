package uk.gov.justice.hmpps.nomis.datacompliance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderImageUpdateRepository;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OffenderImageUpdateService {

    private final OffenderImageUpdateRepository repository;

    public Page<OffenderNumber> getOffendersWithImagesCapturedBetween(final LocalDateTime start,
                                                                      final LocalDateTime end,
                                                                      final Pageable pageable) {
        return repository.getOffendersWithImagesCapturedBetween(start, end, pageable)
                .map(offender -> new OffenderNumber(offender.getOffenderNumber()));
    }
}
