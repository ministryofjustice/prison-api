package net.syscon.elite.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.val;
import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.adjudications.Adjudication;
import net.syscon.elite.api.model.adjudications.AdjudicationDetail;
import net.syscon.elite.api.model.adjudications.AdjudicationOffence;
import net.syscon.elite.api.model.adjudications.AdjudicationSummary;
import net.syscon.elite.api.model.adjudications.Award;
import net.syscon.elite.api.model.adjudications.Hearing;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.AdjudicationsRepository;
import net.syscon.elite.repository.LocationRepository;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.AdjudicationSearchCriteria;
import net.syscon.elite.service.AdjudicationService;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.support.LocationProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static net.syscon.elite.repository.LocationRepository.LocationFilter.ALL;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdjudicationServiceImpl implements AdjudicationService {

    private final AdjudicationsRepository repository;
    private final LocationRepository locationRepository;
    private final BookingService bookingService;

    @Value("${api.cutoff.adjudication.months:3}")
    private int adjudicationCutoffDefault;
    @Value("${api.cutoff.award.months:0}")
    private int awardCutoffDefault;

    @Override
    public AdjudicationDetail findAdjudication(final String offenderNo, final long adjudicationNo) {
        bookingService.verifyCanViewLatestBooking(offenderNo);
        return repository.findAdjudicationDetails(offenderNo, adjudicationNo)
                .map(this::withFormatLocation)
                .orElseThrow(EntityNotFoundException.withId(adjudicationNo));
    }

    private AdjudicationDetail withFormatLocation(final AdjudicationDetail detail) {

        val hearings = hearingWithDescriptions(detail.getHearings());

        return detail.toBuilder()
                .clearHearings()
                .establishment(LocationProcessor.formatLocation(detail.getEstablishment()))
                .interiorLocation(getInteriorLocationDescription(detail.getInternalLocationId()))
                .hearings(hearings)
                .build();
    }

    private List<Hearing> hearingWithDescriptions(final List<Hearing> hearings) {

        return hearings.stream()
                .map(hearing -> hearing.toBuilder()
                        .location(getInteriorLocationDescription(hearing.getInternalLocationId()))
                        .build())
                .collect(toList());
    }

    private String getInteriorLocationDescription(final long id) {
        val location = locationRepository.findLocation(id, ALL).orElseThrow(EntityNotFoundException.withId(id));
        val processedLocation = LocationProcessor.processLocation(location, true);
        return processedLocation.getDescription();
    }

    @Override
    public Page<Adjudication> findAdjudications(final AdjudicationSearchCriteria criteria) {
        bookingService.verifyCanViewLatestBooking(criteria.getOffenderNumber());
        return repository.findAdjudications(criteria);
    }

    @Override
    public List<AdjudicationOffence> findAdjudicationsOffences(final String offenderNo) {
        return repository.findAdjudicationOffences(offenderNo);
    }

    @Override
    public List<Agency> findAdjudicationAgencies(final String offenderNo) {
        return repository.findAdjudicationAgencies(offenderNo).stream()
                .map(agency -> agency.toBuilder()
                        .description(LocationProcessor.formatLocation(agency.getDescription()))
                        .build())
                .collect(toList());
    }

    /**
     * Get awards that have not expired, i.e. the end date is today or later, and
     * count proved adjudications which expired on or later than the from date.
     */
    @Override
    @VerifyBookingAccess
    public AdjudicationSummary getAdjudicationSummary(final Long bookingId, final LocalDate awardCutoffDateParam,
                                                      final LocalDate adjudicationCutoffDateParam) {
        val list = repository.findAwards(bookingId);
        val today = LocalDate.now();
        var awardCutoffDate = awardCutoffDateParam;
        if (awardCutoffDate == null) {
            awardCutoffDate = today.plus(-awardCutoffDefault, ChronoUnit.MONTHS);
        }
        var adjudicationCutoffDate = adjudicationCutoffDateParam;
        if (adjudicationCutoffDate == null) {
            adjudicationCutoffDate = today.plus(-adjudicationCutoffDefault, ChronoUnit.MONTHS);
        }
        val iterator = list.iterator();
        var adjudicationCount = 0;
        Award previous = null;

        while (iterator.hasNext()) {
            val current = iterator.next();
            val endDate = calculateEndDate(current);

            if (!adjudicationCutoffDate.isAfter(endDate) && changed(previous, current)) {
                adjudicationCount++;
                previous = current;
            }
            if (awardCutoffDate.isAfter(endDate)) {
                iterator.remove();
            }
        }
        return AdjudicationSummary.builder().awards(list).adjudicationCount(adjudicationCount).build();
    }

    private LocalDate calculateEndDate(final Award award) {
        var endDate = award.getEffectiveDate();
        if (award.getMonths() != null) {
            endDate = endDate.plusMonths(award.getMonths());
        }
        if (award.getDays() != null) {
            endDate = endDate.plusDays(award.getDays());
        }
        return endDate;
    }

    private boolean changed(final Award previous, final Award current) {
        return previous == null || !Objects.equals(previous.getHearingId(), current.getHearingId());
        // Note we only consider the hearing id, not the sequence number as we only
        // expect at most one proved adjudication in the sequence list
    }
}
