package com.example.providers_registry.domain;

import java.util.Objects;

public record Provider(Long id, String baseUrl, String apiKey) {

    public Provider {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(baseUrl, "baseUrl");
        Objects.requireNonNull(apiKey, "apiKey");
    }

    // Маскировка apiKey из дефолтного toString()
    @Override
    public String toString() {
        return String.format("Provider[id=%s, baseUrl=%s, apiKey=***]", id, baseUrl);
    }

}
