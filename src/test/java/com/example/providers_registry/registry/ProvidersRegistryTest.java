package com.example.providers_registry.registry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.providers_registry.client.ProviderClientFactory;
import com.example.providers_registry.domain.Provider;
import com.example.providers_registry.domain.ProviderNotFoundException;
import com.example.providers_registry.source.StaticProviderSource;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ProvidersRegistryTest {

    private static final Provider A = new Provider(1L, "https://a.example", "key-a");
    private static final Provider B = new Provider(2L, "https://b.example", "key-b");

    private final AtomicReference<List<Provider>> sourceData = new AtomicReference<>(List.of());

    private MockRestServiceServer server;
    private ProvidersRegistry registry;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        registry = new ProvidersRegistry(sourceData::get, new ProviderClientFactory(builder));
    }

    @Test
    void unknownProviderThrows() {
        assertThrows(ProviderNotFoundException.class, () -> registry.isPhoneAvailable(99L, "1"));
    }

    @Test
    void routesRequestToTheRightProvider() {
        registry.replaceProviders(List.of(A, B));
        server.expect(requestTo("https://a.example/subscribers/1"))
              .andRespond(withSuccess("{\"status\":\"ok\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://b.example/subscribers/1"))
              .andRespond(withResourceNotFound());

        assertTrue(registry.isPhoneAvailable(1L, "1"));
        assertFalse(registry.isPhoneAvailable(2L, "1"));
        server.verify();
    }

    @Test
    void replaceProvidersSwapsWholeSet() {
        registry.replaceProviders(List.of(A));
        registry.replaceProviders(List.of(B));
        server.expect(requestTo("https://b.example/subscribers/1"))
              .andRespond(withSuccess("{\"status\":\"ok\"}", MediaType.APPLICATION_JSON));

        assertThrows(ProviderNotFoundException.class, () -> registry.isPhoneAvailable(1L, "1"));
        assertTrue(registry.isPhoneAvailable(2L, "1"));
    }

    @Test
    void duplicateIdsAreRejectedAndOldSetRemains() {
        registry.replaceProviders(List.of(A));
        Provider duplicate = new Provider(1L, "https://dup.example", "key-dup");

        assertThrows(
            IllegalArgumentException.class,
            () -> registry.replaceProviders(List.of(B, duplicate, duplicate))
        );

        server.expect(requestTo("https://a.example/subscribers/1"))
              .andRespond(withSuccess("{\"status\":\"ok\"}", MediaType.APPLICATION_JSON));
        assertTrue(registry.isPhoneAvailable(1L, "1"));
    }

    @Test
    void reloadPicksUpNewSourceContent() {
        sourceData.set(List.of(A));
        registry.reload();
        assertThrows(ProviderNotFoundException.class, () -> registry.isPhoneAvailable(2L, "1"));

        sourceData.set(List.of(A, B));
        registry.reload();
        server.expect(requestTo("https://b.example/subscribers/1"))
              .andRespond(withSuccess("{\"status\":\"ok\"}", MediaType.APPLICATION_JSON));

        assertTrue(registry.isPhoneAvailable(2L, "1"));
    }

    @Test
    void worksWithStaticProviderSource() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer localServer = MockRestServiceServer.bindTo(builder).build();
        ProvidersRegistry staticRegistry = new ProvidersRegistry(
            new StaticProviderSource(List.of(A)), new ProviderClientFactory(builder));
        staticRegistry.reload();
        localServer.expect(requestTo("https://a.example/subscribers/1"))
                   .andRespond(withSuccess("{\"status\":\"ok\"}", MediaType.APPLICATION_JSON));

        assertTrue(staticRegistry.isPhoneAvailable(1L, "1"));
    }

}