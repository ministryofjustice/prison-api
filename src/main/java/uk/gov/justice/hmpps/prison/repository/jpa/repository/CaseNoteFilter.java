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
import uk.gov.justice.hmpps.prison.service.BadRequestException;
import uk.gov.justice.hmpps.prison.service.QueryParamHelper;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder()
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CaseNoteFilter implements Specification<OffenderCaseNote> {

    private Long bookingId;
    private String prisonId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private String subType;
    private List<String> typesSubTypes;

    public static CaseNoteFilterBuilder builder() {
        return new CustomCaseNoteFilterBuilder();
    }

    static class CustomCaseNoteFilterBuilder extends CaseNoteFilterBuilder {
        @Override
       public CaseNoteFilter build() {
            if(super.type != null && super.typesSubTypes != null){
                throw new BadRequestException("Both type and typesAndSubTypes are set, please only use one to filter.");
            }

            return super.build();
        }
    }

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

        if(typesSubTypes != null && !typesSubTypes.isEmpty()){
           predicateBuilder.add(getTypesPredicate(root, cb));
        }

        final var predicates = predicateBuilder.build();
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate getTypesPredicate(final Root<OffenderCaseNote> root, final CriteriaBuilder cb) {
       final var typesAndSubTypes = QueryParamHelper.splitTypes(typesSubTypes);
       final var typesPredicates= typesAndSubTypes.entrySet()
            .stream()
            .map(typeSet -> {
                if(typeSet.getValue().isEmpty()){
                    return cb.equal(root.get("type").get("code"), typeSet.getKey());
                }
                else{
                    return getSubtypesPredicate(root, cb, typeSet.getKey() ,typeSet.getValue());
                }
            })
            .toList();
        return cb.or(typesPredicates.toArray(new Predicate[0]));
    }

    private Predicate getSubtypesPredicate(final Root<OffenderCaseNote> root, final CriteriaBuilder cb, final String type, final List<String> subTypes) {
        final ImmutableList.Builder<Predicate>  typePredicateorBuilder = ImmutableList.builder();
        typePredicateorBuilder.add(cb.equal(root.get("type").get("code"), type));

        final var inTypes = cb.in(root.get("subType").get("code"));
        subTypes.forEach(inTypes::value);
        typePredicateorBuilder.add(inTypes);

        final var typePredicates = typePredicateorBuilder.build();
        return cb.and(typePredicates.toArray(new Predicate[0]));
    }
}
