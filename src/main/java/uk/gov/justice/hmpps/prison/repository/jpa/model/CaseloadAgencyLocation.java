package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "CASELOAD_AGENCY_LOCATIONS")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class CaseloadAgencyLocation extends AuditableEntity {

    @EmbeddedId
    private AgencyLocationCaseloadId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CASELOAD_ID", updatable = false, insertable = false)
    @Exclude
    private Caseload caseload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID", updatable = false, insertable = false)
    @Exclude
    private AgencyLocation agencyLocation;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final CaseloadAgencyLocation that = (CaseloadAgencyLocation) o;

        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
