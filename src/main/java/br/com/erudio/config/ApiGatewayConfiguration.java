package br.com.erudio.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Habilitando Discorevy Location com Eureka para String Cloud Gateway
 * Isso permite que o Gateway aja como se fosse um porteiro, aonde ele recebebe as requisições dos clientes, pode modificá-las, validar, autenticar
 * registrar logs, limitar acesso e etc... E depois encaminha para o destino correto
 */
@Configuration
public class ApiGatewayConfiguration {

    // @Bean diz ao Spring para criar um objeto (RouteLocator) e gerenciá-lo.
    // O RouteLocator é quem armazena todas as rotas do Gateway.
    // O RouteLocatorBuilder é um construtor (Builder) para facilitar a criação dessas rotas.
    @Bean
    public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p.path("/get") // Quando chegar uma requisição para /get, execute esta configuração.
                        .filters(f -> f
                                .addRequestHeader("Hello", "Word")
                                .addRequestParameter("Hello", "Word"))
                        .uri("http://httpbin.org:80")) // Depois de aplicar os filtros, encaminhe a requisição para http://httpbin.org
                        // O httpbin.org é um serviço criado justamente para testes de HTTP. Ele devolve exatamente o que recebeu

                .route(p -> p.path("/book-service/**").uri("lb://book-service")) //lb:// já é reconhecido como loading balancer automaticamente
                .route(p -> p.path("/exchange-service/**").uri("lb://exchange-service"))
                .build();
    }
}
