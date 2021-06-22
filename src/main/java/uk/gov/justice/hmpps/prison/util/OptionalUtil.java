package uk.gov.justice.hmpps.prison.util;

import java.util.Optional;
import java.util.function.Function;

public class OptionalUtil {
    private OptionalUtil() {
    }

    public static <T, R> R getOrNull(final T data, final Function<T, R> map) {
        return Optional.ofNullable(data).map(map).orElse(null);
    }
}
