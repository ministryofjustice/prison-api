package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Immutable
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class VisitInformation {
    @Id
    private Long visitId;
    private Long bookingId;
    private String cancellationReason;
    private String cancelReasonDescription;
    private String eventStatus;
    private String eventStatusDescription;
    private String eventOutcome;
    private String eventOutcomeDescription;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String visitType;
    private String visitTypeDescription;
    private String leadVisitor;
    private String relationship;
    private String relationshipDescription;
}
