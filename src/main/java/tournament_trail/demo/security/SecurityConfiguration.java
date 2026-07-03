package tournament_trail.demo.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                PathRequest.toStaticResources().atCommonLocations())
                        .permitAll()
                        .requestMatchers(
                                "/",
                                "/about",
                                "/contact",
                                "/login",
                                "/register",
                                "/verify",
                                "/check-your-email",
                                "/error")
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/tournaments")
                        .hasAnyRole("ORGANISER", "ADMIN")
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/tournaments/*")
                        .hasAnyRole("ORGANISER", "ADMIN")
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/tournaments/*")
                        .hasAnyRole("ORGANISER", "ADMIN")
                        .requestMatchers(
                                HttpMethod.GET,
                                "/tournaments",
                                "/tournaments/*")
                        .permitAll()
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/organiser/**")
                        .hasAnyRole("ORGANISER", "ADMIN")
                        .anyRequest()
                        .authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll());
        return http.build();
    }
}