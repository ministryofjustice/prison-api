package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Adjudication award / sanction")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@EqualsAndHashCode
public class Award {

    @Schema(required = true, description = "Id of booking")
    private Long bookingId;

    @Schema(required = true, description = "Type of award")
    private String sanctionCode;

    @Schema(description = "Award type description")
    private String sanctionCodeDescription;

    @Schema(description = "Number of months duration")
    private Integer months;

    @Schema(description = "Number of days duration")
    private Integer days;

    @Schema(description = "Compensation amount")
    private BigDecimal limit;

    @Schema(description = "Optional details")
    private String comment;

    @Schema(required = true, description = "Start of sanction")
    private LocalDate effectiveDate;

    @Schema(description = "Award status (ref domain OIC_SANCT_ST)")
    private String status;

    @Schema(description = "Award status description")
    private String statusDescription;

    @Schema(required = true, description = "Id of hearing")
    private Long hearingId;

    @Schema(required = true, description = "hearing record sequence number")
    private Integer hearingSequence;

    private Long adjudicationNumber;

    private Long reportId;

    public Award(Long bookingId, String sanctionCode, String sanctionCodeDescription, Integer months, Integer days, BigDecimal limit, String comment, LocalDate effectiveDate, String status, String statusDescription, Long hearingId, Integer hearingSequence, Long adjudicationNumber, Long reportId) {
        this.bookingId = bookingId;
        this.sanctionCode = sanctionCode;
        this.sanctionCodeDescription = sanctionCodeDescription;
        this.months = months;
        this.days = days;
        this.limit = limit;
        this.comment = comment;
        this.effectiveDate = effectiveDate;
        this.status = status;
        this.statusDescription = statusDescription;
        this.hearingId = hearingId;
        this.hearingSequence = hearingSequence;
        this.adjudicationNumber = adjudicationNumber;
        this.reportId = reportId;
    }

    public Award() {
    }
}
