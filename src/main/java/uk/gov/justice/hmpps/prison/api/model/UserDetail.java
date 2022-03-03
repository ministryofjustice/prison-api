package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.text.WordUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * User Details
 **/
@SuppressWarnings("unused")
@ApiModel(description = "User Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDetail {
    @ApiModelProperty(required = true, value = "Staff Id", example = "231232")
    @NotNull
    private Long staffId;

    @ApiModelProperty(required = true, value = "Username", example = "DEMO_USER1", position = 1)
    @NotBlank
    private String username;

    @ApiModelProperty(required = true, value = "First Name", example = "John", position = 2)
    @NotBlank
    private String firstName;

    @ApiModelProperty(required = true, value = "Last Name", example = "Smith", position = 3)
    @NotBlank
    private String lastName;

    @ApiModelProperty(value = "Image Thumbnail Id", example = "2342341224", position = 4)
    private Long thumbnailId;

    @ApiModelProperty(value = "Current Active Caseload", example = "MDI", position = 5)
    private String activeCaseLoadId;

    @ApiModelProperty(required = true, value = "Status of the User Account", allowableValues = "ACTIVE,INACT,SUS,CAREER,MAT,SAB,SICK", example = "ACTIVE", position = 6)
    private String accountStatus;

    @ApiModelProperty(required = true, value = "Date the user account was locked", example = "2018-06-04T12:35:00", position = 7)
    private LocalDateTime lockDate;

    @ApiModelProperty(value = "Date the user account has expired", example = "2018-01-04T12:35:00", position = 8)
    private LocalDateTime expiryDate;

    @ApiModelProperty(value = "The User account is locked", example = "false", position = 9)
    private Boolean lockedFlag;

    @ApiModelProperty(value = "Indicates the user account has expired", example = "true", position = 10)
    private Boolean expiredFlag;

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    private Map<String, Object> additionalProperties;

    @ApiModelProperty(required = true, value = "Indicate if the account is active", example = "true", position = 11)
    @JsonGetter
    public boolean isActive() {
        return "ACTIVE".equals(accountStatus);
    }

    public String getFirstName() {
        return WordUtils.capitalizeFully(firstName);
    }

    public String getLastName() {
        return WordUtils.capitalizeFully(lastName);
    }
}
