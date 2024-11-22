package com.react.project.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
public class OpenAPIConfiguration {

    @Value("${base.url}")
    private String baseUrl;

    private final Environment environment;

    public OpenAPIConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public OpenAPI defineOpenApi() {
        Server server = new Server();
        server.setUrl(baseUrl);
        server.setDescription(getServerDescription());

        Contact myContact = new Contact();
        myContact.setName("John Doe");
        myContact.setEmail("your.email@example.com");

        Info information = new Info()
                .title("HR API")
                .version("1.0")
                .description("This API exposes endpoints to the HRs, users, and more.")
                .contact(myContact);

        return new OpenAPI().info(information).servers(List.of(server));
    }

    private String getServerDescription() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return activeProfiles[0] + " server";
        } else {
            return "Local server";
        }
    }
}
