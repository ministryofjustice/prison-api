package uk.gov.justice.hmpps.prison.service;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryParamHelper {
    /**
     * Splits list of types of form TYPE, TYPE+SUB, TYPE+SUB2 into map of TYPE with list of sub types (SUB, SUB2)
     *
     * @param types List of types, with type and sub type separated either by a + or space character.
     * @return map of types with list of subtypes inside.  If there are no subtypes for a type then an empty list is returned.
     */
    public static Map<String, List<String>> splitTypes(final List<String> types) {
        return types.stream()
                .map(t -> t.trim().replace(' ', '+'))
                .collect(Collectors.toMap((n) -> StringUtils.substringBefore(n, "+"),
                        (n) -> {
                            final var subtype = StringUtils.substringAfter(n, "+");
                            return subtype.isEmpty() ? List.of() : List.of(subtype);
                        },
                        (v1, v2) -> Stream.of(v1, v2).flatMap(Collection::stream).toList()));
    }
}
