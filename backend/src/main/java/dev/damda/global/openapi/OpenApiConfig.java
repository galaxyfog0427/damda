package dev.damda.global.openapi;

import dev.damda.global.error.ErrorCode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenApiCustomizer contractCustomizer() {
        return openApi -> {
            requireAllProperties(openApi);
            addAuthPaths(openApi);
            addErrorResponse(openApi);
        };
    }

    private void requireAllProperties(OpenAPI openApi) {
        for (Schema<?> schema : openApi.getComponents().getSchemas().values()) {
            if (schema.getProperties() == null) continue;
            schema.setRequired(new ArrayList<>(schema.getProperties().keySet()));
        }
    }

    private List<String> errorCodeNames() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(ErrorCode.class));

        return scanner.findCandidateComponents("dev.damda").stream()
                .map(bd -> ClassUtils.resolveClassName(bd.getBeanClassName(), null))
                .map(errorCodeClass -> (ErrorCode[]) errorCodeClass.getEnumConstants())
                .flatMap(Arrays::stream)
                .map(ErrorCode::name)
                .sorted()
                .toList();
    }

    private void addErrorResponse(OpenAPI openApi) {
        Schema<?> fieldErrorResponseSchema = new Schema<>()
                .types(Set.of("object"))
                .addProperty("field", new Schema<>().types(Set.of("string")))
                .addProperty("message", new Schema<>().types(new LinkedHashSet<>(List.of("string", "null"))))
                .required(List.of("field", "message"));

        Schema<?> errorResponseSchema = new Schema<>()
                .types(Set.of("object"))
                .addProperty("type", new Schema<>().types(Set.of("string")).format("uri"))
                .addProperty("title", new Schema<>().types(Set.of("string")))
                .addProperty("status", new Schema<>().types(Set.of("integer")).format("int32"))
                .addProperty("detail", new Schema<>().types(Set.of("string")))
                .addProperty("code", new Schema<>().types(Set.of("string"))._enum(errorCodeNames()))
                .addProperty(
                        "errors",
                        new Schema<>()
                                .types(Set.of("array"))
                                .items(new Schema<>().$ref("#/components/schemas/FieldErrorResponse")))
                .required(List.of("type", "title", "status", "detail", "code"));

        openApi.getComponents()
                .addSchemas("FieldErrorResponse", fieldErrorResponseSchema)
                .addSchemas("ErrorResponse", errorResponseSchema);

        openApi.getPaths().values().stream()
                .map(PathItem::readOperations)
                .flatMap(List::stream)
                .forEach(operation -> operation.getResponses().addApiResponse("default", problemResponse("에러")));
    }

    private ApiResponse problemResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType(
                                "application/problem+json",
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }

    private void addAuthPaths(OpenAPI openApi) {
        Schema<?> loginRequestSchema = new Schema<>()
                .types(Set.of("object"))
                .addProperty("email", new Schema<>().types(Set.of("string")).format("email"))
                .addProperty("password", new Schema<>().types(Set.of("string")))
                .addProperty("rememberMe", new Schema<>().types(Set.of("boolean")))
                .required(List.of("email", "password"));

        openApi.path(
                "/api/v1/login",
                new PathItem()
                        .post(new Operation()
                                .operationId("login")
                                .tags(List.of("auth"))
                                .requestBody(new RequestBody()
                                        .required(true)
                                        .content(new Content()
                                                .addMediaType(
                                                        "application/x-www-form-urlencoded",
                                                        new MediaType()
                                                                .schema(new Schema<>()
                                                                        .$ref("#/components/schemas/LoginRequest")))))
                                .responses(new ApiResponses()
                                        .addApiResponse(
                                                "204",
                                                new ApiResponse().description("로그인 성공. 새 SESSION·XSRF-TOKEN 쿠키가 실린다"))
                                        .addApiResponse("401", problemResponse("LOGIN_FAILED")))));

        openApi.path(
                "/api/v1/logout",
                new PathItem()
                        .post(new Operation()
                                .operationId("logout")
                                .tags(List.of("auth"))
                                .responses(new ApiResponses()
                                        .addApiResponse("204", new ApiResponse().description("로그아웃. 세션 삭제, 쿠키 지움")))));

        openApi.getComponents().addSchemas("LoginRequest", loginRequestSchema);
    }

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("damda API").version("v1")).servers(List.of(new Server().url("/")));
    }
}
