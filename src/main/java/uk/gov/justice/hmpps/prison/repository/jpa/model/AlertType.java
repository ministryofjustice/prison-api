package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(AlertType.ALERT_TYPE)
@NoArgsConstructor
public class AlertType extends ReferenceCode {

    static final String ALERT_TYPE = "ALERT";

    public AlertType(final String code, final String description) {
        super(ALERT_TYPE, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(ALERT_TYPE, code);
    }
}
