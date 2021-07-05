package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CaseNoteFilter implements Specification<OffenderCaseNote> {

    private Long bookingId;
    private String type;
    private String subType;
    private String prisonId;
    private LocalDate startDate;
    private LocalDate endDate;

    @Override
    public Predicate toPredicate(@NotNull final Root<OffenderCaseNote> root, @NotNull final CriteriaQuery<?> query, @NotNull final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        if (bookingId != null) {
            predicateBuilder.add(cb.equal(root.get("offenderBooking").get("bookingId"), bookingId));
        }

        if (StringUtils.isNotBlank(type)) {
            predicateBuilder.add(cb.equal(root.get("type").get("code"), type));
        }

        if (StringUtils.isNotBlank(subType)) {
            predicateBuilder.add(cb.equal(root.get("subType").get("code"), subType));
        }

        if (StringUtils.isNotBlank(prisonId)) {
            predicateBuilder.add(cb.equal(root.get("agencyLocation").get("id"), prisonId));
        }

        if (startDate != null) {
            predicateBuilder.add(cb.greaterThanOrEqualTo(root.get("occurrenceDate"), startDate));
        }

        if (endDate != null) {
            predicateBuilder.add(cb.lessThan(root.get("occurrenceDate"), endDate.plusDays(1)));
        }

        final var predicates = predicateBuilder.build();
        return cb.and(predicates.toArray(new Predicate[0]));


    }
}
