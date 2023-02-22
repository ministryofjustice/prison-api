package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(OffenderProgramStatus.DOMAIN)
@NoArgsConstructor
public class OffenderProgramStatus extends ReferenceCode {

    static final String DOMAIN = "OFF_PRG_STS";

    public OffenderProgramStatus(final String code, final String description) {
        super(DOMAIN, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(DOMAIN, code);
    }
}
