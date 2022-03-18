package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Offender non-association detail")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class OffenderNonAssociationDetail {

    @Schema(required = true, description = "The non-association reason code", example = "VIC")
    private String reasonCode;

    @Schema(required = true, description = "The non-association reason description", example = "Victim")
    private String reasonDescription;

    @Schema(required = true, description = "The non-association type code", example = "WING")
    private String typeCode;

    @Schema(required = true, description = "The non-association type description", example = "Do Not Locate on Same Wing")
    private String typeDescription;

    @Schema(required = true, description = "Date and time the mom-association is effective from. In Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime effectiveDate;

    @Schema(description = "Date and time the mom-association expires. In Europe/London (ISO 8601) format without timezone offset e.g. YYYY-MM-DDTHH:MM:SS.", example = "2019-12-01T13:34:00")
    private LocalDateTime expiryDate;

    @Schema(description = "The person who authorised the non-association (free text).")
    private String authorisedBy;

    @Schema(description = "Additional free text comments related to the non-association.")
    private String comments;

    @Schema(required = true, description = "The offender with whom not to associate.")
    private OffenderNonAssociation offenderNonAssociation;
}
