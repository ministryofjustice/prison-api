package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Offence Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OffenceDetail {

    @Schema(required = true, description = "Prisoner booking id", example = "1123456")
    @NotNull
    private Long bookingId;

    @Schema(required = true, description = "Description of offence")
    @NotBlank
    private String offenceDescription;

    @Schema(required = true, description = "Reference Code", example = "RR84070")
    @NotBlank
    private String offenceCode;

    @Schema(required = true, description = "Statute code", example = "RR84")
    @NotBlank
    private String statuteCode;

    public OffenceDetail(@NotNull Long bookingId, @NotBlank String offenceDescription, @NotBlank String offenceCode, @NotBlank String statuteCode) {
        this.bookingId = bookingId;
        this.offenceDescription = offenceDescription;
        this.offenceCode = offenceCode;
        this.statuteCode = statuteCode;
    }

    public OffenceDetail() {
    }
}
