package com.github.xltgui.loancaculatorchallenge.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Loan Calculator",
                version = "v1",
                description = "An API for calculating loan installments and simulating financing scenarios.",
                contact = @Contact(
                        name = "Guilherme Passos",
                        email = "xltguidev@gmail.com"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Production server")
        }
)
public class OpenApiConfiguration {
}
