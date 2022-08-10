package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.OffenderEvent;
import uk.gov.justice.hmpps.prison.api.resource.OffenderEventsController;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderEventsRepository;
import uk.gov.justice.hmpps.prison.service.filters.OffenderEventsFilter;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderEventsTransformer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OffenderEventsService {

    private static final Comparator<uk.gov.justice.hmpps.prison.api.model.OffenderEvent> BY_OFFENDER_EVENT_TIMESTAMP =
            Comparator.comparing(uk.gov.justice.hmpps.prison.api.model.OffenderEvent::getEventDatetime);

    private static final Comparator<uk.gov.justice.hmpps.prison.api.model.OffenderEvent> BY_OFFENDER_EVENT_TIMESTAMP_DESC = BY_OFFENDER_EVENT_TIMESTAMP.reversed();

    private final OffenderEventsTransformer offenderEventsTransformer;
    private final OffenderEventsRepository offenderEventsRepository;
    private final XtagEventsService xtagEventsService;

    @Autowired
    public OffenderEventsService(final OffenderEventsTransformer offenderEventsTransformer,
                                 final OffenderEventsRepository offenderEventsRepository,
                                 final XtagEventsService xtagEventsService) {
        this.offenderEventsTransformer = offenderEventsTransformer;
        this.offenderEventsRepository = offenderEventsRepository;
        this.xtagEventsService = xtagEventsService;
    }

    @PreAuthorize("hasRole('PRISON_OFFENDER_EVENTS')")
    public Optional<List<OffenderEvent>> getEvents(final Optional<LocalDateTime> maybeFrom,
                                                   final Optional<LocalDateTime> maybeTo,
                                                   final Optional<Set<String>> maybeTypeFilter,
                                                   final Optional<OffenderEventsController.SortTypes> maybeSortBy) {
        final var from = fromOrDefault(maybeFrom, maybeTo);
        final var to = toOrDefault(maybeTo, from);

        final var oeFilter = OffenderEventsFilter.builder().from(from).to(to).types(maybeTypeFilter).build();
        return getFilteredOffenderEvents(oeFilter, maybeSortBy);
    }

    @PreAuthorize("hasRole('PRISON_OFFENDER_EVENTS')")
    public Optional<List<OffenderEvent>> getTestEvents(
            final Optional<LocalDateTime> maybeFrom,
            final Optional<LocalDateTime> maybeTo,
            final Optional<Set<String>> maybeTypeFilter,
            final boolean useEnq) {
        final var from = fromOrDefault(maybeFrom, maybeTo);
        final var to = toOrDefault(maybeTo, from);

        final var oeFilter = OffenderEventsFilter.builder().from(from).to(to).types(maybeTypeFilter).build();
        return getTestEvents(oeFilter, useEnq);
    }

    private LocalDateTime toOrDefault(final Optional<LocalDateTime> maybeTo, final LocalDateTime from) {
        return maybeTo.orElse(from.plusDays(1));
    }

    private LocalDateTime fromOrDefault(final Optional<LocalDateTime> maybeFrom, final Optional<LocalDateTime> maybeTo) {
        return maybeFrom.orElse(maybeTo.map(to -> to.minusDays(1)).orElse(LocalDate.now().atStartOfDay()));
    }

    private Optional<List<OffenderEvent>> getFilteredOffenderEvents(final OffenderEventsFilter oeFilter, final Optional<OffenderEventsController.SortTypes> maybeSortBy) {

        final var offenderEvents = Optional.ofNullable(offenderEventsRepository.findAll(oeFilter))
                .map(ev -> ev.stream()
                        .map(offenderEventsTransformer::offenderEventOf)
                        .toList())
                .orElse(Collections.emptyList());

        final var xtagEvents = xtagEventsService.findAll(oeFilter);

        final var typeFilter = oeFilter.getTypes()
                .map(types -> types.stream().map(String::toUpperCase).collect(Collectors.toSet()))
                .orElse(ImmutableSet.of());

        final List<OffenderEvent> allEvents = ImmutableList.<OffenderEvent>builder().addAll(offenderEvents).addAll(xtagEvents).build();

        return Optional.of(allEvents.stream()
                .filter(oe -> typeFilter.isEmpty() || typeFilter.contains(oe.getEventType()))
                .sorted(sortFunctionOf(maybeSortBy))
                .toList());
    }

    private Optional<List<OffenderEvent>> getTestEvents(final OffenderEventsFilter oeFilter, final boolean useEnq) {

        final var offenderEvents = Optional.of(offenderEventsRepository.findAll(oeFilter))
                .map(ev -> ev.stream()
                        .map(offenderEventsTransformer::offenderEventOf)
                        .toList())
                .orElse(Collections.emptyList());

        final var xtagEvents = xtagEventsService.findTest(oeFilter, useEnq);

        final var typeFilter = oeFilter.getTypes()
                .map(types -> types.stream().map(String::toUpperCase).collect(Collectors.toSet()))
                .orElse(ImmutableSet.of());

        final List<OffenderEvent> allEvents = ImmutableList.<OffenderEvent>builder().addAll(offenderEvents).addAll(xtagEvents).build();

        return Optional.of(allEvents.stream()
                .filter(oe -> typeFilter.isEmpty() || typeFilter.contains(oe.getEventType()))
                .toList());
    }

    private Comparator<? super OffenderEvent> sortFunctionOf(final Optional<OffenderEventsController.SortTypes> maybeSortBy) {
        return maybeSortBy.filter(sortTypes -> sortTypes.equals(OffenderEventsController.SortTypes.TIMESTAMP_ASC))
                .map(sortTypes -> BY_OFFENDER_EVENT_TIMESTAMP)
                .orElse(BY_OFFENDER_EVENT_TIMESTAMP_DESC);
    }
}
