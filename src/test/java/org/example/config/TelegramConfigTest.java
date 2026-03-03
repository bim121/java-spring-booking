package org.example.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class TelegramConfigTest {
    private TelegramConfig telegramConfig = new TelegramConfig();

    @Test
    void webClient_createsWebClientBean() {
        WebClient webClient = telegramConfig.webClient();
        assertNotNull(webClient);
    }
}
