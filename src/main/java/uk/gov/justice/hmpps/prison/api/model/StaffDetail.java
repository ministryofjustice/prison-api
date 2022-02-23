package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Staff Details
 **/
@SuppressWarnings("unused")
@Schema(description = "Staff Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class StaffDetail {

    @Schema(required = true, description = "Unique identifier for staff member.", example = "423142")
    @NotNull
    private Long staffId;

    @Schema(required = true, description = "Staff member's first name.", example = "JOHN")
    @NotBlank
    private String firstName;

    @Schema(required = true, description = "Staff member's last name.", example = "SMITH")
    @NotBlank
    private String lastName;

    @Schema(required = true, description = "Status of staff member.", example = "ACTIVE", allowableValues = {"ACTIVE","INACTIVE"})
    @NotBlank
    private String status;

    @Schema(description = "Identifier for staff member image.", example = "231232")
    private Long thumbnailId;

    @Schema(description = "Gender of Staff Member", example = "M", allowableValues = {"M","F","NK","NS","REF"})
    private String gender;

    @Schema(description = "Date of Birth of Staff Member", example = "1970-01-02")
    private LocalDate dateOfBirth;

}
