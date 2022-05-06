package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.InstitutionArea.INSTITUTION_AREA_DOMAIN;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.TeamCategory.TEAM_CATEGORY_DOMAIN;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "TEAMS")
@ToString(of = {"id", "code", "description"})
@Entity
public class Team extends AuditableEntity {
    @Id
    @SequenceGenerator(name = "TEAM_ID", sequenceName = "TEAM_ID", allocationSize = 1)
    @GeneratedValue(generator = "TEAM_ID")
    @Column(name = "TEAM_ID")
    private Long id;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "TEAM_CODE")
    private String code;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + TEAM_CATEGORY_DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "CATEGORY", referencedColumnName = "code", nullable = false))
    })
    private TeamCategory category;

    @Column(name = "LIST_SEQ")
    private Long listSequence;

    @Column(name = "ACTIVE_FLAG")
    @Type(type="yes_no")
    private boolean active;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + INSTITUTION_AREA_DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "AREA_CODE", referencedColumnName = "code", nullable = false))
    })
    private InstitutionArea area;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    private AgencyLocation location;

    @Column(name = "QUEUE_CLUSTER_ID")
    private Long queueClusterId;


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
