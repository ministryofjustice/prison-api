package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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
    @Schema(required = true, description = "Last name of the contact", example = "Smith")
    private String lastName;

    @Schema(required = true, description = "First Name", example = "John")
    private String firstName;

    @Schema(description = "Middle Names", example = "Mark")
    private String middleName;

    @Schema(description = "date of birth", example = "1980-01-01")
    private LocalDate dateOfBirth;

    @Schema(required = true, description = "Contact type", example = "O")
    private String contactType;

    @Schema(description = "Contact type text", example = "Official")
    private String contactTypeDescription;

    @Schema(required = true, description = "Relationship to prisoner", example = "RO")
    private String relationshipCode;

    @Schema(description = "Relationship text", example = "Responsible Officer")
    private String relationshipDescription;

    @Schema(description = "Comments", example = "Some additional information")
    private String commentText;

    @Schema(required = true, description = "Is an emergency contact", example = "true")
    private boolean emergencyContact;

    @Schema(required = true, description = "Indicates that the contact is Next of Kin Type", example = "false")
    private boolean nextOfKin;

    @Schema(description = "id of the person", example = "5871791")
    private Long personId;

    @Schema(required = true, description = "Approved Visitor", example = "true")
    private boolean approvedVisitor;

    @Schema(required = true, description = "Offender Booking Id for this contact", example = "2468081")
    private Long bookingId;

    @Schema(description = "List of emails associated with the contact")
    private List<Email> emails;

    @Schema(description = "List of restrictions associated with the contact")
    private List<VisitorRestriction> restrictions;

    @Schema(required = true, description = "active contact", example = "true")
    private boolean active;
}
