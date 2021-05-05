package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
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
@DiscriminatorValue(AgencyPhone.PHONE_TYPE)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@EqualsAndHashCode(exclude = "address", callSuper = true)
@ToString(callSuper = true)
public class AgencyPhone extends Phone {

    static final String PHONE_TYPE = "AGY";

    @JoinColumn(name = "OWNER_CODE")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AgencyLocation agency;

}
