package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@ApiModel(description = "Offender non-association detail")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class OffenderNonAssociationDetail {

    @ApiModelProperty(required = true, value = "The offenders noms ID", position = 1, example = "G9109UD")
    private String offenderNomsId;

    @ApiModelProperty(required = true, value = "The offenders first name", position = 2, example = "Fred")
    private String firstName;

    @ApiModelProperty(required = true, value = "The offenders last name", position = 3, example = "Bloggs")
    private String lastName;

    @ApiModelProperty(required = true, value = "The non-association reason code", position = 4, example = "VIC")
    private String reasonCode;

    @ApiModelProperty(required = true, value = "The non-association reason description", position = 5, example = "Victim")
    private String reasonDescription;

    @ApiModelProperty(required = true, value = "The non-association type code", position = 6, example = "WING")
    private String typeCode;

    @ApiModelProperty(required = true, value = "The non-association type description", position = 7, example = "Do Not Locate on Same Wing")
    private String typeDescription;

    @ApiModelProperty(required = true, value = "Date and time the mom-association is effective from. In Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", position = 8, example = "2019-12-01T13:34:00")
    private LocalDateTime effectiveDate;

    @ApiModelProperty(value = "Date and time the mom-association expires. In Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", position = 9, example = "2019-12-01T13:34:00")
    private LocalDateTime expiryDate;

    @ApiModelProperty(value = "The person who authorised the non-association (free text).", position = 10)
    private String authorisedBy;

    @ApiModelProperty(value = "Additional free text comments related to the non-association.", position = 11)
    private String comments;

    @ApiModelProperty(required = true, value = "Description of the agency (e.g. prison) the offender is assigned to.", position = 12, example = "Moorland (HMP & YOI)")
    private String agencyDescription;

    @ApiModelProperty(required = true, value = "Description of living unit (e.g. cell) the offender is assigned to.", position = 13, example = "MDI-1-1-3")
    private String assignedLivingUnitDescription;

    @ApiModelProperty(required = true, value = "The offender with whom not to associate.", position = 14)
    private OffenderNonAssociation offenderNonAssociation;
}
