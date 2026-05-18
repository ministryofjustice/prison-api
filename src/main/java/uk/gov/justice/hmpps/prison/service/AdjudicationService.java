package uk.gov.justice.hmpps.prison.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Adjudication;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationOffence;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.AdjudicationsRepository;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
public class AdjudicationService {

    private final AdjudicationsRepository repository;

    public AdjudicationService(
        final AdjudicationsRepository repository
    ) {
        this.repository = repository;
    }

    public Page<Adjudication> findAdjudications(final AdjudicationSearchCriteria criteria) {
        return repository.findAdjudications(criteria);
    }

    public List<AdjudicationOffence> findAdjudicationsOffences(final String offenderNo) {
        return repository.findAdjudicationOffences(offenderNo);
    }

    public List<Agency> findAdjudicationAgencies(final String offenderNo) {
        return repository.findAdjudicationAgencies(offenderNo).stream()
            .map(agency -> agency.toBuilder()
                .description(LocationProcessor.formatLocation(agency.getDescription()))
                .build())
            .collect(toList());
    }
}
