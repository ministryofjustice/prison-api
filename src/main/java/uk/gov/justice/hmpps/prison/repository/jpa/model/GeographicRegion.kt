package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue(GeographicRegion.GEOGRAPHIC)
@NoArgsConstructor
public class GeographicRegion extends ReferenceCode {
    public static final String GEOGRAPHIC = "GEOGRAPHIC";

    public GeographicRegion(final String code, final String description) {
        super(GEOGRAPHIC, code, description);
    }

    public GeographicRegion(final String code) {
        super(GEOGRAPHIC, code, null);
    }
}
