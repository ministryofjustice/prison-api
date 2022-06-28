package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDate;

import static org.hibernate.annotations.NotFoundAction.IGNORE;


@Data
@EqualsAndHashCode(callSuper=false, exclude = "charges")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_HEALTH_PROBLEMS")
@ToString(of = {"id"})
public class OffenderHealthProblem {

    @Id
    @Column(name = "OFFENDER_HEALTH_PROBLEM_ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID")
    private OffenderBooking offenderBooking;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + HealthProblemType.HEALTH + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "PROBLEM_TYPE", referencedColumnName = "code"))
    })
    private HealthProblemType problemType;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + HealthProblemCode.HEALTH_PBLM + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "PROBLEM_CODE", referencedColumnName = "code"))
    })
    private HealthProblemCode problemCode;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + HealthProblemStatus.HEALTH_STS + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "PROBLEM_STATUS", referencedColumnName = "code"))
    })
    private HealthProblemStatus problemStatus;

    @Column(name = "DESCRIPTION", nullable = true)
    private String description;

    @Column(name = "START_DATE", nullable = true)
    private LocalDate startDate;

    @Column(name = "END_DATE", nullable = true)
    private LocalDate endDate;

    @Column(name = "CASELOAD_TYPE", nullable = false)
    private String caseloadType;
}
