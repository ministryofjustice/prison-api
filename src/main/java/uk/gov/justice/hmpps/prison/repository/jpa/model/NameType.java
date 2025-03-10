package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue(NameType.NAME_TYPE)
@NoArgsConstructor
public class NameType extends ReferenceCode {
    public static final String NAME_TYPE = "NAME_TYPE";

    public NameType(final String code, final String description) {
        super(NAME_TYPE, code, description);
    }
}
