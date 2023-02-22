package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(PayPeriod.DOMAIN)
@NoArgsConstructor
public class PayPeriod extends ReferenceCode {
    public static final String DOMAIN = "PAY_PERIOD";

    public PayPeriod(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
