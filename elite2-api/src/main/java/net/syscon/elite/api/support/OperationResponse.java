package net.syscon.elite.api.support;

import net.syscon.elite.api.model.ErrorResponse;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class OperationResponse extends ResponseDelegate {
    private OperationResponse(Response response, Object entity) {
        super(response, entity);
    }

    public static OperationResponse respond400WithApplicationJson(ErrorResponse errorResponse) {
        ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", MediaType.APPLICATION_JSON);

        responseBuilder.entity(errorResponse);

        return new OperationResponse(responseBuilder.build(), errorResponse);
    }

    public static OperationResponse respond401WithApplicationJson(ErrorResponse errorResponse) {
        ResponseBuilder responseBuilder = Response.status(401).header("Content-Type", MediaType.APPLICATION_JSON);

        responseBuilder.entity(errorResponse);

        return new OperationResponse(responseBuilder.build(), errorResponse);
    }

    public static OperationResponse respond403WithApplicationJson(ErrorResponse errorResponse) {
        ResponseBuilder responseBuilder = Response.status(403).header("Content-Type", MediaType.APPLICATION_JSON);

        responseBuilder.entity(errorResponse);

        return new OperationResponse(responseBuilder.build(), errorResponse);
    }

    public static OperationResponse respond404WithApplicationJson(ErrorResponse errorResponse) {
        ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", MediaType.APPLICATION_JSON);

        responseBuilder.entity(errorResponse);

        return new OperationResponse(responseBuilder.build(), errorResponse);
    }

    public static OperationResponse respond409WithApplicationJson(ErrorResponse errorResponse) {
        ResponseBuilder responseBuilder = Response.status(409).header("Content-Type", MediaType.APPLICATION_JSON);

        responseBuilder.entity(errorResponse);

        return new OperationResponse(responseBuilder.build(), errorResponse);
    }

    public static OperationResponse respond500WithApplicationJson(ErrorResponse errorResponse) {
        ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", MediaType.APPLICATION_JSON);

        responseBuilder.entity(errorResponse);

        return new OperationResponse(responseBuilder.build(), errorResponse);
    }

    public static OperationResponse respondErrorWithApplicationJson(ErrorResponse errorResponse) {
        switch(Status.fromStatusCode(errorResponse.getStatus().intValue())) {
            case BAD_REQUEST:
                return respond400WithApplicationJson(errorResponse);

            case UNAUTHORIZED:
                return respond401WithApplicationJson(errorResponse);

            case FORBIDDEN:
                return respond403WithApplicationJson(errorResponse);

            case NOT_FOUND:
                return respond404WithApplicationJson(errorResponse);

            case CONFLICT:
                return respond409WithApplicationJson(errorResponse);

            case INTERNAL_SERVER_ERROR:
                return respond500WithApplicationJson(errorResponse);

            default:
                if (StringUtils.isBlank(errorResponse.getDeveloperMessage())) {
                    errorResponse.setDeveloperMessage("To assist with problem diagnosis, refer to original response status and error code, if provided.");
                }

                return respond500WithApplicationJson(errorResponse);
        }
    }
}
