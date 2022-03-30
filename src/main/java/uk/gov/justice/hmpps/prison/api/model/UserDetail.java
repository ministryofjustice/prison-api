package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.text.WordUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * User Details
 **/
@SuppressWarnings("unused")
@Schema(description = "User Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class UserDetail {
    @Schema(required = true, description = "Staff Id", example = "231232")
    @NotNull
    private Long staffId;

    @Schema(required = true, description = "Username", example = "DEMO_USER1")
    @NotBlank
    private String username;

    @Schema(required = true, description = "First Name", example = "John")
    @NotBlank
    private String firstName;

    @Schema(required = true, description = "Last Name", example = "Smith")
    @NotBlank
    private String lastName;

    @Schema(description = "Image Thumbnail Id", example = "2342341224")
    private Long thumbnailId;

    @Schema(description = "Current Active Caseload", example = "MDI")
    private String activeCaseLoadId;

    @Schema(required = true, description = "Status of the User Account", allowableValues = {"ACTIVE","INACT","SUS","CAREER","MAT","SAB","SICK"}, example = "ACTIVE")
    private String accountStatus;

    @Schema(required = true, description = "Date the user account was locked", example = "2018-06-04T12:35:00")
    private LocalDateTime lockDate;

    @Schema(description = "Date the user account has expired", example = "2018-01-04T12:35:00")
    private LocalDateTime expiryDate;

    @Schema(description = "The User account is locked", example = "false")
    private Boolean lockedFlag;

    @Schema(description = "Indicates the user account has expired", example = "true")
    private Boolean expiredFlag;

    public UserDetail(@NotNull Long staffId, @NotBlank String username, @NotBlank String firstName, @NotBlank String lastName, Long thumbnailId, String activeCaseLoadId, String accountStatus, LocalDateTime lockDate, LocalDateTime expiryDate, Boolean lockedFlag, Boolean expiredFlag) {
        this.staffId = staffId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.thumbnailId = thumbnailId;
        this.activeCaseLoadId = activeCaseLoadId;
        this.accountStatus = accountStatus;
        this.lockDate = lockDate;
        this.expiryDate = expiryDate;
        this.lockedFlag = lockedFlag;
        this.expiredFlag = expiredFlag;
    }

    public UserDetail() {
    }

    @Schema(required = true, description = "Indicate if the account is active", example = "true")
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
