package net.syscon.prison.api.resource;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.prison.api.model.auth.UserPersonDetails;
import net.syscon.prison.service.AuthService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@Slf4j
@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@ApiIgnore
public class AuthUserResource {

    private final AuthService authService;

    @GetMapping("/user/{username}")
    public UserPersonDetails getAuthUserDetails(@PathVariable("username") final String username) {
        return authService.getNomisUserByUsername(username.toUpperCase());
    }
}
