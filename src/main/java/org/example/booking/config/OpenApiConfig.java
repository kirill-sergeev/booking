package org.example.booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookingOpenAPI(
            @Value("${server.port}") int port,
            @Value("${spring.application.version}") String applicationVersion) {
        return new OpenAPI()
                .servers(List.of(new Server().url("http://localhost:" + port).description("Local Server")))
                .info(new Info()
                        .title("Booking System API")
                        .description("REST API for a booking service. Allows managing and booking accommodation units.")
                        .version(applicationVersion)
                );
    }
}
