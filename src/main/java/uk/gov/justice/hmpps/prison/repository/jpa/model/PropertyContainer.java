package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(PropertyContainer.CONTAINER)
@NoArgsConstructor
public class PropertyContainer extends ReferenceCode {

    static final String CONTAINER = "PPTY_CNTNR";

    public PropertyContainer(final String code, final String description) {
        super(CONTAINER, code, description);
    }
}
