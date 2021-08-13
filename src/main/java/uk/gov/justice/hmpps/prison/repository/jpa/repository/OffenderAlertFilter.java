package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAlert;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class OffenderAlertFilter implements Specification<OffenderAlert> {
    private String offenderNo;
    private String alertCodes;
    private String alertTypes;
    private String status;
    private Boolean latestBooking;
    private Long bookingId;
    private LocalDate fromAlertDate;
    private LocalDate toAlertDate;

    @Override
    public Predicate toPredicate(@NotNull final Root<OffenderAlert> root, @NotNull final CriteriaQuery<?> query, @NotNull final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        Optional
            .ofNullable(offenderNo)
            .filter(java.util.function.Predicate.not(String::isBlank))
            .ifPresent(nomisId -> predicateBuilder.add(cb.equal(root
                .get("offenderBooking")
                .get("offender")
                .get("nomsId"), nomisId)));

        Optional
            .of(asList(alertCodes))
            .filter(java.util.function.Predicate.not(List::isEmpty))
            .ifPresent(codes -> predicateBuilder.add(root.get("alertCode").in(codes)));

        Optional
            .of(asList(alertTypes))
            .filter(java.util.function.Predicate.not(List::isEmpty))
            .ifPresent(types -> predicateBuilder.add(root.get("alertType").in(types)));

        Optional.ofNullable(latestBooking)
            .filter(Boolean::booleanValue)
            .ifPresent(notUsed -> predicateBuilder.add(cb.equal(root
            .get("offenderBooking")
            .get("bookingSequence"), 1)));

        Optional.ofNullable(bookingId)
            .ifPresent(id -> predicateBuilder.add(cb.equal(root
                .get("offenderBooking")
                .get("bookingId"), id)));

        Optional
            .ofNullable(status)
            .filter(java.util.function.Predicate.not(String::isBlank))
            .ifPresent(status -> predicateBuilder.add(cb.equal(root
                .get("status"), status)));

        Optional
            .ofNullable(fromAlertDate)
            .ifPresent(date -> predicateBuilder.add(cb.greaterThanOrEqualTo(root
                .get("alertDate"), date)));

        Optional
            .ofNullable(toAlertDate)
            .ifPresent(date -> predicateBuilder.add(cb.lessThanOrEqualTo(root
                .get("alertDate"), date)));

        final var predicates = predicateBuilder.build();
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private List<String> asList(String alertCodes) {
        return Optional
            .ofNullable(alertCodes).
            filter(java.util.function.Predicate.not(String::isBlank))
            .map(codes -> codes.split(","))
            .map(Arrays::asList)
            .orElse(List.of());
    }
}
