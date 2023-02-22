package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(VisitStatus.VISIT_STATUS)
@NoArgsConstructor
public class VisitStatus extends ReferenceCode {

    static final String VISIT_STATUS = "VIS_STS";

    public VisitStatus(final String code, final String description) {
        super(VISIT_STATUS, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(VISIT_STATUS, code);
    }
}
