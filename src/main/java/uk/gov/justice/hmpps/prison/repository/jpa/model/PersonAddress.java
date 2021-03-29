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
@DiscriminatorValue(PersonAddress.ADDR_TYPE)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(of = {"person"}, callSuper = true)
public class PersonAddress extends Address {

    static final String ADDR_TYPE = "PER";

    @JoinColumn(name = "OWNER_ID")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Person person;
}
