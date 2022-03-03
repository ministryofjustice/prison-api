package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(description = "Staff Details")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class StaffDetail {

    @ApiModelProperty(required = true, value = "Unique identifier for staff member.", position = 1, example = "423142")
    @NotNull
    private Long staffId;

    @ApiModelProperty(required = true, value = "Staff member's first name.", position = 2, example = "JOHN")
    @NotBlank
    private String firstName;

    @ApiModelProperty(required = true, value = "Staff member's last name.", position = 3, example = "SMITH")
    @NotBlank
    private String lastName;

    @ApiModelProperty(required = true, value = "Status of staff member.", position = 4, example = "ACTIVE", allowableValues = "ACTIVE,INACTIVE")
    @NotBlank
    private String status;

    @ApiModelProperty(value = "Identifier for staff member image.", position = 5, example = "231232")
    private Long thumbnailId;

    @ApiModelProperty(value = "Gender of Staff Member", position = 6, example = "M", allowableValues = "M,F,NK,NS,REF")
    private String gender;

    @ApiModelProperty(value = "Date of Birth of Staff Member", position = 7, example = "1970-01-02")
    private LocalDate dateOfBirth;

}
