package org.carrot.scheduler.center.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/assets/**").permitAll()
                .anyRequest().fullyAuthenticated().and()
                .formLogin().loginPage("/login.html")
                .failureUrl("/login.html?error").loginProcessingUrl("/login")  //very import add
                .permitAll().successForwardUrl("/index.html").and().logout().permitAll();
        http.csrf().disable();
    }
}
