package net.syscon.elite.web.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Component
@Slf4j
public class JwkClient {

    private final String jwkUrl;
    private Optional<Map<String, PublicKey>> publicKeysById = Optional.empty();

    public JwkClient(@Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkUrl) {
        this.jwkUrl = jwkUrl;
    }

    PublicKey getPublicKeyForKeyId(String keyId) {
        return publicKeysById
                .map(keys -> keys.get(keyId))
                .orElse(retrieveKeysAndFind(keyId));
    }

    private PublicKey retrieveKeysAndFind(String keyId) {
        publicKeysById = Optional.of(getPublicKeysById());
        return publicKeysById.get().get(keyId);
    }

    private Map<String, PublicKey> getPublicKeysById() {
        return loadJwkKeys().stream()
                .map(jwk -> Pair.of(jwk.getKeyID(), toPublicKey(jwk)))
                .filter(pair -> pair.getRight().isPresent())
                .collect(Collectors.toUnmodifiableMap(Pair::getLeft, pair -> pair.getRight().get()));
    }

    private List<JWK> loadJwkKeys() {
        try {
            return JWKSet.load(new URL(jwkUrl)).getKeys();
        } catch (Exception e) {
            log.error(String.format("Failed to retrieve JWK set from %s due to exception", jwkUrl), e);
        }
        return emptyList();
    }

    private Optional<PublicKey> toPublicKey(JWK jwk) {
        try {
            return Optional.of(((RSAKey) jwk).toPublicKey());
        } catch (JOSEException e) {
            log.info(String.format("Failed to retrieve public key from JWK %s due to exception", jwk), e);
        }
        return Optional.empty();
    }

}
