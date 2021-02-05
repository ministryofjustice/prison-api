package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(PaymentType.PAY_TYPE)
@NoArgsConstructor
public class PaymentType extends ReferenceCode {
    public static final String PAY_TYPE = "PAY_TYPE";

    public PaymentType(final String code, final String description) {
        super(PAY_TYPE, code, description);
    }
}


