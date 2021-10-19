package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(AdjudicationIncidentType.TYPE)
@NoArgsConstructor
public class AdjudicationIncidentType extends ReferenceCode {

    public static final String TYPE = "INC_TYPE";

    public static final Pk GOVERNORS_REPORT = new Pk(TYPE, "GOV");
    public static final Pk MISCELLANEOUS = new Pk(TYPE, "MISC");

    public AdjudicationIncidentType(final String code, final String description) {
        super(TYPE, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(TYPE, code);
    }

    public static AdjudicationIncidentType of(Pk pk) { return new AdjudicationIncidentType(pk.getCode(), pk.getDomain()); }
}
