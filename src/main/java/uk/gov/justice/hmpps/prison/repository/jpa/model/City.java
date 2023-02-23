package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(City.CITY)
@NoArgsConstructor
public class City extends ReferenceCode {

    static final String CITY = "CITY";

    public City(final String code, final String description) {
        super(CITY, code, description);
    }

    public static ReferenceCode.Pk pk(final String code) {
        return new ReferenceCode.Pk(CITY, code);
    }
}
