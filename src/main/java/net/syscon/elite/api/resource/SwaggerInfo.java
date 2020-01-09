package net.syscon.elite.api.resource;

import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import net.syscon.elite.core.RestResource;

@RestResource
@SwaggerDefinition(
        info = @Info(license = @License(name = "Open Government Licence v3.0",
                url = "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3"),
        version = "unknown",
        title = "HMPPS Nomis API Documentation",
        description = "API for access to NOMIS Database",
        contact = @Contact(name = "HMPPS Digital Studio",
                url = "http://digital.prison.service.justice.gov.uk",
                email = "feedback@digital.justice.gov.uk"),
        termsOfService = "https://gateway.nomis-api.service.justice.gov.uk/auth/terms")  )
public interface SwaggerInfo {
}
