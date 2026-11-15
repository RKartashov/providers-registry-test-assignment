package com.example.providers_registry.configuration;

import com.example.providers_registry.client.ProviderClientFactory;
import com.example.providers_registry.registry.ProvidersRegistry;
import com.example.providers_registry.source.JsonFileProviderSource;
import com.example.providers_registry.source.ProviderSource;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ProvidersRegistryConfiguration {

    @Bean
    ProviderSource providerSource(@Value("${providers-registry.providers-file}") String providersFile) {
        return new JsonFileProviderSource(Path.of(providersFile));
    }

    @Bean
    ProviderClientFactory providerClientFactory() {
        return new ProviderClientFactory(RestClient.builder());
    }

    @Bean
    ProvidersRegistry providersRegistry(ProviderSource source, ProviderClientFactory factory) {
        ProvidersRegistry registry = new ProvidersRegistry(source, factory);
        registry.reload();
        return registry;
    }

}