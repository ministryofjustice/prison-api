package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Embeddable
@Builder
@AllArgsConstructor
public class UserCaseloadRoleIdentity implements Serializable {

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "username")
    private String username;

    @Column(name = "caseload_id")
    private String caseload;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final UserCaseloadRoleIdentity that = (UserCaseloadRoleIdentity) o;

        if (!Objects.equals(getRoleId(), that.getRoleId())) return false;
        if (!Objects.equals(getUsername(), that.getUsername())) return false;
        return Objects.equals(getCaseload(), that.getCaseload());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getRoleId());
        result = 31 * result + (Objects.hashCode(getUsername()));
        result = 31 * result + (Objects.hashCode(getCaseload()));
        return result;
    }
}
