package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Contacts Details for offender
 **/
@SuppressWarnings("unused")
@Schema(description = "Contacts Details for offender")
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
