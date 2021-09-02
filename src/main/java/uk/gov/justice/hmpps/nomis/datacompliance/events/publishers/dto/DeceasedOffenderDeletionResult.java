package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class DeceasedOffenderDeletionResult {

    @JsonProperty("batchId")
    private Long batchId;

    @Singular
    @JsonProperty("deceasedOffenders")
    private List<DeceasedOffender> deceasedOffenders;


    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeceasedOffender {

        @JsonProperty("offenderIdDisplay")
        private String offenderIdDisplay;

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

        @JsonProperty("deceasedDate")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @JsonSerialize(using = LocalDateSerializer.class)
        private LocalDate deceasedDate;

        @JsonProperty("deletionDateTime")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime deletionDateTime;

        @JsonProperty("agencyLocationId")
        private String agencyLocationId;

        @Singular
        @JsonProperty("offenderAliases")
        private List<OffenderAlias> offenderAliases;


    }

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
        @JsonProperty("offenderBookIds")
        private List<Long> offenderBookIds;


    }

}

