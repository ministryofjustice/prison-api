package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
import java.time.LocalDate;

@ApiModel(description = "Agency Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Agency {
    @NotBlank
    @ApiModelProperty(required = true, value = "Agency identifier.", example = "MDI", position = 1)
    @Valid
    @Length(max = 6, message = "Agency Id is max 6 characters")
    private String agencyId;

    @NotBlank
    @ApiModelProperty(required = true, value = "Agency description.", example = "Moorland (HMP & YOI)", position = 2)
    @Valid @Length(max = 40, message = "Agency description is max 40 characters")
    private String description;

    @ApiModelProperty(required = true, value = "Long description of the agency", example = "Moorland (HMP & YOI)", position = 3)
    @Valid @Length(max = 3000, message = "Agency long description is max 3000 characters")
    private String longDescription;

    @NotBlank
    @ApiModelProperty(required = true, value = "Agency type.  Reference domain is AGY_LOC_TYPE", example = "INST", allowableValues = "CRC,POLSTN,INST,COMM,APPR,CRT,POLICE,IMDC,TRN,OUT,YOT,SCH,STC,HOST,AIRPORT,HSHOSP,HOSPITAL,PECS,PAR,PNP,PSY", position = 4)
    @Valid @Length(max = 12, message = "Agency Type is max 12 characters")
    private String agencyType;

    @ApiModelProperty(value = "Indicates the Agency is active", example = "true", position = 5)
    @Default
    private boolean active = true;

    @ApiModelProperty(value = "Date agency became inactive", example = "2012-01-12", position = 6)
    private LocalDate deactivationDate;
}
