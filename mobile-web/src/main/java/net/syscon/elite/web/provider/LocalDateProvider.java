package net.syscon.elite.web.provider;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * This provider allows rest endpoints to have LocalDate query parameters,
 * date-only in raml.
 * 
 * Apparently when Jersey 3.0 comes out, java 8 types such as this should be
 * catered for natively, so this class can be removed then.
 *
 */
@Provider
public class LocalDateProvider implements ParamConverterProvider {

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> clazz, Type type, Annotation[] annotations) {
        if (clazz.getName().equals(LocalDate.class.getName())) {

            return new ParamConverter<T>() {

                @SuppressWarnings("unchecked")
                @Override
                public T fromString(String value) {
                    if (value == null) {
                        return null;
                    }
                    return (T) LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
                }

                @Override
                public String toString(T date) {
                    if (date == null) {
                        return null;
                    }
                    LocalDate t = (LocalDate) date;
                    return t.format(DateTimeFormatter.ISO_LOCAL_DATE);
                }
            };
        }
        return null;
    }
}
