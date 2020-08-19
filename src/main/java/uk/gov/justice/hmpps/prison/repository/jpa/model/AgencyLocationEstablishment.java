package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;

import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(AgencyLocationEstablishment.Pk.class)
@Table(name = "AGY_LOC_ESTABLISHMENTS")
public class AgencyLocationEstablishment {

    @Id
    @Column(name = "AGY_LOC_ID", nullable = false, insertable = false, updatable = false)
    private String agencyLocId;

    @Id
    @Column(name = "ESTABLISHMENT_TYPE", nullable = false, insertable = false, updatable = false)
    private String establishmentType;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        private String agencyLocId;

        private String establishmentType;
    }
}
