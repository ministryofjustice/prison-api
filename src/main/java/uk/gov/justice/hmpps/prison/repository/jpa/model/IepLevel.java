package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(IepLevel.PK.class)
@Table(name = "IEP_LEVELS")
@EqualsAndHashCode(of = { "iepLevel", "agencyLocationId"}, callSuper = false)
@ToString
public class IepLevel extends AuditableEntity  {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {

        @Column(name = "IEP_LEVEL", insertable = false, updatable = false, nullable = false)
        private String iepLevel;

        @Column(name = "AGY_LOC_ID", nullable = false)
        private String agencyLocationId;

    }

    @Id
    @Column(name = "IEP_LEVEL", nullable = false)
    private String iepLevel;

    @Id
    @Column(name = "AGY_LOC_ID", nullable = false)
    private String agencyLocationId;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    private String activeFlag;

    @Column(name = "EXPIRY_DATE")
    private String expiryDate;

    @Column(name = "DEFAULT_FLAG", nullable = false)
    private String defaultFlag;

}