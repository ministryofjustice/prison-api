package net.syscon.elite.web.api.resource;

import net.syscon.elite.exception.EliteRuntimeException;
import net.syscon.elite.web.api.model.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public final class ResourceUtils {

    public static HttpStatus handleNotFoundResponse(Logger logger, String resourceType) {
        org.springframework.http.HttpStatus httpStatus = org.springframework.http.HttpStatus.NOT_FOUND;
        String responseMessage = buildResponseMessage(resourceType, httpStatus);

        log(logger, responseMessage, null);

        return new HttpStatus(httpStatus.toString(), String.valueOf(httpStatus.value()), responseMessage, responseMessage, "");
    }

    private static void log(Logger logger, String responseMessage, EliteRuntimeException exception) {
        if (logger != null) {
            if (exception == null) {
                logger.error(responseMessage);
            } else {
                logger.error(exception.getMessage());
            }
        }
    }

    private static String buildResponseMessage(String resourceType, org.springframework.http.HttpStatus httpStatus) {
        String message = StringUtils.trimToEmpty(resourceType) + " " + httpStatus.getReasonPhrase();

        return StringUtils.trim(message);
    }
}
