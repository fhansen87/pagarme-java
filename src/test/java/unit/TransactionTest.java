package unit;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import me.pagar.APiConfigurations;
import me.pagar.ApiClient;
import me.pagar.ApiErrors;
import me.pagar.FieldsOnHash;
import me.pagar.router.TransactionRouter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class TransactionTest {

    ApiClient client;
    APiConfigurations configs;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void beforeEach() {
        APiConfigurations configs = new APiConfigurations();
        configs.apiKey = "API KEY";
        configs.encryptionKey = "ENCRYPTION KEY";
        configs.baseUrl = "http://localhost:" + wireMockRule.port();
        this.configs = configs;

        ApiClient client = new ApiClient(configs);
        this.client = client;

        wireMockRule.stubFor(
            get(urlPathEqualTo("/transactions/tx_1"))
                .willReturn(aResponse()
                    .withBody("{}")
                    .withStatus(200)
                )
        );
        wireMockRule.stubFor(
            get(urlPathEqualTo("/transactions"))
                .willReturn(aResponse()
                    .withBody("[]")
                    .withStatus(200)
                )
        );
        wireMockRule.stubFor(
            get(urlPathEqualTo("/transactions"))
                .withQueryParam("key", equalTo("value"))
                .willReturn(aResponse()
                        .withBody("[]")
                        .withStatus(200)
                )
        );
        wireMockRule.stubFor(
            post(urlPathEqualTo("/transactions"))
                .willReturn(aResponse()
                    .withBody("{}")
                    .withStatus(200)
                )
        );
    }

    @Test
    public void testTransactionCreate() throws IOException, ApiErrors {
        FieldsOnHash parameters = new FieldsOnHashImpl("{\"key\": \"value\"}");
        new TransactionRouter(client)
            .create(parameters);

        wireMockRule.verify(1, postRequestedFor(urlEqualTo("/transactions"))
            .withBasicAuth(new BasicCredentials(configs.apiKey, "x"))
            .withRequestBody(equalToJson("{\"key\": \"value\"}"))
        );
    }

    @Test
    public void testTransactionFindById() throws IOException, ApiErrors {
        new TransactionRouter(client)
            .findById("tx_1");

        wireMockRule.verify(1, getRequestedFor(urlEqualTo("/transactions/tx_1"))
            .withBasicAuth(new BasicCredentials(configs.apiKey, "x"))
        );
    }

    @Test
    public void testTransactionFind() throws IOException, ApiErrors {
        new TransactionRouter(client)
            .find();

        wireMockRule.verify(1, getRequestedFor(urlEqualTo("/transactions"))
            .withBasicAuth(new BasicCredentials(configs.apiKey, "x"))
        );
    }

    @Test
    public void testTransactionFindWithParameters() throws IOException, ApiErrors {
        FieldsOnHash parameters = new FieldsOnHashImpl("{\"key\": \"value\"}");
        new TransactionRouter(client)
            .find(parameters);

        wireMockRule.verify(1, getRequestedFor(urlEqualTo("/transactions"))
            .withBasicAuth(new BasicCredentials(configs.apiKey, "x"))
            .withQueryParam("key", equalTo("value"))
        );
    }
}
