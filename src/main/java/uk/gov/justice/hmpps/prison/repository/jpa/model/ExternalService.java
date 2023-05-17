package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Objects;

import static org.hibernate.internal.util.collections.CollectionHelper.listOf;

@AllArgsConstructor
@Builder
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "EXTERNAL_SERVICES")
public class ExternalService extends AuditableEntity {

    @Id
    @Column(nullable = false)
    private String serviceName;

    @Column
    private String description;

    @OneToMany(mappedBy = "id.externalService", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<ServiceAgencySwitch> serviceAgencySwitches = listOf();

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final ExternalService other = (ExternalService) o;

        return Objects.equals(getServiceName(), other.getServiceName());
    }


    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

}
