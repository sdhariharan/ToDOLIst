package com.tasknest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskNestOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskNest API")
                        .description("REST API for TaskNest task management.")
                        .version("1.0.0"));
    }
}
