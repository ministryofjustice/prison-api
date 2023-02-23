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
public class AgencyLocationCaseloadId implements Serializable {

    @Column(name = "AGY_LOC_ID")
    private String id;

    @Column(name = "CASELOAD_ID")
    private String caseload;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final AgencyLocationCaseloadId that = (AgencyLocationCaseloadId) o;

        if (!Objects.equals(getId(), that.getId())) return false;
        return Objects.equals(getCaseload(), that.getCaseload());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getId());
        result = 31 * result + (Objects.hashCode(getCaseload()));
        return result;
    }
}
