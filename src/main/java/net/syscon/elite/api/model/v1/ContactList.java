package net.syscon.elite.api.model.v1;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

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
