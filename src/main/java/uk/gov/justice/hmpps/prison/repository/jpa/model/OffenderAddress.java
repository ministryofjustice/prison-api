package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@SuperBuilder
@DiscriminatorValue(OffenderAddress.ADDR_TYPE)
@NoArgsConstructor
@Data
@EqualsAndHashCode(exclude = "offender", callSuper = true)
@ToString(of = {"offender"}, callSuper = true)
public class OffenderAddress extends Address {

    static final String ADDR_TYPE = "OFF";

    @JoinColumn(name = "OWNER_ID")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Offender offender;
}
