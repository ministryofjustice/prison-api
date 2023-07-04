package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Contact
 **/
@Schema(description = "Offender Contact")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class OffenderContact {
    @Schema(requiredMode = REQUIRED, description = "Last name of the contact", example = "Smith")
    private String lastName;

    @Schema(requiredMode = REQUIRED, description = "First Name", example = "John")
    private String firstName;

    @Schema(description = "Middle Names", example = "Mark")
    private String middleName;

    @Schema(description = "date of birth", example = "1980-01-01")
    private LocalDate dateOfBirth;

    @Schema(requiredMode = REQUIRED, description = "Contact type", example = "O")
    private String contactType;

    @Schema(description = "Contact type text", example = "Official")
    private String contactTypeDescription;

    @Schema(requiredMode = REQUIRED, description = "Relationship to prisoner", example = "RO")
    private String relationshipCode;

    @Schema(description = "Relationship text", example = "Responsible Officer")
    private String relationshipDescription;

    @Schema(description = "Comments", example = "Some additional information")
    private String commentText;

    @Schema(requiredMode = REQUIRED, description = "Is an emergency contact", example = "true")
    private boolean emergencyContact;

    @Schema(requiredMode = REQUIRED, description = "Indicates that the contact is Next of Kin Type", example = "false")
    private boolean nextOfKin;

    @Schema(description = "id of the person", example = "5871791")
    private Long personId;

    @Schema(requiredMode = REQUIRED, description = "Approved Visitor", example = "true")
    private boolean approvedVisitor;

    @Schema(requiredMode = REQUIRED, description = "Offender Booking Id for this contact", example = "2468081")
    private Long bookingId;

    @Schema(description = "List of emails associated with the contact")
    private List<Email> emails;

    @Schema(description = "List of phone numbers associated with the contact")
    private List<Telephone> phones;

    @Schema(description = "List of restrictions associated with the contact")
    private List<VisitorRestriction> restrictions;

    @Schema(requiredMode = REQUIRED, description = "active contact", example = "true")
    private boolean active;
}
