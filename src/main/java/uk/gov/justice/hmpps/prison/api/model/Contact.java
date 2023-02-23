package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Contact
 **/
@SuppressWarnings("unused")
@Schema(description = "Contact")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class Contact {
    @Schema(required = true, description = "Last name of the contact", example = "SMITH")
    @NotBlank
    private String lastName;

    @Schema(required = true, description = "First Name", example = "JOHN")
    @NotBlank
    private String firstName;

    @Schema(description = "Middle Names", example = "MARK")
    private String middleName;

    @Schema(required = true, description = "Contact type", example = "O")
    @NotBlank
    private String contactType;

    @Schema(description = "Contact type text", example = "Official")
    private String contactTypeDescription;

    @Schema(required = true, description = "Relationship to prisoner", example = "RO")
    @NotBlank
    private String relationship;

    @Schema(description = "Relationship text", example = "Responsible Officer")
    private String relationshipDescription;

    @Schema(description = "Comments", example = "Some additional information")
    private String commentText;

    @Schema(required = true, description = "Is an emergency contact", example = "true")
    @NotNull
    private boolean emergencyContact;

    @Schema(required = true, description = "Indicates that the contact is Next of Kin Type", example = "false")
    @NotNull
    private boolean nextOfKin;

    @Schema(description = "ID of the relationship (internal)", example = "10466277")
    private Long relationshipId;

    @Schema(description = "id of the person contact", example = "5871791")
    private Long personId;

    @Schema(required = true, description = "Active indicator flag.", example = "true", allowableValues = {"true","false"})
    private boolean activeFlag;

    @Schema(description = "Date made inactive", example = "2019-01-31")
    private LocalDate expiryDate;

    @Schema(required = true, description = "Approved Visitor", example = "true", allowableValues = {"true","false"})
    private boolean approvedVisitorFlag;

    @Schema(required = true, description = "Can be contacted", example = "false", allowableValues = {"true","false"})
    private boolean canBeContactedFlag;

    @Schema(required = true, description = "Aware of charges against prisoner", example = "true", allowableValues = {"true","false"})
    private boolean awareOfChargesFlag;

    @Schema(description = "Link to root offender ID", example = "5871791")
    private Long contactRootOffenderId;

    @Schema(required = true, description = "Offender Booking Id for this contact", example = "2468081")
    private Long bookingId;

    @Schema(required = true, description = "Date time the contact was created", example = "2020-10-10T:20:00")
    private LocalDateTime createDateTime;
}
