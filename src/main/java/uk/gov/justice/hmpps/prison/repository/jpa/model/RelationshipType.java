package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(RelationshipType.RELATIONSHIP)
@NoArgsConstructor
public class RelationshipType extends ReferenceCode {

    static final String RELATIONSHIP = "RELATIONSHIP";

    public RelationshipType(final String code, final String description) {
        super(RELATIONSHIP, code, description);
    }
}
