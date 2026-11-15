package com.example.providers_registry.source;

import com.example.providers_registry.domain.Provider;
import java.nio.file.Path;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

public class JsonFileProviderSource implements ProviderSource {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final TypeReference<List<Provider>> TYPE = new TypeReference<>() {
    };

    private final Path file;

    public JsonFileProviderSource(Path file) {
        this.file = file;
    }

    @Override
    public List<Provider> load() {
        try {
            return MAPPER.readValue(file.toFile(), TYPE);
        } catch (JacksonException e) {
            throw new ProviderSourceException("Cannot read providers from " + file, e);
        }
    }

}
