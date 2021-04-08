package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(County.COUNTY)
@NoArgsConstructor
public class County extends ReferenceCode {

    static final String COUNTY = "COUNTY";

    public County(final String code, final String description) {
        super(COUNTY, code, description);
    }

    public static ReferenceCode.Pk pk(final String code) {
        return new ReferenceCode.Pk(COUNTY, code);
    }
}
