package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Contacts Details for offender
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Contacts Details for offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ContactDetail {
    @NotNull
    private Long bookingId;

    @NotNull
    @Builder.Default
    private List<Contact> nextOfKin = new ArrayList<Contact>();

    @NotNull
    @Builder.Default
    private List<Contact> otherContacts = new ArrayList<Contact>();
}
