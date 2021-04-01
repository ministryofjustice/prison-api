package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@SuperBuilder
@DiscriminatorValue(OffenderAddress.ADDR_TYPE)
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(of = {"offender"})
public class OffenderAddress extends Address {

    static final String ADDR_TYPE = "OFF";

    @JoinColumn(name = "OWNER_ID")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Offender offender;
}
