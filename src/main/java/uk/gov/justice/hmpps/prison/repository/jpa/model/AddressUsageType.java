package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(AddressUsageType.ADDRESS_TYPE)
@NoArgsConstructor
public class AddressUsageType extends ReferenceCode {

    static final String ADDRESS_TYPE = "ADDRESS_TYPE";

    public AddressUsageType(final String code, final String description) {
        super(ADDRESS_TYPE, code, description);
    }
}
