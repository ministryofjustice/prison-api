package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AgencyLocationFilter implements Specification<AgencyLocation>  {
    private String id;
    @Builder.Default
    private ActiveFlag activeFlag = ActiveFlag.Y;
    private String type;
    @Builder.Default
    private List<String> excludedAgencies = List.of("OUT", "TRN");


    public Predicate toPredicate(final Root<AgencyLocation> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        if (StringUtils.isNotBlank(id)) {
            predicateBuilder.add(cb.equal(root.get("id"), id));
        }

        if (activeFlag != null) {
            predicateBuilder.add(cb.equal(root.get("activeFlag"), activeFlag));
        }

        if (StringUtils.isNotBlank(type)) {
            predicateBuilder.add(cb.equal(root.get("type").get("code"), type));
        }

        if (StringUtils.isBlank(id) && excludedAgencies != null && !excludedAgencies.isEmpty()) {
            predicateBuilder.add(cb.not(root.get("id").in(excludedAgencies)));
        }

        final var predicates = predicateBuilder.build();
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
