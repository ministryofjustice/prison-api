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
import java.util.List;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.Gender.SEX;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "OFFENDERS")
@ToString
public class Offender extends AuditableEntity {

    @SequenceGenerator(name = "OFFENDER_ID", sequenceName = "OFFENDER_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFFENDER_ID")
    @Id
    @Column(name = "OFFENDER_ID", nullable = false)
    private Long id;

    @Column(name = "ID_SOURCE_CODE", nullable = false)
    @Builder.Default
    private String idSourceCode = "SEQ";

    @Column(name = "FIRST_NAME", nullable = true)
    private String firstName;

    @Column(name = "MIDDLE_NAME", nullable = true)
    private String middleName;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Column(name = "BIRTH_DATE", nullable = true)
    private LocalDate birthDate;

    @Column(name = "ROOT_OFFENDER_ID")
    private Long rootOffenderId;

    @OneToMany(mappedBy = "offender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OffenderBooking> bookings;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + SEX + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "SEX_CODE", referencedColumnName = "code"))
    })
    private Gender gender;

    @Column(name = "CREATE_DATE", nullable = false)
    @CreatedDate
    private LocalDate createDate;

    @Column(name = "LAST_NAME_KEY", nullable = false)
    private String lastNameKey;

    @Column(name = "OFFENDER_ID_DISPLAY", nullable = false)
    private String nomsId;
}
