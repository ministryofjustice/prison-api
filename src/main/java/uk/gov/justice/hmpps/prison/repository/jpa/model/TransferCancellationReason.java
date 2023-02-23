package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(TransferCancellationReason.TRANSFER_CANCELLATION_REASON)
@NoArgsConstructor
public class TransferCancellationReason extends ReferenceCode {

    static final String TRANSFER_CANCELLATION_REASON = "TRN_CNCL_RSN";

    public TransferCancellationReason(final String code, final String description) {
        super(TRANSFER_CANCELLATION_REASON, code, description);
    }

    public static ReferenceCode.Pk pk(final String key) {
        return new Pk(TRANSFER_CANCELLATION_REASON, key);
    }
}
