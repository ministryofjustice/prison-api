package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.*;

import java.time.LocalDateTime;

import static net.syscon.elite.repository.jpa.model.ReferenceCode.*;
import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_VISITS")
public class OffenderVisit {
    @Id
    @Column(name = "OFFENDER_VISIT_ID", nullable = false)
    private Long visitId;

    @Column(name = "OFFENDER_BOOK_ID", nullable = false)
    private Long bookingId;

    @Column(name = "VISIT_INTERNAL_LOCATION_ID", nullable = false)
    private Long visitLocationId;

    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + VISIT_TYPE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "VISIT_TYPE", referencedColumnName = "code"))
    })
    private VisitType visitType;

}
