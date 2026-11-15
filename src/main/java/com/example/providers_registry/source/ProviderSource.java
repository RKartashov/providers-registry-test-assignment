package com.example.providers_registry.source;

import com.example.providers_registry.domain.Provider;
import java.util.List;

public interface ProviderSource {

    List<Provider> load();

}
