package com.example.demo.config;

import com.example.demo.security.Auth0PermissionsConverter;
import com.example.demo.user.UserEntity;
import com.example.demo.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Configuration
@EnableWebSecurity
@Profile("!noauth")
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    // ===== Common Beans =====
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            UserEntity u = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            var builder = User.withUsername(u.getUsername())
                    .password(u.getPassword())
                    .disabled(!u.isEnabled());
            builder.roles(u.getRoles().stream()
                    .map(r -> r.getName().replaceFirst("^ROLE_", ""))
                    .toArray(String[]::new));
            return builder.build();
        }; }

    @Autowired private Auth0PermissionsConverter auth0PermissionsConverter;
    @Autowired private Environment env;
    @Autowired private ClientRegistrationRepository clientRegistrationRepository;

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(auth0PermissionsConverter);
        return conv;
    }

    // ===== API Resource Server (JWT) =====
    @Bean
    @Order(0)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((req,res,ex)->res.sendError(HttpStatus.FORBIDDEN.value()))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    // ===== Authorization Request Resolver (append audience & force prompt=login) =====
    @Bean
    public OAuth2AuthorizationRequestResolver auth0AuthorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver base = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
        base.setAuthorizationRequestCustomizer(authorizationRequestCustomizer());
        return base;
    }

    private Consumer<OAuth2AuthorizationRequest.Builder> authorizationRequestCustomizer() {
        return builder -> {
//            String audience = env.getProperty("app.auth0.audience");
//            if (audience != null && !audience.isBlank()) {
//                builder.additionalParameters(p -> p.put("audience", audience));
//            }
            // Always force user to re-enter credentials to avoid silent SSO reuse
            builder.additionalParameters(p -> p.put("prompt", "login"));
        }; }

    // ===== Logout success handler (invalidate Auth0 session) =====
    @Bean
    public LogoutSuccessHandler auth0LogoutSuccessHandler() {
        return (request, response, authentication) -> {
            String domain = env.getProperty("app.auth0.domain");
            String clientId = env.getProperty("spring.security.oauth2.client.registration.auth0.client-id");
            if (domain == null || domain.contains("your-tenant")) {
                response.sendRedirect("/");
                return;
            }
            String returnTo = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/";
            String url = "https://" + domain + "/v2/logout?client_id=" + clientId + "&returnTo=" + URLEncoder.encode(returnTo, StandardCharsets.UTF_8);
            response.sendRedirect(url);
        }; }

    // ===== Web (Interactive) Security Chain =====
    @Bean
    @Order(1)
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        http.securityMatcher(new AntPathRequestMatcher("/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/h2-console/**", "/hello", "/login", "/error", "/debug/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        // The authorizationRequestResolver will automatically add the audience.
                        // No need to add it in the HTML link.
                        .authorizationEndpoint(a -> a.authorizationRequestResolver(auth0AuthorizationRequestResolver()))
                        .failureHandler((request, response, ex) -> {
                            log.error("OIDC login failed: {}", ex.getMessage(), ex);
                            String msg = URLEncoder.encode(ex.getMessage()==null?"error":ex.getMessage(), StandardCharsets.UTF_8);
                            response.sendRedirect("/login?error="+msg);
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(auth0LogoutSuccessHandler())
                )
                .headers(h -> h.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }
}
