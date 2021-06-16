package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "OFFENDER_EMPLOYMENTS")
public class OffenderEmployment {

    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @Id
    @Column(name = "EMPLOY_SEQ")
    private Long employSeq;

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

    @Column(name = "PARTIAL_EMPLOYMENT_DATE_FLAG")
    private String partialEmploymentDateFlag; //" VARCHAR2(1 CHAR) DEFAULT 'N'

    @Column(name = "PARTIAL_TERMINATION_DATE_FLAG")
    private String partialTerminationDateFlag; //" VARCHAR2(1 CHAR) DEFAULT 'N'

    @Column(name = "CHECK_BOX_1")
    private String checkBox1; //" VARCHAR2(1 CHAR) DEFAULT 'N'

    @Column(name = "CHECK_BOX_2")
    private String checkBox2; //" VARCHAR2(1 CHAR) DEFAULT 'N'

    @Column(name = "EMPLOYER_AWARE_FLAG")
    private String employerAwareFlag; //" VARCHAR2(1 CHAR) DEFAULT 'N' NOT NULL

    @Column(name = "CONTACT_EMPLOYER_FLAG")
    private String contactEmployerFlag; //" VARCHAR2(1 CHAR) DEFAULT 'N' NOT NULL

    @Column(name = "OFFENDER_EMPLOYMENT_ID")
    private Integer offenderEmploymentId;

    @Column(name = "EMPLOYMENT_SCHEDULE")
    private String employmentSchedule;

    @Column(name = "CERTIFICATION")
    private String certification;

}

