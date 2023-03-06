package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(Occupation.DOMAIN)
@NoArgsConstructor
public class Occupation extends ReferenceCode {
    public static final String DOMAIN = "OCCUPATION";

    public Occupation(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
