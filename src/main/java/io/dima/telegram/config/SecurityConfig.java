package io.dima.telegram.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderNames.WWW_AUTHENTICATE;

@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${io.dima.auth.enable_www_auth:true}")
    private boolean enableWWWAuthenticate;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http.authorizeExchange()
                .pathMatchers("/channel").authenticated()
                .pathMatchers("/channel/*").authenticated()
                .pathMatchers("/post").authenticated()
                .pathMatchers("/post/*").authenticated()
                .anyExchange().permitAll()
                .and()
                .httpBasic()
                .authenticationEntryPoint((exchange, denied) -> {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    List<String> requestedWith = exchange.getRequest().getHeaders().get("X-Requested-With");
                    if (enableWWWAuthenticate && (requestedWith == null || !requestedWith.contains("XMLHttpRequest"))) {
                        response.getHeaders().set(WWW_AUTHENTICATE.toString(),
                                String.format("Basic realm=\"%s\"", "Realm"));
                    }
                    exchange.mutate().response(response);
                    return Mono.empty();
                })
                .and()
                .csrf().disable()
                .build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService(@Value("${io.dima.auth.username}") String username,
                                                            @Value("${io.dima.auth.password}") String password) {
        UserDetails user = User
                .withUsername(username)
                .password(passwordEncoder().encode(password))
                .roles("USER")
                .build();
        return new MapReactiveUserDetailsService(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
