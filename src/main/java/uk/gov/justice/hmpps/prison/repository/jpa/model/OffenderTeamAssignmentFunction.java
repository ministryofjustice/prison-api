package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(OffenderTeamAssignmentFunction.FUNCTION_DOMAIN)
@NoArgsConstructor
public class OffenderTeamAssignmentFunction extends ReferenceCode {

    static final String FUNCTION_DOMAIN = "FUNCTION";

    public OffenderTeamAssignmentFunction(final String code, final String description) {
        super(FUNCTION_DOMAIN, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(FUNCTION_DOMAIN, code);
    }
}
