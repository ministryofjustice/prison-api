package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue(OffenderInternetAddress.TYPE)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@EqualsAndHashCode(exclude = "offender", callSuper = true)
@ToString(of = {"offender"})
public class OffenderInternetAddress extends InternetAddress {

    static final String TYPE = "OFF";


    @JoinColumn(name = "OWNER_ID")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Offender offender;
}
