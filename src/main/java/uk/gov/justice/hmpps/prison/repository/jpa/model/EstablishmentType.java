package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(EstablishmentType.DOMAIN)
@NoArgsConstructor
public class EstablishmentType extends ReferenceCode {

    public static final String DOMAIN = "ESTAB_TYPE";

    public EstablishmentType(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
