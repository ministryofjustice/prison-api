package net.syscon.elite.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(ReferenceCode.CASE_STATUS)
@NoArgsConstructor
public class CaseStatus extends ReferenceCode {
    public CaseStatus(final String code, final String description) {
        super(ReferenceCode.CASE_STATUS, code, description);
    }
}
