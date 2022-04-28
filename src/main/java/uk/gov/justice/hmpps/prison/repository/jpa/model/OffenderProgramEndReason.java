package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(OffenderProgramEndReason.DOMAIN)
@NoArgsConstructor
public class OffenderProgramEndReason extends ReferenceCode {
    public static final String DOMAIN = "PS_END_RSN";

    public static final ReferenceCode.Pk TRF = new ReferenceCode.Pk(DOMAIN, "TRF");

    public OffenderProgramEndReason(final String code, final String description) {
        super(DOMAIN, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(DOMAIN, code);
    }
}
