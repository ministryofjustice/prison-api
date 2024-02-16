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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    private Map<String, List<String>> types;

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

        if(types!=null&&!types.isEmpty()){
            final ImmutableList.Builder<Predicate> typesPredicateorBuilder = ImmutableList.builder();
            types.forEach((String type, List<String> subTypes)->{

                if(subTypes==null||subTypes.isEmpty()){
                    typesPredicateorBuilder.add(cb.equal(root.get("type").get("code"), type));
                }
                else{

                    final ImmutableList.Builder<Predicate>  typePredicateorBuilder = ImmutableList.builder();
                    typePredicateorBuilder.add(cb.equal(root.get("type").get("code"), type));

                    final var inTypes = cb.in(root.get("subType").get("code"));
                    subTypes.forEach(inTypes::value);
                    typePredicateorBuilder.add(inTypes);

                    final var typePredicates = typePredicateorBuilder.build();
                    typesPredicateorBuilder.add(cb.and(typePredicates.toArray(new Predicate[0])));

                }

            });
           final var typesPredicates=  typesPredicateorBuilder.build();
           predicateBuilder.add(cb.or(typesPredicates.toArray(new Predicate[0])));
        }

        final var predicates = predicateBuilder.build();
        return cb.and(predicates.toArray(new Predicate[0]));


    }
}
