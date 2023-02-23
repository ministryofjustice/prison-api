package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(NonAssociationType.DOMAIN)
@NoArgsConstructor
public class NonAssociationType extends ReferenceCode {

    public static final String DOMAIN = "NON_ASSO_TYP";

    public NonAssociationType(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
