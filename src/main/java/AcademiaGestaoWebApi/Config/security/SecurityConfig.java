package AcademiaGestaoWebApi.Config.security;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableWebSecurity
@EnableAuthorizationServer
@EnableResourceServer
@Configuration
public class SecurityConfig<CustomizeAuthenticationSuccessHandler> extends WebSecurityConfigurerAdapter {

    @Autowired
    DataSource dataSource;

    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                .usersByUsernameQuery("SELECT username, password, true AS Enable FROM acesso.usuario WHERE username=?")
                .passwordEncoder(new BCryptPasswordEncoder())
                .authoritiesByUsernameQuery("SELECT U.username, R.role FROM acesso.usuario_role AS UR \n" +
                                                                    "INNER JOIN acesso.usuario AS U ON U.id_usuario = UR.id_usuario\n" +
                                                                    "INNER JOIN acesso.role AS R ON R.id_role = UR.id_role WHERE U.username=?");
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
                .antMatchers("swagger**").permitAll()
                .antMatchers("/oauth/token").permitAll()                
                .anyRequest().authenticated()
                .and()
            .httpBasic()
                .and()
            .csrf().disable();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs",
                                   "/configuration/ui",
                                   "/swagger-resources/**",
                                   "/configuration/security",
                                   "/swagger-ui.html",
                                   "/webjars/**");
    }
}