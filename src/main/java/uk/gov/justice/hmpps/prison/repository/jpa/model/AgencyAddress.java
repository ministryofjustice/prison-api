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
@DiscriminatorValue(AgencyAddress.ADDR_TYPE)
@NoArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(of = {"agency"})
public class AgencyAddress extends Address {

    static final String ADDR_TYPE = "AGY";

    @JoinColumn(name = "OWNER_CODE")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AgencyLocation agency;
}
