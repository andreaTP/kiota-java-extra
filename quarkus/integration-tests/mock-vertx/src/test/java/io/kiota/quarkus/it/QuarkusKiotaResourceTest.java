package io.kiota.quarkus.it;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.apisdk.example.json.ApiClient;
import io.apisdk.example.json.models.CreateChatCompletionRequest;
import io.apisdk.example.json.models.CreateChatCompletionResponse;
import io.kiota.http.vertx.VertXRequestAdapter;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkiverse.mockserver.test.MockServerTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;

@QuarkusTest
@QuarkusTestResource(MockServerTestResource.class)
public class QuarkusKiotaResourceTest {
    @Inject Vertx vertx;

    @InjectMockServerClient MockServerClient mockServerClient;

    @BeforeEach
    public void beforeEach() throws Exception {
        given().when()
                .header(new Header("Content-Type", "application/json"))
                .body(
                        "{ \"specUrlOrPayload\": "
                                + Files.readString(
                                        Path.of("src", "main", "openapi", "example.json"))
                                + " }")
                .put(
                        new URI(
                                "http://localhost:"
                                        + mockServerClient.getPort()
                                        + "/mockserver/openapi"))
                .then()
                .statusCode(201);
    }

    @Test
    public void testHelloEndpointUsingTheKiotaClient() throws Exception {
        // Arrange
        var adapter = new VertXRequestAdapter(vertx);
        adapter.setBaseUrl("http://localhost:" + mockServerClient.getPort());
        ApiClient client = new ApiClient(adapter);

        // Act
        CreateChatCompletionResponse result =
                client.chat().completions().post(new CreateChatCompletionRequest());

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getCreated());
    }
}
