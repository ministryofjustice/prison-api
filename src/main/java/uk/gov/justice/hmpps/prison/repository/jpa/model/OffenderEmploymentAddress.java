package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(exclude = "employment", callSuper = true)
@ToString(exclude = {"employment"}, callSuper = true)
@DiscriminatorValue(OffenderEmploymentAddress.ADDR_TYPE)
public class OffenderEmploymentAddress extends Address {

    static final String ADDR_TYPE = "OFF_EMP";

    @JoinColumns({
        @JoinColumn(name = "OWNER_ID", referencedColumnName = "OFFENDER_BOOK_ID"),
        @JoinColumn(name = "OWNER_SEQ", referencedColumnName = "EMPLOY_SEQ")
    })
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OffenderEmployment employment;
}
