package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "OFFENDER_EDUCATIONS")
public class OffenderEducation {

    @EmbeddedId
    private PK id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + StudyArea.DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "STUDY_AREA_CODE", referencedColumnName = "code"))
    })
    private StudyArea studyArea;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + EducationLevel.DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "EDUCATION_LEVEL_CODE", referencedColumnName = "code"))
    })
    private EducationLevel educationLevel;

    @Column(name = "NUMBER_OF_YEARS")
    private Integer numberOfYears;

    @Column(name = "GRADUATION_YEAR")
    private String graduationYear;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "COMMENT_TEXT")
    private String comment;

    @Column(name = "SCHOOL_NAME")
    private String school;

    @Column(name = "SPECIAL_EDUCATION_FLAG")
    @Convert(converter = YesNoToBooleanConverter.class)
    private Boolean isSpecialEducation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + EducationSchedule.DOMAIN + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "EDUCATION_SCHEDULE", referencedColumnName = "code"))
    })
    private EducationSchedule schedule;

    @Builder.Default
    @Where(clause = "OWNER_CLASS = '" + OffenderEducationAddress.ADDR_TYPE + "'")
    @OneToMany(mappedBy = "education", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OffenderEducationAddress> addresses = new ArrayList<>();

    @Data
    @Embeddable
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        @Column(name = "OFFENDER_BOOK_ID")
        private Long bookingId;

        @Column(name = "EDUCATION_SEQ")
        private Long educationSeq;
    }
}

