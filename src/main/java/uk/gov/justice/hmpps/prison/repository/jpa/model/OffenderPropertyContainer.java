package uk.gov.justice.hmpps.prison.repository.jpa.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import static org.hibernate.annotations.NotFoundAction.IGNORE;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.PropertyContainer.CONTAINER;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_PPTY_CONTAINERS")
public class OffenderPropertyContainer {

    @Id
    @Column(name = "PROPERTY_CONTAINER_ID")
    private Long containerId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INTERNAL_LOCATION_ID")
    private AgencyInternalLocation internalLocation;

    @Column(name = "ACTIVE_FLAG")
    private String activeFlag;

    @Column(name = "SEAL_MARK")
    private String sealMark;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + CONTAINER + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "CONTAINER_CODE", referencedColumnName = "code"))
    })
    private PropertyContainer containerType;

    public boolean isActive() {
        return activeFlag != null && activeFlag.equals("Y");
    }


}
