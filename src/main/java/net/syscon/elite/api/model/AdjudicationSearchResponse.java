package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import net.syscon.elite.api.support.Page;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class AdjudicationSearchResponse {

    private List<Adjudication> results;
}
