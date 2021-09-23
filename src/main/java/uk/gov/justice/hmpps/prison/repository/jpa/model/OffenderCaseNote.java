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
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.String.format;
import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "OFFENDER_CASE_NOTES")
@ToString(exclude = { "offenderBooking", "agencyLocation" } )
public class OffenderCaseNote extends AuditableEntity {
    private static final String AMEND_CASE_NOTE_FORMAT = "%s ...[%s updated the case notes on %s] %s";

    @Id
    @Column(name = "CASE_NOTE_ID", nullable = false)
    @SequenceGenerator(name = "CASE_NOTE_ID", sequenceName = "CASE_NOTE_ID", allocationSize = 1)
    @GeneratedValue(generator = "CASE_NOTE_ID")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "CONTACT_DATE")
    private LocalDate occurrenceDate;

    @Column(name = "CONTACT_TIME")
    private LocalDateTime occurrenceDateTime;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + CaseNoteType.CASE_NOTE_TYPE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "CASE_NOTE_TYPE", referencedColumnName = "code"))
    })
    private CaseNoteType type;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + CaseNoteSubType.CASE_NOTE_SUB_TYPE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "CASE_NOTE_SUB_TYPE", referencedColumnName = "code"))
    })
    private CaseNoteSubType subType;

    @Column(name = "CASE_NOTE_TEXT")
    private String caseNoteText;

    @Column(name = "AMENDMENT_FLAG")
    @Type(type="yes_no")
    private boolean amendmentFlag;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID", nullable = true)
    private AgencyLocation agencyLocation;

    @ManyToOne
    @JoinColumn(name = "STAFF_ID")
    private Staff author;

    @Column(name = "NOTE_SOURCE_CODE")
    private String noteSourceCode;

    @Column(name = "DATE_CREATION")
    @CreatedDate
    private LocalDate dateCreation;

    @Column(name = "TIME_CREATION")
    @CreatedDate
    private LocalDateTime timeCreation;

    @Override
    public LocalDateTime getCreateDatetime() {
        return super.getCreateDatetime();
    }

    public String createAppendedText(final String appendedText, final String username) {
        return format(AMEND_CASE_NOTE_FORMAT,
            caseNoteText,
            username,
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")),
            appendedText);

    }
}
