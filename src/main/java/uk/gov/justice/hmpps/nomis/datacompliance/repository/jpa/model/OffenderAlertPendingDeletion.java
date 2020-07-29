package uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(exclude = {"offenderAlertPK", "offenderBooking"}, callSuper = false)
@ToString(exclude = {"offenderAlertPK", "offenderBooking"})
@Table(name = "OFFENDER_ALERTS")
public class OffenderAlertPendingDeletion {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class OffenderAlertPK implements Serializable {

        @Column(name = "OFFENDER_BOOK_ID", nullable = false)
        private Long offenderId;

        @Column(name = "ALERT_SEQ", nullable = false)
        private Long offenderIdSeq;
    }

    @EmbeddedId
    private OffenderAlertPK offenderAlertPK;

    @ManyToOne
    @JoinColumn(name = "OFFENDER_BOOK_ID", insertable = false, updatable = false)
    private OffenderBookingPendingDeletion offenderBooking;

    @Column(name = "ALERT_CODE", nullable = false)
    private String alertCode;
}
