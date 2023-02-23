package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "OMS_ROLES")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class Role implements Serializable {

    @Id()
    @Column(name = "ROLE_ID", nullable = false)
    private Long id;

    @Column(name = "ROLE_CODE", nullable = false, unique = true)
    private String code;

    @Column(name = "ROLE_NAME", nullable = false)
    private String name;

    @Column(name = "PARENT_ROLE_CODE")
    private String parentCode;

    @Column(name = "ROLE_FUNCTION")
    private String roleFunction;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final Role role = (Role) o;

        return Objects.equals(getId(), role.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
