package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * Offender Sentence Calculation
 **/
@SuppressWarnings("unused")
@Schema(description = "Offender Sentence Calculation")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OffenderSentenceCalculation {
    private Long bookingId;
    private String offenderNo;
    private String firstName;
    private String lastName;
    private String agencyLocationId;
    private Long offenderSentCalculationId;
    private LocalDateTime calculationDate;

    private LocalDate sentenceExpiryDate;
    private LocalDate licenceExpiryDate;
    private LocalDate paroleEligibilityDate;
    private LocalDate homeDetCurfEligibilityDate;
    private LocalDate homeDetCurfActualDate;
    private LocalDate automaticReleaseDate;
    private LocalDate conditionalReleaseDate;
    private LocalDate nonParoleDate;
    private LocalDate postRecallReleaseDate;
    private LocalDate actualParolDate;
    private LocalDate topupSupervisionExpiryDate;
    private LocalDate earlyTermDate;
    private LocalDate midTermDate;
    private LocalDate lateTermDate;
    private LocalDate tariffDate;
    private LocalDate rotl;
    private LocalDate ersed;
    private String commentText;
}
