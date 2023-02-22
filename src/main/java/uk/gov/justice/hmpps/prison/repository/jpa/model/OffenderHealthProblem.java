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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;


@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_HEALTH_PROBLEMS")
@ToString(of = {"id"})
public class OffenderHealthProblem {

    @Id
    @SequenceGenerator(name = "OFFENDER_HEALTH_PROBLEM_ID", sequenceName = "OFFENDER_HEALTH_PROBLEM_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFFENDER_HEALTH_PROBLEM_ID")
    @Column(name = "OFFENDER_HEALTH_PROBLEM_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID")
    private OffenderBooking offenderBooking;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + HealthProblemType.HEALTH + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "PROBLEM_TYPE", referencedColumnName = "code"))
    })
    private HealthProblemType problemType;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + HealthProblemCode.HEALTH_PBLM + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "PROBLEM_CODE", referencedColumnName = "code"))
    })
    private HealthProblemCode problemCode;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + HealthProblemStatus.HEALTH_STS + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "PROBLEM_STATUS", referencedColumnName = "code"))
    })
    private HealthProblemStatus problemStatus;

    @Column(name = "DESCRIPTION", nullable = true)
    private String commentText;

    @Column(name = "START_DATE", nullable = true)
    private LocalDate startDate;

    @Column(name = "END_DATE", nullable = true)
    private LocalDate endDate;

    @Column(name = "CASELOAD_TYPE", nullable = false)
    private String caseloadType;
}
