package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(StudyArea.DOMAIN)
@NoArgsConstructor
public class StudyArea extends ReferenceCode {
    public static final String DOMAIN = "STUDY_AREA";

    public StudyArea(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
