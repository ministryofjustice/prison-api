package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Builder
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
