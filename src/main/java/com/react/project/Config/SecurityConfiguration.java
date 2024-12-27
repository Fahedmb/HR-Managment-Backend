package com.react.project.Config;
import com.react.project.Exception.CustomAccessDeniedHandler;
import com.react.project.Exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtAuthenticationFilter f;private final AuthenticationProvider p;
    @Bean public SecurityFilterChain s(HttpSecurity h,CustomAuthenticationEntryPoint e,CustomAccessDeniedHandler d)throws Exception{
        h.csrf(AbstractHttpConfigurer::disable).cors(c->c.configurationSource(u()))
                .authorizeHttpRequests(r->r
                        .requestMatchers("/auth/**","/api/users/**","/api/chart-data","/api/leave-requests/**","/api/analytics/**","/api/requests/**","/api/time-sheets/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(s->s.sessionCreationPolicy(STATELESS))
                .authenticationProvider(p)
                .addFilterBefore(f,UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(x->x.authenticationEntryPoint(e).accessDeniedHandler(d));
        return h.build();
    }
    @Bean public UrlBasedCorsConfigurationSource u(){
        CorsConfiguration c=new CorsConfiguration();c.setAllowCredentials(true);c.setAllowedOrigins(List.of("http://localhost:5173"));c.addAllowedHeader("*");c.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;
    }
}
