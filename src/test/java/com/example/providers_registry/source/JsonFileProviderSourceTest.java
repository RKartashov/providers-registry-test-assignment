package com.example.providers_registry.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.providers_registry.domain.Provider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonFileProviderSourceTest {

    @TempDir
    Path dir;

    @Test
    void loadsProvidersFromFile() throws IOException {
        Path file = dir.resolve("providers.json");
        Files.writeString(
            file,
            """
                [{"id":1,"baseUrl":"https://a.example","apiKey":"key-a"}]
                """
        );

        List<Provider> result = new JsonFileProviderSource(file).load();

        assertEquals(List.of(new Provider(1L, "https://a.example", "key-a")), result);
    }

    @Test
    void missingFileThrowsSourceException() {
        Path file = dir.resolve("nope.json");

        assertThrows(ProviderSourceException.class, () -> new JsonFileProviderSource(file).load());
    }

    @Test
    void malformedJsonThrowsSourceException() throws IOException {
        Path file = dir.resolve("providers.json");
        Files.writeString(file, "[{ not json");

        assertThrows(ProviderSourceException.class, () -> new JsonFileProviderSource(file).load());
    }

}
