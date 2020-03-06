package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import static net.syscon.elite.repository.jpa.model.CaseStatus.CASE_STS;

@Entity
@DiscriminatorValue(CASE_STS)
@NoArgsConstructor
public class CaseStatus extends ReferenceCode {

    static final String CASE_STS = "CASE_STS";

    public CaseStatus(final String code, final String description) {
        super(CASE_STS, code, description);
    }
}
