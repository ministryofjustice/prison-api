package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(Title.TITLE)
@NoArgsConstructor
public class Title extends ReferenceCode {
    public static final String TITLE = "TITLE";

    public Title(final String code, final String description) {
        super(TITLE, code, description);
    }
}
