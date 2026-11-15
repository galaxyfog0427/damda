package dev.damda.global.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import java.util.ArrayList;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenApiCustomizer requiredByDefault() {
        return openApi -> {
            var components = openApi.getComponents();
            if (components == null || components.getSchemas() == null) return;
            for (Schema<?> schema : components.getSchemas().values()) {
                if (schema.getProperties() == null) continue;
                schema.setRequired(new ArrayList<>(schema.getProperties().keySet()));
            }
        };
    }

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("damda API").version("v1")).servers(List.of(new Server().url("/")));
    }
}
