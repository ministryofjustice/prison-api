package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue(AreaType.AREA_TYPE)
@NoArgsConstructor
public class AreaType extends ReferenceCode {
    public static final String AREA_TYPE = "AREA_TYPE";

    public AreaType(final String code, final String description) {
        super(AREA_TYPE, code, description);
    }

    public AreaType(final String code) {
        super(AREA_TYPE, code, null);
    }
}
