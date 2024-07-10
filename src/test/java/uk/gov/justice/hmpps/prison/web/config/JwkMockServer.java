package uk.gov.justice.hmpps.prison.web.config;

import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

class JwkMockServer extends WireMockRule {

    JwkMockServer(int port) {
        super(port);
    }

    void stubJwkServer() {
        stubFor(
                get(urlEqualTo("/auth/.well-known/jwks.json"))
                        .willReturn(
                                aResponse()
                                        .withHeaders(new com.github.tomakehurst.wiremock.http.HttpHeaders(new HttpHeader("Content-Type", "application/json")))
                                        .withStatus(200)
                                        .withBody(jwkSet()))
        );
    }

    private String jwkSet() {
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
                "}";
    }

}
