package com.example.providers_registry.client;

import com.example.providers_registry.domain.UnexpectedResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class ProviderClient {

    private final RestClient restClient;

    public boolean isAvailable(String phone) {
        try {
            StatusResponse response = restClient.get()
                                                .uri("/subscribers/{phone}", phone)
                                                .retrieve()
                                                .body(StatusResponse.class);

            if (response != null && "ok".equals(response.status())) {
                return true;
            }

            // Даже если вернулось 200, но статус не ОК - кидаем исключение
            throw new UnexpectedResponseException("Unexpected body from provider: " + response);
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    record StatusResponse(String status) {

    }

}
