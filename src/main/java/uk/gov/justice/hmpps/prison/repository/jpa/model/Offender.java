package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.Ethnicity.ETHNICITY;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.Gender.SEX;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.Suffix.SUFFIX;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.Title.TITLE;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "OFFENDERS")
@ToString(of = {"nomsId", "firstName", "lastName", "birthDate", "id", "rootOffenderId"})
public class Offender extends AuditableEntity {

    @SequenceGenerator(name = "OFFENDER_ID", sequenceName = "OFFENDER_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFFENDER_ID")
    @Id
    @Column(name = "OFFENDER_ID", nullable = false)
    private Long id;

    @Column(name = "OFFENDER_ID_DISPLAY", nullable = false)
    private String nomsId;

    @Column(name = "ID_SOURCE_CODE", nullable = false)
    @Default
    private String idSourceCode = "SEQ";

    @Column(name = "NAME_SEQUENCE")
    @Default
    private String nameSequence = "1234";

    @Column(name = "CASELOAD_TYPE")
    @Default
    private String caseloadType = "INST";

    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Column(name = "MIDDLE_NAME")
    private String middleName;

    @Column(name = "MIDDLE_NAME_2")
    private String middleName2;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Column(name = "BIRTH_DATE", nullable = false)
    private LocalDate birthDate;

    @Column(name = "ROOT_OFFENDER_ID")
    private Long rootOffenderId;

    @OneToMany(mappedBy = "offender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OffenderBooking> bookings;

    @OneToMany(mappedBy = "offender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OffenderIdentifier> identifiers;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + SEX + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "SEX_CODE", referencedColumnName = "code", nullable = false))
    })
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + ETHNICITY + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "RACE_CODE", referencedColumnName = "code"))
    })
    private Ethnicity ethnicity;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + TITLE + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "TITLE", referencedColumnName = "code"))
    })
    private Title title;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + SUFFIX + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "SUFFIX", referencedColumnName = "code"))
    })
    private Suffix suffix;

    @Column(name = "CREATE_DATE", nullable = false)
    @CreatedDate
    private LocalDate createDate;

    @Column(name = "LAST_NAME_KEY", nullable = false)
    private String lastNameKey;

    @Column(name = "LAST_NAME_SOUNDEX")
    private String lastNameSoundex;

    @Column(name = "LAST_NAME_ALPHA_KEY")
    private String lastNameAlphaKey;


    public Optional<String> getLatestIdentifierOfType(final String type) {
        final var mapOfTypes = identifiers.stream().collect(Collectors.groupingBy(OffenderIdentifier::getIdentifierType));
        return mapOfTypes.get(type).stream().max(Comparator.comparing(id -> id.getOffenderIdentifierPK().getOffenderIdSeq())).map(OffenderIdentifier::getIdentifier);
    }

    public Optional<String> getPnc() {
        return getLatestIdentifierOfType("PNC");
    }

    public Optional<String> getCro() {
        return getLatestIdentifierOfType("CRO");
    }
}
