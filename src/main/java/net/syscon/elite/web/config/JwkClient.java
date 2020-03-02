package net.syscon.elite.web.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.GET;

@Component
public class JwkClient {

    private RestTemplate restTemplate;
    private String jwkUrl;
    private Map<String, String> keyIdToPublicKeys = new HashMap<>();

    public JwkClient(RestTemplate restTemplate, @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkUrl) throws ParseException, JOSEException {
        this.restTemplate = restTemplate;
        this.jwkUrl = jwkUrl;
        getJwkSets();
    }

    private void getJwkSets() throws ParseException, JOSEException {

        ResponseEntity<String> jwkSetResponse = restTemplate.exchange(jwkUrl, GET, null, String.class);
        List<JWK> jwks = JWKSet.parse(jwkSetResponse.getBody()).getKeys();
        for (JWK jwk : jwks) {
            keyIdToPublicKeys.put(jwk.getKeyID(), ((RSAKey)jwk).toPublicKey().toString());
        }
    }

    public String getPublicKeyForKeyId(String keyId) {
        return keyIdToPublicKeys.get(keyId);
    }

}
