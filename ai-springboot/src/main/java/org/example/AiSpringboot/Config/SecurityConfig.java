package org.example.AiSpringboot.Config;


import cn.hutool.core.text.AntPathMatcher;
import org.example.AiSpringboot.Util.JwtAuthticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    public static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
    private static final String[] PUBLIC_PATHS   = {
            "/",
            "/error",
            "/api/test",
            "/api/user/login",
            "/api/user/add",
            // 上传文件（文章封面等）的静态资源目录，浏览器加载图片时不带 token，必须公开访问
            "/uploads/**",
    };

    public static Boolean isPublicPath(String requestURI){
        for(String url:PUBLIC_PATHS){
            if(ANT_PATH_MATCHER.match(url,requestURI)){
                return true;
            }
        }
        return false;
    }

    @Bean
    public JwtAuthticationFilter jwtAuthticationFilter(){
        return new JwtAuthticationFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth->auth.requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated())
                //添加JWT认证过滤器
                .addFilterBefore(jwtAuthticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
