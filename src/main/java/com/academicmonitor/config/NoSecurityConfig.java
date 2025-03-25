package com.academicmonitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 此配置类已被弃用，使用WebSecurityConfig代替
 */
@Deprecated
// @Configuration  // 已禁用
// @EnableWebSecurity // 已禁用
// @Order(1)  // 已禁用
public class NoSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 已被禁用，避免与主要配置冲突
        /*
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/**").permitAll();
        */
    }
} 