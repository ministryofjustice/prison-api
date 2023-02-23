package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(Ethnicity.ETHNICITY)
@NoArgsConstructor
public class Ethnicity extends ReferenceCode {
    public static final String ETHNICITY = "ETHNICITY";

    public Ethnicity(final String code, final String description) {
        super(ETHNICITY, code, description);
    }
}
