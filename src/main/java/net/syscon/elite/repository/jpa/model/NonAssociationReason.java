package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(NonAssociationReason.DOMAIN)
@NoArgsConstructor
public class NonAssociationReason extends ReferenceCode {

    public static final String DOMAIN = "NON_ASSO_RSN";

    public NonAssociationReason(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
