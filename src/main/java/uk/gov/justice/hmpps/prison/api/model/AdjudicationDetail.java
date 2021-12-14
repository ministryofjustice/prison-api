package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@ApiModel(description = "AdjudicationDetail")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class AdjudicationDetail {
    @ApiModelProperty(value = "Adjudication number", position = 1, example = "123")
    @NotNull
    private Long adjudicationNumber;

    @ApiModelProperty(value = "The staff id of the reporter", position = 2, example = "123456")
    @NotNull
    private Long reporterStaffId;

    @ApiModelProperty(value = "Booking number", position = 3, example = "123456")
    @NotNull
    private Long bookingId;

    @ApiModelProperty(value = "Offender number (NOMS ID)", example = "G3878UK")
    @NotNull
    private String offenderNo;

    @ApiModelProperty(value = "The id of the agency related to this incident", example = "MDI")
    @NotNull
    private String agencyId;

    @ApiModelProperty(value = "When the incident took place", position = 4, example = "15-06-2020T09:03:11")
    @NotNull
    private LocalDateTime incidentTime;

    @ApiModelProperty(value = "The id to indicate where the incident took place", notes = "This will be an agency's internal location id", position = 5)
    @NotNull
    private Long incidentLocationId;

    @ApiModelProperty(value = "The adjudication statement", position = 6, example = "The offence involved ...")
    @NotNull
    private String statement;

    @ApiModelProperty(value = "The id of the user the created the adjudication", position = 10, example = "ASMITH")
    private String createdByUserId;
}
