package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.Hibernate;

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
@IdClass(IepPrisonMap.PK.class)
@Table(name = "IEP_LEVELS")
@ToString
public class IepPrisonMap extends AuditableEntity  {

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private String iepLevel;
        private AgencyLocation agencyLocation;
    }

    @Id
    @Column(name = "IEP_LEVEL", nullable = false)
    private String iepLevel;

    @Id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "AGY_LOC_ID", nullable = false)
    @Exclude
    private AgencyLocation agencyLocation;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    private String activeFlag;

    @Column(name = "EXPIRY_DATE")
    private String expiryDate;

    @Column(name = "DEFAULT_FLAG", nullable = false)
    private String defaultFlag;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final IepPrisonMap iepPrisonMap1 = (IepPrisonMap) o;

        if (!Objects.equals(getIepLevel(), iepPrisonMap1.getIepLevel())) return false;
        return Objects.equals(getAgencyLocation(), iepPrisonMap1.getAgencyLocation());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getIepLevel());
        result = 31 * result + (Objects.hashCode(getAgencyLocation()));
        return result;
    }
}