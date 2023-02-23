package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(SearchLevel.SEARCH_LEVEL)
@NoArgsConstructor
public class SearchLevel extends ReferenceCode {

    static final String SEARCH_LEVEL = "SEARCH_LEVEL";

    public SearchLevel(final String code, final String description) {
        super(SEARCH_LEVEL, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(SEARCH_LEVEL, code);
    }
}
