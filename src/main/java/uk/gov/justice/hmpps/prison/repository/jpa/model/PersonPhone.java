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
@DiscriminatorValue(PersonPhone.PHONE_TYPE)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@EqualsAndHashCode(exclude = "person", callSuper = true)
@ToString(callSuper = true)
public class PersonPhone extends Phone {

    static final String PHONE_TYPE = "PER";

    @JoinColumn(name = "OWNER_ID")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Person person;

}
