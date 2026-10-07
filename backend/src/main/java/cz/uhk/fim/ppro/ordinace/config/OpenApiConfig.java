package cz.uhk.fim.ppro.ordinace.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ordinace Na Vyhlídce – REST API")
                        .description("Semestrální projekt PPRO: Rezervační a ordinační systém soukromé praxe MUDr. Jany Hrubé.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Michal Bleha")
                                .email("michal.bleha@uhk.cz")));
    }
}
