package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(AssessmentClassification.ASSESS_CLASS)
@NoArgsConstructor
public class AssessmentClassification extends ReferenceCode {

    static final String ASSESS_CLASS = "SUP_LVL_TYPE";

    static final String PENDING_CODE = "PEND";

    public AssessmentClassification(final String code, final String description) {
        super(ASSESS_CLASS, code, description);
    }

    public boolean isPending() {
        return PENDING_CODE.equals(getCode());
    }
}
