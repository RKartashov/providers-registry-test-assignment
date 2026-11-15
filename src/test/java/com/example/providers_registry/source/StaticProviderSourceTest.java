package com.example.providers_registry.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.providers_registry.domain.Provider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class StaticProviderSourceTest {

    private static final Provider A = new Provider(1L, "https://a.example", "key-a");

    @Test
    void returnsGivenProviders() {
        StaticProviderSource source = new StaticProviderSource(List.of(A));

        assertEquals(List.of(A), source.load());
    }

    @Test
    void isNotAffectedByLaterChangesOfOriginalList() {
        List<Provider> original = new ArrayList<>(List.of(A));
        StaticProviderSource source = new StaticProviderSource(original);

        original.clear();

        assertEquals(List.of(A), source.load());
    }

}