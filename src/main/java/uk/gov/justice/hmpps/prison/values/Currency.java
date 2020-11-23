package uk.gov.justice.hmpps.prison.values;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Builder
@EqualsAndHashCode
@AllArgsConstructor
@Getter
@ToString
public class Currency {
    private final String code;
}
