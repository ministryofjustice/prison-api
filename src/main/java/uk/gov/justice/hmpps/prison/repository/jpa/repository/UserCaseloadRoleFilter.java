package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.justice.hmpps.prison.repository.jpa.model.UserCaseloadRole;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.Optional;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UserCaseloadRoleFilter implements Specification<UserCaseloadRole> {

    private String username;
    private String roleCode;
    private String caseload;
    private String excludeRoleFunction;

    @Override
    public Predicate toPredicate(@NotNull final Root<UserCaseloadRole> root, @NotNull final CriteriaQuery<?> query, @NotNull final CriteriaBuilder cb) {
        final ImmutableList.Builder<Predicate> predicateBuilder = ImmutableList.builder();

        Optional.ofNullable(username)
            .ifPresent(username -> predicateBuilder.add(cb.equal(root
                .get("id").get("username"), username)));

        Optional.ofNullable(caseload)
            .ifPresent(caseload -> predicateBuilder.add(cb.equal(root
                .get("id").get("caseload"), caseload)));

        Optional.ofNullable(roleCode)
            .ifPresent(roleCode -> predicateBuilder.add(cb.equal(root
                .get("role").get("code"), roleCode)));

        Optional.ofNullable(excludeRoleFunction)
            .ifPresent(roleFunction -> predicateBuilder.add(cb.notEqual(root
                .get("role").get("roleFunction"), roleFunction)));

        final var predicates = predicateBuilder.build();
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
