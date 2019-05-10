package vn.vccorp.servicemonitoring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

/**
 * Name: tuyennta
 * Date: 08/05/2019.
 * Time: 12:48.
 */
@Configuration
@EnableSwagger2
@PropertySource("classpath:properties/swagger.properties")
public class SwaggerConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("vn.vccorp.servicemonitoring.rest.api"))
                .paths(PathSelectors.any()).build()
                .apiInfo(apiInfo())
                .pathMapping(environment.getProperty("example.api.basePath"));
//                .securitySchemes(Arrays.asList(apiKey()));
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                environment.getProperty("example.api.title"),
                environment.getProperty("example.api.description"),
                environment.getProperty("example.api.version"),
                environment.getProperty("example.api.termsOfServiceUrl"),
                environment.getProperty("example.api.contact"),
                environment.getProperty("example.api.license"),
                environment.getProperty("example.api.licenseUrl"));
    }

    private ApiKey apiKey() {
        return new ApiKey("Authorization", "Authorization", "header");
    }
}
