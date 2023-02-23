package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(exclude = "education", callSuper = true)
@ToString(exclude = {"education"}, callSuper = true)
@DiscriminatorValue(OffenderEducationAddress.ADDR_TYPE)
public class OffenderEducationAddress extends Address {

    static final String ADDR_TYPE = "OFF_EDU";

    @JoinColumns({
        @JoinColumn(name = "OWNER_ID", referencedColumnName = "OFFENDER_BOOK_ID"),
        @JoinColumn(name = "OWNER_SEQ", referencedColumnName = "EDUCATION_SEQ")
    })
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private OffenderEducation education;
}
