package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(description = "Offender non-association detail")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
public class OffenderNonAssociationDetail {

    @Schema(requiredMode = REQUIRED, description = "The non-association reason code", example = "VIC")
    private String reasonCode;

    @Schema(requiredMode = REQUIRED, description = "The non-association reason description", example = "Victim")
    private String reasonDescription;

    @Schema(requiredMode = REQUIRED, description = "The non-association type code", example = "WING")
    private String typeCode;

    @Schema(requiredMode = REQUIRED, description = "The non-association type description", example = "Do Not Locate on Same Wing")
    private String typeDescription;

    @Schema(requiredMode = REQUIRED, description = "Date and time the non-association is effective from", example = "2019-12-01")
    private LocalDate effectiveDate;

    @Schema(description = "Date and time the non-association expires", example = "2019-12-01")
    private LocalDate expiryDate;

    @Schema(description = "The person who authorised the non-association (free text).")
    private String authorisedBy;

    @Schema(description = "Additional free text comments related to the non-association.")
    private String comments;

    @Schema(requiredMode = REQUIRED, description = "The offender with whom not to associate.")
    private OffenderNonAssociation offenderNonAssociation;
}
