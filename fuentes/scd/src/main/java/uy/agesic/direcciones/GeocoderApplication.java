package uy.agesic.direcciones;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableScheduling
public class GeocoderApplication extends SpringBootServletInitializer {
	
	// TO ALLOW RUNNING IN WILDFLY
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }
  
    private static Class<GeocoderApplication> applicationClass = GeocoderApplication.class;
	    
	// END TO ALLOW RUNNING IN WILDFLY

    @Bean
    public Docket swagger() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
//                .paths(PathSelectors.ant("/api/v1/geocode/*"))
//                .paths(PathSelectors.ant("/api/*"))
                .build()
               
                .apiInfo(apiInfo());
    }
    
    @Bean
    UiConfiguration uiConfig() {
      return UiConfigurationBuilder.builder()
          .docExpansion(DocExpansion.LIST) // or DocExpansion.NONE or DocExpansion.FULL
          .build();
    }  
    
    private ApiInfo apiInfo() {
        return new ApiInfo(
          "API Buscador de Direcciones", 
          "Puede utilizar este servicio para encontrar calles, calles y números de portal o puntos de interés.", 
          "v1.0", 
          "", 
          new Contact("Infraestructura de Datos Espaciales de Uruguay", "", "ideuy@ide.gub.uy"),
          "GNU Affero GPL",
          "https://www.gnu.org/licenses/agpl-3.0.html",
          Collections.emptyList());
    }

	public static void main(String[] args) {
		String myEnv = System.getenv("env_name");
		SpringApplication.run(GeocoderApplication.class, args);
	}

}
