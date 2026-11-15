package com.example.providers_registry.registry;

import com.example.providers_registry.client.ProviderClient;
import com.example.providers_registry.client.ProviderClientFactory;
import com.example.providers_registry.domain.Provider;
import com.example.providers_registry.domain.ProviderNotFoundException;
import com.example.providers_registry.source.ProviderSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProvidersRegistry {

    private final ProviderSource source;
    private final ProviderClientFactory clientFactory;

    private volatile Map<Long, ProviderClient> clients = Map.of();

    public boolean isPhoneAvailable(Long providerId, String phone) {
        Objects.requireNonNull(providerId, "providerId");

        ProviderClient client = clients.get(providerId);
        if (client == null) {
            throw new ProviderNotFoundException(providerId);
        }
        return client.isAvailable(phone);
    }

    // Синхронизация на случай параллельного вызова в нескольких потоках
    public synchronized void replaceProviders(Collection<Provider> providers) {
        Map<Long, ProviderClient> fresh = new HashMap<>();
        for (Provider provider : providers) {
            if (fresh.put(provider.id(), clientFactory.create(provider)) != null) {
                throw new IllegalArgumentException("Duplicate provider id: " + provider.id());
            }
        }
        clients = Map.copyOf(fresh);
    }

    public synchronized void reload() {
        replaceProviders(source.load());
    }

}
