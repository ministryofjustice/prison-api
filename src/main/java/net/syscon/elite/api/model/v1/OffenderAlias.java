package net.syscon.elite.api.model.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"given_name", "middle_names", "surname", "date_of_birth"})
@Builder
@EqualsAndHashCode
@ToString
public class OffenderAlias {

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("middle_names")
    private String middleNames;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("date_of_birth")
    private LocalDate birthDate;
}
