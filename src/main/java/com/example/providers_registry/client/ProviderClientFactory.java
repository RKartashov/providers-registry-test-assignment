package com.example.providers_registry.client;

import com.example.providers_registry.domain.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class ProviderClientFactory {

    private final RestClient.Builder builder;

    public ProviderClient create(Provider provider) {
        RestClient restClient = builder.clone()
                                       .baseUrl(provider.baseUrl())
                                       .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + provider.apiKey())
                                       .build();

        return new ProviderClient(restClient);
    }

}
