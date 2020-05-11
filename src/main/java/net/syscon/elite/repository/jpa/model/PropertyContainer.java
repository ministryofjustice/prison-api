package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(PropertyContainer.CONTAINER)
@NoArgsConstructor
public class PropertyContainer extends ReferenceCode {

    static final String CONTAINER = "PPTY_CNTNR";

    public PropertyContainer(final String code, final String description) {
        super(CONTAINER, code, description);
    }
}
