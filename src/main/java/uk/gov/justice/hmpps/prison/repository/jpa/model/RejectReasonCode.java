package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(RejectReasonCode.DOMAIN)
@NoArgsConstructor
public class RejectReasonCode extends ReferenceCode {

    static final String DOMAIN = "PS_REJ_RSN";

    public RejectReasonCode(final String code, final String description) {
        super(DOMAIN, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(DOMAIN, code);
    }
}
