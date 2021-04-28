package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(CourtType.JURISDICTION)
@NoArgsConstructor
public class CourtType extends ReferenceCode {
    public static final String JURISDICTION = "JURISDICTION";

    public CourtType(final String code, final String description) {
        super(JURISDICTION, code, description);
    }

    public CourtType(final String code) {
        super(JURISDICTION, code, null);
    }
}
