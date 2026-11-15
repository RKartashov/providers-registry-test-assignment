package com.example.providers_registry.domain;

public class ProviderNotFoundException extends RuntimeException {

    public ProviderNotFoundException(Long providerId) {
        super("Provider not found: " + providerId);
    }

}