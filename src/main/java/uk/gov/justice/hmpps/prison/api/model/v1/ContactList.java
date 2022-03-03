package uk.gov.justice.hmpps.prison.api.model.v1;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Schema(description = "Contact List")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ContactList {

    @Schema(description = "Available Dates")
    private List<ContactPerson> contacts;
}
