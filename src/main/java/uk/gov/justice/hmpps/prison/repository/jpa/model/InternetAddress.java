package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "INTERNET_ADDRESSES")
@DiscriminatorColumn(name = "OWNER_CLASS")
@Inheritance
@EqualsAndHashCode(of = "internetAddressId", callSuper = false)
public abstract class InternetAddress {
    @Id
    @Column(name = "INTERNET_ADDRESS_ID", nullable = false)
    private Long internetAddressId;

    @Column(name = "INTERNET_ADDRESS_CLASS")
    private String internetAddressClass;

    @Column(name = "INTERNET_ADDRESS")
    private String internetAddress;

}
