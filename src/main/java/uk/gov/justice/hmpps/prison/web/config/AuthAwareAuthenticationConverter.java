package uk.gov.justice.hmpps.prison.web.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthAwareAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    public AuthAwareAuthenticationToken convert(final Jwt jwt) {
        Map<String, Object> map = jwt.getClaims();
        final var clientId = jwt.getClaims().get("client_id");
        final var clientOnly = jwt.getSubject() != null && jwt.getSubject().equals(clientId);

        final var principal = map.get("user_name");
        final var authorities = extractAuthorities(jwt);
        final var authSourceObject = map.get("auth_source");
        final var authSource = authSourceObject instanceof String ? (String) authSourceObject : null;
        return new AuthAwareAuthenticationToken(jwt, principal, clientOnly, authSource, authorities);
    }

    @SuppressWarnings("ConstantConditions")
    private Collection<GrantedAuthority> extractAuthorities(final Jwt jwt) {
        final var authorities = new HashSet<>(this.jwtGrantedAuthoritiesConverter.convert(jwt));
        if (jwt.getClaims().containsKey("authorities")) {
            authorities.addAll(((Collection<String>) jwt.getClaims().get("authorities")).stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet()));
        }
        return Set.copyOf(authorities);
    }
}
