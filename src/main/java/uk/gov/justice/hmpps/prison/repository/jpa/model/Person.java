package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Where;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "PERSONS")
@ToString(of = {"id"})
public class Person extends AuditableEntity {

    @Id
    @Column(name = "PERSON_ID")
    private Long id;

    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastName;

    @Column(name = "MIDDLE_NAME")
    private String middleName;

    @Column(name = "BIRTHDATE")
    private LocalDate birthDate;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = "OWNER_CLASS = '"+PersonAddress.ADDR_TYPE+"'")
    @Default
    private List<PersonAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = "OWNER_CLASS = '"+PersonPhone.PHONE_TYPE+"'")
    @Default
    private List<PersonPhone> phones = new ArrayList<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Where(clause = "OWNER_CLASS = '"+PersonInternetAddress.TYPE+"'")
    @Default
    private List<PersonInternetAddress> internetAddresses = new ArrayList<>();

    public List<PersonInternetAddress> getEmails() {
        return internetAddresses.stream().filter(ia -> "EMAIL".equals(ia.getInternetAddressClass())).toList();
    }
}
