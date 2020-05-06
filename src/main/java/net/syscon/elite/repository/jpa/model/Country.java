package net.syscon.elite.repository.jpa.model;

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
}
