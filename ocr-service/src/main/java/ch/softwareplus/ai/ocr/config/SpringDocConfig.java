package ch.softwareplus.ai.ocr.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.Scopes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * The SpringDoc Configuration for REST API documentation.
 */
@Configuration
public class SpringDocConfig {

    @Autowired
    private Environment env;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(env.getProperty("springdoc.title"))
                        .version(env.getProperty("springdoc.version"))
                        .description(env.getProperty("springdoc.description"))
                        .termsOfService(env.getProperty("springdoc.termsOfServiceUrl"))
                        .license(new License().name(env.getProperty("springdoc.license"))
                                .url(env.getProperty("springdoc.licenseUrl"))));
    }


    private Scopes scopes() {
        return new Scopes().addString("openid", "Open ID Connect Scope");
    }
}