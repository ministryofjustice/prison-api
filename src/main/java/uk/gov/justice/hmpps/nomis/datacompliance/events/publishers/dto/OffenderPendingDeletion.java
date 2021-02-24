package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * This event signifies that an offender's
 * data is eligible for deletion, subject to
 * further checks by the Data Compliance Service.
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class OffenderPendingDeletion {

    @JsonProperty("offenderIdDisplay")
    private String offenderIdDisplay;

    @JsonProperty("batchId")
    private Long batchId;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("middleName")
    private String middleName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("birthDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate birthDate;

    @JsonProperty("agencyLocationId")
    private String agencyLocationId;

    @Singular
    @JsonProperty("offenderAliases")
    private List<OffenderAlias> offenderAliases;

    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OffenderAlias {

        @JsonProperty("offenderId")
        private Long offenderId;

        @Singular
        @JsonProperty("bookings")
        private List<Booking> bookings;
    }

    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Booking {

        @JsonProperty("offenderBookId")
        private Long offenderBookId;

        @Singular
        @JsonProperty("offenceCodes")
        private Set<String> offenceCodes;

        @Singular
        @JsonProperty("alertCodes")
        private Set<String> alertCodes;
    }
}

