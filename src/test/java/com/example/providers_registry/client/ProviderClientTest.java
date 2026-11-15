package com.example.providers_registry.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.providers_registry.domain.Provider;
import com.example.providers_registry.domain.UnexpectedResponseException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

class ProviderClientTest {

    private static final String BASE_URL = "https://a.example";

    private MockRestServiceServer server;
    private ProviderClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ProviderClientFactory(builder).create(new Provider(1L, BASE_URL, "key-a"));
    }

    // Тесты ответов 200 со статусом ОК и 404:

    @Test
    void returnsTrueWhenStatusOk() {
        server.expect(requestTo(BASE_URL + "/subscribers/79991234567"))
              .andExpect(method(HttpMethod.GET))
              .andRespond(withSuccess("{\"status\":\"ok\"}", MediaType.APPLICATION_JSON));

        assertTrue(client.isAvailable("79991234567"));
        server.verify();
    }

    @Test
    void returnsFalseOn404() {
        server.expect(requestTo(BASE_URL + "/subscribers/79991234567"))
              .andRespond(withResourceNotFound());

        assertFalse(client.isAvailable("79991234567"));
        server.verify();
    }

    // Проброс сетевых ошибок:

    @Test
    void throwsOnUnexpectedBody() {
        server.expect(requestTo(BASE_URL + "/subscribers/1"))
              .andRespond(withSuccess("{\"status\":\"maintenance\"}", MediaType.APPLICATION_JSON));

        assertThrows(UnexpectedResponseException.class, () -> client.isAvailable("1"));
    }

    @Test
    void propagatesServerError() {
        server.expect(requestTo(BASE_URL + "/subscribers/1"))
              .andRespond(withServerError());

        assertThrows(RestClientResponseException.class, () -> client.isAvailable("1"));
    }

    @Test
    void propagatesNetworkError() {
        server.expect(requestTo(BASE_URL + "/subscribers/1"))
              .andRespond(request -> {
                  throw new IOException("connection reset");
              });

        assertThrows(ResourceAccessException.class, () -> client.isAvailable("1"));
    }
}