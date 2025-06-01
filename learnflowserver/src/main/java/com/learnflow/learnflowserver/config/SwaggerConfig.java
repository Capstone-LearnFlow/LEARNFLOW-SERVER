package com.learnflow.learnflowserver.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI learnflow_API(){
        Info info=new Info()
                .title("LEARNFLOW_API")
                .description("캡스톤 - LearnFlow API입니다")
                .version("1.0");

        return new OpenAPI()
                .addServersItem(new Server().url("/"))
                .info(info);

    }

}
