package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Data
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

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "MIDDLE_NAME")
    private String middleName;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @OneToMany(mappedBy = "offenderAlias", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OffenderBookingPendingDeletion> offenderBookings;
}
