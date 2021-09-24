package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseloadAgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OffenderBookingFilter implements Specification<OffenderBooking> {
    private List<String> offenderNos;
    private String prisonId;
    private List<Long> bookingIds;
    private Boolean active;
    private Integer bookingSequence;
    private List<String> caseloadIds;

    @Override
    public Predicate toPredicate(@NotNull final Root<OffenderBooking> root, @NotNull final CriteriaQuery<?> query, @NotNull final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        Optional.ofNullable(active)
            .ifPresent(activeBooking -> predicateBuilder.add(cb.equal(root
                .get("active"), activeBooking)));

        Optional.ofNullable(bookingSequence)
            .ifPresent(seq -> predicateBuilder.add(cb.equal(root
                .get("bookingSequence"), seq)));

        Optional.ofNullable(prisonId)
            .ifPresent(id -> predicateBuilder.add(cb.equal(root
                .get("location").get("id"), id)));

        Optional
            .ofNullable(offenderNos)
            .filter(java.util.function.Predicate.not(List::isEmpty))
            .ifPresent(nomsId -> predicateBuilder.add(root
                .get("offender")
                .get("nomsId").in(nomsId)));

        Optional
            .ofNullable(bookingIds)
            .filter(java.util.function.Predicate.not(List::isEmpty))
            .ifPresent(id -> predicateBuilder.add(root
                .get("bookingId").in(id)));

        Optional
            .ofNullable(caseloadIds)
            .filter(java.util.function.Predicate.not(List::isEmpty))
            .ifPresent(caseloads -> {
                final var subquery = query.subquery(CaseloadAgencyLocation.class);
                final var subqueryRoot = subquery.from(CaseloadAgencyLocation.class);
                subquery.select(subqueryRoot);
                final ImmutableList.Builder<Predicate> builder = ImmutableList.builder();
                builder.add(subqueryRoot.get("id").get("caseload").in(caseloads));
                builder.add(cb.equal(root.get("location").get("id"), subqueryRoot.get("id").get("id")));
                predicateBuilder.add(cb.exists(subquery.select(subqueryRoot).where(builder.build().toArray(new Predicate[0]))));
            });

        final var predicates = predicateBuilder.build();
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
