package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CourtEventFilter implements Specification<CourtEvent> {

    private Long bookingId;

    private LocalDate fromDate;

    private LocalDate toDate;


    public Predicate toPredicate(final Root<CourtEvent> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        if (bookingId != null) {
            predicateBuilder.add(cb.equal(root.get("offenderBooking"), bookingId));
        }

        if (fromDate != null) {
            predicateBuilder.add(cb.greaterThanOrEqualTo(root.get("eventDate"), fromDate));
        }

        if (toDate != null) {
            predicateBuilder.add(cb.lessThanOrEqualTo(root.get("eventDate"), toDate));
        }

        return cb.and(predicateBuilder.build().toArray(new Predicate[0]));
    }
}
