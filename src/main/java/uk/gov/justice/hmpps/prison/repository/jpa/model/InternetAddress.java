package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "INTERNET_ADDRESSES")
public class InternetAddress {
    @Id
    @Column(name = "INTERNET_ADDRESS_ID", nullable = false)
    private Long internetAddressId;

    @Column(name = "OWNER_ID", nullable = false)
    private Long ownerId;

    @Column(name = "OWNER_CLASS", nullable = false)
    private String ownerClass;

    @Column(name = "INTERNET_ADDRESS_CLASS")
    private String internetAddressClass;

    @Column(name = "INTERNET_ADDRESS")
    private String internetAddress;

}
