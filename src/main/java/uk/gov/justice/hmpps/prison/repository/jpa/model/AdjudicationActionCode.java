package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(AdjudicationActionCode.TYPE)
@NoArgsConstructor
public class AdjudicationActionCode extends ReferenceCode {

    public static final String TYPE = "INC_DECISION";

    public static final Pk PLACED_ON_REPORT = new Pk(TYPE, "POR");
    public static final Pk NO_FURTHER_ACTION = new Pk(TYPE, "NFA");

    public AdjudicationActionCode(final String code, final String description) {
        super(TYPE, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(TYPE, code);
    }

    public static AdjudicationActionCode of(final Pk pk) { return new AdjudicationActionCode(pk.getCode(), pk.getDomain()); }
}
