package uk.gov.justice.hmpps.prison.repository.jpa.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue(OffenderPhone.PHONE_TYPE)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Data
@EqualsAndHashCode(exclude = "offender", callSuper = true)
@ToString(callSuper = true)
public class OffenderPhone extends Phone {

    static final String PHONE_TYPE = "OFF";

    @JoinColumn(name = "OWNER_ID")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Offender offender;
}
