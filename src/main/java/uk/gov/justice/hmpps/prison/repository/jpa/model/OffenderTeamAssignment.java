package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTeamAssignmentFunction.FUNCTION_DOMAIN;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "OFFENDER_TEAM_ASSIGNMENTS")
@Entity
public class OffenderTeamAssignment extends AuditableEntity {

    public static final String AUTO_TRANSFER_FROM_COURT_OR_TAP = "AUTO_TRN";
    @EmbeddedId
    private PK id;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class PK implements Serializable {
        @ManyToOne(optional = false, fetch = FetchType.LAZY)
        @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
        private OffenderBooking offenderBooking;

        @Column(name = "FUNCTION_TYPE", nullable = false)
        private String functionTypeCode;
    }

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + FUNCTION_DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "FUNCTION_TYPE", referencedColumnName = "code", nullable = false, insertable = false, updatable = false))
    })
    private OffenderTeamAssignmentFunction functionType;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "TEAM_ID", nullable = false)
    @Exclude
    private Team team;

    @Column(name = "ASSIGNMENT_DATE")
    private LocalDate assignmentDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        OffenderTeamAssignment that = (OffenderTeamAssignment) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
