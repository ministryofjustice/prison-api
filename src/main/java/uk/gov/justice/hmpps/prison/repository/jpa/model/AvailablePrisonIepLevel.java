package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@Entity
@IdClass(AvailablePrisonIepLevel.PK.class)
@Table(name = "IEP_LEVELS")
@ToString
@BatchSize(size = 25)
public class AvailablePrisonIepLevel extends AuditableEntity  {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private String id;
        private AgencyLocation agencyLocation;
    }

    @Id
    @Column(name = "IEP_LEVEL", nullable = false)
    private String id;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    @Exclude
    private AgencyLocation agencyLocation;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumnsOrFormulas(value = {
        @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + IepLevel.IEP_LEVEL + "'", referencedColumnName = "domain")),
        @JoinColumnOrFormula(column = @JoinColumn(name = "IEP_LEVEL", referencedColumnName = "code", updatable = false, insertable = false))
    })
    @Exclude
    private IepLevel iepLevel;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    @Type(type="yes_no")
    @Default
    private boolean active = true;

    @Column(name = "EXPIRY_DATE")
    private String expiryDate;

    @Column(name = "DEFAULT_FLAG", nullable = false)
    @Type(type="yes_no")
    private boolean defaultIep;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final AvailablePrisonIepLevel that = (AvailablePrisonIepLevel) o;

        if (!Objects.equals(getIepLevel(), that.getIepLevel())) return false;
        return Objects.equals(getAgencyLocation(), that.getAgencyLocation());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getIepLevel());
        result = 31 * result + (Objects.hashCode(getAgencyLocation()));
        return result;
    }
}