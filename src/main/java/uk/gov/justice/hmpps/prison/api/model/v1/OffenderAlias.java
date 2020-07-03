package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@ApiModel(description = "Offender Alias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"given_name", "middle_names", "surname", "date_of_birth"})
@Builder
@EqualsAndHashCode
@ToString
public class OffenderAlias {

    @ApiModelProperty(value = "Given Name", name = "given_name", example = "JENNIFER", position = 0)
    @JsonProperty("given_name")
    private String givenName;

    @ApiModelProperty(value = "Middle Names", name = "middle_names", example = "ESMERALADA JANE", position = 1)
    @JsonProperty("middle_names")
    private String middleNames;

    @ApiModelProperty(value = "Surname", name = "surname", example = "HALLIBUT", position = 2)
    private String surname;

    @ApiModelProperty(value = "Date of Birth", name = "date_of_birth", example = "1970-01-01", position = 3)
    @JsonProperty("date_of_birth")
    private LocalDate birthDate;
}
