package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;


@Entity
@Table(name = "CASELOADS")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString(of = {"id", "name"})
public class Caseload extends AuditableEntity {

    @Id
    @Column(name = "CASELOAD_ID", nullable = false)
    private String id;

    @Column(name = "DESCRIPTION", nullable = false)
    private String name;

    @Column(name = "CASELOAD_TYPE", nullable = false)
    private String type;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    @Convert(converter = YesNoConverter.class)
    private boolean active;

    @Column(name = "DEACTIVATION_DATE", nullable = false)
    private LocalDate deactivationDate;

    @Column(name = "CASELOAD_FUNCTION", nullable = false)
    private String caseloadFunction;


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final Caseload caseload = (Caseload) o;

        return Objects.equals(getId(), caseload.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
