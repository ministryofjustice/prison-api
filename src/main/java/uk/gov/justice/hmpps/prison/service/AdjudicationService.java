package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Adjudication;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationOffence;
import uk.gov.justice.hmpps.prison.api.model.adjudications.OffenderAdjudicationHearing;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.repository.AdjudicationsRepository;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;
import uk.gov.justice.hmpps.prison.util.CalcDateRanges;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
public class AdjudicationService {

    private final AdjudicationsRepository repository;
    @Value("${batch.max.size:1000}")
    private final int batchSize;

    public AdjudicationService(
        final AdjudicationsRepository repository,
        @Value("${batch.max.size:1000}") final int batchSize
    ) {
        this.repository = repository;
        this.batchSize = batchSize;
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

    public List<OffenderAdjudicationHearing> findOffenderAdjudicationHearings(final String agencyId,
                                                                              final LocalDate fromDate,
                                                                              final LocalDate toDate,
                                                                              final Set<String> offenderNos,
                                                                              final TimeSlot timeSlot) {
        if (!toDate.isAfter(fromDate)) {
            throw new BadRequestException("The from date must be before the to date.");
        }

        if (ChronoUnit.DAYS.between(fromDate, toDate) > 31) {
            throw new BadRequestException("A maximum of 31 days worth of offender adjudication hearings is allowed.");
        }

        if (offenderNos.isEmpty()) {
            throw new BadRequestException("At least one offender number must be supplied.");
        }

        val hearings = Lists.partition(offenderNos.stream().toList(), batchSize)
            .stream()
            .flatMap(nos -> repository.findOffenderAdjudicationHearings(agencyId, fromDate, toDate, Set.copyOf(nos)).stream())
            .collect(Collectors.toList());

        if (timeSlot != null) {
            return hearings.stream()
                .filter(hearing -> CalcDateRanges.eventStartsInTimeslot(hearing.getStartTime(), timeSlot))
                .collect(toList());
        }

        return hearings;
    }
}
