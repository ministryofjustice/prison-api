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
@DiscriminatorValue(AgencyAddress.ADDR_TYPE)
@NoArgsConstructor
@Data
@SuperBuilder
@EqualsAndHashCode(exclude = "agency", callSuper = true)
@ToString(of = {"agency"}, callSuper = true)
public class AgencyAddress extends Address {

    static final String ADDR_TYPE = "AGY";

    @JoinColumn(name = "OWNER_CODE")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AgencyLocation agency;
}
