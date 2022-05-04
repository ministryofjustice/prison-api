package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "TEAMS")
@ToString(of = {"id", "code", "description"})
@Entity
/*
 * NB: This entity is only partially mapped - just enough to allow workflow tasks to be called
 */
public class Team extends AuditableEntity {
    @Id
    @Column(name = "TEAM_ID")
    private Long id;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "TEAM_CODE")
    private String code;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Team that = (Team) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
