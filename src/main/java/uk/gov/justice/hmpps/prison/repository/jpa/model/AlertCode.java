package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(AlertCode.ALERT_CODE)
@NoArgsConstructor
public class AlertCode extends ReferenceCode {

    static final String ALERT_CODE = "ALERT_CODE";

    public AlertCode(final String code, final String description) {
        super(ALERT_CODE, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(ALERT_CODE, code);
    }
}
