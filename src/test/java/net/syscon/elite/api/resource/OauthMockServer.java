package net.syscon.elite.api.resource;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class OauthMockServer extends WireMockRule {

    public OauthMockServer(int port) {
        super(port);
    }

    public void stubJwkServer() {
        stubFor(
                WireMock.get(WireMock.urlEqualTo("/auth/.well-known/jwks.json"))
                        .willReturn(WireMock.aResponse()
                                .withHeaders(new HttpHeaders(new HttpHeader("Content-Type", "application/json")))
                                .withBody(jwks())
                        )
        );
    }

    private String jwks() {
        return "{\n" +
                "  \"keys\": [\n" +
                "    {\n" +
                "      \"kty\": \"RSA\",\n" +
                "      \"e\": \"AQAB\",\n" +
                "      \"use\": \"sig\",\n" +
                "      \"kid\": \"dps-client-key\",\n" +
                "      \"alg\": \"RS256\",\n" +
                "      \"n\": \"sOPAtsQADdbRu_EH6LP5BM1_mF40VDBn12hJSXPPd5WYK0HLY20VM7AxxR9mnYCF6So1Wt7fGNqUx_WyemBpIJNrs_7Dzwg3uwiQuNh4zKR-EGxWbLwi3yw7lXPUzxUyC5xt88e_7vO-lz1oCnizjh4mxNAms6ZYF7qfnhJE9WvWPwLLkojkZu1JdusLaVowN7GTGNpME8dzeJkam0gp4oxHQGhMN87K6jqX3cEwO6Dvhemg8whs96nzQl8n2LFvAK2up9Prr9Gi2LFgTt7KqXA06kC4Kgw2IR1eFgzcBlTOEwmzjre65HoNaJBr9uNZzV5sILPMczzhQj_fMhz3_Q\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
    }

}
