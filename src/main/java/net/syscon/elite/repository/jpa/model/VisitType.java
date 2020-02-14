package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ReferenceCode.VISIT_TYPE)
@NoArgsConstructor
public class VisitType extends ReferenceCode {
    public VisitType(final String code, final String description) {
        super(ReferenceCode.VISIT_TYPE, code, description);
    }
}
