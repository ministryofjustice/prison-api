package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(Country.COUNTRY)
@NoArgsConstructor
public class Country extends ReferenceCode {

    static final String COUNTRY = "COUNTRY";

    public Country(final String code, final String description) {
        super(COUNTRY, code, description);
    }

    public Country(final String code, final String description, final int sequence) {
        super(COUNTRY, code, description, sequence,true);
    }

    public static ReferenceCode.Pk pk(final String code) {
        return new ReferenceCode.Pk(COUNTRY, code);
    }
}
