package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.hibernate.type.YesNoConverter;
import jakarta.persistence.Convert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "OFFENDER_KEY_DATE_ADJUSTS")
@With
public class KeyDateAdjustment extends AuditableEntity {

    @Id
    @Column(name = "OFFENDER_KEY_DATE_ADJUST_ID", nullable = false)
    @SequenceGenerator(name = "OFFENDER_KEY_DATE_ADJUST_ID", sequenceName = "OFFENDER_KEY_DATE_ADJUST_ID", allocationSize = 1)
    @GeneratedValue(generator = "OFFENDER_KEY_DATE_ADJUST_ID")
    private Long id;

    @Column(name = "SENTENCE_ADJUST_CODE", nullable = false)
    private String sentenceAdjustCode;

    @Column(name = "ADJUST_DAYS")
    private Integer adjustDays;

    @Column
    private LocalDate adjustFromDate;

    @Column
    private LocalDate adjustToDate;

    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking offenderBooking;

    @Column(name = "ACTIVE_FLAG")
    @Convert(converter = YesNoConverter.class)
    private boolean active;
}
