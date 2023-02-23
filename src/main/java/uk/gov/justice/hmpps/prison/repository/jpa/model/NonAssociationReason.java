package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(NonAssociationReason.DOMAIN)
@NoArgsConstructor
public class NonAssociationReason extends ReferenceCode {

    public static final String DOMAIN = "NON_ASSO_RSN";

    public NonAssociationReason(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
