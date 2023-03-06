package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(Suffix.SUFFIX)
@NoArgsConstructor
public class Suffix extends ReferenceCode {
    public static final String SUFFIX = "SUFFIX";

    public Suffix(final String code, final String description) {
        super(SUFFIX, code, description);
    }
}
