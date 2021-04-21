package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
public class Person extends ExtendedAuditableEntity {

    @Id
    @Column(name = "PERSON_ID")
    private Long id;

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
        return internetAddresses.stream().filter(ia -> "EMAIL".equals(ia.getInternetAddressClass())).collect(Collectors.toList());
    }
}
