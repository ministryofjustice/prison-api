package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue(HealthProblemCode.HEALTH_PBLM)
@Getter
@NoArgsConstructor
public class HealthProblemCode extends ReferenceCode {
    static final String HEALTH_PBLM = "HEALTH_PBLM";

    public static ReferenceCode.Pk pk(final String code) {
        return new ReferenceCode.Pk(HEALTH_PBLM, code);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "PARENT_DOMAIN", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "PARENT_CODE", referencedColumnName = "code"))
    })
    private HealthProblemType problemType;

    public HealthProblemCode(final String code, final String description) {
        super(HEALTH_PBLM, code, description);
    }

    public HealthProblemCode(final String code, final String description, final HealthProblemType problemType) {
        super(HEALTH_PBLM, code, description, 99, true);
        this.problemType = problemType;
    }
}
