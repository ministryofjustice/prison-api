package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@NoArgsConstructor
@DiscriminatorValue(EmploymentStatus.DOMAIN)
public class EmploymentStatus extends ReferenceCode {
    public static final String DOMAIN = "EMPLOY_STS";

    public EmploymentStatus(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
