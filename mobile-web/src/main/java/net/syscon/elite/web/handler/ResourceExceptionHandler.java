package net.syscon.elite.web.handler;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.service.EntityNotFoundException;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static java.lang.String.format;

@Provider
@Slf4j
public class ResourceExceptionHandler implements ExceptionMapper<Exception> {

    private static final String MESSAGE_FORMAT = "{\"%s\": \"%s\"}";

    @Override
    @Produces(MediaType.APPLICATION_JSON)
    public Response toResponse(Exception ex) {

        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        String entity = format(MESSAGE_FORMAT, "errorMessage", ex.getMessage());

        if (ex instanceof EntityNotFoundException) {
            status = Response.Status.NOT_FOUND;
            entity = format(MESSAGE_FORMAT, "notFoundId", ex.getMessage());
        }

        return Response.status(status).entity(entity).build();

    }
}


