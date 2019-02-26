package net.syscon.elite.web.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Component
public class AuthAwareAuthenticationConverter extends DefaultUserAuthenticationConverter {
    @Override
    public Authentication extractAuthentication(final Map<String, ?> map) {
        if (map.containsKey(USERNAME)) {
            final var principal = map.get(USERNAME);
            final var authorities = getAuthorities(map);
            final var authSourceObject = map.get("auth_source");
            final var authSource = authSourceObject instanceof String ? (String) authSourceObject : null;
            return new AuthAwareAuthenticationToken(principal, "N/A", authSource, authorities);
        }
        return null;
    }

    // Copied from DefaultUserAuthenticationConverter as private method
    private Collection<? extends GrantedAuthority> getAuthorities(final Map<String, ?> map) {
        if (!map.containsKey(AUTHORITIES)) {
            return Collections.emptyList();
        }
        final var authorities = map.get(AUTHORITIES);
        if (authorities instanceof String) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList((String) authorities);
        }
        if (authorities instanceof Collection) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils
                    .collectionToCommaDelimitedString((Collection<?>) authorities));
        }
        throw new IllegalArgumentException("Authorities must be either a String or a Collection");
    }
}
