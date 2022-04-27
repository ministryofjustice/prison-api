package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.*;

import javax.persistence.*;

@Entity
@DiscriminatorValue(OffenderProgramEndReason.DOMAIN)
@NoArgsConstructor
public class OffenderProgramEndReason extends ReferenceCode {
    public static final String DOMAIN = "PS_END_RSN";

    public static final ReferenceCode.Pk TRF = new ReferenceCode.Pk(DOMAIN, "TRF");

    public OffenderProgramEndReason(final String code, final String description) {
        super(DOMAIN, code, description);
    }

}
