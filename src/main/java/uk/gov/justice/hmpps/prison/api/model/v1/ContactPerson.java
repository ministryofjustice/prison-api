package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.List;

@Schema(description = "Contact Person")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "_id")
@JsonInclude(Include.NON_NULL)
@ToString
@JsonPropertyOrder({"id", "given_name", "middle_names", "surname", "date_of_birth",
        "gender", "relationship_type", "contact_type", "approved_visitor", "active", "restrictions"})

public class ContactPerson {

    @JsonIgnore
    private Long _id;

    @Schema(description = "ID", name = "id", example = "1234567")
    private Long id;

    @Schema(description = "Given Name", name = "given_name", example = "JENNIFER")
    @JsonProperty("given_name")
    private String firstName;

    @Schema(description = "Middle Names", name = "middle_names", example = "ESMERALADA JANE")
    @JsonProperty("middle_names")
    private String middleName;

    @Schema(description = "Last Name", name = "surname", example = "HALLIBUT")
    @JsonProperty("surname")
    private String lastName;

    @Schema(description = "Date of Birth", name = "date_of_birth", example = "1970-01-01")
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @Schema(description = "Gender", name = "gender")
    @JsonProperty("gender")
    private CodeDescription gender;

    @Schema(description = "Relationship Type", name = "relationship type")
    @JsonProperty("relationship_type")
    private CodeDescription relationshipType;

    @Schema(description = "Contact Type", name = "contact type")
    @JsonProperty("contact_type")
    private CodeDescription contactType;

    @Schema(description = "Approved Visitor", name = "approved visitor")
    @JsonProperty("approved_visitor")
    private boolean approvedVisitor;

    @Schema(description = "Active", name = "active")
    @JsonProperty("active")
    private boolean active;

    @Schema(description = "Restrictions", name = "restrictions")
    @JsonProperty("restrictions")
    private List<VisitRestriction> restrictions;
}
