package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ContactType.CONTACTS)
@NoArgsConstructor
public class ContactType extends ReferenceCode {

    static final String CONTACTS = "CONTACTS";

    public ContactType(final String code, final String description) {
        super(CONTACTS, code, description);
    }
}
