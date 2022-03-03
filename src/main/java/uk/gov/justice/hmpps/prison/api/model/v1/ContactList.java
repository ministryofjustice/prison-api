package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ApiModel(description = "Contact List")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ContactList {

    @ApiModelProperty(value = "Available Dates", allowEmptyValue = true)
    private List<ContactPerson> contacts;
}
