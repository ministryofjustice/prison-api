package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Schema(description = "Update Agency Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonPropertyOrder({ "description", "longDescription", "agencyType", "active"})
public class RequestToUpdateAgency {
    @NotBlank
    @Schema(required = true, description = "Agency description.", example = "Moorland (HMP & YOI)")
    @Length(max = 40, min = 3, message = "Agency description is max 40 characters and min of 3")
    private String description;

    @Schema(description = "Long description of the agency", example = "Moorland (HMP & YOI)")
    @Length(max = 3000, min = 3, message = "Agency long description is max 3000 characters and min of 3")
    private String longDescription;

    @NotBlank
    @Schema(required = true, description = "Agency type.  Reference domain is AGY_LOC_TYPE", example = "INST", allowableValues = "CRC,POLSTN,INST,COMM,APPR,CRT,POLICE,IMDC,TRN,OUT,YOT,SCH,STC,HOST,AIRPORT,HSHOSP,HOSPITAL,PECS,PAR,PNP,PSY")
    @Valid @Length(max = 12, min = 2, message = "Agency Type is max 12 characters and min of 2")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Invalid characters for agencyType")
    private String agencyType;

    @Schema(required = true, description = "Court Type.  Reference domain is JURISDICTION", example = "CC", allowableValues = "CACD,CB,CC,CO,DCM,GCM,IMM,MC,OTHER,YC")
    @Valid @Length(max = 12, min = 2, message = "Court Type is max 12 characters and min of 2")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Invalid characters for Court Type")
    private String courtType;

    @Schema(description = "Indicates the Agency is active. Note: If set false, the current date will be the deactivation date", example = "true")
    @Default
    private boolean active = true;
}
