package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(EmploymentSchedule.DOMAIN)
@NoArgsConstructor
public class EmploymentSchedule extends ReferenceCode {
    public static final String DOMAIN = "EMP_SCH_TYPE";

    public EmploymentSchedule(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
