package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.hmpps.prison.repository.jpa.model.VisitInformation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class VisitInformationFilter implements Specification<VisitInformation> {

    private Long bookingId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String visitType;
    private String visitStatus;
    private String prisonId;

    public Predicate toPredicate(final Root<VisitInformation> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        if (bookingId != null) {
            predicateBuilder.add(cb.equal(root.get("bookingId"), bookingId));
        }

        if (fromDate != null) {
            predicateBuilder.add(cb.greaterThanOrEqualTo(root.get("startTime"), fromDate.atStartOfDay()));
        }

        if (toDate != null) {
            predicateBuilder.add(cb.lessThanOrEqualTo(root.get("startTime"), toDate.atTime(LocalTime.MAX)));
        }

        if (visitType != null) {
            predicateBuilder.add(cb.equal(root.get("visitType"), visitType));
        }

        if (visitStatus != null) {
            predicateBuilder.add(cb.equal(root.get("visitStatus"), visitStatus));
        }

        if (StringUtils.isNotBlank(prisonId)) {
            predicateBuilder.add(cb.equal(root.get("prisonId"), prisonId));
        }

        return cb.and(predicateBuilder.build().toArray(new Predicate[0]));
    }
}
