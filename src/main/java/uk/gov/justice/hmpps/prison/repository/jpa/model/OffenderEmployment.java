package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "OFFENDER_EMPLOYMENTS")
public class OffenderEmployment {

    @EmbeddedId
    private PK id;

    @Column(name = "EMPLOYMENT_DATE")
    private LocalDate startDate;

    @Column(name = "TERMINATION_DATE")
    private LocalDate endDate;

    @Column(name = "EMPLOYMENT_POST_CODE")
    @Enumerated(EnumType.STRING)
    private EmploymentPostType postType;

    @Column(name = "EMPLOYER_NAME")
    private String employerName;

    @Column(name = "SUPERVISOR_NAME")
    private String supervisorName;

    @Column(name = "POSITION")
    private String position;

    @Column(name = "TERMINATION_REASON_TEXT")
    private String terminationReason;

    @Column(name = "WAGE")
    private Double wage;

    @Column(name = "WAGE_PERIOD_CODE")
    private PayPeriodType wagePeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + Occupation.DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "OCCUPATIONS_CODE", referencedColumnName = "code"))
    })
    private Occupation occupation;

    @Column(name = "COMMENT_TEXT")
    private String comment;

    @Column(name = "SCHEDULE_TYPE")
    private ScheduleType scheduleType;

    @Column(name = "HOURS_WEEK")
    private Integer hoursWeek;

    @Column(name = "EMPLOYER_AWARE_FLAG")
    @Convert(converter = YesNoToBooleanConverter.class)
    private Boolean employerAware;

    @Column(name = "CONTACT_EMPLOYER_FLAG")
    @Convert(converter = YesNoToBooleanConverter.class)
    private Boolean employerContactable;


    @Where(clause = "OWNER_CLASS = '" + OffenderEmploymentAddress.ADDR_TYPE + "'")
    @OneToMany(mappedBy = "employment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OffenderEmploymentAddress> addresses = new ArrayList<>();

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

    @Getter
    @RequiredArgsConstructor
    public enum EmploymentPostType {
        CARE("Carer"),
        CAS("Casual"),
        FT("Full Time"),
        FTEDU("Full Time Student"),
        NDEAL("New Deal"),
        NK("Not Known"),
        PT("Part Time"),
        PTEDU("Part Time Student"),
        SEMP("Self Employed"),
        SES("Sessional"),
        UNAV("Unavailable for Work (Retired/Housewife)"),
        UNEMP("Unemployed"),
        UP("Unpaid Work (Court Order)"),
        VL("Volunteer"),
        WORKTRIAL("Work Trial");

        private final String description;
    }

    @Getter
    @RequiredArgsConstructor
    public enum PayPeriodType {
        HOUR("Hourly"),
        MONTH("Monthly"),
        TWO_WEEKS("Fortnightly"),
        WEEK("Weekly");

        private final String description;
    }

    @Getter
    @RequiredArgsConstructor
    public enum ScheduleType {
        FTNIGHT("Fortnightly"),
        HOUR("Hourly"),
        INHAND("In Hand"),
        MONTH("Monthly"),
        WEEK("Weekly");

        private final String description;
    }
}

