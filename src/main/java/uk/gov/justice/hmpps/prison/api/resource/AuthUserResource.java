package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.auth.UserPersonDetails;
import uk.gov.justice.hmpps.prison.service.AuthService;

@Slf4j
@RestController
@Tag(name = "auth")
@Validated
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Hidden
public class AuthUserResource {

    private final AuthService authService;

    @GetMapping("/user/{username}")
    public UserPersonDetails getAuthUserDetails(@PathVariable("username") final String username) {
        return authService.getNomisUserByUsername(username.toUpperCase());
    }
}
