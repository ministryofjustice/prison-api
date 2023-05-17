package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@AllArgsConstructor
@Builder
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "SERVICE_AGENCY_SWITCHES")
public class ServiceAgencySwitch extends AuditableEntity {

    @EmbeddedId
    private ServiceAgencySwitchId id;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final ServiceAgencySwitch other = (ServiceAgencySwitch) o;

        return Objects.equals(getId().getExternalService().getServiceName(), other.getId().getExternalService().getServiceName())
            && Objects.equals(getId().getAgencyLocation().getId(), other.getId().getAgencyLocation().getId());
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }
}
