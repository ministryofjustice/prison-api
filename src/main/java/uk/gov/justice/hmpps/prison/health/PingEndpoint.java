package uk.gov.justice.hmpps.prison.health;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.context.annotation.Configuration;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@Configuration
@WebEndpoint(id = "ping")
public class PingEndpoint {

    @ReadOperation(produces = TEXT_PLAIN_VALUE)
    public String ping() {
        return "pong";
    }
}
