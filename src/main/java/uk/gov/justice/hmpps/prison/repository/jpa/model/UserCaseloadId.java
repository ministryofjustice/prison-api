package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Embeddable
@Builder
@AllArgsConstructor
public class UserCaseloadId implements Serializable {

    @Column(name = "username")
    private String username;

    @Column(name = "caseload_id")
    private String caseload;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final UserCaseloadId that = (UserCaseloadId) o;

        if (!Objects.equals(username, that.username)) return false;
        return Objects.equals(caseload, that.caseload);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(username);
        result = 31 * result + (Objects.hashCode(caseload));
        return result;
    }
}
