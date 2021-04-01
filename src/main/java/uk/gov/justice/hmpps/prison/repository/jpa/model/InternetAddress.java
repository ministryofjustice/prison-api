package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "INTERNET_ADDRESSES")
@DiscriminatorColumn(name = "OWNER_CLASS")
@Inheritance
@EqualsAndHashCode(of = "internetAddressId")
public abstract class InternetAddress {
    @Id
    @Column(name = "INTERNET_ADDRESS_ID", nullable = false)
    private Long internetAddressId;

    @Column(name = "INTERNET_ADDRESS_CLASS")
    private String internetAddressClass;

    @Column(name = "INTERNET_ADDRESS")
    private String internetAddress;

}
