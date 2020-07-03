package uk.gov.justice.hmpps.prison.api.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.List;

@ApiModel(description = "Contact Person")
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

    @ApiModelProperty(value = "ID", name = "id", example = "1234567", position = 1)
    private Long id;

    @ApiModelProperty(value = "Given Name", name = "given_name", example = "JENNIFER", position = 2)
    @JsonProperty("given_name")
    private String firstName;

    @ApiModelProperty(value = "Middle Names", name = "middle_names", example = "ESMERALADA JANE", position = 3)
    @JsonProperty("middle_names")
    private String middleName;

    @ApiModelProperty(value = "Last Name", name = "surname", example = "HALLIBUT", position = 4)
    @JsonProperty("surname")
    private String lastName;

    @ApiModelProperty(value = "Date of Birth", name = "date_of_birth", example = "1970-01-01", position = 5)
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;

    @ApiModelProperty(value = "Gender", name = "gender", position = 6)
    @JsonProperty("gender")
    private CodeDescription gender;

    @ApiModelProperty(value = "Relationship Type", name = "relationship type", position = 7)
    @JsonProperty("relationship_type")
    private CodeDescription relationshipType;

    @ApiModelProperty(value = "Contact Type", name = "contact type", position = 8)
    @JsonProperty("contact_type")
    private CodeDescription contactType;

    @ApiModelProperty(value = "Approved Visitor", name = "approved visitor", position = 9)
    @JsonProperty("approved_visitor")
    private boolean approvedVisitor;

    @ApiModelProperty(value = "Active", name = "active", position = 10)
    @JsonProperty("active")
    private boolean active;

    @ApiModelProperty(value = "Restrictions", name = "restrictions", position = 11)
    @JsonProperty("restrictions")
    private List<VisitRestriction> restrictions;
}
