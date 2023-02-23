package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AgencyLocationFilter implements Specification<AgencyLocation> {
    private String id;

    private Boolean active;

    private String type;

    @Builder.Default
    private final List<String> excludedAgencies = List.of("OUT", "TRN");

    private List<String> courtTypes;

    public Predicate toPredicate(final Root<AgencyLocation> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        if (StringUtils.isNotBlank(id)) {
            predicateBuilder.add(cb.equal(root.get("id"), id));
        }

        if (active != null) {
            predicateBuilder.add(cb.equal(root.get("active"), active));
        }

        if (StringUtils.isNotBlank(type)) {
            predicateBuilder.add(cb.equal(root.get("type").get("code"), type));
        }

        if (StringUtils.isBlank(id) && excludedAgencies != null && !excludedAgencies.isEmpty()) {
            predicateBuilder.add(cb.not(root.get("id").in(excludedAgencies)));
        }

        if (courtTypes != null && courtTypes.size() > 0) {
            predicateBuilder.add(root.get("courtType").get("code").in(courtTypes));
        }

        final var predicates = predicateBuilder.build();
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
