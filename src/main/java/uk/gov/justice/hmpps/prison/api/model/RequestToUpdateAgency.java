package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@ApiModel(description = "Update Agency Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonPropertyOrder({ "description", "longDescription", "agencyType", "active"})
public class RequestToUpdateAgency {
    @NotBlank
    @ApiModelProperty(required = true, value = "Agency description.", example = "Moorland (HMP & YOI)", position = 1)
    @Length(max = 40, min = 3, message = "Agency description is max 40 characters and min of 3")
    private String description;

    @ApiModelProperty(value = "Long description of the agency", example = "Moorland (HMP & YOI)", position = 2)
    @Length(max = 3000, min = 3, message = "Agency long description is max 3000 characters and min of 3")
    private String longDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "Agency type.  Reference domain is AGY_LOC_TYPE", example = "INST", allowableValues = "CRC,POLSTN,INST,COMM,APPR,CRT,POLICE,IMDC,TRN,OUT,YOT,SCH,STC,HOST,AIRPORT,HSHOSP,HOSPITAL,PECS,PAR,PNP,PSY", position = 3)
    @Valid @Length(max = 12, min = 2, message = "Agency Type is max 12 characters and min of 2")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Invalid characters for agencyType")
    private String agencyType;

    @ApiModelProperty(value = "Indicates the Agency is active",  notes = "if set false, the current date will be the deactivation date", example = "true", position = 4)
    @Default
    private boolean active = true;
}
