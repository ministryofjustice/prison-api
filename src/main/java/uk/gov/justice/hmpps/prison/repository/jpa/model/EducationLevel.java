package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(EducationLevel.DOMAIN)
@NoArgsConstructor
public class EducationLevel extends ReferenceCode {
    public static final String DOMAIN = "EDU_LEVEL";

    public EducationLevel(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
