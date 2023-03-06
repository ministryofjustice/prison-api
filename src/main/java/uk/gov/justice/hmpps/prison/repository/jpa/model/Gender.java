package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(Gender.SEX)
@NoArgsConstructor
public class Gender extends ReferenceCode {
    public static final String SEX = "SEX";

    public static final ReferenceCode.Pk MALE = new Pk(SEX, "M");

    public static final ReferenceCode.Pk FEMALE = new Pk(SEX, "F");

    public Gender(final String code, final String description) {
        super(SEX, code, description);
    }

    public static Pk pk(final String code) {
        return new Pk(SEX, code);
    }

}
