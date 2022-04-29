package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus.CASE_STS;

@Entity
@DiscriminatorValue(CASE_STS)
@NoArgsConstructor
public class CaseStatus extends ReferenceCode {

    private static final String ACTIVE_CODE = "A";

    static final String CASE_STS = "CASE_STS";

    public CaseStatus(final String code, final String description) {
        super(CASE_STS, code, description);
    }

    boolean isActiveStatus() {
        return ACTIVE_CODE.equalsIgnoreCase(this.getCode());
    }

    public static ReferenceCode.Pk pk(final String code) {
        return new ReferenceCode.Pk(CASE_STS, code);
    }
}
