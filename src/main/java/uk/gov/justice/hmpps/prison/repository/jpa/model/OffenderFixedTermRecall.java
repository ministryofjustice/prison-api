package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.*;
import org.hibernate.annotations.BatchSize;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceAndOffences;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerm;
import uk.gov.justice.hmpps.prison.api.model.ReturnToCustodyDate;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"bookingId"}, callSuper = false)
@Table(name = "OFFENDER_FIXED_TERM_RECALLS")
public class OffenderFixedTermRecall extends AuditableEntity {

    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    @PrimaryKeyJoinColumn
    private OffenderBooking offenderBooking;

    @Column(name = "RETURN_TO_CUSTODY_DATE")
    private LocalDate returnToCustodyDate;

    public ReturnToCustodyDate mapToReturnToCustody() {
        return ReturnToCustodyDate.builder()
            .bookingId(offenderBooking.getBookingId())
            .returnToCustodyDate(returnToCustodyDate)
            .build();
    }
}
