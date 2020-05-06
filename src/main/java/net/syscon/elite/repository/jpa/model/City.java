package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(City.CITY)
@NoArgsConstructor
public class City extends ReferenceCode {

    static final String CITY = "CITY";

    public City(final String code, final String description) {
        super(CITY, code, description);
    }
}
