package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Contact
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Contact")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class Contact {
    @ApiModelProperty(required = true, value = "Last name of the contact", example = "SMITH", position = 1)
    @NotBlank
    private String lastName;

    @ApiModelProperty(required = true, value = "First Name", example = "JOHN", position = 2)
    @NotBlank
    private String firstName;

    @ApiModelProperty(value = "Middle Names", example = "MARK", position = 3)
    private String middleName;

    @ApiModelProperty(required = true, value = "Contact type", example = "O", position = 4)
    @NotBlank
    private String contactType;

    @ApiModelProperty(value = "Contact type text", example = "Official", position = 5)
    private String contactTypeDescription;

    @ApiModelProperty(required = true, value = "Relationship to prisoner", example = "RO", position = 6)
    @NotBlank
    private String relationship;

    @ApiModelProperty(value = "Relationship text", example = "Responsible Officer", position = 7)
    private String relationshipDescription;

    @ApiModelProperty(value = "Comments", example = "Some additional information", position = 8)
    private String commentText;

    @ApiModelProperty(required = true, value = "Is an emergency contact", example = "true", position = 9)
    @NotNull
    private boolean emergencyContact;

    @ApiModelProperty(required = true, value = "Indicates that the contact is Next of Kin Type", example = "false", position = 10)
    @NotNull
    private boolean nextOfKin;

    @ApiModelProperty(value = "ID of the relationship (internal)", example = "10466277", position = 11)
    private Long relationshipId;

    @ApiModelProperty(value = "id of the person contact", example = "5871791", position = 12)
    private Long personId;

    @ApiModelProperty(required = true, value = "Active indicator flag.", example = "true", allowableValues = "true,false", position = 12)
    private boolean activeFlag;

    @ApiModelProperty(value = "Date made inactive", example = "2019-01-31", position = 13)
    private LocalDate expiryDate;

    @ApiModelProperty(required = true, value = "Approved Visitor", example = "true", allowableValues = "true,false", position = 14)
    private boolean approvedVisitorFlag;

    @ApiModelProperty(required = true, value = "Can be contacted", example = "false", allowableValues = "true,false", position = 15)
    private boolean canBeContactedFlag;

    @ApiModelProperty(required = true, value = "Aware of charges against prisoner", example = "true", allowableValues = "true,false", position = 16)
    private boolean awareOfChargesFlag;

    @ApiModelProperty(value = "Link to root offender ID", example = "5871791", position = 17)
    private Long contactRootOffenderId;

    @ApiModelProperty(required = true, value = "Offender Booking Id for this contact", example = "2468081", position = 18)
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Date time the contact was created", example = "2020-10-10T:20:00", position = 19)
    private LocalDateTime createDateTime;
}
