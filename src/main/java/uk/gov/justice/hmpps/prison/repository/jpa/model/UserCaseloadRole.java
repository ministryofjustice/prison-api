package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import uk.gov.justice.hmpps.prison.api.model.UserRole;
import uk.gov.justice.hmpps.prison.api.model.UserRole.UserRoleBuilder;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "USER_CASELOAD_ROLES")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class UserCaseloadRole implements Serializable {

    @EmbeddedId
    private UserCaseloadRoleIdentity id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ROLE_ID", updatable = false, insertable = false)
    @Exclude
    private Role role;

    public UserRole transformWithoutCaseload() {
        return builderForUserRole().roleCode(getRole().getCode()).build();
    }

    public UserRole transform() {
        return builderForUserRole().build();
    }

    private UserRoleBuilder builderForUserRole() {
        return UserRole.builder()
            .roleId(getId().getRoleId())
            .roleCode(getId().getCaseload() + "_" + getRole().getCode())
            .caseloadId(getId().getCaseload())
            .roleName(getRole().getName())
            .parentRoleCode(getRole().getParentCode());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final UserCaseloadRole that = (UserCaseloadRole) o;

        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
