package org.example.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

class SwaggerConfigTest {
    private SwaggerConfig swaggerConfig = new SwaggerConfig();

    @Test
    void customOpenApi_createsOpenApiBean() {
        OpenAPI openApi = swaggerConfig.customOpenApi();
        assertNotNull(openApi);
        assertNotNull(openApi.getInfo());
        assertEquals("Booking Service Api", openApi.getInfo().getTitle());
        assertEquals("1.0", openApi.getInfo().getVersion());
        assertNotNull(openApi.getComponents());
        assertNotNull(openApi.getComponents().getSecuritySchemes());
    }
}
