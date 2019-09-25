package net.syscon.elite.web.provider;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This provider allows rest endpoints to have LocalDateTime query parameters,
 * datetime in raml.
 * <p>
 * Apparently when Jersey 3.0 comes out, java 8 types such as this should be
 * catered for natively, so this class can be removed then.
 */
@Provider
public class LocalDateTimeProvider implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(final Class<T> clazz, final Type type, final Annotation[] annotations) {
        if (clazz.getName().equals(LocalDateTime.class.getName())) {

            return new ParamConverter<T>() {

                @SuppressWarnings("unchecked")
                @Override
                public T fromString(final String value) {
                    if (value == null) {
                        return null;
                    }
                    return (T) LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }

                @Override
                public String toString(final T time) {
                    if (time == null) {
                        return null;
                    }
                    final var t = (LocalDateTime) time;
                    return t.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }
            };
        }
        return null;
    }
}
