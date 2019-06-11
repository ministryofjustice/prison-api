package net.syscon.elite.health;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.context.annotation.Configuration;

@Configuration
@WebEndpoint(id = "ping")
public class PingEndpoint {
    @ReadOperation
    public String ping() {
        return "pong";
    }
}
