package uk.gov.justice.hmpps.prison.service.filters;

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderEvent;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Builder(toBuilder = true)
@Value
@EqualsAndHashCode
public class OffenderEventsFilter implements Specification<OffenderEvent> {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Builder.Default
    private LocalDateTime from = null;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Builder.Default
    private LocalDateTime to = null;

    @Builder.Default
    private Optional<Set<String>> types = Optional.empty();

    @Override
    public Predicate toPredicate(final Root<OffenderEvent> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        var tsFrom = Timestamp.valueOf(from);
        var tsTo = Timestamp.valueOf(to);

        if (tsFrom.after(tsTo)) {
            final var tsTemp = tsFrom;
            tsFrom = tsTo;
            tsTo = tsTemp;
        }

        final Root alertsTable = root;
        final var eventTimestamp = alertsTable.get("eventTimestamp");
        final Path eventType = root.get("eventType");

        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        predicateBuilder
                .add(cb.greaterThanOrEqualTo(eventTimestamp, tsFrom))
                .add(cb.lessThanOrEqualTo(eventTimestamp, tsTo));

        types.ifPresent(filter -> {
            filter.add("CASE_NOTE");
            predicateBuilder.add(valueInList(cb, eventType, filter));
        });

        final var predicates = predicateBuilder.build();

        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }

    private Predicate valueInList(final CriteriaBuilder cb, final Path eventType, final Set<String> list) {
        final var inTypes = cb.in(eventType);
        list.stream().map(String::toUpperCase).forEach(inTypes::value);
        return inTypes;
    }

}
