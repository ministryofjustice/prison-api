package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(WaitlistDecisionCode.DOMAIN)
@NoArgsConstructor
public class WaitlistDecisionCode extends ReferenceCode {

    static final String DOMAIN = "PS_ACT_DEC";

    public static final ReferenceCode.Pk REJ = new ReferenceCode.Pk(DOMAIN, "REJ");

    public WaitlistDecisionCode(final String code, final String description) {
        super(DOMAIN, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(DOMAIN, code);
    }
}
