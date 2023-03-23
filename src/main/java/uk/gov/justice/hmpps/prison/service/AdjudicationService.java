package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Adjudication;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationDetail;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationOffence;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationSummary;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Award;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Hearing;
import uk.gov.justice.hmpps.prison.api.model.adjudications.OffenderAdjudicationHearing;
import uk.gov.justice.hmpps.prison.api.model.adjudications.ProvenAdjudicationSummary;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.repository.AdjudicationsRepository;
import uk.gov.justice.hmpps.prison.repository.AgencyRepository;
import uk.gov.justice.hmpps.prison.repository.LocationRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;
import uk.gov.justice.hmpps.prison.util.CalcDateRanges;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.hmpps.prison.repository.support.StatusFilter.ALL;

@Service
@Transactional(readOnly = true)
public class AdjudicationService {

    private final AdjudicationsRepository repository;
    private final AgencyRepository agencyRepository;
    private final LocationRepository locationRepository;
    @Value("${batch.max.size:1000}")
    private final int batchSize;

    public AdjudicationService(
        final AdjudicationsRepository repository,
        final AgencyRepository agencyRepository,
        LocationRepository locationRepository,
        @Value("${batch.max.size:1000}") final int batchSize
    ) {
        this.repository = repository;
        this.agencyRepository = agencyRepository;
        this.locationRepository = locationRepository;
        this.batchSize = batchSize;
    }

    @Value("${api.cutoff.adjudication.months:3}")
    private int adjudicationCutoffDefault;
    @Value("${api.cutoff.award.months:0}")
    private int awardCutoffDefault;

    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public AdjudicationDetail findAdjudication(final String offenderNo, final long adjudicationNo) {
        return repository.findAdjudicationDetails(offenderNo, adjudicationNo)
            .map(this::enrich)
            .orElseThrow(EntityNotFoundException.withId(adjudicationNo));
    }

    private AdjudicationDetail enrich(final AdjudicationDetail detail) {

        val locationFinder = locationFinder();
        val establishmentFinder = establishmentFinder();

        val hearings = detail.getHearings().stream()
            .map(hearing -> {
                val location = locationFinder.apply(hearing.getInternalLocationId());
                val establishment = establishmentFinder.apply(location.getAgencyId());
                return enrich(hearing, location, establishment);
            })
            .collect(toList());

        val location = locationFinder.apply(detail.getInternalLocationId());
        val establishment = establishmentFinder.apply(detail.getAgencyId());

        return detail.toBuilder()
            .clearHearings()
            .establishment(establishment)
            .interiorLocation(getInteriorLocationDescription(location))
            .hearings(hearings)
            .build();
    }


    private Hearing enrich(final Hearing hearing, final Location location, final String establishment) {

        return hearing.toBuilder()
            .establishment(establishment)
            .location(getInteriorLocationDescription(location))
            .build();
    }

    private Function<Long, Location> locationFinder() {
        val locations = new HashMap<Long, Location>();
        return locationId -> locations.computeIfAbsent(locationId, id ->
            locationRepository.findLocation(id, ALL)
                .orElseThrow(EntityNotFoundException.withId(id)));
    }

    private Function<String, String> establishmentFinder() {
        val establishments = new HashMap<String, String>();
        return agencyId -> establishments.computeIfAbsent(agencyId, id ->
            agencyRepository.findAgency(id, ALL, null)
                .map(agency -> LocationProcessor.formatLocation(agency.getDescription()))
                .orElseThrow(EntityNotFoundException.withId(agencyId)));
    }

    private String getInteriorLocationDescription(final Location location) {
        val processedLocation = LocationProcessor.processLocation(location, true, false);
        return processedLocation.getDescription();
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

    /**
     * Get awards that have not expired, i.e. the end date is today or later, and
     * count proved adjudications which expired on or later than the from date.
     */
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public AdjudicationSummary getAdjudicationSummary(final Long bookingId, final LocalDate awardCutoffDateParam,
                                                      final LocalDate adjudicationCutoffDateParam) {
        val list = repository.findAwards(bookingId);
        int adjudicationCount = getAdjudicationCount(awardCutoffDateParam, adjudicationCutoffDateParam, list);
        return AdjudicationSummary.builder().awards(list).adjudicationCount(adjudicationCount).build();
    }

    private int getAdjudicationCount(LocalDate awardCutoffDateParam, LocalDate adjudicationCutoffDateParam, List<Award> list) {
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
        return adjudicationCount;
    }

    public List<ProvenAdjudicationSummary> getProvenAdjudications(final List<Long> bookingIds,
                                                                  final LocalDate cutoffDateParam) {
        return repository.findAwardsForMultipleBookings(bookingIds).stream()
            .collect(groupingBy(Award::getBookingId))
            .entrySet().stream().map(e -> {

                    int provenAdjudicationCount = getAdjudicationCount(null, cutoffDateParam, e.getValue());
                    return ProvenAdjudicationSummary.builder().bookingId(e.getKey()).provenAdjudicationCount(provenAdjudicationCount).build();
                }
            ).toList();
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
