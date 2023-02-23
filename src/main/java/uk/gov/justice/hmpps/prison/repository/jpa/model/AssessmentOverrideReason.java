package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(AssessmentOverrideReason.OVERRIDE_REASON)
@NoArgsConstructor
public class AssessmentOverrideReason extends ReferenceCode {

    static final String OVERRIDE_REASON = "OVERRIDE_RSN";

    public AssessmentOverrideReason(final String code, final String description) {
        super(OVERRIDE_REASON, code, description);
    }
}
