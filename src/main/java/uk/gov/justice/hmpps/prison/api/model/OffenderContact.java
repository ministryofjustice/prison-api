package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "Offender Contact")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class OffenderContact {
    @ApiModelProperty(required = true, value = "Last name of the contact", example = "Smith")
    private String lastName;

    @ApiModelProperty(required = true, value = "First Name", example = "John")
    private String firstName;

    @ApiModelProperty(value = "Middle Names", example = "Mark")
    private String middleName;

    @ApiModelProperty(value = "date of birth", example = "1980-01-01")
    private LocalDate dateOfBirth;

    @ApiModelProperty(required = true, value = "Contact type", example = "O")
    private String contactType;

    @ApiModelProperty(value = "Contact type text", example = "Official")
    private String contactTypeDescription;

    @ApiModelProperty(required = true, value = "Relationship to prisoner", example = "RO")
    private String relationshipCode;

    @ApiModelProperty(value = "Relationship text", example = "Responsible Officer")
    private String relationshipDescription;

    @ApiModelProperty(value = "Comments", example = "Some additional information")
    private String commentText;

    @ApiModelProperty(required = true, value = "Is an emergency contact", example = "true")
    private boolean emergencyContact;

    @ApiModelProperty(required = true, value = "Indicates that the contact is Next of Kin Type", example = "false")
    private boolean nextOfKin;

    @ApiModelProperty(value = "id of the person", example = "5871791")
    private Long personId;

    @ApiModelProperty(required = true, value = "Approved Visitor", example = "true")
    private boolean approvedVisitor;

    @ApiModelProperty(required = true, value = "Offender Booking Id for this contact", example = "2468081")
    private Long bookingId;

    @ApiModelProperty(value = "List of emails associated with the contact")
    private List<Email> emails;

    @ApiModelProperty(value = "List of restrictions associated with the contact")
    private List<VisitorRestriction> restrictions;

    @Schema(required = true, description = "active contact", example = "true")
    private boolean active;
}
