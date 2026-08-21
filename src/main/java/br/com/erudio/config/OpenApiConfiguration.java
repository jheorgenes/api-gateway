package br.com.erudio.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

@Configuration // Classe responsável pelas configurações do Spring
public class OpenApiConfiguration {

    @Bean
    public SwaggerUiConfigParameters swaggerUiConfigParameters(SwaggerUiConfigProperties properties) {
        // Cria um objeto responsável por armazenar as configurações do Swagger UI, incluindo os grupos que serão exibidos.
        return new SwaggerUiConfigParameters(properties);
    }

    @Bean
    @Lazy(false) // Cria este Bean durante a inicialização da aplicação, garantindo que os grupos do Swagger existam antes da interface Swagger UI ser carregada.
    public List<GroupedOpenApi> apis(
            // É justamente o Bean criado anteriormente
            SwaggerUiConfigParameters config,
            // É um componente do String Cloud Gateway. Ele sabe todas as rotas existentes e consegue buscá-las automaticamente
            RouteDefinitionLocator locator
    ) {

        // Recupera todas as rotas cadastradas no Spring Cloud Gateway.
        // getRouteDefinitions() retorna um Flux (reativo),
        // collectList() transforma em List
        // block() aguarda a conclusão para obter a lista.
        List<RouteDefinition> definitions =
                locator.getRouteDefinitions()
                        .collectList()
                        .block();

        // Lista que armazenará os grupos criados dinamicamente.
        List<GroupedOpenApi> groups = new ArrayList<>();

        // Apenas continua se existirem rotas cadastradas.
        if (definitions != null) {
            definitions.stream()
                    // Seleciona apenas rotas cujo ID termina com "-service".
                    // Exemplo:
                    // book-service ✔
                    // exchange-service ✔
                    // gateway ✘
                    // eureka-server ✘
                    .filter(routeDefinition -> routeDefinition.getId().matches(".*-service"))
                    // Para cada microsserviço encontrado...
                    .forEach(routeDefinition -> {
                        // Obtém o ID da rota.
                        // Exemplo:
                        // "book-service"
                        String name = routeDefinition.getId();
                        // Adiciona esse serviço como um grupo
                        // dentro da interface Swagger UI.
                        config.addGroup(name);
                        // Cria dinamicamente um grupo OpenAPI
                        // correspondente ao microsserviço.
                        groups.add(GroupedOpenApi.builder()
                                // Nome do grupo exibido no Swagger.
                                .group(name)
                                // Todas as URLs iniciadas por
                                // /book-service/**
                                // /exchange-service/**
                                // etc., pertencerão a esse grupo.
                                .pathsToMatch("/" + name + "/**")
                                .build());
                    });
        }

        // Retorna todos os grupos criados dinamicamente para o Spring.
        return groups;
    }
}