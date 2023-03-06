package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(TeamCategory.TEAM_CATEGORY_DOMAIN)
@NoArgsConstructor
public class TeamCategory extends ReferenceCode {

    static final String TEAM_CATEGORY_DOMAIN = "TEAMCATEGORY";

    public TeamCategory(final String code, final String description) {
        super(TEAM_CATEGORY_DOMAIN, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(TEAM_CATEGORY_DOMAIN, code);
    }
}
