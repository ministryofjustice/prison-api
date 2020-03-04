package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDERS")
public class OffenderAliasPendingDeletion {

    @Id
    @Column(name = "OFFENDER_ID", nullable = false)
    private Long offenderId;

    @Column(name = "OFFENDER_ID_DISPLAY", nullable = false)
    private String offenderNumber;

    @Column(name = "ROOT_OFFENDER_ID")
    private Long rootOffenderId;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "MIDDLE_NAME")
    private String middleName;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Singular
    @OneToMany(mappedBy = "offenderAlias")
    private List<OffenderBookingPendingDeletion> offenderBookings;
}
