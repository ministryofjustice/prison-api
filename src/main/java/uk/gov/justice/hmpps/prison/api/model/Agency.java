package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@ApiModel(description = "Agency Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Agency {
    @ApiModelProperty(required = true, value = "Agency identifier.", example = "MDI", position = 1)
    private String agencyId;

    @ApiModelProperty(required = true, value = "Agency description.", example = "Moorland (HMP & YOI)", position = 2)
    private String description;

    @ApiModelProperty(value = "Long description of the agency", example = "Moorland (HMP & YOI)", position = 3)
    private String longDescription;

    @ApiModelProperty(required = true, value = "Agency type.  Reference domain is AGY_LOC_TYPE", example = "INST", allowableValues = "CRC,POLSTN,INST,COMM,APPR,CRT,POLICE,IMDC,TRN,OUT,YOT,SCH,STC,HOST,AIRPORT,HSHOSP,HOSPITAL,PECS,PAR,PNP,PSY", position = 4)
    private String agencyType;

    @ApiModelProperty(required = true, value = "Indicates the Agency is active", example = "true", position = 5)
    @Default
    private boolean active = true;

    @ApiModelProperty(value = "Date agency became inactive", example = "2012-01-12", position = 6)
    private LocalDate deactivationDate;

    @ApiModelProperty(value = "List of addresses associated with agency",  position = 7)
    private List<AddressDto> addresses;
}
