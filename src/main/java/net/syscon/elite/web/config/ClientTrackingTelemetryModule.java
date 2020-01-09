package net.syscon.elite.web.config;

import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.extensibility.TelemetryModule;
import com.microsoft.applicationinsights.web.extensibility.modules.WebTelemetryModule;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import net.syscon.util.IpAddressHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import static net.syscon.util.MdcUtility.IP_ADDRESS;

@Slf4j
@Configuration
public class ClientTrackingTelemetryModule implements WebTelemetryModule, TelemetryModule {
    private final String jwtPublicKey;

    @Autowired
    public ClientTrackingTelemetryModule(
            @Value("${jwt.public.key}") final String jwtPublicKey) {
        this.jwtPublicKey = jwtPublicKey;
    }

    @Override
    public void onBeginRequest(final ServletRequest req, final ServletResponse res) {

        HttpServletRequest httpServletRequest = (HttpServletRequest) req;
        final var token = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        final var bearer = "Bearer ";
        if (StringUtils.startsWithIgnoreCase(token, bearer)) {

            try {
                final var jwtBody = getClaimsFromJWT(token);

                final var properties = ThreadContext.getRequestTelemetryContext().getHttpRequestTelemetry().getProperties();

                properties.put("username", String.valueOf(jwtBody.get("user_name")));
                properties.put("clientId", String.valueOf(jwtBody.get("client_id")));
                properties.put(IP_ADDRESS, IpAddressHelper.retrieveIpFromRemoteAddr(httpServletRequest));
            } catch (ExpiredJwtException e) {
                // Expired token which spring security will handle
            } catch (GeneralSecurityException | IOException e) {
                log.warn("problem decoding jwt public key for application insights", e);
            }

        }
    }

    private Claims getClaimsFromJWT(final String token) throws ExpiredJwtException, IOException, GeneralSecurityException {

        return Jwts.parser()
                .setSigningKey(getPublicKeyFromString(jwtPublicKey))
                .parseClaimsJws(token.substring(7))
                .getBody();
    }

    RSAPublicKey getPublicKeyFromString(String key) throws IOException,
            GeneralSecurityException {
        String publicKey = new String(Base64.decodeBase64(key));
        publicKey = publicKey.replace("-----BEGIN PUBLIC KEY-----", "");
        publicKey = publicKey.replace("-----END PUBLIC KEY-----", "");
        publicKey = publicKey.replaceAll("\\R", "");
        byte[] encoded = Base64.decodeBase64(publicKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(new
                X509EncodedKeySpec(encoded));
        return pubKey;
    }

    @Override
    public void onEndRequest(final ServletRequest req, final ServletResponse res) {
    }

    @Override
    public void initialize(final TelemetryConfiguration configuration) {

    }
}
