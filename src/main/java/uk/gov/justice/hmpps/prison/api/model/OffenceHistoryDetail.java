package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Offence History Item")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OffenceHistoryDetail {

    @Schema(required = true, description = "Prisoner booking id", example = "1123456")
    @NotNull
    private Long bookingId;

    @Schema(required = true, description = "Date the offence took place", example = "2018-02-10")
    @NotNull
    private LocalDate offenceDate;

    @Schema(description = "End date if range the offence was believed to have taken place", example = "2018-03-10")
    private LocalDate offenceRangeDate;

    @Schema(required = true, description = "Description associated with the offence code", example = "Commit an act / series of acts with intent to pervert the course of public justice")
    @NotBlank
    private String offenceDescription;

    @Schema(required = true, description = "Reference Code", example = "RR84070")
    @NotBlank
    private String offenceCode;

    @Schema(required = true, description = "Statute code", example = "RR84")
    @NotBlank
    private String statuteCode;

    @Schema(required = true, description = "Identifies the main offence per booking")
    private Boolean mostSerious;

    @Schema(description = "Primary result code ")
    private String primaryResultCode;

    @Schema(description = "Secondary result code")
    private String secondaryResultCode;

    @Schema(description = "Description for Primary result")
    private String primaryResultDescription;

    @Schema(description = "Description for Secondary result")
    private String secondaryResultDescription;

    @Schema(description = "Conviction flag for Primary result ")
    private Boolean primaryResultConviction;

    @Schema(description = "Conviction flag for Secondary result ")
    private Boolean secondaryResultConviction;

    @Schema(description = "Latest court date associated with the offence", example = "2018-02-10")
    private LocalDate courtDate;

    @Schema(description = "Court case id", example = "100")
    private Long caseId;

    public OffenceHistoryDetail(@NotNull Long bookingId, @NotNull LocalDate offenceDate, LocalDate offenceRangeDate, @NotBlank String offenceDescription, @NotBlank String offenceCode, @NotBlank String statuteCode, Boolean mostSerious, String primaryResultCode, String secondaryResultCode, String primaryResultDescription, String secondaryResultDescription, Boolean primaryResultConviction, Boolean secondaryResultConviction, LocalDate courtDate, Long caseId) {
        this.bookingId = bookingId;
        this.offenceDate = offenceDate;
        this.offenceRangeDate = offenceRangeDate;
        this.offenceDescription = offenceDescription;
        this.offenceCode = offenceCode;
        this.statuteCode = statuteCode;
        this.mostSerious = mostSerious;
        this.primaryResultCode = primaryResultCode;
        this.secondaryResultCode = secondaryResultCode;
        this.primaryResultDescription = primaryResultDescription;
        this.secondaryResultDescription = secondaryResultDescription;
        this.primaryResultConviction = primaryResultConviction;
        this.secondaryResultConviction = secondaryResultConviction;
        this.courtDate = courtDate;
        this.caseId = caseId;
    }

    public OffenceHistoryDetail() {
    }
}
