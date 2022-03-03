package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Schema(description = "Offender Alias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"given_name", "middle_names", "surname", "date_of_birth"})
@Builder
@EqualsAndHashCode
@ToString
public class OffenderAlias {

    @Schema(description = "Given Name", name = "given_name", example = "JENNIFER")
    @JsonProperty("given_name")
    private String givenName;

    @Schema(description = "Middle Names", name = "middle_names", example = "ESMERALADA JANE")
    @JsonProperty("middle_names")
    private String middleNames;

    @Schema(description = "Surname", name = "surname", example = "HALLIBUT")
    private String surname;

    @Schema(description = "Date of Birth", name = "date_of_birth", example = "1970-01-01")
    @JsonProperty("date_of_birth")
    private LocalDate birthDate;
}
