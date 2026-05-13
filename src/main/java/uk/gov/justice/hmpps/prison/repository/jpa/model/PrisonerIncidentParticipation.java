package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue(PrisonerIncidentParticipation.IR_OFF_PART)
@NoArgsConstructor
public class PrisonerIncidentParticipation extends ReferenceCode {
    public static final String IR_OFF_PART = "IR_OFF_PART";

    public PrisonerIncidentParticipation(final String code, final String description) {
        super(IR_OFF_PART, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(IR_OFF_PART, code);
    }

}
