package com.example.providers_registry.source;

import com.example.providers_registry.domain.Provider;
import java.util.Collection;
import java.util.List;

public class StaticProviderSource implements ProviderSource {

    private final List<Provider> providers;

    public StaticProviderSource(Collection<Provider> providers) {
        this.providers = List.copyOf(providers);
    }

    @Override
    public List<Provider> load() {
        return providers;
    }

}
