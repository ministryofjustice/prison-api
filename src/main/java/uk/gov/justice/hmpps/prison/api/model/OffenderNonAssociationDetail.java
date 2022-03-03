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

    @ApiModelProperty(required = true, value = "The non-association reason code", position = 1, example = "VIC")
    private String reasonCode;

    @ApiModelProperty(required = true, value = "The non-association reason description", position = 2, example = "Victim")
    private String reasonDescription;

    @ApiModelProperty(required = true, value = "The non-association type code", position = 3, example = "WING")
    private String typeCode;

    @ApiModelProperty(required = true, value = "The non-association type description", position = 4, example = "Do Not Locate on Same Wing")
    private String typeDescription;

    @ApiModelProperty(required = true, value = "Date and time the mom-association is effective from. In Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", position = 5, example = "2019-12-01T13:34:00")
    private LocalDateTime effectiveDate;

    @ApiModelProperty(value = "Date and time the mom-association expires. In Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", position = 6, example = "2019-12-01T13:34:00")
    private LocalDateTime expiryDate;

    @ApiModelProperty(value = "The person who authorised the non-association (free text).", position = 7)
    private String authorisedBy;

    @ApiModelProperty(value = "Additional free text comments related to the non-association.", position = 8)
    private String comments;

    @ApiModelProperty(required = true, value = "The offender with whom not to associate.", position = 9)
    private OffenderNonAssociation offenderNonAssociation;
}
