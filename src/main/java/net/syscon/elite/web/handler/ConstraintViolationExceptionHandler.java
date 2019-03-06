package net.syscon.elite.web.handler;

import net.syscon.elite.web.config.ServletContextConfigs;

import javax.validation.ValidationException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * This wrapper overrides the builtin Jersey handler ValidationExceptionMapper
 * for validation exceptions, which would otherwise consume them rather than ResourceExceptionHandler
 * Bind register in {@link ServletContextConfigs#setEnv} is also needed
 * 
 * @author steve
 */
@Provider
public class ConstraintViolationExceptionHandler implements ExceptionMapper<ValidationException> {

    @Override
    @Produces(MediaType.APPLICATION_JSON)
    public Response toResponse(final ValidationException ex) {
        return new ResourceExceptionHandler().toResponse(ex);
    }
}
