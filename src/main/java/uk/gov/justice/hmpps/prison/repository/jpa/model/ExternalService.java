package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "EXTERNAL_SERVICES")
public class ExternalService extends AuditableEntity {

    @Id
    @Column(nullable = false)
    private String serviceName;

    @Column
    private String description;

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
