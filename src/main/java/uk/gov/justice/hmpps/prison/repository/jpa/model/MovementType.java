package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(MovementType.TYPE)
@NoArgsConstructor
public class MovementType extends ReferenceCode {

    static final String TYPE = "MOVE_TYPE";

    public MovementType(final String code, final String description) {
        super(TYPE, code, description);
    }
}
