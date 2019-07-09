package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"offender_surname", "offender_given_name_1", "offender_dob", "gender", "religion", "security_category", "nationality", "ethnicity"})
public class PssPersonalData {

    @ApiModelProperty(value = "Offender surname", name = "offender_surname", example = "SMITH", position = 0)
    @JsonProperty("offender_surname")
    private String surname;

    @ApiModelProperty(value = "Offender given name", name = "offender_given_name_1", example = "STEPHEN", position = 1)
    @JsonProperty("offender_given_name_1")
    private String givenName;

    @ApiModelProperty(value = "Offender date of birth", name = "offender_dob", example = "1990-12-06 00:00:00", position = 2)
    @JsonProperty("offender_dob")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dob;

    @ApiModelProperty(value = "Gender code and description", name = "gender", position = 3)
    @JsonProperty("gender")
    private CodeDescription gender;

    @ApiModelProperty(value = "Religion code and description", name = "religion", position = 4)
    @JsonProperty("religion")
    private CodeDescription religion;

    @ApiModelProperty(value = "Security category code and description", name = "security_category", position = 5)
    @JsonProperty("security_category")
    private CodeDescription securityCategory;

    @ApiModelProperty(value = "Nationality code and description", name = "nationality", position = 6)
    @JsonProperty("nationality")
    private CodeDescription nationality;

    @ApiModelProperty(value = "Ethnicity code and description", name = "ethnicity", position = 7)
    @JsonProperty("ethnicity")
    private CodeDescription ethnicity;
}
