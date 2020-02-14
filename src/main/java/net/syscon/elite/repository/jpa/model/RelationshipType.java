package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ReferenceCode.RELATIONSHIP)
@NoArgsConstructor
public class RelationshipType extends ReferenceCode {
    public RelationshipType(final String code, final String description) {
        super(ReferenceCode.RELATIONSHIP, code, description);
    }
}
