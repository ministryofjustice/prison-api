package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.Where;
import uk.gov.justice.hmpps.prison.repository.converter.YesNoToBooleanConverter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "OFFENDER_EMPLOYMENTS")
public class OffenderEmployment {

    @EmbeddedId
    private PK id;

    @Column(name = "EMPLOYMENT_DATE")
    private LocalDate startDate;

    @Column(name = "TERMINATION_DATE")
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + EmploymentStatus.DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "EMPLOYMENT_POST_CODE", referencedColumnName = "code"))
    })
    private EmploymentStatus postType;

    @Column(name = "EMPLOYER_NAME")
    private String employerName;

    @Column(name = "SUPERVISOR_NAME")
    private String supervisorName;

    @Column(name = "POSITION")
    private String position;

    @Column(name = "TERMINATION_REASON_TEXT")
    private String terminationReason;

    @Column(name = "WAGE")
    private BigDecimal wage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + PayPeriod.DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "WAGE_PERIOD_CODE", referencedColumnName = "code"))
    })
    private PayPeriod wagePeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + Occupation.DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "OCCUPATIONS_CODE", referencedColumnName = "code"))
    })
    private Occupation occupation;

    @Column(name = "COMMENT_TEXT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + EmploymentSchedule.DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "EMPLOYMENT_SCHEDULE", referencedColumnName = "code"))
    })
    private EmploymentSchedule scheduleType;

    @Column(name = "HOURS_WEEK")
    private Integer hoursWeek;

    @Column(name = "EMPLOYER_AWARE_FLAG")
    @Convert(converter = YesNoToBooleanConverter.class)
    private Boolean isEmployerAware;

    @Column(name = "CONTACT_EMPLOYER_FLAG")
    @Convert(converter = YesNoToBooleanConverter.class)
    private Boolean isEmployerContactable;


    @Builder.Default
    @Where(clause = "OWNER_CLASS = '" + OffenderEmploymentAddress.ADDR_TYPE + "'")
    @OneToMany(mappedBy = "employment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OffenderEmploymentAddress> addresses = new ArrayList<>();

    @Data
    @Embeddable
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        @Column(name = "OFFENDER_BOOK_ID")
        private Long bookingId;

        @Column(name = "EMPLOY_SEQ")
        private Long employSeq;
    }
}
