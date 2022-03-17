package uk.gov.justice.hmpps.prison.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "Details required for IEP review for offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder(toBuilder = true)
public class OffenderIepReview {
    @Schema(description = "Booking ID of offender", required = true, example = "1111111")
    private long bookingId;

    @Schema(description = "Number for case notes of type POS and subtype IEP_ENC", required = true, example = "3")
    private int positiveIeps;

    @Schema(description = "Number for case notes of type NEG and subtype IEP_WARN", required = true, example = "3")
    private int negativeIeps;

    @Schema(description = "Number of proven adjudications", required = true, example = "3")
    private int provenAdjudications;

    @Schema(description = "Date of last IEP review", example = "2017-03-17T08:02:00")
    private LocalDateTime lastReviewTime;

    @Schema(description = "The current IEP level for offender", example = "Basic")
    private String currentLevel;

    @Schema(description = "Offender first name", required = true, example = "John")
    private String firstName;

    @Schema(description = "Offender middle name", example = "James")
    private String middleName;

    @Schema(description = "Offender last name", required = true, example = "Smith")
    private String lastName;

    @Schema(description = "The current cell location of Offender", required = true, example = "LEI-A-3-003")
    private String cellLocation;

    @Schema(description = "Offender Number", required = true, example = "G1401GN")
    private String offenderNo;

    public OffenderIepReview(long bookingId, int positiveIeps, int negativeIeps, int provenAdjudications, LocalDateTime lastReviewTime, String currentLevel, String firstName, String middleName, String lastName, String cellLocation, String offenderNo) {
        this.bookingId = bookingId;
        this.positiveIeps = positiveIeps;
        this.negativeIeps = negativeIeps;
        this.provenAdjudications = provenAdjudications;
        this.lastReviewTime = lastReviewTime;
        this.currentLevel = currentLevel;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.cellLocation = cellLocation;
        this.offenderNo = offenderNo;
    }

    public OffenderIepReview() {
    }
}
