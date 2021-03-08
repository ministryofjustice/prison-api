package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(AssessmentCommittee.ASSESS_COMMITTEE)
@NoArgsConstructor
public class AssessmentCommittee extends ReferenceCode {

    static final String ASSESS_COMMITTEE = "ASSESS_COMM";

    public AssessmentCommittee(final String code, final String description) {
        super(ASSESS_COMMITTEE, code, description);
    }
}
