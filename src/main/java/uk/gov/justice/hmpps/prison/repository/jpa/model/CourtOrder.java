package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.With;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This represents the outcomes of offences in after going to court and linked to a {@link CourtEvent} by the id.
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "ORDERS")
@With
public class CourtOrder extends AuditableEntity {

    @Id
    @Column(name = "ORDER_ID", nullable = false)
    @SequenceGenerator(name = "ORDER_ID", sequenceName = "ORDER_ID", allocationSize = 1)
    @GeneratedValue(generator = "ORDER_ID")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "CASE_ID", nullable = false)
    private OffenderCourtCase courtCase;

    @OneToMany(mappedBy = "courtOrder")
    @Default
    private final List<OffenderSentence> sentences = new ArrayList<>();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ISSUING_AGY_LOC_ID", nullable = false)
    private AgencyLocation issuingCourt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID")
    private CourtEvent courtEvent;

    private String orderType;

    private LocalDate courtDate;

    private String orderStatus;
}
