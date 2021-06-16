package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import uk.gov.justice.hmpps.prison.repository.converter.YesNoToBooleanConverter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
    private LocalDate employmentDate;


    @Column(name = "EMPLOYMENT_POST_CODE")
    private String employmentPostCode;

    @Column(name = "EMPLOYMENT_TYPE")
    private String employmentType;

    @Column(name = "TERMINATION_DATE")
    private LocalDate terminationDate;

    @Column(name = "EMPLOYER_NAME")
    private String employerName;

    @Column(name = "SUPERVISOR_NAME")
    private String supervisorName;

    @Column(name = "POSITION")
    private String POSITION;

    @Column(name = "TERMINATION_REASON_TEXT")
    private String terminationReasonText;

    @Column(name = "WAGE")
    private Double WAGE;

    @Column(name = "WAGE_PERIOD_CODE")
    private String wagePeriodCode;

    @Column(name = "OCCUPATIONS_CODE")
    private String occupationsCode;

    @Column(name = "COMMENT_TEXT")
    private String commentText;

    @Column(name = "CASELOAD_TYPE")
    private String caseloadType;

    @Column(name = "ROOT_OFFENDER_ID")
    private String rootOffenderId;

    @Column(name = "CONTACT_TYPE")
    private String contactType;

    @Column(name = "CONTACT_NUMBER")
    private String contactNumber;

    @Column(name = "SCHEDULE_TYPE")
    private String scheduleType;

    @Column(name = "SCHEDULE_HOURS")
    private Integer scheduleHours;

    @Column(name = "HOURS_WEEK")
    private Integer hoursWeek;

    @Convert(converter = YesNoToBooleanConverter.class)
    @Column(name = "PARTIAL_EMPLOYMENT_DATE_FLAG")
    private Boolean partialEmploymentDateFlag;

    @Convert(converter = YesNoToBooleanConverter.class)
    @Column(name = "PARTIAL_TERMINATION_DATE_FLAG")
    private Boolean partialTerminationDateFlag;

    @Convert(converter = YesNoToBooleanConverter.class)
    @Column(name = "CHECK_BOX_1")
    private Boolean checkBox1;

    @Convert(converter = YesNoToBooleanConverter.class)
    @Column(name = "CHECK_BOX_2")
    private Boolean checkBox2;

    @Convert(converter = YesNoToBooleanConverter.class)
    @Column(name = "EMPLOYER_AWARE_FLAG")
    private Boolean employerAwareFlag;

    @Convert(converter = YesNoToBooleanConverter.class)
    @Column(name = "CONTACT_EMPLOYER_FLAG")
    private String contactEmployerFlag;

    @Column(name = "OFFENDER_EMPLOYMENT_ID")
    private Integer offenderEmploymentId;

    @Column(name = "EMPLOYMENT_SCHEDULE")
    private String employmentSchedule;

    @Column(name = "CERTIFICATION")
    private String certification;

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
}

